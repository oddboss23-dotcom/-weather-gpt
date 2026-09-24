package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanLight
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

/**
 * Production-ready Markdown & Structured Chat Message Formatter.
 * Eliminates raw markdown symbols (**, ###, etc.) and formats responses into
 * elegant headings, bullet points, emphasized metrics, and structured telemetry pills.
 */
@Composable
fun FormattedMarkdownContent(
    text: String,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    if (isUser) {
        // User messages are simple plain text
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp, fontSize = 14.sp),
            color = Color.White,
            modifier = modifier
        )
        return
    }

    val lines = text.lines()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        var inListBlock = false

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                if (inListBlock) {
                    Spacer(modifier = Modifier.height(2.dp))
                    inListBlock = false
                }
                continue
            }

            when {
                // Section Headers: ### Header or ## Header or # Header
                trimmed.startsWith("###") || trimmed.startsWith("##") || trimmed.startsWith("#") -> {
                    inListBlock = false
                    val headerText = trimmed.replace(Regex("^#+\\s*"), "").replace(Regex("\\*\\*"), "").trim()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp)
                    ) {
                        Text(
                            text = headerText,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                letterSpacing = 0.4.sp
                            ),
                            color = CyanLight
                        )
                    }
                }

                // Section Bracket Badges e.g. [WEATHER SUMMARY], [KEY PARAMETERS], [मौसम पूर्वानुमान], etc.
                trimmed.startsWith("[") && trimmed.contains("]") && trimmed.indexOf("]") < 35 -> {
                    inListBlock = false
                    val tag = trimmed.substring(1, trimmed.indexOf("]")).trim()
                    val rest = trimmed.substring(trimmed.indexOf("]") + 1).trim()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(SurfaceCard, RoundedCornerShape(6.dp))
                                .border(0.5.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tag.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.6.sp
                                ),
                                color = ElectricCyan
                            )
                        }
                        if (rest.isNotEmpty()) {
                            Text(
                                text = parseInlineMarkdown(rest, isUser = false),
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp, fontSize = 13.sp),
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Bullet points: -, *, •, or 1.
                trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") || Regex("^\\d+\\.\\s").containsMatchIn(trimmed) -> {
                    inListBlock = true
                    val bulletContent = trimmed
                        .replace(Regex("^[-*•]\\s*"), "")
                        .replace(Regex("^\\d+\\.\\s*"), "")
                        .trim()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = ElectricCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(bulletContent, isUser = false),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 20.sp,
                                fontSize = 13.sp
                            ),
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Standard Paragraph
                else -> {
                    inListBlock = false
                    Text(
                        text = parseInlineMarkdown(trimmed, isUser = false),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp,
                            fontSize = 13.sp
                        ),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

/**
 * Parses inline formatting like **bold text** without showing markdown asterisks
 */
fun parseInlineMarkdown(rawText: String, isUser: Boolean): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val pattern = Regex("\\*\\*(.*?)\\*\\*")
    var lastIndex = 0

    val matches = pattern.findAll(rawText)
    for (match in matches) {
        val start = match.range.first
        val end = match.range.last + 1
        val boldContent = match.groupValues[1]

        if (start > lastIndex) {
            builder.append(rawText.substring(lastIndex, start))
        }

        val boldSpan = SpanStyle(
            fontWeight = FontWeight.Bold,
            color = if (isUser) Color.White else CyanLight
        )
        builder.pushStyle(boldSpan)
        builder.append(boldContent)
        builder.pop()

        lastIndex = end
    }

    if (lastIndex < rawText.length) {
        builder.append(rawText.substring(lastIndex))
    }

    return builder.toAnnotatedString()
}
