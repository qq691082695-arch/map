package com.mapvendor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.mapvendor.module.app.map.query.AppMapQueryMapper;
import com.mapvendor.module.university.repository.UniversityMapper;
import com.mapvendor.module.business.repository.BusinessMapper;
import com.mapvendor.module.business.repository.BusinessResourceMapper;
import com.mapvendor.module.file.repository.FileResourceMapper;
import com.mapvendor.module.order.repository.AdminOrderMapper;
import com.mapvendor.module.statistics.repository.AdminStatisticsMapper;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
                + "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration"
})
@AutoConfigureMockMvc
class MapVendorApplicationTest {
    @MockBean
    private AppMapQueryMapper appMapQueryMapper;

    @MockBean
    private UniversityMapper universityMapper;

    @MockBean
    private BusinessMapper businessMapper;

    @MockBean
    private BusinessResourceMapper businessResourceMapper;

    @MockBean
    private FileResourceMapper fileResourceMapper;

    @MockBean
    private AdminOrderMapper adminOrderMapper;

    @MockBean
    private AdminStatisticsMapper adminStatisticsMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void appPingUsesUnifiedEnvelopeAndRequestId() throws Exception {
        mockMvc.perform(get("/api/v1/app/system/ping").header("X-Request-Id", "test-request"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "test-request"))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.requestId").value("test-request"));
    }

    @Test
    void adminNamespaceDoesNotRequireBackendAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void appOpenApiGroupContainsPingEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs/app"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.version").value("v1"))
                .andExpect(jsonPath("$.paths['/api/v1/app/system/ping']").exists());
    }

    @Test
    void generatedOpenApiHasNoBackendAuthenticationContract() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes").doesNotExist())
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.containsString(
                        "/api/v1/admin/** 必须由 Nginx")));
    }
}
