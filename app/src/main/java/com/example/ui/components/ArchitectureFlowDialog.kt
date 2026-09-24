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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertYellow
import com.example.ui.theme.CyanLight
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

data class ArchitectureDiagramInfo(
    val id: String,
    val number: String,
    val title: String,
    val svgFile: String,
    val scope: String,
    val icon: ImageVector,
    val keyNodes: List<String>,
    val scientificPrinciple: String
)

val SYSTEM_ARCHITECTURE_DIAGRAMS = listOf(
    ArchitectureDiagramInfo(
        id = "master_arch",
        number = "01",
        title = "Master System Architecture",
        svgFile = "docs/architecture/01-master-architecture.svg",
        scope = "8-layer horizontal enterprise architecture from raw ingest to Android Compose UX",
        icon = Icons.Default.AccountTree,
        keyNodes = listOf("Data Ingest Layer", "Canonical Normalization", "NWP Integration", "Hyperlocal Engine", "Bust Detection", "Agromet / KrishiGPT", "Voice State Machine", "Offline Room DB"),
        scientificPrinciple = "Separation of raw telemetry ingestion, canonical data snapshot normalization, and downstream natural language translation."
    ),
    ArchitectureDiagramInfo(
        id = "e2e_flow",
        number = "02",
        title = "End-to-End Data Flow Pipeline",
        svgFile = "docs/architecture/02-end-to-end-flow.svg",
        scope = "Full ingestion → cleaning → canonical snapshot → multi-channel dispatch",
        icon = Icons.Default.Dataset,
        keyNodes = listOf("Open-Meteo REST / NWP", "IMD Radar Composite", "CWC Hydrology", "Fusion Engine", "CanonicalWeatherSnapshot", "Room SQLite Cache", "UI Layer"),
        scientificPrinciple = "CanonicalWeatherSnapshot acts as the immutable single source of truth for all UI cards, alerts, and AI prompts."
    ),
    ArchitectureDiagramInfo(
        id = "forecast_flow",
        number = "03",
        title = "Multi-Horizon Forecast & Bust Detection",
        svgFile = "docs/architecture/03-forecast-flow.svg",
        scope = "0–3h Nowcasting, 24h & 48h Hourly forecasting, Bust Detection Engine",
        icon = Icons.Default.CompassCalibration,
        keyNodes = listOf("Observation Check (T-0)", "Forecast Fetch (24H/48H)", "Model Divergence Calculator", "Spread Analysis (>4°C bust)", "Confidence Scoring", "Flag & Mitigation"),
        scientificPrinciple = "Forecast bust detection flags divergence between synoptic NWP guidance and rapid local convective initiation."
    ),
    ArchitectureDiagramInfo(
        id = "radar_sat",
        number = "04",
        title = "Radar, Satellite & NWP Tri-Pipeline",
        svgFile = "docs/architecture/04-radar-satellite-nwp.svg",
        scope = "Specialized spatial fusion across DWR radars, INSAT-3D and NWP grids",
        icon = Icons.Default.Radar,
        keyNodes = listOf("DWR Reflectivity (dBZ)", "INSAT-3D IR & VIS", "Open-Meteo 4km NWP", "Spatial Alignment", "Convective Extrapolation", "GIS Overlay Vector"),
        scientificPrinciple = "Multi-sensor alignment prevents false radar echoes while enhancing convective precipitation lead times."
    ),
    ArchitectureDiagramInfo(
        id = "hyperlocal",
        number = "05",
        title = "Hyperlocal Weather Intelligence",
        svgFile = "docs/architecture/05-hyperlocal.svg",
        scope = "Lat/Lon to Gram Panchayat hierarchy + 6-layer topographic conditioning",
        icon = Icons.Default.Layers,
        keyNodes = listOf("FusedLocationProviderClient", "Reverse Geocoder", "District/Block/Panchayat Hierarchy", "Lapse Rate Adjustment", "Land Use Correction", "Hyperlocal Output"),
        scientificPrinciple = "Downscaling meteorological grids using elevation lapse rate (-6.5°C/km) and micro-topography rather than raw interpolation."
    ),
    ArchitectureDiagramInfo(
        id = "flood_cyclone",
        number = "06",
        title = "Flood & Cyclone Early Warning Engine",
        svgFile = "docs/architecture/06-flood-cyclone.svg",
        scope = "River basin hydrology & inundation, cyclone track impact, alert escalation",
        icon = Icons.Default.WaterDamage,
        keyNodes = listOf("Catchment Runoff Model", "Soil Saturation Index", "CWC Gauge Level", "Inundation Risk Evaluator", "CAP Alert Builder", "n8n Webhook Trigger"),
        scientificPrinciple = "Heavy rainfall risk strictly != flood risk. Decouples atmospheric precipitation volume from soil moisture saturation and hydrological basin thresholds."
    ),
    ArchitectureDiagramInfo(
        id = "ai_voice_krishi",
        number = "07",
        title = "AI, Voice & KrishiGPT Decision Support",
        svgFile = "docs/architecture/07-ai-voice-krishi.svg",
        scope = "Zero-hallucination Gemini grounding, 10-language voice loop, agromet rules",
        icon = Icons.Default.Mic,
        keyNodes = listOf("VoiceRecognitionController", "Intent Domain Extractor", "Agromet Decision Engine", "Gemini Explanation Grounding", "Android TextToSpeech", "Barge-In Interrupt"),
        scientificPrinciple = "Gemini is used strictly as a natural language explanation and translation engine grounded on verified canonical weather data."
    ),
    ArchitectureDiagramInfo(
        id = "online_offline",
        number = "08",
        title = "Online, Offline & Reconnection Continuity",
        svgFile = "docs/architecture/08-online-offline.svg",
        scope = "Live streaming, Room SQLite offline continuity, truth-in-metrics stale flags",
        icon = Icons.Default.WifiOff,
        keyNodes = listOf("ConnectivityManager Flow", "Cache Hit Evaluator", "Stale Timestamp Warning", "Sync Queue", "Atomic Reconnection Ingestion", "UI Truth Banner"),
        scientificPrinciple = "Offline cache != new forecast. Pre-cached data is clearly flagged with precise observation timestamp; never simulates new forecasts without telemetry."
    ),
    ArchitectureDiagramInfo(
        id = "alert_gis",
        number = "09",
        title = "Alert Engine & Vector GIS Map",
        svgFile = "docs/architecture/09-alert-gis.svg",
        scope = "CAP-compliant deduplicated alerts, 7-layer Jetpack Compose vector GIS stack",
        icon = Icons.Default.Security,
        keyNodes = listOf("CAP XML Parser", "Geographic Polygons", "Deduplication Cache", "Canvas Vector Renderer", "Radar Overlay", "Haptic Alert Feedback"),
        scientificPrinciple = "Common Alerting Protocol (CAP) compliance ensuring disaster alerts are deduplicated, geospatial, and prioritized by threat severity."
    ),
    ArchitectureDiagramInfo(
        id = "backend_persistence",
        number = "10",
        title = "Backend Microservices & Persistence",
        svgFile = "docs/architecture/10-backend.svg",
        scope = "API Gateway, 10 specialized domain services, hybrid Room/PostGIS storage",
        icon = Icons.Default.Storage,
        keyNodes = listOf("Retrofit HTTP Layer", "Room Local Database", "WeatherDao", "ChatHistoryDao", "LocationRepository", "n8n Webhook Dispatcher"),
        scientificPrinciple = "Local-first Room SQLite persistence paired with robust HTTP/REST backends for rural resilience."
    )
)

@Composable
fun ArchitectureFlowDialog(
    onDismiss: () -> Unit
) {
    var selectedDiagram by remember { mutableStateOf<ArchitectureDiagramInfo?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepNavyBg)
                .padding(16.dp),
            color = DeepNavyBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElectricCyan.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountTree,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "TECHNICAL ARCHITECTURE",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "SIH 2026 Presentation System • 10 Production Flows",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Core Architectural Guarantees Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceNavy)
                        .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CORE ARCHITECTURAL GUARANTEES",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 10.sp),
                        color = ElectricCyan
                    )
                    Text(
                        text = "1. Heavy Rainfall Risk ≠ Flood Risk (Decoupled soil saturation & basin runoff)\n2. Zero-Hallucination AI Grounding (CanonicalWeatherSnapshot single source of truth)\n3. Offline Cache ≠ New Forecast (Stale flags, never fabricate telemetry)\n4. Forecast Bust Detection (>4°C or precipitation divergence automatically lowers confidence)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 17.sp),
                        color = TextHighlight
                    )
                }

                // Diagrams List
                Text(
                    text = "PROCESS-FLOW & SUBSYSTEM DIAGRAMS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 10.sp),
                    color = TextTertiary
                )

                SYSTEM_ARCHITECTURE_DIAGRAMS.forEach { diagram ->
                    val isExpanded = selectedDiagram?.id == diagram.id
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceCard.copy(alpha = 0.7f))
                            .border(1.dp, if (isExpanded) ElectricCyan else SurfaceBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                selectedDiagram = if (isExpanded) null else diagram
                            }
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ElectricTeal.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = diagram.icon, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text(
                                        text = "${diagram.number}. ${diagram.title}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextHighlight
                                    )
                                    Text(
                                        text = diagram.scope,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = TextSecondary,
                                        maxLines = if (isExpanded) 4 else 1
                                    )
                                }
                            }

                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DeepNavyBg)
                                        .padding(8.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = "Primary Pipeline Nodes:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = ElectricCyan)
                                        diagram.keyNodes.forEach { node ->
                                            Text(text = "• $node", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp), color = TextPrimary)
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DeepNavyBg)
                                        .padding(8.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(text = "Scientific Principle:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = AlertGreen)
                                        Text(text = diagram.scientificPrinciple, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp), color = TextSecondary)
                                    }
                                }

                                Text(
                                    text = "Vector Source: ${diagram.svgFile}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = TextTertiary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
