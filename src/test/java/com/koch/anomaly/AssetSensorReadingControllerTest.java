package com.koch.anomaly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssetSensorReadingController.class)
class AssetSensorReadingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssetSensorReadingRepository repository;

    @MockBean
    private KilnSensorProducer producer;

    @Test
    @DisplayName("GET /api/sensors/readings should return a paginated list of sensor readings")
    void getReadings_ShouldReturnPaginatedList() throws Exception {
        // Arrange
        var timestamp = LocalDateTime.now();
        var entity = new AssetSensorReadingEntity();
        entity.setReadingId(1);
        entity.setAssetId("KILN-01");
        entity.setSensorType("TEMPERATURE");
        entity.setReadingValue(105.5);
        entity.setUom("C");
        entity.setTimestamp(timestamp);
        entity.setStatus("NORMAL");

        var page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        
        // Ensure our repository mock supports both Specification and Pageable
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/sensors/readings")
                .param("page", "0")
                .param("size", "10")
                .param("assetId", "KILN-01")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].assetId").value("KILN-01"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("GET /api/sensors/readings with timestamp filter should return filtered results")
    void getReadings_WithTimestampFilter_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        var targetTimestamp = LocalDateTime.of(2026, 3, 24, 12, 0);
        var entity = new AssetSensorReadingEntity();
        entity.setReadingId(2);
        entity.setAssetId("KILN-02");
        entity.setSensorType("TEMPERATURE");
        entity.setReadingValue(110.0);
        entity.setUom("C");
        entity.setTimestamp(targetTimestamp);
        entity.setStatus("NORMAL");

        var page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        // The ISO-8601 string for the target timestamp
        String isoTimestamp = "2026-03-24T12:00:00";

        mockMvc.perform(get("/api/sensors/readings")
                .param("timestamp", isoTimestamp)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].timestamp").value(isoTimestamp))
                .andExpect(jsonPath("$.content[0].assetId").value("KILN-02"));
    }

    @Test
    @DisplayName("GET /api/sensors/readings with range filter for readingValue should return filtered results")
    void getReadings_WithRangeFilter_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        var entity = new AssetSensorReadingEntity();
        entity.setReadingId(3);
        entity.setAssetId("KILN-03");
        entity.setSensorType("TEMPERATURE");
        entity.setReadingValue(125.0);
        entity.setUom("C");
        entity.setTimestamp(LocalDateTime.now());
        entity.setStatus("WARNING");

        var page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/sensors/readings")
                .param("minReadingValue", "120")
                .param("maxReadingValue", "140")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].readingValue").value(125.0))
                .andExpect(jsonPath("$.content[0].assetId").value("KILN-03"));
    }

    @Test
    @DisplayName("GET /api/sensors/readings with timestamp range filter should return filtered results")
    void getReadings_WithTimestampRangeFilter_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        var startTime = LocalDateTime.of(2026, 3, 24, 0, 0);
        var endTime = LocalDateTime.of(2026, 3, 24, 23, 59);
        var entity = new AssetSensorReadingEntity();
        entity.setReadingId(4);
        entity.setAssetId("KILN-04");
        entity.setSensorType("TEMPERATURE");
        entity.setReadingValue(115.0);
        entity.setUom("C");
        entity.setTimestamp(LocalDateTime.of(2026, 3, 24, 15, 0));
        entity.setStatus("NORMAL");

        var page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/sensors/readings")
                .param("startTime", startTime.toString())
                .param("endTime", endTime.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].assetId").value("KILN-04"));
    }

    @Test
    @DisplayName("POST /api/sensors/readings should publish a reading and return 201 Created")
    void postReading_ShouldPublishAndReturnCreated() throws Exception {
        // Arrange
        var timestamp = LocalDateTime.now();
        String jsonPayload = """
                {
                    "readingId": 1,
                    "assetId": "KILN-01",
                    "sensorType": "TEMPERATURE",
                    "readingValue": 105.5,
                    "uom": "C",
                    "timestamp": "%s",
                    "status": "NORMAL"
                }
                """.formatted(timestamp.toString());

        // Act & Assert
        mockMvc.perform(post("/api/sensors/readings")
                .content(jsonPayload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(producer).publishReading(any(AssetSensorReading.class));
    }
}
