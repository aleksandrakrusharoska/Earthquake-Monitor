package mk.codeit.earthquake.service;

import mk.codeit.earthquake.domain.model.Earthquake;

import java.util.List;

public interface UsgsApiService {
    List<Earthquake> fetchEarthquakes();

    String fetchRawGeoJson();

    List<Earthquake> parseGeoJson(String geoJson);
}
