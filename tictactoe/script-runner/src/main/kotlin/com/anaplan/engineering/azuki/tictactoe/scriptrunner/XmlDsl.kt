package com.anaplan.systemspecification.junitresultdsl

import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

interface ElementTag {
    fun render(builder: StringBuilder, indent: String)
}

@Target(AnnotationTarget.PROPERTY)
annotation class Attribute(
    val name: String = ""
)

@Target(AnnotationTarget.PROPERTY)
annotation class Text(
    val cdata: Boolean = false
)

@Target(AnnotationTarget.PROPERTY)
annotation class Element

@Target(AnnotationTarget.PROPERTY)
annotation class ElementList

abstract class XmlTag(private val tagName: String) : ElementTag {

    override fun render(builder: StringBuilder, indent: String) {
        val elements = javaClass.kotlin.memberProperties.filter { property ->
            property.findAnnotation<Element>() != null
        }
        val elementLists = javaClass.kotlin.memberProperties.filter { property ->
            property.findAnnotation<ElementList>() != null
        }
        val texts = javaClass.kotlin.memberProperties.filter { property ->
            property.findAnnotation<Text>() != null
        }
        if (texts.size > 1) {
            throw IllegalStateException("May only declare one text per element")
        }
        val text = if (texts.isEmpty()) null else texts[0]
        @Suppress("UNCHECKED_CAST") val childTags =
            elementLists.map { property -> property.get(this) as List<ElementTag> }
                .flatten() + elements.mapNotNull { property -> property.get(this) }.map { it as ElementTag }
        if (childTags.isEmpty() && text == null) {
            builder.append("$indent<$tagName${renderAttributes()}/>\n")
        } else {
            builder.append("$indent<$tagName${renderAttributes()}>\n")
            childTags.forEach { child ->
                child.render(builder, "$indent  ")
            }
            if (text != null) {
                if (text.findAnnotation<Text>()!!.cdata) {
                    builder.append("$indent  <![CDATA[${text.get(this)}]]>\n")
                } else {
                    builder.append("$indent  ${text.get(this)}\n")
                }
            }
            builder.append("$indent</$tagName>\n")
        }
    }

    private fun renderAttributes(): String {
        val attributeProperties = javaClass.kotlin.memberProperties.filter { property ->
            property.findAnnotation<Attribute>() != null
        }
        val builder = StringBuilder()
        attributeProperties.forEach { property ->
            val annotation = property.findAnnotation<Attribute>() as Attribute
            val attributeName = annotation.name.ifEmpty { property.name }
            val value = property.get(this)
            // ignore null attributes
            if (value != null) {
                builder.append(" $attributeName=\"$value\"")
            }
        }
        return builder.toString()
    }

    override fun toString(): String {
        val builder = StringBuilder()
        render(builder, "")
        return builder.toString()
    }
}
