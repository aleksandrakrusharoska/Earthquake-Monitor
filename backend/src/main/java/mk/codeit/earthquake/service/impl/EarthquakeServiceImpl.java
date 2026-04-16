package mk.codeit.earthquake.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mk.codeit.earthquake.domain.dto.EarthquakeDTO;
import mk.codeit.earthquake.domain.exception.EarthquakeNotFoundException;
import mk.codeit.earthquake.domain.model.Earthquake;
import mk.codeit.earthquake.repository.EarthquakeRepository;
import mk.codeit.earthquake.service.EarthquakeService;
import mk.codeit.earthquake.service.UsgsApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EarthquakeServiceImpl implements EarthquakeService {

    private final EarthquakeRepository earthquakeRepository;
    private final UsgsApiService usgsApiService;

    @Value("${earthquake.filter.min-magnitude}")
    private double minMagnitude;

    /**
     * Fetches latest earthquakes from USGS, filters by magnitude > 2.0,
     * clears existing data, and saves the new results.
     *
     * @return list of saved earthquake DTOs
     */
    @Transactional
    @Override
    public List<EarthquakeDTO> fetchAndStore() {
        log.info("Starting fetch-and-store cycle from USGS API...");

        List<Earthquake> allEarthquakes = usgsApiService.fetchEarthquakes();

        List<Earthquake> filtered = allEarthquakes.stream()
                .filter(eq -> eq.getMagnitude() != null && eq.getMagnitude() > minMagnitude)
                .collect(Collectors.toList());

        log.info("Filtered {} earthquakes with magnitude > {}", filtered.size(), minMagnitude);

        // Delete existing records to avoid duplicates
        earthquakeRepository.deleteAllEarthquakes();
        log.info("Cleared existing earthquake records");

        List<Earthquake> saved = earthquakeRepository.saveAll(filtered);
        log.info("Saved {} earthquake records to database", saved.size());

        return saved.stream()
                .map(EarthquakeDTO::fromEntity)
                .toList();
    }

    /**
     * Returns all stored earthquakes ordered by time descending.
     */
    @Transactional(readOnly = true)
    @Override
    public List<EarthquakeDTO> getAllEarthquakes() {
        return earthquakeRepository.findAllByOrderByTimeDesc()
                .stream()
                .map(EarthquakeDTO::fromEntity)
                .toList();
    }

    /**
     * Returns earthquakes with magnitude greater than the given threshold.
     */
    @Transactional(readOnly = true)
    @Override
    public List<EarthquakeDTO> getByMinMagnitude(double magnitude) {
        if (magnitude < 0) {
            throw new IllegalArgumentException("Magnitude cannot be negative");
        }
        return earthquakeRepository.findByMagnitudeGreaterThanOrderByTimeDesc(magnitude)
                .stream()
                .map(EarthquakeDTO::fromEntity)
                .toList();
    }

    /**
     * Returns earthquakes that occurred after the given timestamp (epoch millis).
     */
    @Transactional(readOnly = true)
    @Override
    public List<EarthquakeDTO> getAfterTime(long epochMillis) {
        if (epochMillis < 0) {
            throw new IllegalArgumentException("Timestamp cannot be negative");
        }
        Instant after = Instant.ofEpochMilli(epochMillis);
        return earthquakeRepository.findByTimeAfterOrderByTimeDesc(after)
                .stream()
                .map(EarthquakeDTO::fromEntity)
                .toList();
    }

    /**
     * Deletes a specific earthquake record by ID.
     */
    @Transactional
    @Override
    public void deleteById(Long id) {
        if (!earthquakeRepository.existsById(id)) {
            throw new EarthquakeNotFoundException(id);
        }
        earthquakeRepository.deleteById(id);
        log.info("Deleted earthquake with id: {}", id);
    }

    /**
     * Returns a single earthquake by ID.
     */
    @Transactional(readOnly = true)
    @Override
    public EarthquakeDTO getById(Long id) {
        Earthquake earthquake = earthquakeRepository.findById(id)
                .orElseThrow(() -> new EarthquakeNotFoundException(id));
        return EarthquakeDTO.fromEntity(earthquake);
    }
}
