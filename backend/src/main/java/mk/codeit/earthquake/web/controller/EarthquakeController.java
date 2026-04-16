package mk.codeit.earthquake.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mk.codeit.earthquake.web.dto.ApiResponse;
import mk.codeit.earthquake.domain.dto.EarthquakeDTO;
import mk.codeit.earthquake.service.EarthquakeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/earthquakes")
@RequiredArgsConstructor
public class EarthquakeController {

    private final EarthquakeService earthquakeService;

    /**
     * GET /api/earthquakes
     * Returns all stored earthquake records.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EarthquakeDTO>>> getAllEarthquakes() {
        log.info("GET /api/earthquakes");
        List<EarthquakeDTO> earthquakes = earthquakeService.getAllEarthquakes();
        return ResponseEntity.ok(ApiResponse.success(earthquakes,
                "Retrieved " + earthquakes.size() + " earthquakes"));
    }

    /**
     * GET /api/earthquakes/{id}
     * Returns a single earthquake by database ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EarthquakeDTO>> getById(@PathVariable Long id) {
        log.info("GET /api/earthquakes/{}", id);
        EarthquakeDTO dto = earthquakeService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(dto, "Earthquake found"));
    }

    /**
     * POST /api/earthquakes/fetch
     * Triggers a new fetch from USGS API, clears old data, and stores filtered results.
     */
    @PostMapping("/fetch")
    public ResponseEntity<ApiResponse<List<EarthquakeDTO>>> fetchAndStore() {
        log.info("POST /api/earthquakes/fetch");
        List<EarthquakeDTO> saved = earthquakeService.fetchAndStore();
        return ResponseEntity.ok(ApiResponse.success(saved,
                "Fetched and stored " + saved.size() + " earthquakes"));
    }

    /**
     * GET /api/earthquakes/filter/magnitude?min=2.5
     * Returns earthquakes with magnitude greater than the given value.
     */
    @GetMapping("/filter/magnitude")
    public ResponseEntity<ApiResponse<List<EarthquakeDTO>>> filterByMagnitude(
            @RequestParam(defaultValue = "2.0") double min) {
        log.info("GET /api/earthquakes/filter/magnitude?min={}", min);
        List<EarthquakeDTO> results = earthquakeService.getByMinMagnitude(min);
        return ResponseEntity.ok(ApiResponse.success(results,
                "Found " + results.size() + " earthquakes with magnitude > " + min));
    }

    /**
     * GET /api/earthquakes/filter/time?after=1700000000000
     * Returns earthquakes after the given epoch milliseconds timestamp.
     */
    @GetMapping("/filter/time")
    public ResponseEntity<ApiResponse<List<EarthquakeDTO>>> filterByTime(
            @RequestParam long after) {
        log.info("GET /api/earthquakes/filter/time?after={}", after);
        List<EarthquakeDTO> results = earthquakeService.getAfterTime(after);
        return ResponseEntity.ok(ApiResponse.success(results,
                "Found " + results.size() + " earthquakes after given time"));
    }

    /**
     * DELETE /api/earthquakes/{id}
     * Deletes a specific earthquake record by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        log.info("DELETE /api/earthquakes/{}", id);
        earthquakeService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Earthquake deleted successfully"));
    }
}
