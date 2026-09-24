package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserCustomAlertRule
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.AlertYellow
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanLight
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun AlertRuleAutomationCard(
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val defaultRules = remember {
        mutableStateListOf(
            UserCustomAlertRule(
                id = "1",
                ruleName = "Heavy Cloudburst Alert",
                conditionType = "Rainfall",
                thresholdValue = "> 50 mm / hour",
                channel = "App Notification + Voice",
                isEnabled = true
            ),
            UserCustomAlertRule(
                id = "2",
                ruleName = "Extreme Heatwave Warning",
                conditionType = "Temperature",
                thresholdValue = "> 42°C (Heat Index > 48°C)",
                channel = "App Notification",
                isEnabled = true
            ),
            UserCustomAlertRule(
                id = "3",
                ruleName = "Damini Lightning Proximity",
                conditionType = "Lightning",
                thresholdValue = "Strike within 15 km",
                channel = "Voice Audio Siren",
                isEnabled = true
            ),
            UserCustomAlertRule(
                id = "4",
                ruleName = "Cyclone Storm Surge",
                conditionType = "Cyclone",
                thresholdValue = "Eye within 100 km",
                channel = "High Priority Broadcast",
                isEnabled = false
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceNavy)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Rule,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "CUSTOM ALERT AUTOMATION ENGINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 11.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Event-driven meteorological threshold rules",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = CyanLight
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ElectricCyan.copy(alpha = 0.15f))
                        .border(0.8.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .clickable { showAddDialog = !showAddDialog }
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(12.dp))
                        Text(
                            text = "Add Rule",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = ElectricCyan
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showAddDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "QUICK PRESET RULE TEMPLATES",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                            color = TextTertiary
                        )
                        listOf(
                            Triple("Wind Speed > 45 km/h", "Wind", "SMS Broadcast"),
                            Triple("Urban Flood Risk = HIGH", "Flood", "App Notification"),
                            Triple("Humidity > 90% (Pest Alert)", "Agriculture", "Voice Advisory")
                        ).forEach { (presetName, type, channel) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DeepNavyBg)
                                    .clickable {
                                        defaultRules.add(
                                            UserCustomAlertRule(
                                                id = System.currentTimeMillis().toString(),
                                                ruleName = presetName,
                                                conditionType = type,
                                                thresholdValue = presetName,
                                                channel = channel,
                                                isEnabled = true
                                            )
                                        )
                                        showAddDialog = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "+ $presetName", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = ElectricCyan)
                                Text(text = channel, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextTertiary)
                            }
                        }
                    }
                }
            }

            // Rules List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                defaultRules.forEachIndexed { index, rule ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCard)
                            .border(0.5.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = rule.ruleName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    ),
                                    color = if (rule.isEnabled) Color.White else TextTertiary
                                )
                                Text(
                                    text = "• ${rule.channel}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = if (rule.isEnabled) ElectricCyan else TextTertiary
                                )
                            }
                            Text(
                                text = "Trigger condition: ${rule.thresholdValue}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (rule.isEnabled) TextSecondary else TextTertiary
                            )
                        }

                        Switch(
                            checked = rule.isEnabled,
                            onCheckedChange = { checked ->
                                defaultRules[index] = rule.copy(isEnabled = checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ElectricCyan,
                                checkedTrackColor = ElectricCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = SurfaceNavy
                            ),
                            modifier = Modifier.size(width = 38.dp, height = 24.dp)
                        )
                    }
                }
            }
        }
    }
}
