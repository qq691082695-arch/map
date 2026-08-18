package com.mapvendor.module.app.map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mapvendor.MapVendorApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = MapVendorApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:mapoverview;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false"
})
@AutoConfigureMockMvc
@Sql(scripts = {"classpath:map-overview-schema.sql", "classpath:map-overview-data.sql"})
class AppMapOverviewIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsOnlyVisibleDataWithStableImagesAndUnifiedEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/app/map-overview").header("X-Request-Id", "map-test"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "map-test"))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.requestId").value("map-test"))
                .andExpect(jsonPath("$.data.universities.length()").value(1))
                .andExpect(jsonPath("$.data.universities[0].id").value(2))
                .andExpect(jsonPath("$.data.universities[0].polygonPoints.length()").value(3))
                .andExpect(jsonPath("$.data.universities[0].imageUrls[0]").value("/images/first.png"))
                .andExpect(jsonPath("$.data.universities[0].imageUrls[1]").value("/images/second.png"))
                .andExpect(jsonPath("$.data.businesses.length()").value(3))
                .andExpect(jsonPath("$.data.businesses[0].businessType").value("FOOD"))
                .andExpect(jsonPath("$.data.businesses[1].businessType").value("HOTEL"))
                .andExpect(jsonPath("$.data.businesses[2].businessType").value("TRAVEL"))
                .andExpect(jsonPath("$.data.businesses[2].coverImageUrl").value("/images/first.png"));
    }

    @Test
    void filtersBusinessesByTypeWithoutFilteringUniversities() throws Exception {
        mockMvc.perform(get("/api/v1/app/map-overview").param("type", "HOTEL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.universities.length()").value(1))
                .andExpect(jsonPath("$.data.businesses.length()").value(1))
                .andExpect(jsonPath("$.data.businesses[0].id").value(11))
                .andExpect(jsonPath("$.data.businesses[0].coverImageUrl").isEmpty());
    }

    @Test
    void rejectsUnknownBusinessType() throws Exception {
        mockMvc.perform(get("/api/v1/app/map-overview").param("type", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void publishesMapEndpointInGeneratedAppOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs/app"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/app/map-overview'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/app/map-overview'].get.parameters[0].name").value("type"));
    }
}
