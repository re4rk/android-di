package com.re4rk.arkdi

import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import javax.lang.model.element.ExecutableElement
import javax.lang.model.util.Elements

class ProvideProcessorInformation(
    val executableElement: ExecutableElement,
    val singleton: Boolean,
    elements: Elements
) {
    val packageName: String = elements.getPackageOf(executableElement).toString()
    val className: String = executableElement.enclosingElement.toString()
    val methodName: String = executableElement.simpleName.toString()

    private val returnName: String = executableElement.returnType.toString()
    private val returnElement = elements.getTypeElement(returnName)

    val returnPackageName: String = elements.getPackageOf(returnElement).toString()
    val returnClassName: String = returnElement.simpleName.toString()
    val returnType = TypeVariableName(returnElement.simpleName.toString())

    val factoryName: String = FACTORY_NAME_FORMAT.format(
        executableElement.enclosingElement.simpleName.toString(),
        methodName
    )
    val factoryType = Factory::class.asClassName().parameterizedBy(returnType)

    companion object {
        private const val FACTORY_NAME_FORMAT = "DI_%s_%s"
    }
}
