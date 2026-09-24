package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.canonical.CheckStatus
import com.example.data.canonical.IntegrityReport
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanLight
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.WeatherGPTViewModel

/**
 * Authoritative Canonical Data Architecture Badge & Audit Dialog.
 * Provides verifiable proof of single-source-of-truth determinism and location consistency.
 */
@Composable
fun CanonicalIntegrityCard(
    viewModel: WeatherGPTViewModel,
    modifier: Modifier = Modifier
) {
    val canonicalLocation by viewModel.canonicalLocation.collectAsState()
    val canonicalSnapshot by viewModel.canonicalSnapshot.collectAsState()
    val integrityReport by viewModel.systemIntegrityReport.collectAsState()
    val isDialogOpen by viewModel.isIntegritySheetOpen.collectAsState()

    val report = integrityReport
    val statusColor = when (report?.overallStatus) {
        CheckStatus.PASS -> AlertGreen
        CheckStatus.WARN -> AlertOrange
        CheckStatus.FAIL -> AlertRed
        null -> ElectricCyan
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceNavy.copy(alpha = 0.92f))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .clickable { viewModel.setIntegritySheetOpen(true) }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Data Integrity",
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CANONICAL DATA INTEGRITY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = statusColor
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(statusColor.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (report != null) "${report.passedCount}/${report.totalTests} VERIFIED" else "ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Loc: ${canonicalLocation.id.take(28)}... • ${canonicalLocation.geographyTypes.take(3).joinToString(" • ")}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElectricCyan.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Audit ❯",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    CanonicalIntegrityAuditDialog(viewModel = viewModel)
}

/**
 * Standalone Data Integrity Audit Dialog - accessible from Developer Diagnostics in Settings.
 */
@Composable
fun CanonicalIntegrityAuditDialog(viewModel: WeatherGPTViewModel) {
    val isDialogOpen by viewModel.isIntegritySheetOpen.collectAsState()
    val canonicalLocation by viewModel.canonicalLocation.collectAsState()
    val canonicalSnapshot by viewModel.canonicalSnapshot.collectAsState()
    val integrityReport by viewModel.systemIntegrityReport.collectAsState()
    val report = integrityReport

    // Comprehensive 14-Point Pre-Demo Audit Modal
    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.setIntegritySheetOpen(false) },
            confirmButton = {
                Button(
                    onClick = { viewModel.setIntegritySheetOpen(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.runSystemIntegrityAudit() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Rerun Audit",
                        modifier = Modifier.size(16.dp),
                        tint = CyanAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Re-Run Audit", color = CyanAccent)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "System Data Integrity Audit",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Single Deterministic State Validation (PS-68 / PS-26071)",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Canonical ID summary
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceNavy)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "CANONICAL LOCATION ID:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextTertiary
                            )
                            Text(
                                text = canonicalLocation.id,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyanLight
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "GEOGRAPHY CLASSIFICATION: ${canonicalLocation.geographyTypes.joinToString()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            if (canonicalSnapshot != null) {
                                Text(
                                    text = "SNAPSHOT: ${canonicalSnapshot!!.snapshotId}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextTertiary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (report == null) {
                        Text(
                            text = "Executing architectural validation...",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(report.items) { item ->
                                val itemColor = when (item.status) {
                                    CheckStatus.PASS -> AlertGreen
                                    CheckStatus.WARN -> AlertOrange
                                    CheckStatus.FAIL -> AlertRed
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceCard)
                                        .border(1.dp, itemColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.category,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CyanLight
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(itemColor.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = item.status.name,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = itemColor
                                                )
                                            }
                                        }

                                        Text(
                                            text = item.testName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )

                                        Text(
                                            text = item.evidence,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            containerColor = SurfaceNavy,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
