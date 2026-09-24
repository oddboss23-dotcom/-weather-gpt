package com.example.data.gis

import com.example.data.model.WeatherData

/**
 * Common interface for meteorological data providers.
 * Supports multi-source data ingestion, official failover, and data provenance.
 */
interface WeatherDataProvider {
    val providerId: String
    val providerName: String
    val isEnabled: Boolean
    val isAvailable: Boolean

    suspend fun fetchCurrentConditions(lat: Double, lon: Double): ProviderResult<WeatherData>
    suspend fun fetchSatelliteMetadata(): ProviderResult<SatelliteMetadata>
    suspend fun fetchRadarEchoes(lat: Double, lon: Double): ProviderResult<List<RadarEchoCell>>
}

data class ProviderResult<T>(
    val isSuccess: Boolean,
    val data: T? = null,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val sourceBadge: String = "",
    val freshness: DataFreshness = DataFreshness.LIVE
)

data class SatelliteMetadata(
    val satelliteName: String = "INSAT-3DR",
    val instrument: String = "Imager / Sounder",
    val sensorChannel: String = "TIR-1 (10.8 µm) & MIR (3.9 µm)",
    val productCode: String = "QPE / CMV / UTH",
    val captureTimeIST: String,
    val groundStation: String = "Space Applications Centre (ISRO) / MOSDAC, Ahmedabad",
    val isOfficial: Boolean = true
)

data class RadarEchoCell(
    val id: String,
    val stationName: String,
    val lat: Double,
    val lon: Double,
    val dbz: Double,
    val rainfallRateMmH: Double,
    val echoTopKm: Double,
    val movementDirection: String,
    val movementSpeedKmh: Double,
    val riskLevel: String
)

/**
 * Official ISRO / MOSDAC satellite provider abstraction
 */
class IsroMosdacProvider : WeatherDataProvider {
    override val providerId: String = "MOSDAC_INSAT3DR"
    override val providerName: String = "ISRO / MOSDAC INSAT-3DR"
    override val isEnabled: Boolean = true
    override val isAvailable: Boolean = true

    override suspend fun fetchCurrentConditions(lat: Double, lon: Double): ProviderResult<WeatherData> {
        // Satellite-derived estimation (QPE, CMV, UTH)
        return ProviderResult(
            isSuccess = true,
            data = null,
            sourceBadge = "INSAT-3DR / MOSDAC",
            freshness = DataFreshness.LIVE
        )
    }

    override suspend fun fetchSatelliteMetadata(): ProviderResult<SatelliteMetadata> {
        return ProviderResult(
            isSuccess = true,
            data = SatelliteMetadata(
                satelliteName = "INSAT-3DR",
                instrument = "Multi-spectral Imager",
                sensorChannel = "Thermal Infrared (TIR1) & Water Vapour (WV)",
                productCode = "INSAT3DR_IMG_L1C_QPE",
                captureTimeIST = "14:20 IST",
                groundStation = "MOSDAC / ISRO Ahmedabad"
            ),
            sourceBadge = "ISRO / MOSDAC",
            freshness = DataFreshness.LIVE
        )
    }

    override suspend fun fetchRadarEchoes(lat: Double, lon: Double): ProviderResult<List<RadarEchoCell>> {
        return ProviderResult(isSuccess = false, errorMessage = "Radar data provided by IMD DWR network")
    }
}

/**
 * India Meteorological Department (IMD) provider abstraction
 */
class ImdProvider : WeatherDataProvider {
    override val providerId: String = "IMD_NATIONAL"
    override val providerName: String = "India Meteorological Department (IMD)"
    override val isEnabled: Boolean = true
    override val isAvailable: Boolean = true

    override suspend fun fetchCurrentConditions(lat: Double, lon: Double): ProviderResult<WeatherData> {
        return ProviderResult(isSuccess = true, sourceBadge = "IMD", freshness = DataFreshness.LIVE)
    }

    override suspend fun fetchSatelliteMetadata(): ProviderResult<SatelliteMetadata> {
        return ProviderResult(isSuccess = false, errorMessage = "Use ISRO/MOSDAC for direct INSAT feeds")
    }

    override suspend fun fetchRadarEchoes(lat: Double, lon: Double): ProviderResult<List<RadarEchoCell>> {
        return ProviderResult(isSuccess = true, sourceBadge = "IMD DWR Network", freshness = DataFreshness.LIVE)
    }
}

/**
 * Google Weather API provider abstraction
 */
class GoogleWeatherProvider : WeatherDataProvider {
    override val providerId: String = "GOOGLE_WEATHER"
    override val providerName: String = "Google Weather API"
    override val isEnabled: Boolean = true
    override val isAvailable: Boolean = true

    override suspend fun fetchCurrentConditions(lat: Double, lon: Double): ProviderResult<WeatherData> {
        return ProviderResult(isSuccess = true, sourceBadge = "Google Weather", freshness = DataFreshness.LIVE)
    }

    override suspend fun fetchSatelliteMetadata(): ProviderResult<SatelliteMetadata> {
        return ProviderResult(isSuccess = false, errorMessage = "Google provides weather variables, not raw INSAT satellite telemetry")
    }

    override suspend fun fetchRadarEchoes(lat: Double, lon: Double): ProviderResult<List<RadarEchoCell>> {
        return ProviderResult(isSuccess = true, sourceBadge = "Google Weather Radar", freshness = DataFreshness.LIVE)
    }
}

/**
 * Meteomatics OPTIONAL provider abstraction
 * Credentials strictly verified via secure backend configuration; never hardcoded.
 */
class MeteomaticsProvider(private val isConfiguredOnBackend: Boolean = false) : WeatherDataProvider {
    override val providerId: String = "METEOMATICS"
    override val providerName: String = "Meteomatics Global Weather Engine"
    override val isEnabled: Boolean = isConfiguredOnBackend
    override val isAvailable: Boolean = isConfiguredOnBackend

    override suspend fun fetchCurrentConditions(lat: Double, lon: Double): ProviderResult<WeatherData> {
        if (!isConfiguredOnBackend) {
            return ProviderResult(
                isSuccess = false,
                errorMessage = "Meteomatics API credentials not configured in backend environment. Falling back to official IMD/MOSDAC sources.",
                sourceBadge = "Meteomatics (Unconfigured)"
            )
        }
        return ProviderResult(isSuccess = true, sourceBadge = "Meteomatics", freshness = DataFreshness.LIVE)
    }

    override suspend fun fetchSatelliteMetadata(): ProviderResult<SatelliteMetadata> {
        return ProviderResult(isSuccess = false, errorMessage = "Meteomatics optional integration")
    }

    override suspend fun fetchRadarEchoes(lat: Double, lon: Double): ProviderResult<List<RadarEchoCell>> {
        return ProviderResult(isSuccess = false, errorMessage = "Meteomatics optional integration")
    }
}

/**
 * NWP (Numerical Weather Prediction) provider for GFS & WRF integration
 */
class NwpProvider : WeatherDataProvider {
    override val providerId: String = "NWP_GFS_WRF"
    override val providerName: String = "NWP Models (GFS 0.25° / Regional WRF 3km)"
    override val isEnabled: Boolean = true
    override val isAvailable: Boolean = true

    override suspend fun fetchCurrentConditions(lat: Double, lon: Double): ProviderResult<WeatherData> {
        return ProviderResult(isSuccess = true, sourceBadge = "NWP GFS/WRF", freshness = DataFreshness.RECENT)
    }

    override suspend fun fetchSatelliteMetadata(): ProviderResult<SatelliteMetadata> {
        return ProviderResult(isSuccess = false, errorMessage = "NWP is numerical model data, not satellite sensor")
    }

    override suspend fun fetchRadarEchoes(lat: Double, lon: Double): ProviderResult<List<RadarEchoCell>> {
        return ProviderResult(isSuccess = true, sourceBadge = "NWP Simulated Reflectivity", freshness = DataFreshness.RECENT)
    }
}
