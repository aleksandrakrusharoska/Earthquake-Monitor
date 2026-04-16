package mk.codeit.earthquake.service;

import mk.codeit.earthquake.domain.dto.EarthquakeDTO;

import java.util.List;

public interface EarthquakeService {
    List<EarthquakeDTO> fetchAndStore();

    List<EarthquakeDTO> getAllEarthquakes();

    List<EarthquakeDTO> getByMinMagnitude(double magnitude);

    List<EarthquakeDTO> getAfterTime(long epochMillis);

    EarthquakeDTO getById(Long id);

    void deleteById(Long id);
}