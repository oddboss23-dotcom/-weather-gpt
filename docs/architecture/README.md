# WeatherGPT Technical Architecture &amp; Process-Flow Diagram Suite
**Smart India Hackathon (SIH 2026) | Problem Statement: PS-68 | Ministry of Earth Sciences**

---

### Core Philosophy: "Data → Intelligence → Forecast → Risk → Action"
WeatherGPT is engineered not as a speculative forecasting model or a basic weather wrapper, but as an **authoritative meteorological intelligence and decision-support layer** designed specifically for the complex climatic and disaster realities of India.

All diagrams in this suite represent the **actual implemented production architecture** of the project, with zero invented APIs or mock infrastructure.

---

## Complete Diagram Catalog

| # | Diagram Name | File | Primary Technical Scope |
|---|---|---|---|
| **01** | **Master System Architecture** | [`01-master-architecture.svg`](./01-master-architecture.svg) | 8-layer horizontal enterprise architecture from raw ingest to mobile UX |
| **02** | **End-to-End Data Flow Pipeline** | [`02-end-to-end-flow.svg`](./02-end-to-end-flow.svg) | Full ingestion → cleaning → canonical snapshot → multi-channel dispatch |
| **03** | **Multi-Horizon Forecast Flow** | [`03-forecast-flow.svg`](./03-forecast-flow.svg) | 0–3h Nowcasting, 24h & 48h Hourly forecasting, Bust Detection Engine |
| **04** | **Radar, Satellite &amp; NWP Tri-Pipeline** | [`04-radar-satellite-nwp.svg`](./04-radar-satellite-nwp.svg) | Specialized spatial fusion across DWR radars, INSAT-3D and NWP grids |
| **05** | **Hyperlocal Weather Intelligence** | [`05-hyperlocal.svg`](./05-hyperlocal.svg) | Lat/Lon to Gram Panchayat hierarchy + 6-layer topographic conditioning |
| **06** | **Flood &amp; Cyclone Early Warning** | [`06-flood-cyclone.svg`](./06-flood-cyclone.svg) | River basin hydrology &amp; inundation, cyclone track impact, alert escalation |
| **07** | **AI, Voice &amp; KrishiGPT Decision Support** | [`07-ai-voice-krishi.svg`](./07-ai-voice-krishi.svg) | Zero-hallucination Gemini grounding, 10-language voice loop, agromet rules |
| **08** | **Online, Offline &amp; Reconnection** | [`08-online-offline.svg`](./08-online-offline.svg) | Live streaming, Room SQLite offline continuity, truth-in-metrics stale flags |
| **09** | **Alert Engine &amp; Vector GIS Map** | [`09-alert-gis.svg`](./09-alert-gis.svg) | CAP-compliant deduplicated alerts, 7-layer Jetpack Compose vector GIS stack |
| **10** | **Backend Microservices &amp; Persistence** | [`10-backend.svg`](./10-backend.svg) | API Gateway, 10 specialized domain services, hybrid Room/PostGIS storage |

---

## Architectural Distinctions & Guarantees

1. **Heavy Rainfall Risk ≠ Flood Risk (Diagram 06)**
   Heavy localized rainfall on steep well-drained terrain drains away safely; modest rainfall over an already saturated river basin or urban drainage depression causes catastrophic inundation. WeatherGPT explicitly decouples meteorological precipitation from hydrological flood modeling.

2. **Zero-Hallucination AI Grounding (Diagram 07)**
   The `CanonicalWeatherSnapshot` is the single source of truth. Google Gemini is utilized strictly as a natural language translation and explanation engine. It is architecturally prevented from inventing, adjusting, or speculating on meteorological metrics.

3. **Offline Cache ≠ New Forecast (Diagram 08)**
   When connectivity drops in remote rural areas, WeatherGPT serves the pre-cached snapshot with prominent age and stale timestamps. It never fabricates simulated forward weather without telemetry.

4. **Multi-Model Consensus &amp; Bust Detection (Diagram 03 &amp; 04)**
   When numerical weather prediction models diverge significantly or Doppler radar indicates rapid convective initiation not anticipated by synoptic models, the system automatically lowers confidence scores and highlights uncertainty zones to disaster managers.
