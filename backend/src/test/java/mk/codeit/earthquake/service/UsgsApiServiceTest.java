package mk.codeit.earthquake.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.codeit.earthquake.config.JacksonConfig;
import mk.codeit.earthquake.domain.exception.UsgsApiException;
import mk.codeit.earthquake.domain.model.Earthquake;
import mk.codeit.earthquake.service.impl.UsgsApiServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsgsApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper = new JacksonConfig().objectMapper();

    @InjectMocks
    private UsgsApiServiceImpl usgsApiService;

    @BeforeEach
    void setUp() throws Exception {
        var urlField = UsgsApiServiceImpl.class.getDeclaredField("usgsApiUrl");
        urlField.setAccessible(true);
        urlField.set(usgsApiService, "https://earthquake.usgs.gov/test");
    }

    private static final String VALID_GEOJSON = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "id": "us2024abc1",
                  "type": "Feature",
                  "properties": {
                    "mag": 3.5,
                    "magType": "ml",
                    "place": "10 km NE of Springfield",
                    "title": "M 3.5 - 10 km NE of Springfield",
                    "time": 1700000000000
                  },
                  "geometry": {
                    "type": "Point",
                    "coordinates": [-122.5, 37.5, 10.0]
                  }
                },
                {
                  "id": "us2024abc2",
                  "type": "Feature",
                  "properties": {
                    "mag": 5.1,
                    "magType": "mb",
                    "place": "30 km SW of Los Angeles",
                    "title": "M 5.1 - 30 km SW of Los Angeles",
                    "time": 1700003600000
                  },
                  "geometry": {
                    "type": "Point",
                    "coordinates": [-118.5, 34.0, 15.0]
                  }
                }
              ]
            }
            """;

    private static final String GEOJSON_WITH_NULL_MAG = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "id": "us2024null",
                  "type": "Feature",
                  "properties": {
                    "mag": null,
                    "place": "Somewhere",
                    "time": 1700000000000
                  },
                  "geometry": { "type": "Point", "coordinates": [-100.0, 40.0, 5.0] }
                }
              ]
            }
            """;

    private static final String GEOJSON_MISSING_FEATURES = """
            {
              "type": "FeatureCollection"
            }
            """;

    @Test
    @DisplayName("parseGeoJson - should parse valid GeoJSON into earthquake list")
    void parseGeoJson_shouldParseValidGeoJson() {
        List<Earthquake> result = usgsApiService.parseGeoJson(VALID_GEOJSON);

        assertThat(result).hasSize(2);

        Earthquake first = result.get(0);
        assertThat(first.getUsgsId()).isEqualTo("us2024abc1");
        assertThat(first.getMagnitude()).isEqualTo(3.5);
        assertThat(first.getMagType()).isEqualTo("ml");
        assertThat(first.getPlace()).isEqualTo("10 km NE of Springfield");
        assertThat(first.getLatitude()).isEqualTo(37.5);
        assertThat(first.getLongitude()).isEqualTo(-122.5);
        assertThat(first.getDepth()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("parseGeoJson - should skip features with null magnitude")
    void parseGeoJson_shouldSkipNullMagnitude() {
        List<Earthquake> result = usgsApiService.parseGeoJson(GEOJSON_WITH_NULL_MAG);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("parseGeoJson - should throw UsgsApiException when features array is missing")
    void parseGeoJson_shouldThrowWhenFeaturesArrayMissing() {
        assertThatThrownBy(() -> usgsApiService.parseGeoJson(GEOJSON_MISSING_FEATURES))
                .isInstanceOf(UsgsApiException.class)
                .hasMessageContaining("features");
    }

    @Test
    @DisplayName("parseGeoJson - should throw UsgsApiException for malformed JSON")
    void parseGeoJson_shouldThrowForMalformedJson() {
        assertThatThrownBy(() -> usgsApiService.parseGeoJson("not valid json {{"))
                .isInstanceOf(UsgsApiException.class);
    }

    @Test
    @DisplayName("fetchRawGeoJson - should throw UsgsApiException when API returns null")
    void fetchRawGeoJson_shouldThrowWhenApiReturnsNull() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(null);

        assertThatThrownBy(() -> usgsApiService.fetchRawGeoJson())
                .isInstanceOf(UsgsApiException.class)
                .hasMessageContaining("empty response");
    }

    @Test
    @DisplayName("fetchRawGeoJson - should throw UsgsApiException when RestTemplate throws")
    void fetchRawGeoJson_shouldThrowOnRestClientException() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

        assertThatThrownBy(() -> usgsApiService.fetchRawGeoJson())
                .isInstanceOf(UsgsApiException.class)
                .hasMessageContaining("Failed to connect");
    }

    @Test
    @DisplayName("fetchEarthquakes - should fetch and parse successfully")
    void fetchEarthquakes_shouldFetchAndParse() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(VALID_GEOJSON);

        List<Earthquake> result = usgsApiService.fetchEarthquakes();

        assertThat(result).hasSize(2);
        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }
}
