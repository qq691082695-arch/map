package com.mapvendor.module.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mapvendor.MapVendorApplication;
import com.mapvendor.common.error.BusinessException;
import com.mapvendor.module.order.service.AdminOrderCommandService;
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
 "spring.datasource.url=jdbc:h2:mem:adminorderstate;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
 "spring.datasource.username=sa", "spring.datasource.password=", "spring.flyway.enabled=false"})
@AutoConfigureMockMvc
@Sql(scripts = {"classpath:admin-order-schema.sql", "classpath:admin-order-data.sql"})
class AdminOrderStateMachineIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired AdminOrderCommandService service;

    @Test void confirmsPendingOrderAndWritesAdminLogInSameCommand() throws Exception {
        mvc.perform(post("/api/v1/admin/orders/101/confirm").header("X-Request-Id", "req-confirm-101"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
            .andExpect(jsonPath("$.data.confirmedAt").isNotEmpty());

        assertThat(jdbc.queryForObject("select version from reserve_order where id=101", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select operator_type from order_status_log where order_id=101", String.class)).isEqualTo("ADMIN");
        assertThat(jdbc.queryForObject("select request_id from order_status_log where order_id=101", String.class)).isEqualTo("req-confirm-101");
        assertThat(jdbc.queryForObject("select reason from order_status_log where order_id=101", String.class)).isNull();
    }

    @Test void cancelsPendingOrderWithTrimmedReasonAndAdminSource() throws Exception {
        mvc.perform(post("/api/v1/admin/orders/104/cancel").header("X-Request-Id", "req-cancel-104")
                .contentType("application/json").content("{\"reason\":\"  无法接待  \"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("CANCELLED"))
            .andExpect(jsonPath("$.data.cancelSource").value("ADMIN"))
            .andExpect(jsonPath("$.data.cancelReason").value("无法接待"));

        assertThat(jdbc.queryForObject("select reason from order_status_log where order_id=104", String.class)).isEqualTo("无法接待");
        assertThat(jdbc.queryForObject("select request_id from order_status_log where order_id=104", String.class)).isEqualTo("req-cancel-104");
    }

    @Test void rejectsMissingReasonNotFoundAndTerminalOrders() throws Exception {
        mvc.perform(post("/api/v1/admin/orders/101/cancel").contentType("application/json").content("{\"reason\":\"   \"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mvc.perform(post("/api/v1/admin/orders/999/confirm"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
        mvc.perform(post("/api/v1/admin/orders/102/confirm"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ORDER_STATUS_CONFLICT"));
        mvc.perform(post("/api/v1/admin/orders/103/cancel").contentType("application/json").content("{\"reason\":\"再次取消\"}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ORDER_STATUS_CONFLICT"));
    }

    @Test void concurrentConfirmAndCancelAllowExactlyOneSuccess() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
        futures.add(pool.submit(() -> runAfterLatch(ready, start, () -> service.confirm(101))));
        futures.add(pool.submit(() -> runAfterLatch(ready, start, () -> service.cancel(101, "并发取消"))));
        ready.await();
        start.countDown();
        int successes = 0;
        for (Future<Boolean> future : futures) if (future.get()) successes++;
        pool.shutdownNow();

        assertThat(successes).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from order_status_log where order_id=101", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select version from reserve_order where id=101", Integer.class)).isEqualTo(1);
    }

    private boolean runAfterLatch(CountDownLatch ready, CountDownLatch start, Command command) throws Exception {
        ready.countDown();
        start.await();
        try {
            command.run();
            return true;
        } catch (BusinessException ex) {
            assertThat(ex.getCode()).isEqualTo("ORDER_STATUS_CONFLICT");
            return false;
        }
    }

    private interface Command { void run(); }
}
