package mk.codeit.earthquake.domain.dto;

import mk.codeit.earthquake.domain.model.Earthquake;

import java.time.Instant;

public record EarthquakeDTO(
        Long id,
        String usgsId,
        Double magnitude,
        String magType,
        String place,
        String title,
        Instant time,
        Double latitude,
        Double longitude,
        Double depth,
        Instant fetchedAt
) {
    public static EarthquakeDTO fromEntity(Earthquake earthquake) {
        return new EarthquakeDTO(
                earthquake.getId(),
                earthquake.getUsgsId(),
                earthquake.getMagnitude(),
                earthquake.getMagType(),
                earthquake.getPlace(),
                earthquake.getTitle(),
                earthquake.getTime(),
                earthquake.getLatitude(),
                earthquake.getLongitude(),
                earthquake.getDepth(),
                earthquake.getFetchedAt()
        );
    }
}
