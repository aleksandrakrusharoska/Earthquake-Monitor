package mk.codeit.earthquake.domain.dto;

import mk.codeit.earthquake.domain.model.Earthquake;

import java.time.Instant;

public record CreateEarthquakeDTO(
        String usgsId,
        Double magnitude,
        String magType,
        String place,
        String title,
        Instant time,
        Double latitude,
        Double longitude,
        Double depth
) {
    public Earthquake toEntity() {
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
}