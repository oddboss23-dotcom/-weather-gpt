package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import com.example.data.gis.WeatherGisLayer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * High-performance vector/chip bitmap generator for Google Maps SDK meteorological markers.
 * Caches generated BitmapDescriptors to avoid repeated allocations during panning and zooming.
 */
object GisMarkerIconFactory {

    private val cache = ConcurrentHashMap<String, BitmapDescriptor>()

    fun getTemperatureMarker(tempC: Double): BitmapDescriptor {
        val rounded = Math.round(tempC).toInt()
        val key = "temp_$rounded"
        return cache.getOrPut(key) {
            val bgColor = when {
                rounded < 10 -> Color.rgb(0, 180, 216)    // Cyan/Cold
                rounded in 10..19 -> Color.rgb(72, 202, 228) // Cool
                rounded in 20..29 -> Color.rgb(16, 185, 129) // Green/Comfortable
                rounded in 30..39 -> Color.rgb(245, 158, 11) // Amber/High
                else -> Color.rgb(239, 68, 68)             // Red/Extreme Heat
            }
            createChipBitmap(
                text = "${rounded}°C",
                subText = when {
                    rounded < 10 -> "COLD"
                    rounded in 10..19 -> "COOL"
                    rounded in 20..29 -> "NORMAL"
                    rounded in 30..39 -> "HIGH"
                    else -> "HEAT"
                },
                backgroundColor = bgColor,
                textColor = Color.WHITE
            )
        }
    }

    fun getRainfallMarker(rainMmH: Double): BitmapDescriptor {
        val rounded = (Math.round(rainMmH * 10.0) / 10.0)
        val key = "rain_$rounded"
        return cache.getOrPut(key) {
            val (bgColor, label) = when {
                rounded <= 0.1 -> Pair(Color.rgb(51, 65, 85), "0mm")
                rounded <= 2.5 -> Pair(Color.rgb(16, 185, 129), "LIGHT")
                rounded <= 7.5 -> Pair(Color.rgb(59, 130, 246), "MOD")
                rounded <= 15.0 -> Pair(Color.rgb(245, 158, 11), "HEAVY")
                rounded <= 30.0 -> Pair(Color.rgb(239, 68, 68), "V.HEAVY")
                else -> Pair(Color.rgb(168, 85, 247), "EXTREME")
            }
            createChipBitmap(
                text = if (rounded <= 0.1) "0 mm" else "${rounded}mm/h",
                subText = label,
                backgroundColor = bgColor,
                textColor = Color.WHITE
            )
        }
    }

    fun getWindMarker(speedKmh: Double, directionDeg: Int): BitmapDescriptor {
        val speed = Math.round(speedKmh).toInt()
        val dirArrow = getWindArrow(directionDeg)
        val key = "wind_${speed}_${dirArrow}"
        return cache.getOrPut(key) {
            val bgColor = when {
                speed <= 10 -> Color.rgb(51, 65, 85)
                speed <= 20 -> Color.rgb(16, 185, 129)
                speed <= 40 -> Color.rgb(14, 165, 233)
                speed <= 60 -> Color.rgb(245, 158, 11)
                else -> Color.rgb(239, 68, 68)
            }
            createChipBitmap(
                text = "$dirArrow $speed",
                subText = "km/h",
                backgroundColor = bgColor,
                textColor = Color.WHITE
            )
        }
    }

    fun getRadarStationMarker(stationCode: String, dbz: Double): BitmapDescriptor {
        val roundedDbz = Math.round(dbz).toInt()
        val key = "radar_${stationCode}_$roundedDbz"
        return cache.getOrPut(key) {
            val bgColor = when {
                roundedDbz < 20 -> Color.rgb(16, 185, 129)  // Light
                roundedDbz < 35 -> Color.rgb(59, 130, 246)  // Moderate
                roundedDbz < 50 -> Color.rgb(245, 158, 11)  // Heavy
                roundedDbz < 65 -> Color.rgb(239, 68, 68)   // Very Heavy
                else -> Color.rgb(168, 85, 247)            // Severe Hail
            }
            createChipBitmap(
                text = stationCode.replace("DWR-", ""),
                subText = "${roundedDbz} dBZ",
                backgroundColor = bgColor,
                textColor = Color.WHITE,
                hasRadarBorder = true
            )
        }
    }

    fun getHeatIndexMarker(feelsLikeC: Double, category: String): BitmapDescriptor {
        val rounded = Math.round(feelsLikeC).toInt()
        val key = "heat_${rounded}_$category"
        return cache.getOrPut(key) {
            val bgColor = when (category) {
                "Extreme Danger" -> Color.rgb(147, 51, 234)
                "Danger" -> Color.rgb(239, 68, 68)
                "Extreme Caution" -> Color.rgb(249, 115, 22)
                "Caution" -> Color.rgb(234, 179, 8)
                else -> Color.rgb(16, 185, 129)
            }
            createChipBitmap(
                text = "${rounded}°C",
                subText = category.uppercase(),
                backgroundColor = bgColor,
                textColor = Color.WHITE
            )
        }
    }

    fun getFloodRiskMarker(risk: String): BitmapDescriptor {
        val key = "flood_$risk"
        return cache.getOrPut(key) {
            val bgColor = when (risk.uppercase()) {
                "VERY HIGH", "CRITICAL" -> Color.rgb(239, 68, 68)
                "HIGH" -> Color.rgb(249, 115, 22)
                "MODERATE" -> Color.rgb(234, 179, 8)
                else -> Color.rgb(16, 185, 129)
            }
            createChipBitmap(
                text = risk.uppercase(),
                subText = "RUNOFF",
                backgroundColor = bgColor,
                textColor = Color.WHITE
            )
        }
    }

    fun getWaterloggingMarker(severity: String, depthCm: Int): BitmapDescriptor {
        val key = "waterlogging_${severity}_$depthCm"
        return cache.getOrPut(key) {
            val bgColor = when (severity.uppercase()) {
                "SEVERE", "CRITICAL" -> Color.rgb(220, 38, 38)
                "MODERATE" -> Color.rgb(234, 88, 12)
                "LOW" -> Color.rgb(202, 138, 4)
                else -> Color.rgb(37, 99, 235)
            }
            createChipBitmap(
                text = "🌊 ${depthCm}cm",
                subText = severity.uppercase(),
                backgroundColor = bgColor,
                textColor = Color.WHITE
            )
        }
    }

    fun getLightningMarker(strikeCount: Int): BitmapDescriptor {
        val key = "lightning_$strikeCount"
        return cache.getOrPut(key) {
            val bgColor = if (strikeCount > 15) Color.rgb(239, 68, 68) else Color.rgb(245, 158, 11)
            createChipBitmap(
                text = "⚡ $strikeCount",
                subText = "DAMINI",
                backgroundColor = bgColor,
                textColor = Color.WHITE
            )
        }
    }

    fun getPressureMarker(hpa: Double): BitmapDescriptor {
        val rounded = Math.round(hpa).toInt()
        val key = "pressure_$rounded"
        return cache.getOrPut(key) {
            val bgColor = when {
                rounded < 1000 -> Color.rgb(239, 68, 68)
                rounded < 1008 -> Color.rgb(245, 158, 11)
                else -> Color.rgb(59, 130, 246)
            }
            createChipBitmap(
                text = "$rounded",
                subText = "hPa",
                backgroundColor = bgColor,
                textColor = Color.WHITE
            )
        }
    }

    fun getHumidityMarker(humidityPercent: Int): BitmapDescriptor {
        val key = "humidity_$humidityPercent"
        return cache.getOrPut(key) {
            val bgColor = when {
                humidityPercent > 80 -> Color.rgb(14, 165, 233)
                humidityPercent > 50 -> Color.rgb(16, 185, 129)
                else -> Color.rgb(245, 158, 11)
            }
            createChipBitmap(
                text = "$humidityPercent%",
                subText = "RH",
                backgroundColor = bgColor,
                textColor = Color.WHITE
            )
        }
    }

    fun getCloudMarker(cloudPercent: Int): BitmapDescriptor {
        val key = "cloud_$cloudPercent"
        return cache.getOrPut(key) {
            val bgColor = Color.rgb(71, 85, 105)
            createChipBitmap(
                text = "${cloudPercent}%",
                subText = "CLOUD",
                backgroundColor = bgColor,
                textColor = Color.WHITE
            )
        }
    }

    private fun getWindArrow(deg: Int): String {
        return when (((deg % 360 + 360) % 360)) {
            in 338..360, in 0..22 -> "↓"   // From North, blowing South
            in 23..67 -> "↙"                // From NE
            in 68..112 -> "←"               // From East
            in 113..157 -> "↖"              // From SE
            in 158..202 -> "↑"              // From South
            in 203..247 -> "↗"              // From SW
            in 248..292 -> "→"              // From West
            in 293..337 -> "↘"              // From NW
            else -> "↗"
        }
    }

    fun getConfidenceMarker(confidenceScore: Int): BitmapDescriptor {
        val key = "conf_$confidenceScore"
        return cache.getOrPut(key) {
            val (bgColor, label) = when {
                confidenceScore >= 85 -> Pair(Color.rgb(16, 185, 129), "HIGH CONF")
                confidenceScore >= 70 -> Pair(Color.rgb(0, 229, 255), "MOD CONF")
                else -> Pair(Color.rgb(245, 158, 11), "UNCERTAIN")
            }
            createChipBitmap(
                text = "$confidenceScore%",
                subText = label,
                backgroundColor = bgColor,
                textColor = Color.rgb(10, 15, 29),
                hasRadarBorder = true
            )
        }
    }

    fun getBustRiskMarker(bustScore: Int): BitmapDescriptor {
        val key = "bust_$bustScore"
        return cache.getOrPut(key) {
            val (bgColor, label) = when {
                bustScore >= 50 -> Pair(Color.rgb(239, 68, 68), "HIGH BUST ⚠️")
                bustScore >= 25 -> Pair(Color.rgb(245, 158, 11), "MOD BUST")
                else -> Pair(Color.rgb(16, 185, 129), "LOW BUST")
            }
            createChipBitmap(
                text = "$bustScore%",
                subText = label,
                backgroundColor = bgColor,
                textColor = Color.WHITE,
                hasRadarBorder = bustScore >= 50
            )
        }
    }

    private fun createChipBitmap(
        text: String,
        subText: String,
        backgroundColor: Int,
        textColor: Int,
        hasRadarBorder: Boolean = false
    ): BitmapDescriptor {
        val density = 2.5f
        val paddingHorizontal = (10 * density).toInt()
        val paddingVertical = (5 * density).toInt()

        val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 12 * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(230, 255, 255, 255)
            textSize = 8.5f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val textBounds = Rect()
        mainPaint.getTextBounds(text, 0, text.length, textBounds)
        val subBounds = Rect()
        subPaint.getTextBounds(subText, 0, subText.length, subBounds)

        val contentWidth = Math.max(textBounds.width(), subBounds.width())
        val width = contentWidth + (paddingHorizontal * 2)
        val height = textBounds.height() + subBounds.height() + (paddingVertical * 2) + (3 * density).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background rounded pill
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundColor
            style = Paint.Style.FILL
        }
        val cornerRadius = 8f * density
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

        // Subtle dark outline
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (hasRadarBorder) Color.rgb(0, 229, 255) else Color.argb(100, 0, 0, 0)
            style = Paint.Style.STROKE
            strokeWidth = if (hasRadarBorder) 2f * density else 1f * density
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint)

        // Draw main text (centered)
        val textX = (width - textBounds.width()) / 2f - textBounds.left
        val textY = paddingVertical.toFloat() + textBounds.height()
        canvas.drawText(text, textX, textY, mainPaint)

        // Draw subtext (centered)
        val subX = (width - subBounds.width()) / 2f - subBounds.left
        val subY = textY + (4 * density) + subBounds.height()
        canvas.drawText(subText, subX, subY, subPaint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
