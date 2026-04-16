package mk.codeit.earthquake.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mk.codeit.earthquake.domain.exception.UsgsApiException;
import mk.codeit.earthquake.domain.model.Earthquake;
import mk.codeit.earthquake.domain.model.EarthquakeFactory;
import mk.codeit.earthquake.service.UsgsApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsgsApiServiceImpl implements UsgsApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${usgs.api.url}")
    private String usgsApiUrl;

    /**
     * Fetches raw GeoJSON string from USGS API.
     */
    @Override
    public String fetchRawGeoJson() {
        try {
            log.info("Fetching earthquake data from USGS API: {}", usgsApiUrl);
            String response = restTemplate.getForObject(usgsApiUrl, String.class);
            if (response == null || response.isBlank()) {
                throw new UsgsApiException("USGS API returned empty response");
            }
            return response;
        } catch (RestClientException ex) {
            throw new UsgsApiException("Failed to connect to USGS API", ex);
        }
    }

    /**
     * Fetches and parses GeoJSON into a list of Earthquake objects.
     */
    @Override
    public List<Earthquake> fetchEarthquakes() {
        String rawJson = fetchRawGeoJson();
        return parseGeoJson(rawJson);
    }

    /**
     * Parses GeoJSON string and converts to Earthquake entities.
     */
    @Override
    public List<Earthquake> parseGeoJson(String geoJson) {
        List<Earthquake> earthquakes = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(geoJson);
            JsonNode features = root.path("features");

            if (features.isMissingNode() || !features.isArray()) {
                throw new UsgsApiException("Invalid GeoJSON: missing 'features' array");
            }

            for (JsonNode feature : features) {
                try {
                    Earthquake eq = EarthquakeFactory.fromGeoJsonFeature(feature);
                    if (eq != null) {
                        earthquakes.add(eq);
                    }
                } catch (Exception ex) {
                    log.warn("Skipping invalid earthquake feature: {}", ex.getMessage());
                }
            }

            log.info("Parsed {} earthquakes from USGS API", earthquakes.size());

        } catch (UsgsApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UsgsApiException("Failed to parse GeoJSON response", ex);
        }
        return earthquakes;
    }

    private Earthquake parseFeature(JsonNode feature) {
        String usgsId = getTextOrThrow(feature, "id", "Missing earthquake ID");

        JsonNode properties = feature.path("properties");
        if (properties.isMissingNode()) {
            throw new IllegalArgumentException("Missing 'properties' for feature: " + usgsId);
        }

        JsonNode magNode = properties.path("mag");
        if (magNode.isMissingNode() || magNode.isNull()) {
            log.debug("Skipping earthquake {} - missing magnitude", usgsId);
            return null;
        }
        Double magnitude = magNode.asDouble();

        String place = properties.path("place").asText(null);
        if (place == null || place.isBlank()) {
            log.debug("Skipping earthquake {} - missing place", usgsId);
            return null;
        }

        JsonNode timeNode = properties.path("time");
        if (timeNode.isMissingNode() || timeNode.isNull()) {
            throw new IllegalArgumentException("Missing 'time' for feature: " + usgsId);
        }
        Instant time = Instant.ofEpochMilli(timeNode.asLong());

        String title = properties.path("title").asText(place);
        String magType = properties.path("magType").asText(null);

        // Parse geometry coordinates
        JsonNode geometry = feature.path("geometry");
        Double latitude = null;
        Double longitude = null;
        Double depth = null;

        if (!geometry.isMissingNode() && !geometry.isNull()) {
            JsonNode coords = geometry.path("coordinates");
            if (coords.isArray() && coords.size() >= 2) {
                longitude = coords.get(0).asDouble();
                latitude = coords.get(1).asDouble();
                if (coords.size() >= 3) {
                    depth = coords.get(2).asDouble();
                }
            }
        }

        return Earthquake.builder()
                .usgsId(usgsId)
                .magnitude(magnitude)
                .magType(magType)
                .place(place)
                .title(title)
                .time(time)
                .latitude(latitude)
                .longitude(longitude)
                .depth(depth)
                .build();
    }

    private String getTextOrThrow(JsonNode node, String field, String errorMessage) {
        JsonNode fieldNode = node.path(field);
        if (fieldNode.isMissingNode() || fieldNode.isNull() || fieldNode.asText().isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return fieldNode.asText();
    }
}
