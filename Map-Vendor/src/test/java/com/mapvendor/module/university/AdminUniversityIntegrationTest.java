package com.mapvendor.module.university;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mapvendor.MapVendorApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = MapVendorApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:universityadmin;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false"
})
@AutoConfigureMockMvc
@Sql(scripts = {"classpath:university-admin-schema.sql", "classpath:university-admin-data.sql"})
class AdminUniversityIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsWithStableOrderFiltersAndSortedActiveImages() throws Exception {
        mockMvc.perform(get("/api/v1/admin/universities").param("page", "1").param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.items[0].id").value(11));

        mockMvc.perform(get("/api/v1/admin/universities/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.images.length()").value(2))
                .andExpect(jsonPath("$.data.images[0].resourceId").value(101))
                .andExpect(jsonPath("$.data.images[1].resourceId").value(100));

        mockMvc.perform(get("/api/v1/admin/universities").param("keyword", "Beta")
                        .param("status", "DISABLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(11));
    }

    @Test
    void createsUpdatesChangesStatusAndLogicallyDeletes() throws Exception {
        String create = "{\"name\":\"New University\",\"intro\":\" new intro \","
                + "\"polygonPoints\":[{\"latitude\":31.0,\"longitude\":121.0},"
                + "{\"latitude\":31.1,\"longitude\":121.1},{\"latitude\":31.2,\"longitude\":121.2}],"
                + "\"imageResourceIds\":[101,100]}";
        String response = mockMvc.perform(post("/api/v1/admin/universities")
                        .header("X-Request-Id", "university-create")
                        .contentType(MediaType.APPLICATION_JSON).content(create))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ENABLED"))
                .andExpect(jsonPath("$.data.images[0].resourceId").value(101))
                .andReturn().getResponse().getContentAsString();
        long id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).path("data").path("id").asLong();

        String update = create.replace("New University", "Renamed University")
                .replace("[101,100]", "[100]");
        mockMvc.perform(put("/api/v1/admin/universities/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Renamed University"))
                .andExpect(jsonPath("$.data.images.length()").value(1));

        mockMvc.perform(patch("/api/v1/admin/universities/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("DISABLED"));

        mockMvc.perform(delete("/api/v1/admin/universities/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
        mockMvc.perform(get("/api/v1/admin/universities/{id}", id))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("UNIVERSITY_NOT_FOUND"));
    }

    @Test
    void rejectsInvalidPolygonsAndImageResources() throws Exception {
        String duplicatePoints = "{\"name\":\"Bad\",\"polygonPoints\":["
                + "{\"latitude\":31,\"longitude\":121},{\"latitude\":31,\"longitude\":121},"
                + "{\"latitude\":31,\"longitude\":121}]}";
        mockMvc.perform(post("/api/v1/admin/universities").contentType(MediaType.APPLICATION_JSON)
                        .content(duplicatePoints))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        String invalidRange = duplicatePoints.replace("31,\"longitude\":121", "91,\"longitude\":121");
        mockMvc.perform(post("/api/v1/admin/universities").contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRange))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        String invalidImage = "{\"name\":\"Bad image\",\"polygonPoints\":["
                + "{\"latitude\":30,\"longitude\":120},{\"latitude\":31,\"longitude\":121},"
                + "{\"latitude\":32,\"longitude\":122}],\"imageResourceIds\":[102]}";
        mockMvc.perform(post("/api/v1/admin/universities").contentType(MediaType.APPLICATION_JSON)
                        .content(invalidImage))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void validatesPaginationAndPublishesAdminOpenApi() throws Exception {
        mockMvc.perform(get("/api/v1/admin/universities").param("pageSize", "101"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/v3/api-docs/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/admin/universities'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/universities'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/universities/{id}'].put").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/universities/{id}'].delete").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/universities/{id}/status'].patch").exists());
    }
}
