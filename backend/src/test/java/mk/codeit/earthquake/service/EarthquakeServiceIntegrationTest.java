package mk.codeit.earthquake.service;

import mk.codeit.earthquake.domain.dto.EarthquakeDTO;
import mk.codeit.earthquake.domain.exception.EarthquakeNotFoundException;
import mk.codeit.earthquake.domain.exception.UsgsApiException;
import mk.codeit.earthquake.domain.model.Earthquake;
import mk.codeit.earthquake.repository.EarthquakeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EarthquakeServiceIntegrationTest {

    @Autowired
    private EarthquakeService earthquakeService;

    @Autowired
    private EarthquakeRepository earthquakeRepository;

    @MockBean
    private UsgsApiService usgsApiService;

    private Earthquake earthquake1;
    private Earthquake earthquake2;
    private Earthquake earthquake3;

    @BeforeEach
    void setUp() {
        earthquakeRepository.deleteAll();

        earthquake1 = earthquakeRepository.save(Earthquake.builder()
                .usgsId("us001")
                .magnitude(3.5)
                .magType("ml")
                .place("10 km NE of Springfield")
                .title("M 3.5 - 10 km NE of Springfield")
                .time(Instant.now().minusSeconds(3600))
                .latitude(37.5)
                .longitude(-122.5)
                .depth(10.0)
                .build());

        earthquake2 = earthquakeRepository.save(Earthquake.builder()
                .usgsId("us002")
                .magnitude(5.1)
                .magType("mb")
                .place("30 km SW of Los Angeles")
                .title("M 5.1 - 30 km SW of Los Angeles")
                .time(Instant.now().minusSeconds(1800))
                .latitude(34.0)
                .longitude(-118.5)
                .depth(15.0)
                .build());

        earthquake3 = earthquakeRepository.save(Earthquake.builder()
                .usgsId("us003")
                .magnitude(1.8)
                .magType("md")
                .place("5 km N of Oakland")
                .title("M 1.8 - 5 km N of Oakland")
                .time(Instant.now().minusSeconds(900))
                .latitude(37.8)
                .longitude(-122.2)
                .depth(5.0)
                .build());
    }

    @Test
    @DisplayName("getAllEarthquakes - should return all stored earthquakes ordered by time desc")
    void getAllEarthquakes_shouldReturnAllOrderedByTimeDesc() {
        List<EarthquakeDTO> result = earthquakeService.getAllEarthquakes();

        assertThat(result).hasSize(3);
        // Most recent first
        assertThat(result.get(0).usgsId()).isEqualTo("us003");
        assertThat(result.get(1).usgsId()).isEqualTo("us002");
        assertThat(result.get(2).usgsId()).isEqualTo("us001");
    }

    @Test
    @DisplayName("getById - should return earthquake when it exists")
    void getById_shouldReturnEarthquake() {
        EarthquakeDTO result = earthquakeService.getById(earthquake1.getId());

        assertThat(result).isNotNull();
        assertThat(result.usgsId()).isEqualTo("us001");
        assertThat(result.magnitude()).isEqualTo(3.5);
        assertThat(result.place()).isEqualTo("10 km NE of Springfield");
    }

    @Test
    @DisplayName("getById - should throw EarthquakeNotFoundException for unknown id")
    void getById_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> earthquakeService.getById(9999L))
                .isInstanceOf(EarthquakeNotFoundException.class)
                .hasMessageContaining("9999");
    }

    @Test
    @DisplayName("getByMinMagnitude - should return only earthquakes above threshold")
    void getByMinMagnitude_shouldFilterCorrectly() {
        List<EarthquakeDTO> result = earthquakeService.getByMinMagnitude(3.0);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(EarthquakeDTO::magnitude)
                .allSatisfy(mag -> assertThat(mag).isGreaterThan(3.0));
    }

    @Test
    @DisplayName("getByMinMagnitude - should throw for negative magnitude")
    void getByMinMagnitude_shouldThrowForNegativeMagnitude() {
        assertThatThrownBy(() -> earthquakeService.getByMinMagnitude(-1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    @DisplayName("getAfterTime - should return only earthquakes after specified time")
    void getAfterTime_shouldFilterByTime() {
        long twoHoursAgo = Instant.now().minusSeconds(7200).toEpochMilli();
        List<EarthquakeDTO> result = earthquakeService.getAfterTime(twoHoursAgo);

        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("getAfterTime - should return empty list when no earthquakes match")
    void getAfterTime_shouldReturnEmptyWhenNoMatch() {
        long futureTime = Instant.now().plusSeconds(3600).toEpochMilli();
        List<EarthquakeDTO> result = earthquakeService.getAfterTime(futureTime);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getAfterTime - should throw for negative timestamp")
    void getAfterTime_shouldThrowForNegativeTimestamp() {
        assertThatThrownBy(() -> earthquakeService.getAfterTime(-1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deleteById - should delete existing earthquake")
    void deleteById_shouldDeleteEarthquake() {
        Long id = earthquake1.getId();
        earthquakeService.deleteById(id);

        assertThat(earthquakeRepository.existsById(id)).isFalse();
        assertThat(earthquakeRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("deleteById - should throw EarthquakeNotFoundException for unknown id")
    void deleteById_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> earthquakeService.deleteById(9999L))
                .isInstanceOf(EarthquakeNotFoundException.class);
    }

    @Test
    @DisplayName("fetchAndStore - should delete existing and save filtered earthquakes")
    void fetchAndStore_shouldClearAndSaveFiltered() {
        List<Earthquake> mockEarthquakes = List.of(
                buildMockEarthquake("usgs-new-1", 4.2),
                buildMockEarthquake("usgs-new-2", 1.5), // below 2.0 - should be filtered out
                buildMockEarthquake("usgs-new-3", 6.0)
        );
        when(usgsApiService.fetchEarthquakes()).thenReturn(mockEarthquakes);

        List<EarthquakeDTO> result = earthquakeService.fetchAndStore();

        // Only magnitudes > 2.0 should be saved (4.2 and 6.0)
        assertThat(result).hasSize(2);
        assertThat(result).extracting(EarthquakeDTO::magnitude)
                .containsExactlyInAnyOrder(4.2, 6.0);

        // Old data should be gone
        List<EarthquakeDTO> allNow = earthquakeService.getAllEarthquakes();
        assertThat(allNow).hasSize(2);
    }

    @Test
    @DisplayName("fetchAndStore - should propagate UsgsApiException on API failure")
    void fetchAndStore_shouldPropagateUsgsException() {
        when(usgsApiService.fetchEarthquakes())
                .thenThrow(new UsgsApiException("API unavailable"));

        assertThatThrownBy(() -> earthquakeService.fetchAndStore())
                .isInstanceOf(UsgsApiException.class)
                .hasMessageContaining("unavailable");
    }

    private Earthquake buildMockEarthquake(String usgsId, double magnitude) {
        return Earthquake.builder()
                .usgsId(usgsId)
                .magnitude(magnitude)
                .magType("ml")
                .place("Test place")
                .title("M " + magnitude + " - Test place")
                .time(Instant.now())
                .latitude(0.0)
                .longitude(0.0)
                .depth(10.0)
                .build();
    }
}
