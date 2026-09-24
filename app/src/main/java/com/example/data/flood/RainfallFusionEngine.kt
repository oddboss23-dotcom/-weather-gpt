package com.example.data.flood

import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Probabilistic Rainfall Forecast for a specific time horizon.
 */
data class ProbabilisticRainfallHorizon(
    val horizonHours: Int,
    val expectedAccumulationMm: Double,
    val minAccumulationMm: Double,
    val maxAccumulationMm: Double,
    val probRainOver10mm: Double, // 0.0 to 1.0 (e.g. 0.78 = 78%)
    val probRainOver25mm: Double,
    val probRainOver50mm: Double,
    val probRainOver100mm: Double,
    val primaryDriver: String // e.g., "Doppler Radar Convective Core + NWP Ensemble"
)

/**
 * Multi-sensor fused rainfall intelligence output.
 */
data class FusedRainfallEstimate(
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val timestamp: Long,
    val observedLast1hMm: Double,
    val observedLast24hMm: Double,
    val radarReflectivityDbz: Double?,
    val radarEstimatedRateMmPerHour: Double?,
    val satellitePrecipitationMm: Double?,
    val nwpForecast24hMm: Double,
    val fusedCurrentIntensityMmPerHour: Double,
    val probabilisticHorizons: List<ProbabilisticRainfallHorizon>,
    val sensorWeights: Map<String, Double>,
    val dataQualityScore: Double // 0.0 to 1.0
)

/**
 * RainfallFusionEngine (PS-26071 Core Module)
 * Combines station rainfall observations (AWS/ARG), Doppler Weather Radar (DWR) reflectivity,
 * INSAT-3D/GPM satellite precipitation, and Numerical Weather Prediction (NWP / GFS / WRF) models
 * to produce accurate probabilistic rainfall estimates across 1h, 3h, 6h, 12h, and 24h horizons.
 */
object RainfallFusionEngine {

    /**
     * Fuses multi-source meteorological inputs into calibrated probabilistic estimates.
     */
    fun fuseRainfall(
        latitude: Double,
        longitude: Double,
        locationName: String,
        stationObs1hMm: Double,
        stationObs24hMm: Double,
        radarDbz: Double?,
        satelliteMm: Double?,
        nwpForecast24hMm: Double,
        timestamp: Long = System.currentTimeMillis()
    ): FusedRainfallEstimate {
        // 1. Marshall-Palmer Z-R conversion for Radar (Z = 200 * R^1.6)
        val radarRate = radarDbz?.let { dbz ->
            val zLinear = 10.0.pow(dbz / 10.0)
            (zLinear / 200.0).pow(1.0 / 1.6).coerceIn(0.0, 150.0)
        }

        // 2. Dynamic multi-sensor weighting based on availability & physics
        val hasRadar = radarRate != null && radarRate > 0.1
        val hasStation = stationObs1hMm >= 0.0
        val hasSatellite = satelliteMm != null && satelliteMm >= 0.0

        val weightStation = if (hasStation) 0.40 else 0.0
        val weightRadar = if (hasRadar) 0.35 else 0.0
        val weightSatellite = if (hasSatellite) 0.15 else 0.0
        val weightNwp = if (!hasRadar && !hasStation) 0.70 else 0.10

        val totalWeight = (weightStation + weightRadar + weightSatellite + weightNwp).coerceAtLeast(0.01)
        val normWeightStation = weightStation / totalWeight
        val normWeightRadar = weightRadar / totalWeight
        val normWeightSatellite = weightSatellite / totalWeight
        val normWeightNwp = weightNwp / totalWeight

        // 3. Fused instantaneous intensity (mm/h)
        val satRate = (satelliteMm ?: 0.0) / 3.0 // convert 3-hourly sat estimate to hourly
        val nwpRate = nwpForecast24hMm / 24.0

        val fusedIntensity = (stationObs1hMm * normWeightStation) +
                ((radarRate ?: 0.0) * normWeightRadar) +
                (satRate * normWeightSatellite) +
                (nwpRate * normWeightNwp)

        // 4. Compute Probabilistic Forecasts across Horizons (1h, 3h, 6h, 12h, 24h)
        val horizons = listOf(1, 3, 6, 12, 24).map { hours ->
            computeHorizonProbabilities(
                hours = hours,
                currentIntensity = fusedIntensity,
                station24h = stationObs24hMm,
                radarRate = radarRate ?: 0.0,
                nwpTotal24h = nwpForecast24hMm
            )
        }

        val qualityScore = (if (hasStation) 0.35 else 0.1) +
                (if (hasRadar) 0.40 else 0.1) +
                (if (hasSatellite) 0.15 else 0.05) +
                (if (nwpForecast24hMm > 0) 0.10 else 0.05)

        return FusedRainfallEstimate(
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            timestamp = timestamp,
            observedLast1hMm = stationObs1hMm,
            observedLast24hMm = stationObs24hMm,
            radarReflectivityDbz = radarDbz,
            radarEstimatedRateMmPerHour = radarRate,
            satellitePrecipitationMm = satelliteMm,
            nwpForecast24hMm = nwpForecast24hMm,
            fusedCurrentIntensityMmPerHour = (fusedIntensity * 10.0).toInt() / 10.0,
            probabilisticHorizons = horizons,
            sensorWeights = mapOf(
                "Station Observations (AWS/ARG)" to normWeightStation,
                "Doppler Weather Radar (DWR)" to normWeightRadar,
                "Satellite Precipitation (INSAT-3D)" to normWeightSatellite,
                "NWP Model Ensemble (GFS/WRF)" to normWeightNwp
            ),
            dataQualityScore = qualityScore.coerceIn(0.1, 1.0)
        )
    }

    private fun computeHorizonProbabilities(
        hours: Int,
        currentIntensity: Double,
        station24h: Double,
        radarRate: Double,
        nwpTotal24h: Double
    ): ProbabilisticRainfallHorizon {
        // Horizon expected accumulation using exponential decay blending between nowcast (radar/obs) and NWP
        val persistenceWeight = exp(-hours / 4.0) // strong for 1-3h, decays by 12-24h
        val nwpWeight = 1.0 - persistenceWeight

        val nowcastAccumulation = currentIntensity * hours * 0.85
        val nwpAccumulation = (nwpTotal24h / 24.0) * hours

        val expectedAccumulation = (nowcastAccumulation * persistenceWeight) + (nwpAccumulation * nwpWeight)
        val spreadFactor = 0.25 + (hours * 0.025) // uncertainty widens with horizon

        val minAcc = max(0.0, expectedAccumulation * (1.0 - spreadFactor))
        val maxAcc = expectedAccumulation * (1.0 + spreadFactor * 1.5)

        // Logistic CDF approximation for threshold exceedance probability
        fun probExceed(threshold: Double): Double {
            if (expectedAccumulation <= 0.1) return 0.02
            val z = (expectedAccumulation - threshold) / (expectedAccumulation * spreadFactor + 1.0)
            val prob = 1.0 / (1.0 + exp(-1.7 * z))
            return (prob * 100.0).toInt() / 100.0
        }

        val primaryDriver = when {
            hours <= 3 && radarRate > 5.0 -> "Doppler Radar Convective Core Tracking"
            hours <= 6 && currentIntensity > 2.0 -> "Station Nowcast & Catchment Hydro-Flow"
            else -> "NWP High-Resolution Atmospheric Ensemble"
        }

        return ProbabilisticRainfallHorizon(
            horizonHours = hours,
            expectedAccumulationMm = (expectedAccumulation * 10.0).toInt() / 10.0,
            minAccumulationMm = (minAcc * 10.0).toInt() / 10.0,
            maxAccumulationMm = (maxAcc * 10.0).toInt() / 10.0,
            probRainOver10mm = probExceed(10.0),
            probRainOver25mm = probExceed(25.0),
            probRainOver50mm = probExceed(50.0),
            probRainOver100mm = probExceed(100.0),
            primaryDriver = primaryDriver
        )
    }
}
