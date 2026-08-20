package com.mapvendor.module.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mapvendor.MapVendorApplication;
import com.mapvendor.common.error.BusinessException;
import com.mapvendor.module.order.service.AppOrderQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = MapVendorApplication.class, properties = {
 "spring.datasource.url=jdbc:h2:mem:apporderquery;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
 "spring.datasource.username=sa", "spring.datasource.password=", "spring.flyway.enabled=false"})
@AutoConfigureMockMvc
@Sql(scripts = {"classpath:admin-order-schema.sql", "classpath:admin-order-data.sql"})
class AppOrderQueryIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired AppOrderQueryService service;

    @Test void listsOnlyOwnedOrdersWithStablePaginationAndMaskedPhone() throws Exception {
        mvc.perform(get("/api/v1/app/orders").param("openid", "openid-travel-12345678")
                .param("page", "1").param("pageSize", "20"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.items[0].id").value(101))
            .andExpect(jsonPath("$.data.items[0].contactPhone").value("138****5678"));
        mvc.perform(get("/api/v1/app/orders").param("openid", "unknown-openid"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(0));
    }

    @Test void detailRequiresMatchingOpenidAndDoesNotLeakExistence() throws Exception {
        mvc.perform(get("/api/v1/app/orders/101").param("openid", "openid-travel-12345678"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.orderNo").value("MV101"));
        mvc.perform(get("/api/v1/app/orders/101").param("openid", "other-openid"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }

    @Test void userCancelsOwnedPendingOrderAndWritesUserLog() throws Exception {
        mvc.perform(post("/api/v1/app/orders/101/cancel").header("X-Request-Id", "req-user-cancel")
                .contentType("application/json").content("{\"openid\":\"openid-travel-12345678\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("CANCELLED"))
            .andExpect(jsonPath("$.data.cancelSource").value("USER"))
            .andExpect(jsonPath("$.data.cancelReason").doesNotExist());
        assertThat(jdbc.queryForObject("select version from reserve_order where id=101", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select operator_type from order_status_log where order_id=101", String.class)).isEqualTo("USER");
        assertThat(jdbc.queryForObject("select request_id from order_status_log where order_id=101", String.class)).isEqualTo("req-user-cancel");
    }

    @Test void rejectsWrongOwnerConfirmedAndRepeatedCancellation() throws Exception {
        mvc.perform(post("/api/v1/app/orders/101/cancel").contentType("application/json")
                .content("{\"openid\":\"other-openid\"}"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
        mvc.perform(post("/api/v1/app/orders/102/cancel").contentType("application/json")
                .content("{\"openid\":\"openid-hotel-12345678\"}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ORDER_STATUS_CONFLICT"));
        mvc.perform(post("/api/v1/app/orders/103/cancel").contentType("application/json")
                .content("{\"openid\":\"openid-food-12345678\"}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ORDER_STATUS_CONFLICT"));
    }

    @Test void concurrentUserCancellationAllowsExactlyOneSuccessAndOneLog() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
        for (int i = 0; i < 2; i++) futures.add(pool.submit(() -> {
            ready.countDown(); start.await();
            try { service.cancel(101, "openid-travel-12345678"); return true; }
            catch (BusinessException ex) { assertThat(ex.getCode()).isEqualTo("ORDER_STATUS_CONFLICT"); return false; }
        }));
        ready.await(); start.countDown();
        int successes = 0; for (Future<Boolean> result : futures) if (result.get()) successes++;
        pool.shutdownNow();
        assertThat(successes).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from order_status_log where order_id=101", Integer.class)).isEqualTo(1);
    }
}
