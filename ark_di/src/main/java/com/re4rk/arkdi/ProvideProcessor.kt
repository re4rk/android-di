package com.re4rk.arkdi

import com.google.auto.service.AutoService
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
                    // TODO
                }
            }

        return true
    }
}
