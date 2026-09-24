package com.example.data.gis

import com.example.data.model.CityLocation

/**
 * State and District Hierarchy for Intelligent Map Zoom Levels
 */
data class IndianStateGis(
    val name: String,
    val centerLat: Double,
    val centerLon: Double,
    val zoomLevel: Float,
    val districts: List<IndianDistrictGis>
)

data class IndianDistrictGis(
    val name: String,
    val stateName: String,
    val centerLat: Double,
    val centerLon: Double,
    val weatherStationCode: String = "IMD-AWS",
    val elevationM: Double = 120.0
)

object IndianGeographicRegistry {

    val states: List<IndianStateGis> = listOf(
        IndianStateGis(
            name = "Delhi NCT",
            centerLat = 28.6139,
            centerLon = 77.2090,
            zoomLevel = 10f,
            districts = listOf(
                IndianDistrictGis("New Delhi", "Delhi NCT", 28.6139, 77.2090, "DWR-PALAM", 215.0),
                IndianDistrictGis("Central Delhi", "Delhi NCT", 28.6500, 77.2200, "IMD-SAFDARJUNG", 216.0),
                IndianDistrictGis("South Delhi", "Delhi NCT", 28.5355, 77.2410, "IMD-MEHRAULI", 228.0),
                IndianDistrictGis("North Delhi", "Delhi NCT", 28.7200, 77.1600, "IMD-ALIPUR", 210.0),
                IndianDistrictGis("East Delhi", "Delhi NCT", 28.6300, 77.3000, "IMD-MAYURVIHAR", 205.0)
            )
        ),
        IndianStateGis(
            name = "Maharashtra",
            centerLat = 19.7515,
            centerLon = 75.7139,
            zoomLevel = 6.8f,
            districts = listOf(
                IndianDistrictGis("Mumbai City", "Maharashtra", 18.9600, 72.8200, "DWR-MUMBAI-COLABA", 10.0),
                IndianDistrictGis("Mumbai Suburban", "Maharashtra", 19.1200, 72.8800, "IMD-SANTA-CRUZ", 14.0),
                IndianDistrictGis("Pune", "Maharashtra", 18.5204, 73.8567, "IMD-SHIVAJINAGAR", 560.0),
                IndianDistrictGis("Nagpur", "Maharashtra", 21.1458, 79.0882, "DWR-NAGPUR", 310.0),
                IndianDistrictGis("Nashik", "Maharashtra", 19.9975, 73.7898, "IMD-OJAR", 600.0),
                IndianDistrictGis("Aurangabad (Chhatrapati Sambhajinagar)", "Maharashtra", 19.8762, 75.3433, "IMD-CHIKALTHANA", 568.0)
            )
        ),
        IndianStateGis(
            name = "West Bengal",
            centerLat = 22.9868,
            centerLon = 87.8550,
            zoomLevel = 7.0f,
            districts = listOf(
                IndianDistrictGis("Kolkata", "West Bengal", 22.5726, 88.3639, "DWR-KOLKATA-ALIPORE", 9.0),
                IndianDistrictGis("North 24 Parganas", "West Bengal", 22.7200, 88.4800, "IMD-DUMDUM", 11.0),
                IndianDistrictGis("South 24 Parganas", "West Bengal", 22.1500, 88.4000, "IMD-CANNING", 6.0),
                IndianDistrictGis("Darjeeling", "West Bengal", 27.0410, 88.2663, "IMD-DARJEELING", 2042.0),
                IndianDistrictGis("Howrah", "West Bengal", 22.5958, 88.2636, "IMD-HOWRAH", 12.0)
            )
        ),
        IndianStateGis(
            name = "Tamil Nadu",
            centerLat = 11.1271,
            centerLon = 78.6569,
            zoomLevel = 6.9f,
            districts = listOf(
                IndianDistrictGis("Chennai", "Tamil Nadu", 13.0827, 80.2707, "DWR-CHENNAI-PORT", 7.0),
                IndianDistrictGis("Coimbatore", "Tamil Nadu", 11.0168, 76.9558, "IMD-PEELAMEDU", 411.0),
                IndianDistrictGis("Madurai", "Tamil Nadu", 9.9252, 78.1198, "IMD-MADURAI-AIRPORT", 101.0),
                IndianDistrictGis("Kanyakumari", "Tamil Nadu", 8.0883, 77.5385, "IMD-NAGERCOIL", 25.0)
            )
        ),
        IndianStateGis(
            name = "Karnataka",
            centerLat = 15.3173,
            centerLon = 75.7139,
            zoomLevel = 6.8f,
            districts = listOf(
                IndianDistrictGis("Bengaluru Urban", "Karnataka", 12.9716, 77.5946, "IMD-BENGALURU-CITY", 920.0),
                IndianDistrictGis("Mysuru", "Karnataka", 12.2958, 76.6394, "IMD-MYSURU", 763.0),
                IndianDistrictGis("Dakshina Kannada (Mangaluru)", "Karnataka", 12.9141, 74.8560, "IMD-PANAMBUR", 22.0)
            )
        ),
        IndianStateGis(
            name = "Odisha",
            centerLat = 20.9517,
            centerLon = 85.0985,
            zoomLevel = 7.0f,
            districts = listOf(
                IndianDistrictGis("Khurda (Bhubaneswar)", "Odisha", 20.2961, 85.8245, "DWR-BHUBANESWAR", 45.0),
                IndianDistrictGis("Puri", "Odisha", 19.8135, 85.8312, "IMD-PURI-COASTAL", 5.0),
                IndianDistrictGis("Balasore", "Odisha", 21.4934, 86.9135, "IMD-CHANDIPUR", 16.0),
                IndianDistrictGis("Ganjam (Gopalpur)", "Odisha", 19.3149, 84.7865, "DWR-GOPALPUR", 18.0)
            )
        ),
        IndianStateGis(
            name = "Telangana",
            centerLat = 18.1124,
            centerLon = 79.0193,
            zoomLevel = 7.0f,
            districts = listOf(
                IndianDistrictGis("Hyderabad", "Telangana", 17.3850, 78.4867, "DWR-HYDERABAD-BEGUMPET", 505.0),
                IndianDistrictGis("Warangal", "Telangana", 17.9689, 79.5941, "IMD-HANAMKONDA", 270.0)
            )
        ),
        IndianStateGis(
            name = "Gujarat",
            centerLat = 22.2587,
            centerLon = 71.1924,
            zoomLevel = 6.8f,
            districts = listOf(
                IndianDistrictGis("Ahmedabad", "Gujarat", 23.0225, 72.5714, "IMD-AHMEDABAD", 53.0),
                IndianDistrictGis("Surat", "Gujarat", 21.1702, 72.8311, "IMD-SURAT-COASTAL", 13.0),
                IndianDistrictGis("Kutch (Bhuj)", "Gujarat", 23.2420, 69.6669, "DWR-BHUJ", 110.0)
            )
        ),
        IndianStateGis(
            name = "Rajasthan",
            centerLat = 27.0238,
            centerLon = 74.2179,
            zoomLevel = 6.5f,
            districts = listOf(
                IndianDistrictGis("Jaipur", "Rajasthan", 26.9124, 75.7873, "DWR-JAIPUR-SANGANER", 431.0),
                IndianDistrictGis("Jodhpur", "Rajasthan", 26.2389, 73.0243, "IMD-JODHPUR", 231.0),
                IndianDistrictGis("Udaipur", "Rajasthan", 24.5854, 73.7125, "IMD-DABOK", 598.0)
            )
        ),
        IndianStateGis(
            name = "Kerala",
            centerLat = 10.8505,
            centerLon = 76.2711,
            zoomLevel = 7.2f,
            districts = listOf(
                IndianDistrictGis("Ernakulam (Kochi)", "Kerala", 9.9312, 76.2673, "DWR-KOCHI", 4.0),
                IndianDistrictGis("Thiruvananthapuram", "Kerala", 8.5241, 76.9366, "IMD-THIRUVANANTHAPURAM", 64.0),
                IndianDistrictGis("Wayanad", "Kerala", 11.6854, 76.1320, "IMD-KALPETTA", 780.0)
            )
        ),
        IndianStateGis(
            name = "Assam",
            centerLat = 26.2006,
            centerLon = 92.9376,
            zoomLevel = 7.0f,
            districts = listOf(
                IndianDistrictGis("Kamrup Metropolitan (Guwahati)", "Assam", 26.1445, 91.7362, "IMD-BORJHAR-AIRPORT", 54.0),
                IndianDistrictGis("Dibrugarh", "Assam", 27.4728, 94.9120, "IMD-MOHANBARI", 108.0)
            )
        ),
        IndianStateGis(
            name = "Bihar",
            centerLat = 25.0961,
            centerLon = 85.3131,
            zoomLevel = 7.2f,
            districts = listOf(
                IndianDistrictGis("Patna", "Bihar", 25.5941, 85.1376, "DWR-PATNA", 53.0),
                IndianDistrictGis("Gaya", "Bihar", 24.7955, 84.9994, "IMD-GAYA", 116.0)
            )
        )
    )

    fun findStateByName(stateName: String): IndianStateGis? {
        return states.firstOrNull { it.name.contains(stateName, ignoreCase = true) }
    }
}
