package com.mapvendor.module.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mapvendor.MapVendorApplication;
import java.io.ByteArrayInputStream;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(classes=MapVendorApplication.class, properties={
 "spring.datasource.url=jdbc:h2:mem:adminorderexport;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
 "spring.datasource.username=sa", "spring.datasource.password=", "spring.flyway.enabled=false",
 "map-vendor.export.max-rows=3", "map-vendor.export.max-date-range-days=31", "map-vendor.export.fetch-size=1"})
@AutoConfigureMockMvc
@Sql(scripts={"classpath:admin-order-schema.sql","classpath:admin-order-data.sql"})
class AdminOrderExportIntegrationTest {
    @Autowired MockMvc mvc;

    @Test void exportsSameFiltersWithStableOrderAndFullPhone() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/admin/orders/export")
                .param("serviceDateFrom", "2026-08-21").param("serviceDateTo", "2026-08-23")
                .param("type", "FOOD").header("X-Request-Id", "export-test-1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("orders-2026-08-21-2026-08-23.xlsx")))
            .andReturn();
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
            Sheet sheet = workbook.getSheet("订单");
            assertEquals(3, sheet.getPhysicalNumberOfRows());
            assertEquals("联系电话", sheet.getRow(0).getCell(7).getStringCellValue());
            assertEquals("MV104", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("13633334444", sheet.getRow(1).getCell(7).getStringCellValue());
            assertEquals("MV103", sheet.getRow(2).getCell(0).getStringCellValue());
        }
    }

    @Test void rejectsMissingOrExcessiveDateRangeAndTooManyRowsBeforeStreaming() throws Exception {
        mvc.perform(get("/api/v1/admin/orders/export"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mvc.perform(get("/api/v1/admin/orders/export")
                .param("serviceDateFrom", "2026-01-01").param("serviceDateTo", "2026-02-01"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("EXPORT_DATE_RANGE_EXCEEDED"));
        mvc.perform(get("/api/v1/admin/orders/export")
                .param("serviceDateFrom", "2026-08-01").param("serviceDateTo", "2026-08-31"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("EXPORT_ROW_LIMIT_EXCEEDED"));
    }

    @Test void publishesExportRouteInGeneratedOpenApi() throws Exception {
        mvc.perform(get("/v3/api-docs/admin")).andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/api/v1/admin/orders/export'].get").exists());
    }
}
