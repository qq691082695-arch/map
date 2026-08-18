-- Remove the cancelled backend login model and merchant role legacy.
-- MySQL DDL implicitly commits. Run deploy/mysql/precheck_v002.sql and take a backup first.

ALTER TABLE reserve_order DROP CHECK ck_reserve_order_cancel_fields;
ALTER TABLE order_status_log DROP CHECK ck_order_status_log_operator;

UPDATE reserve_order
SET cancel_source = 'ADMIN',
    cancel_reason = CASE
        WHEN cancel_reason IS NULL OR CHAR_LENGTH(TRIM(cancel_reason)) = 0
            THEN '[LEGACY_MERCHANT] 历史商家角色取消'
        ELSE CONCAT('[LEGACY_MERCHANT] ', LEFT(cancel_reason,
            500 - CHAR_LENGTH('[LEGACY_MERCHANT] ')))
    END
WHERE cancel_source = 'MERCHANT';

UPDATE order_status_log
SET operator_type = 'ADMIN',
    reason = CASE
        WHEN reason IS NULL OR CHAR_LENGTH(TRIM(reason)) = 0
            THEN '[LEGACY_MERCHANT] 历史商家角色操作'
        ELSE CONCAT('[LEGACY_MERCHANT] ', LEFT(reason,
            500 - CHAR_LENGTH('[LEGACY_MERCHANT] ')))
    END
WHERE operator_type = 'MERCHANT';

ALTER TABLE reserve_order
    ADD CONSTRAINT ck_reserve_order_cancel_fields CHECK (
        (status = 'CANCELLED' AND cancelled_at IS NOT NULL AND cancel_source IN ('USER', 'ADMIN')
            AND (cancel_source = 'USER' OR cancel_reason IS NOT NULL))
        OR (status <> 'CANCELLED' AND cancelled_at IS NULL AND cancel_source IS NULL AND cancel_reason IS NULL)
    );

ALTER TABLE order_status_log
    ADD CONSTRAINT ck_order_status_log_operator
        CHECK (operator_type IN ('USER', 'ADMIN', 'SYSTEM'));

DROP TABLE merchant_account;
DROP TABLE sys_admin;
