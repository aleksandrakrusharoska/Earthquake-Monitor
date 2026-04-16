package mk.codeit.earthquake.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import mk.codeit.earthquake.domain.dto.CreateEarthquakeDTO;

import java.time.Instant;

@Slf4j
public class EarthquakeFactory {

    private EarthquakeFactory() {
    }

    public static Earthquake fromGeoJsonFeature(JsonNode feature) {
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

        Double latitude = null;
        Double longitude = null;
        Double depth = null;

        JsonNode geometry = feature.path("geometry");
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

        return new CreateEarthquakeDTO(
                usgsId, magnitude, magType, place,
                title, time, latitude, longitude, depth
        ).toEntity();
    }

    private static String getTextOrThrow(JsonNode node, String field, String errorMessage) {
        JsonNode fieldNode = node.path(field);
        if (fieldNode.isMissingNode() || fieldNode.isNull() || fieldNode.asText().isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return fieldNode.asText();
    }
}