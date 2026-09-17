package com.artrubadur.teno.ui.components.markdown

data class MarkdownDocument(
    val blocks: List<MarkdownBlock>,
)

sealed interface MarkdownBlock {
    data class Paragraph(val inlines: List<MarkdownInline>) : MarkdownBlock
    data class Heading(val level: Int, val inlines: List<MarkdownInline>) : MarkdownBlock
    data class CodeBlock(val code: String, val language: String?) : MarkdownBlock
    data class Quote(val blocks: List<MarkdownBlock>) : MarkdownBlock
    data class ListBlock(
        val ordered: Boolean,
        val items: List<List<MarkdownBlock>>,
    ) : MarkdownBlock

    data class Table(
        val headers: List<List<MarkdownInline>>,
        val rows: List<List<List<MarkdownInline>>>,
    ) : MarkdownBlock

    data object ThematicBreak : MarkdownBlock
}

sealed interface MarkdownInline {
    data class Text(val value: String) : MarkdownInline
    data class Strong(val children: List<MarkdownInline>) : MarkdownInline
    data class Emphasis(val children: List<MarkdownInline>) : MarkdownInline
    data class Strikethrough(val children: List<MarkdownInline>) : MarkdownInline
    data class Code(val value: String) : MarkdownInline
    data class Link(val destination: String, val children: List<MarkdownInline>) : MarkdownInline
    data object LineBreak : MarkdownInline
}
