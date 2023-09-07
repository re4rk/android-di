package com.re4rk.arkdi

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor::class)
class ProvideProcessor : AbstractProcessor() {

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
        roundEnv.getElementsAnnotatedWith(Provides::class.java)
            .filter { it.kind == ElementKind.METHOD }
            .map {
                ProvideProcessorInformation(
                    executableElement = it as ExecutableElement,
                    it.getAnnotation(Singleton::class.java) != null,
                    processingEnv.elementUtils
                ).let { information ->
                    generateFactory(information)
                }
            }

        return true
    }

    private fun generateFactory(information: ProvideProcessorInformation) {
        val factoryClass = TypeSpec.classBuilder(information.factoryName)
            .addSuperinterface(information.factoryType)

        if (information.singleton) {
            generateSingletonFactory(factoryClass, information)
        }

        createFactoryFile(information, factoryClass)
    }

    private fun generateSingletonFactory(
        factoryClass: TypeSpec.Builder,
        information: ProvideProcessorInformation
    ) {
        factoryClass.addFunction(
            FunSpec
                .builder("get")
                .addModifiers(KModifier.OVERRIDE)
                .returns(information.returnType)
                .addStatement(
                    "return InstanceHolder.INSTANCE ?: create().apply { InstanceHolder.INSTANCE = this }"
                )
                .build()
        )
        val companionObject = TypeSpec.companionObjectBuilder("InstanceHolder")
            .addProperty(
                PropertySpec.builder("INSTANCE", information.returnType.copy(nullable = true))
                    .addModifiers(KModifier.PRIVATE)
                    .initializer("null")
                    .mutable()
                    .build()
            )
            .addFunction(
                FunSpec.builder("create")
                    .addModifiers(KModifier.PUBLIC)
                    .addStatement("return ${information.methodName}()")
                    .returns(information.returnType)
                    .build()
            )
            .build()

        factoryClass.addType(companionObject)
    }

    private fun createFactoryFile(
        information: ProvideProcessorInformation,
        factoryClass: TypeSpec.Builder
    ) {
        FileSpec.builder(information.packageName, information.factoryName)
            .addImport(information.className, information.methodName)
            .addImport(information.returnPackageName, information.returnClassName)
            .addType(factoryClass.build())
            .build()
            .writeTo(File(processingEnv.options["kapt.kotlin.generated"], ""))
    }
}
