package com.mapvendor.module.business;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapvendor.MapVendorApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes=MapVendorApplication.class, properties={
 "spring.datasource.url=jdbc:h2:mem:businessadmin;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
 "spring.datasource.username=sa", "spring.datasource.password=", "spring.flyway.enabled=false"})
@AutoConfigureMockMvc
@Sql(scripts={"classpath:business-admin-schema.sql","classpath:business-admin-data.sql"})
class AdminBusinessIntegrationTest {
 @Autowired MockMvc mvc; @Autowired JdbcTemplate jdbc;
 private static final String TRAVEL="{\"name\":\" New Travel \",\"address\":\" New Road \",\"longitude\":121.5,\"latitude\":31.5,\"businessType\":\"TRAVEL\",\"intro\":\" intro \"}";
 @Test void listsFiltersAndExcludesDeleted() throws Exception {
  mvc.perform(get("/api/v1/admin/businesses").param("pageSize","1")).andExpect(status().isOk())
   .andExpect(jsonPath("$.data.total").value(2)).andExpect(jsonPath("$.data.items[0].id").value(11));
  mvc.perform(get("/api/v1/admin/businesses").param("keyword","Road 1").param("type","TRAVEL").param("status","ENABLED"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(1)).andExpect(jsonPath("$.data.items[0].id").value(10));
 }
 @Test void supportsAllTypesCrudStatusAndLogicalDeleteWhilePreservingOrder() throws Exception {
  String response=mvc.perform(post("/api/v1/admin/businesses").header("X-Request-Id","business-create")
   .contentType(MediaType.APPLICATION_JSON).content(TRAVEL)).andExpect(status().isOk())
   .andExpect(jsonPath("$.data.name").value("New Travel")).andExpect(jsonPath("$.data.status").value("ENABLED"))
   .andReturn().getResponse().getContentAsString();
  long id=new ObjectMapper().readTree(response).path("data").path("id").asLong();
  String hotel=TRAVEL.replace("New Travel","New Hotel").replace("TRAVEL","HOTEL");
  mvc.perform(post("/api/v1/admin/businesses").contentType(MediaType.APPLICATION_JSON).content(hotel))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.businessType").value("HOTEL"));
  String food=TRAVEL.replace("New Travel","New Food").replace("TRAVEL","FOOD").replace("}",",\"foodContactName\":\"Chef\",\"foodContactPhone\":\"13800000000\",\"foodRecommendedDishes\":\"Noodles\"}");
  mvc.perform(post("/api/v1/admin/businesses").contentType(MediaType.APPLICATION_JSON).content(food))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.foodContactName").value("Chef"));
  mvc.perform(put("/api/v1/admin/businesses/{id}",id).contentType(MediaType.APPLICATION_JSON).content(TRAVEL.replace("New Travel","Renamed")))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.name").value("Renamed"));
  mvc.perform(patch("/api/v1/admin/businesses/{id}/status",id).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DISABLED\"}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("DISABLED"));
  mvc.perform(delete("/api/v1/admin/businesses/10")).andExpect(status().isOk());
  mvc.perform(get("/api/v1/admin/businesses/10")).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("BUSINESS_NOT_FOUND"));
  org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject("SELECT business_name_snapshot FROM reserve_order WHERE id=1000",String.class)).isEqualTo("Travel Alpha");
 }
 @Test void rejectsInvalidCoordinatesTypeChangesAndCrossTypeFields() throws Exception {
  mvc.perform(post("/api/v1/admin/businesses").contentType(MediaType.APPLICATION_JSON).content(TRAVEL.replace("121.5","181")))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  mvc.perform(put("/api/v1/admin/businesses/10").contentType(MediaType.APPLICATION_JSON).content(TRAVEL.replace("TRAVEL","HOTEL")))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  mvc.perform(post("/api/v1/admin/businesses").contentType(MediaType.APPLICATION_JSON).content(TRAVEL.replace("}",",\"foodContactName\":\"x\"}")))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
 }
 @Test void replacesOrderedBusinessImageRelationsAndRejectsInactiveImages() throws Exception {
  jdbc.update("INSERT INTO file_resource(id,public_url,status) VALUES(?,?,?)",501,"/files/a.png","ACTIVE");
  jdbc.update("INSERT INTO file_resource(id,public_url,status) VALUES(?,?,?)",502,"/files/b.png","ACTIVE");
  String withImages=TRAVEL.replace("}",",\"imageResourceIds\":[502,501]}");
  String response=mvc.perform(post("/api/v1/admin/businesses").contentType(MediaType.APPLICATION_JSON).content(withImages))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.images[0].resourceId").value(502))
   .andExpect(jsonPath("$.data.images[1].url").value("/files/a.png")).andReturn().getResponse().getContentAsString();
  long id=new ObjectMapper().readTree(response).path("data").path("id").asLong();
  org.assertj.core.api.Assertions.assertThat(jdbc.queryForList("SELECT file_resource_id FROM business_file_relation WHERE business_id=? ORDER BY sort_no",Long.class,id))
   .containsExactly(502L,501L);
  jdbc.update("UPDATE file_resource SET status='DISABLED' WHERE id=501");
  mvc.perform(put("/api/v1/admin/businesses/{id}",id).contentType(MediaType.APPLICATION_JSON).content(withImages))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
 }
 @Test void publishesRoutesAndValidatesPagination() throws Exception {
  mvc.perform(get("/api/v1/admin/businesses").param("pageSize","101")).andExpect(status().isBadRequest());
  mvc.perform(get("/v3/api-docs/admin")).andExpect(status().isOk())
   .andExpect(jsonPath("$.paths['/api/v1/admin/businesses'].post").exists())
   .andExpect(jsonPath("$.paths['/api/v1/admin/businesses/{id}'].delete").exists())
   .andExpect(jsonPath("$.paths['/api/v1/admin/businesses/{id}/status'].patch").exists());
 }
}
