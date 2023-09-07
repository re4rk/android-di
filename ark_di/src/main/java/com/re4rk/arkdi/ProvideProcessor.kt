package com.re4rk.arkdi

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import java.io.File
import java.util.Locale
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor::class)
class ProvideProcessor : AbstractProcessor() {
    private lateinit var informationList: List<ProvideProcessorInformation>

    @Override
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        this.processingEnv = processingEnv
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Provides::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        informationList = roundEnv.getElementsAnnotatedWith(Provides::class.java)
            .filter { it.kind == ElementKind.METHOD }
            .map {
                ProvideProcessorInformation(it as ExecutableElement, processingEnv.elementUtils)
            }

        informationList.map { generateFactory(it) }

        roundEnv.getElementsAnnotatedWith(ArkDiAndroidApp::class.java)
            .filter { it.kind == ElementKind.CLASS }
            .map {
                generateAndroidAppFactory(it as TypeElement, processingEnv.elementUtils)
            }
        return true
    }

    private fun generateAndroidAppFactory(typeElement: TypeElement, elements: Elements) {
        val applicationClass = TypeSpec.classBuilder("DI_" + typeElement.simpleName.toString())
            .superclass(DiContainer::class.asTypeName())
            .addFunctions(
                informationList.map {
                    FunSpec.builder("get" + it.returnType.toString() + "Factory")
                        .addStatement(
                            "return %T.create(${
                                it.executableElement.parameters.joinToString(", ") {
                                    "get" + it.simpleName.toString().capitalize() + "Factory" + "()"
                                }
                            })",
                            ClassName(it.packageName, it.factoryName)
                        )
                        .returns(it.factoryType)
                        .build()
                }
            )

        FileSpec.builder(
            elements.getPackageOf(typeElement).toString(),
            "DI_${typeElement.simpleName}"
        )
            .addType(applicationClass.build())
            .apply {
                informationList.map {
                    addImport(it.returnPackageName, it.returnClassName)
                }
            }
            .build()
            .writeTo(File(processingEnv.options["kapt.kotlin.generated"], ""))
    }

    private fun generateFactory(information: ProvideProcessorInformation) {
        val parameterInformationList = provideProcessorInformations(information)

        val funSpec = FunSpec.constructorBuilder()

        parameterInformationList.map {
            funSpec.addParameter(
                ParameterSpec.builder(
                    it.returnType.toString().decapitalize(),
                    it.providerType
                ).build()
            ).addStatement(
                "this.${it.returnType.toString().decapitalize()} = ${
                    it.returnType.toString().decapitalize()
                }"
            )
        }

        TypeSpec.classBuilder(information.factoryName)
            .addSuperinterface(information.factoryType)
            .primaryConstructor(funSpec.build())
            .addProperties(makePropertySpecs(parameterInformationList))
            .addGetter(information, getParameters(information))
            .addType(generateCompanionObject(getParameterProviders(information), information))
            .createFactoryFile(information)
    }

    private fun provideProcessorInformations(information: ProvideProcessorInformation) =
        information.executableElement.parameters.map { parameter ->
            val parameterInformation = informationList
                .find { it.executableElement.returnType == parameter.asType() }
                ?: throw IllegalStateException()
            generateFactory(parameterInformation)
            parameterInformation
        }

    private fun makePropertySpecs(parameterInformationList: List<ProvideProcessorInformation>) =
        parameterInformationList.map { parameterInformation ->
            PropertySpec.builder(
                parameterInformation.returnType.toString()
                    .replaceFirstChar { it.lowercase(Locale.getDefault()) },
                parameterInformation.providerType
            ).build()
        }

    private fun getParameters(information: ProvideProcessorInformation): List<ParameterSpec> =
        information.executableElement.parameters.map { parameter ->
            ParameterSpec.builder(
                parameter.simpleName.toString(),
                parameter.asType().asTypeName()
            ).build()
        }

    private fun getParameterProviders(information: ProvideProcessorInformation): List<ParameterSpec> =
        information.executableElement.parameters.map { parameter ->
            ParameterSpec.builder(
                parameter.simpleName.toString() + "Provider",
                informationList
                    .find { it.executableElement.returnType == parameter.asType() }
                    ?.providerType ?: throw IllegalStateException()
            ).build()
        }

    private fun TypeSpec.Builder.addGetter(
        information: ProvideProcessorInformation,
        parameters: List<ParameterSpec>
    ): TypeSpec.Builder = this.addFunction(
        FunSpec.builder("get")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement(
                "return ${information.methodName}(${
                    parameters.joinToString(", ") {
                        it.name + ".get()"
                    }
                })"
            )
            .returns(information.returnType)
            .build()
    )

    private fun generateCompanionObject(
        parameterProviders: List<ParameterSpec>,
        information: ProvideProcessorInformation
    ) = TypeSpec.companionObjectBuilder()
        .addFunction(
            FunSpec.builder("create")
                .addModifiers(KModifier.PUBLIC)
                .addParameters(parameterProviders)
                .addStatement(
                    "return ${information.factoryName}(${
                        parameterProviders.joinToString(", ") {
                            it.name
                        }
                    })"
                )
                .returns(information.factoryType)
                .build()
        )
        .build()

    private fun TypeSpec.Builder.createFactoryFile(information: ProvideProcessorInformation) {
        FileSpec.builder(information.packageName, information.factoryName)
            .addImport(information.className, information.methodName)
            .addImport(information.returnPackageName, information.returnClassName)
            .addType(this.build())
            .build()
            .writeTo(File(processingEnv.options["kapt.kotlin.generated"], ""))
    }

    private fun String.decapitalize(): String = when (isEmpty()) {
        true -> this
        false -> {
            String(this.toCharArray().apply { this[0] = Character.toLowerCase(this[0]) })
        }
    }

    private fun String.capitalize(): String = when (isEmpty()) {
        true -> this
        false -> {
            String(this.toCharArray().apply { this[0] = Character.toUpperCase(this[0]) })
        }
    }
}
