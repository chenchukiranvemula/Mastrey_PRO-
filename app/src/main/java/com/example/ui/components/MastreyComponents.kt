package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.ExamGroup
import com.example.data.model.ExamSystem
import com.example.data.model.VerificationStatus
import com.example.ui.theme.CaAccentColor
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.JeeAccentColor
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalMastreyDesignConfig
import com.example.ui.theme.NeetAccentColor
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.VividViolet
import com.example.ui.theme.WarningAmber

fun examAccentColor(exam: ExamSystem): Color = when (exam.group) {
    ExamGroup.JEE -> JeeAccentColor
    ExamGroup.NEET -> NeetAccentColor
    ExamGroup.CA -> CaAccentColor
}

@Composable
fun MastreyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    accentBorder: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cfg = LocalMastreyDesignConfig.current
    val pad = cfg.density.cardPaddingDp.dp
    val borderColor = accentBorder?.copy(alpha = 0.45f) ?: MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    val cardModifier = if (onClick != null) {
        modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick)
    } else {
        modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (cfg.isDark) 0.88f else 1f)
        ),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (cfg.isDark) 2.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(pad), content = content)
    }
}

@Composable
fun MastreyGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val cfg = LocalMastreyDesignConfig.current
    val minHeight = if (cfg.largerControls) 56.dp else 48.dp
    val brush = Brush.horizontalGradient(
        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(minHeight),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(minHeight)
                .background(if (enabled) brush else Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray)), RoundedCornerShape(14.dp))
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(text = text, style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
        }
    }
}

@Composable
fun FormulaExpressionBox(
    expression: String,
    modifier: Modifier = Modifier,
    useMonoHighlight: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f)
                    )
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = expression,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = if (useMonoHighlight) JetBrainsMonoFontFamily else MaterialTheme.typography.titleMedium.fontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun VerificationBadge(status: VerificationStatus, modifier: Modifier = Modifier) {
    val (bg, fg, icon) = when (status) {
        VerificationStatus.VERIFIED_EDUCATIONAL -> Triple(
            SuccessGreen.copy(alpha = 0.16f),
            SuccessGreen,
            Icons.Filled.Verified
        )
        VerificationStatus.AI_GENERATED_VALIDATED -> Triple(
            VividViolet.copy(alpha = 0.18f),
            VividViolet,
            Icons.Rounded.AutoAwesome
        )
        VerificationStatus.AWAITING_VERIFIED -> Triple(
            WarningAmber.copy(alpha = 0.16f),
            WarningAmber,
            Icons.Filled.Info
        )
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(5.dp))
            Text(status.badgeText, style = MaterialTheme.typography.labelMedium, color = fg)
        }
    }
}

@Composable
fun OfflineStatusBanner(isOffline: Boolean) {
    if (!isOffline) return
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = WarningAmber.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.CloudOff, contentDescription = "Offline status", tint = WarningAmber, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Offline Mode • Cached notes, formulas, bookmarks & local progress stay available.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MastreyEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    MastreyCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(14.dp))
                MastreyGradientButton(text = actionLabel, onClick = onAction)
            }
        }
    }
}

@Composable
fun ScoreTrendChart(
    scoresPercent: List<Float>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val accentColor = CyberCyan
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    Canvas(modifier = modifier.fillMaxWidth().height(150.dp)) {
        val w = size.width
        val h = size.height
        val pad = 16f
        for (i in 0..3) {
            val y = pad + (h - 2 * pad) * (i / 3f)
            drawLine(gridColor, Offset(pad, y), Offset(w - pad, y), strokeWidth = 1.5f)
        }
        if (scoresPercent.isEmpty()) return@Canvas
        val stepX = if (scoresPercent.size == 1) 0f else (w - 2 * pad) / (scoresPercent.size - 1)
        val path = Path()
        scoresPercent.forEachIndexed { idx, pct ->
            val clamped = pct.coerceIn(0f, 100f)
            val x = if (scoresPercent.size == 1) w / 2f else pad + idx * stepX
            val y = h - pad - ((clamped / 100f) * (h - 2 * pad))
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        if (scoresPercent.size > 1) {
            drawPath(path, brush = Brush.horizontalGradient(listOf(lineColor, accentColor)), style = Stroke(width = 6f, cap = StrokeCap.Round))
        }
        scoresPercent.forEachIndexed { idx, pct ->
            val clamped = pct.coerceIn(0f, 100f)
            val x = if (scoresPercent.size == 1) w / 2f else pad + idx * stepX
            val y = h - pad - ((clamped / 100f) * (h - 2 * pad))
            drawCircle(accentColor, radius = 9f, center = Offset(x, y))
            drawCircle(Color.White, radius = 4f, center = Offset(x, y))
        }
    }
}

@Composable
fun SubjectBarChart(
    entries: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val track = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        entries.forEach { (label, pct) ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.titleSmall)
                    Text("${pct.toInt()}%", style = MaterialTheme.typography.labelLarge, color = primary)
                }
                Spacer(Modifier.height(4.dp))
                Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
                    drawRoundRect(track, cornerRadius = CornerRadius(8f, 8f), size = size)
                    val fillW = (size.width * (pct.coerceIn(0f, 100f) / 100f)).coerceAtLeast(6f)
                    drawRoundRect(
                        brush = Brush.horizontalGradient(listOf(primary, secondary)),
                        cornerRadius = CornerRadius(8f, 8f),
                        size = Size(fillW, size.height)
                    )
                }
            }
        }
    }
}

@Composable
fun ReportIssueDialog(
    title: String,
    placeholder: String = "Describe the inaccuracy, issue, or suggestion...",
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Flag, contentDescription = null, tint = WarningAmber) },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                placeholder = { Text(placeholder) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth().testTag("report_reason_input")
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isNotBlank()) {
                        onSubmit(reason.trim())
                        onDismiss()
                    }
                },
                enabled = reason.isNotBlank(),
                modifier = Modifier.testTag("submit_report_button")
            ) { Text("Submit Report") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ExamBadgePill(
    exam: ExamSystem,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val accent = examAccentColor(exam)
    Surface(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(50),
        color = accent.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(6.dp))
            Text(exam.title, style = MaterialTheme.typography.labelLarge, color = accent)
        }
    }
}

val StudentAvatarLabels = listOf("Scholar", "Physicist", "Chemist", "Mathematician", "Medic", "CA Pro", "Strategist", "Achiever")
val StudentAvatarColors = listOf(
    ElectricBlue, VividViolet, CyberCyan, Color(0xFF10B981),
    Color(0xFFF59E0B), Color(0xFFEC4899), Color(0xFF6366F1), Color(0xFF14B8A6)
)
