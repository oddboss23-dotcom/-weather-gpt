package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.HapticFeedbackManager
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.WeatherGPTViewModel

@Composable
fun RadarForecastScreen(
    viewModel: WeatherGPTViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(1) } // 0 = Forecast & Charts, 1 = Radar & GIS Map

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
    ) {
        // Top Segment Tab: Forecast vs GIS Radar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceNavy,
                contentColor = ElectricCyan,
                divider = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(50))
                    .padding(3.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        if (selectedTab != 0) {
                            HapticFeedbackManager.triggerMapViewSwitch(context)
                            selectedTab = 0
                        }
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selectedTab == 0) SurfaceCard else Color.Transparent),
                    text = {
                        Text(
                            text = "FORECAST",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                fontSize = 11.sp
                            ),
                            color = if (selectedTab == 0) ElectricCyan else TextTertiary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        if (selectedTab != 1) {
                            HapticFeedbackManager.triggerMapViewSwitch(context)
                            selectedTab = 1
                        }
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selectedTab == 1) SurfaceCard else Color.Transparent),
                    text = {
                        Text(
                            text = "GIS RADAR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                fontSize = 11.sp
                            ),
                            color = if (selectedTab == 1) ElectricCyan else TextTertiary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }

        // Body Content
        if (selectedTab == 0) {
            ForecastScreen(viewModel = viewModel)
        } else {
            RadarMapScreen(viewModel = viewModel)
        }
    }
}
