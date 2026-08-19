package com.mapvendor.module.statistics;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mapvendor.MapVendorApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes=MapVendorApplication.class, properties={
 "spring.datasource.url=jdbc:h2:mem:adminstatistics;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
 "spring.datasource.username=sa", "spring.datasource.password=", "spring.flyway.enabled=false"})
@AutoConfigureMockMvc
@Sql(scripts={"classpath:admin-order-schema.sql","classpath:admin-order-data.sql"})
class AdminStatisticsIntegrationTest {
    @Autowired MockMvc mvc;

    @Test void totalsThreeStatusesByServiceDateAndKeepsSnapshotGroups() throws Exception {
        mvc.perform(get("/api/v1/admin/statistics/overview")
                .param("serviceDateFrom", "2026-08-20").param("serviceDateTo", "2026-08-23"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total.pendingCount").value(2))
            .andExpect(jsonPath("$.data.total.confirmedCount").value(1))
            .andExpect(jsonPath("$.data.total.cancelledCount").value(1))
            .andExpect(jsonPath("$.data.total.totalCount").value(4))
            .andExpect(jsonPath("$.data.businesses.length()").value(4))
            .andExpect(jsonPath("$.data.businesses[0].businessNameSnapshot").value("Travel Snapshot"));
    }

    @Test void filtersByInclusiveServiceDateTypeAndBusiness() throws Exception {
        mvc.perform(get("/api/v1/admin/statistics/overview")
                .param("serviceDateFrom", "2026-08-22").param("serviceDateTo", "2026-08-23")
                .param("type", "FOOD").param("businessId", "30"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total.pendingCount").value(1))
            .andExpect(jsonPath("$.data.total.cancelledCount").value(1))
            .andExpect(jsonPath("$.data.total.totalCount").value(2))
            .andExpect(jsonPath("$.data.businesses.length()").value(2));
    }

    @Test void returnsZeroForEmptyRangeAndValidatesParametersAndOpenApi() throws Exception {
        mvc.perform(get("/api/v1/admin/statistics/overview")
                .param("serviceDateFrom", "2027-01-01").param("serviceDateTo", "2027-01-31"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.total.totalCount").value(0))
            .andExpect(jsonPath("$.data.businesses.length()").value(0));
        mvc.perform(get("/api/v1/admin/statistics/overview")
                .param("serviceDateFrom", "2026-08-23").param("serviceDateTo", "2026-08-20"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mvc.perform(get("/api/v1/admin/statistics/overview").param("type", "UNKNOWN"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/v3/api-docs/admin")).andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/api/v1/admin/statistics/overview'].get").exists());
    }
}
