package com.mapvendor.module.order;

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
 "spring.datasource.url=jdbc:h2:mem:adminorder;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
 "spring.datasource.username=sa", "spring.datasource.password=", "spring.flyway.enabled=false"})
@AutoConfigureMockMvc
@Sql(scripts={"classpath:admin-order-schema.sql","classpath:admin-order-data.sql"})
class AdminOrderQueryIntegrationTest {
 @Autowired MockMvc mvc;

 @Test void listsWithStablePaginationAndMasksSensitiveIdentifiers() throws Exception {
  mvc.perform(get("/api/v1/admin/orders").param("pageSize","2"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(4))
   .andExpect(jsonPath("$.data.items[0].id").value(104))
   .andExpect(jsonPath("$.data.items[1].id").value(103))
   .andExpect(jsonPath("$.data.items[0].contactPhoneMasked").value("136****4444"))
   .andExpect(jsonPath("$.data.items[0].openidMasked").value("open****efgh"))
   .andExpect(jsonPath("$.data.items[0].contactPhone").doesNotExist())
   .andExpect(jsonPath("$.data.items[0].openid").doesNotExist());
 }

 @Test void filtersByServiceDateStatusTypeAndBusiness() throws Exception {
  mvc.perform(get("/api/v1/admin/orders").param("serviceDateFrom","2026-08-21").param("serviceDateTo","2026-08-22")
    .param("status","CANCELLED").param("type","FOOD").param("businessId","30"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(1))
   .andExpect(jsonPath("$.data.items[0].id").value(103));
  mvc.perform(get("/api/v1/admin/orders").param("serviceDateFrom","2026-08-23").param("serviceDateTo","2026-08-20"))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
 }

 @Test void detailUsesSnapshotsAndShowsCategoryFieldsWithoutFullOpenid() throws Exception {
  mvc.perform(get("/api/v1/admin/orders/101"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.businessNameSnapshot").value("Travel Snapshot"))
   .andExpect(jsonPath("$.data.carSpecSnapshot").value("7-seat snapshot"))
   .andExpect(jsonPath("$.data.serviceMode").value("DAY_CHARTER"))
   .andExpect(jsonPath("$.data.contactPhone").value("13812345678"))
   .andExpect(jsonPath("$.data.openid").doesNotExist());
  mvc.perform(get("/api/v1/admin/orders/999")).andExpect(status().isNotFound())
   .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
 }

 @Test void validatesEnumsPaginationAndPublishesOpenApiRoutes() throws Exception {
  mvc.perform(get("/api/v1/admin/orders").param("pageSize","101")).andExpect(status().isBadRequest());
  mvc.perform(get("/api/v1/admin/orders").param("status","REJECTED")).andExpect(status().isBadRequest());
  mvc.perform(get("/v3/api-docs/admin")).andExpect(status().isOk())
   .andExpect(jsonPath("$.paths['/api/v1/admin/orders'].get").exists())
   .andExpect(jsonPath("$.paths['/api/v1/admin/orders/{id}'].get").exists());
 }
}
