package com.artrubadur.teno.ui.components.markdown

import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak

object MarkdownDocumentParser {
    private val parser = org.commonmark.parser.Parser.builder()
        .extensions(
            listOf(
                StrikethroughExtension.create(),
                TablesExtension.create(),
            )
        )
        .build()

    fun parse(markdown: String): MarkdownDocument = MarkdownDocument(
        blocks = parseBlocks(parser.parse(markdown))
    )

    private fun parseBlocks(parent: Node): List<MarkdownBlock> = buildList {
        var node = parent.firstChild
        while (node != null) {
            parseBlock(node)?.let(::add)
            node = node.next
        }
    }

    private fun parseBlock(node: Node): MarkdownBlock? = when (node) {
        is Paragraph -> MarkdownBlock.Paragraph(parseInlines(node))
        is Heading -> MarkdownBlock.Heading(node.level, parseInlines(node))
        is FencedCodeBlock -> MarkdownBlock.CodeBlock(
            code = node.literal.trimEnd('\n'),
            language = node.info.trim().takeIf(String::isNotBlank),
        )

        is IndentedCodeBlock -> MarkdownBlock.CodeBlock(
            code = node.literal.trimEnd('\n'),
            language = null,
        )

        is BlockQuote -> MarkdownBlock.Quote(parseBlocks(node))
        is BulletList -> MarkdownBlock.ListBlock(
            ordered = false,
            items = parseListItems(node),
        )

        is OrderedList -> MarkdownBlock.ListBlock(
            ordered = true,
            items = parseListItems(node),
        )

        is TableBlock -> parseTable(node)
        is ThematicBreak -> MarkdownBlock.ThematicBreak
        else -> null
    }

    private fun parseTable(table: TableBlock): MarkdownBlock.Table {
        var headers = emptyList<List<MarkdownInline>>()
        val rows = mutableListOf<List<List<MarkdownInline>>>()
        var section = table.firstChild
        while (section != null) {
            when (section) {
                is TableHead -> headers = parseTableRows(section).firstOrNull().orEmpty()
                is TableBody -> rows += parseTableRows(section)
            }
            section = section.next
        }
        return MarkdownBlock.Table(headers = headers, rows = rows)
    }

    private fun parseTableRows(parent: Node): List<List<List<MarkdownInline>>> = buildList {
        var row = parent.firstChild
        while (row != null) {
            if (row is TableRow) {
                val cells = buildList {
                    var cell = row.firstChild
                    while (cell != null) {
                        add(parseInlines(cell))
                        cell = cell.next
                    }
                }
                add(cells)
            }
            row = row.next
        }
    }

    private fun parseListItems(list: Node): List<List<MarkdownBlock>> = buildList {
        var node = list.firstChild
        while (node != null) {
            if (node is ListItem) add(parseBlocks(node))
            node = node.next
        }
    }

    private fun parseInlines(parent: Node): List<MarkdownInline> = buildList {
        var node = parent.firstChild
        while (node != null) {
            parseInline(node)?.let(::add)
            node = node.next
        }
    }

    private fun parseInline(node: Node): MarkdownInline? = when (node) {
        is Text -> MarkdownInline.Text(node.literal)
        is StrongEmphasis -> MarkdownInline.Strong(parseInlines(node))
        is Emphasis -> MarkdownInline.Emphasis(parseInlines(node))
        is Strikethrough -> MarkdownInline.Strikethrough(parseInlines(node))
        is Code -> MarkdownInline.Code(node.literal)
        is Link -> MarkdownInline.Link(node.destination, parseInlines(node))
        is HardLineBreak, is SoftLineBreak -> MarkdownInline.LineBreak
        else -> null
    }
}
