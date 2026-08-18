package com.mapvendor.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import org.junit.jupiter.api.Test;

class BaselineMigrationContractTest {
    @Test
    void immutableBaselineContainsHistoricalTablesAndOrderIndexes() throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get("src/main/resources/db/migration/V001__baseline.sql"));
        String sql = new String(bytes, StandardCharsets.UTF_8);
        String[] tables = {"sys_admin", "merchant_account", "wx_user", "university", "business",
                "business_travel_car", "business_food_dish", "business_hotel_room", "file_resource",
                "reserve_order", "order_status_log", "api_idempotency"};
        for (String table : tables) {
            assertThat(sql).contains("CREATE TABLE " + table + " (");
        }
        assertThat(sql).contains("idx_reserve_order_openid_created")
                .contains("idx_reserve_order_service_status_type")
                .contains("idx_reserve_order_business_service")
                .contains("status IN ('PENDING', 'CONFIRMED', 'CANCELLED')");
        assertThat(toHex(MessageDigest.getInstance("SHA-256").digest(bytes)))
                .isEqualTo("3ad2b104b7ffccffb64ac53a8f3a9729b212a051b28f886ee65ca3e992e07ab4");
    }

    @Test
    void v002RemovesAccountTablesAndNarrowsRoleValuesInSafeOrder() throws Exception {
        String sql = new String(Files.readAllBytes(Paths.get(
                "src/main/resources/db/migration/V002__remove_auth_and_merchant_role_legacy.sql")),
                StandardCharsets.UTF_8);
        assertThat(sql).contains("WHERE cancel_source = 'MERCHANT'")
                .contains("WHERE operator_type = 'MERCHANT'")
                .contains("[LEGACY_MERCHANT]")
                .contains("cancel_source IN ('USER', 'ADMIN')")
                .contains("operator_type IN ('USER', 'ADMIN', 'SYSTEM')")
                .contains("DROP TABLE merchant_account")
                .contains("DROP TABLE sys_admin");
        assertThat(sql.indexOf("UPDATE reserve_order"))
                .isLessThan(sql.indexOf("ADD CONSTRAINT ck_reserve_order_cancel_fields"));
        assertThat(sql.indexOf("UPDATE order_status_log"))
                .isLessThan(sql.indexOf("ADD CONSTRAINT ck_order_status_log_operator"));
        assertThat(sql.indexOf("ADD CONSTRAINT ck_order_status_log_operator"))
                .isLessThan(sql.indexOf("DROP TABLE merchant_account"));
    }

    private static String toHex(byte[] bytes) {
        StringBuilder value = new StringBuilder();
        for (byte item : bytes) {
            value.append(String.format("%02x", item & 0xff));
        }
        return value.toString();
    }
}
