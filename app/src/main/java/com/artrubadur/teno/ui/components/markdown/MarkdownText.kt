package com.artrubadur.teno.ui.components.markdown

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import kotlinx.coroutines.launch

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    val document = remember(markdown) { MarkdownDocumentParser.parse(markdown) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        document.blocks.forEach { block ->
            MarkdownBlockView(
                block = block,
                maxLines = maxLines,
            )
        }
    }
}

@Composable
private fun MarkdownBlockView(
    block: MarkdownBlock,
    maxLines: Int,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    when (block) {
        is MarkdownBlock.Paragraph -> MarkdownInlineText(
            inlines = block.inlines,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = maxLines,
        )

        is MarkdownBlock.Heading -> MarkdownInlineText(
            inlines = block.inlines,
            style = when (block.level) {
                1 -> MaterialTheme.typography.headlineSmall
                2 -> MaterialTheme.typography.titleLarge
                else -> MaterialTheme.typography.titleMedium
            },
            maxLines = maxLines,
        )

        is MarkdownBlock.CodeBlock -> CodeBlock(block, maxLines)
        is MarkdownBlock.Quote -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        color = primaryColor,
                        size = Size(3.dp.toPx(), size.height),
                    )
                }
                .padding(start = 11.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            block.blocks.forEach { child ->
                MarkdownBlockView(child, maxLines)
            }
        }

        is MarkdownBlock.ListBlock -> Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            block.items.forEachIndexed { index, item ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (block.ordered) "${index + 1}." else "•",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        item.forEach { child ->
                            MarkdownBlockView(child, maxLines)
                        }
                    }
                }
            }
        }

        is MarkdownBlock.Table -> MarkdownTable(block, maxLines)
        MarkdownBlock.ThematicBreak -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(vertical = 4.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

@Composable
private fun CodeBlock(block: MarkdownBlock.CodeBlock, maxLines: Int) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_code),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp),
            )
            block.language
                ?.takeIf(String::isNotBlank)
                ?.let {
                    Text(
                        text = it.replaceFirstChar(Char::titlecase),
                        modifier = Modifier.padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipEntry(ClipData.newPlainText("code", block.code))
                        )
                    }
                },
                modifier = Modifier.size(16.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_copy),
                    contentDescription = "Copy code",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Text(
                text = block.code,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MarkdownTable(table: MarkdownBlock.Table, maxLines: Int) {
    val outline = MaterialTheme.colorScheme.outline
    val rows = buildList {
        if (table.headers.isNotEmpty()) add(table.headers to true)
        table.rows.forEach { add(it to false) }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
    ) {
        Column {
            rows.forEach { (cells, isHeader) ->
                Row {
                    cells.forEach { cell ->
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .drawBehind {
                                    drawLine(
                                        color = outline,
                                        start = androidx.compose.ui.geometry.Offset(
                                            0f,
                                            size.height - if (isHeader) 2.dp.toPx() else 1.dp.toPx(),
                                        ),
                                        end = androidx.compose.ui.geometry.Offset(
                                            size.width,
                                            size.height - if (isHeader) 2.dp.toPx() else 1.dp.toPx(),
                                        ),
                                        strokeWidth = if (isHeader) 2.dp.toPx() else 1.dp.toPx(),
                                    )
                                }
                                .padding(8.dp)
                        ) {
                            MarkdownInlineText(
                                inlines = cell,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isHeader) FontWeight.Bold else null,
                                ),
                                maxLines = maxLines,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkdownInlineText(
    inlines: List<MarkdownInline>,
    style: TextStyle,
    maxLines: Int,
) {
    val inlineCodeBackground = MaterialTheme.colorScheme.surfaceVariant
    val linkColor = MaterialTheme.colorScheme.primary
    val annotated = remember(inlines, style, inlineCodeBackground, linkColor) {
        buildAnnotatedString {
            inlines.forEach {
                appendInline(
                    inline = it,
                    inlineCodeBackground = inlineCodeBackground,
                    linkColor = linkColor,
                )
            }
        }
    }

    Text(
        text = annotated,
        modifier = Modifier.fillMaxWidth(),
        style = style,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

private fun AnnotatedString.Builder.appendInline(
    inline: MarkdownInline,
    inlineCodeBackground: Color,
    linkColor: Color,
) {
    when (inline) {
        is MarkdownInline.Text -> append(inline.value)
        is MarkdownInline.Strong -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            inline.children.forEach { appendInline(it, inlineCodeBackground, linkColor) }
        }

        is MarkdownInline.Emphasis -> withStyle(SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) {
            inline.children.forEach { appendInline(it, inlineCodeBackground, linkColor) }
        }

        is MarkdownInline.Strikethrough -> withStyle(
            SpanStyle(textDecoration = TextDecoration.LineThrough)
        ) {
            inline.children.forEach { appendInline(it, inlineCodeBackground, linkColor) }
        }

        is MarkdownInline.Code -> withStyle(
            SpanStyle(
                fontFamily = FontFamily.Monospace,
                background = inlineCodeBackground.copy(alpha = 0.6f),
            )
        ) {
            append(inline.value)
        }

        is MarkdownInline.Link -> {
            withLink(
                LinkAnnotation.Url(
                    url = inline.destination,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline,
                        )
                    )
                )
            ) {
                inline.children.forEach { appendInline(it, inlineCodeBackground, linkColor) }
            }
        }

        MarkdownInline.LineBreak -> append('\n')
    }
}
