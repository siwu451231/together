package com.example.backend.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Configuration
public class DatabaseFixConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void fixDatabaseColumns() {
        // Apply each fix independently to avoid one failure masking the others.
        ensureLongTextColumn("document_operations", "content_snapshot");
        ensureLongTextColumn("documents", "content");

        // op_payload may be JSON in some schemas; if it is not JSON, widen to LONGTEXT.
        String opPayloadType = getColumnType("document_operations", "op_payload");
        if (opPayloadType != null && !"json".equals(opPayloadType)) {
            try {
                jdbcTemplate.execute("ALTER TABLE document_operations MODIFY COLUMN op_payload LONGTEXT NOT NULL");
                System.out.println("[DB-FIX] document_operations.op_payload -> LONGTEXT");
            } catch (Exception e) {
                System.out.println("[DB-FIX] skip alter document_operations.op_payload: " + e.getMessage());
            }
        }
    }

    private void ensureLongTextColumn(String tableName, String columnName) {
        String columnType = getColumnType(tableName, columnName);

        if (columnType == null) {
            try {
                jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " LONGTEXT NOT NULL");
                System.out.println("[DB-FIX] add " + tableName + "." + columnName + " as LONGTEXT");
            } catch (Exception e) {
                System.out.println("[DB-FIX] skip add " + tableName + "." + columnName + ": " + e.getMessage());
            }
            return;
        }

        if (!"longtext".equals(columnType)) {
            try {
                jdbcTemplate.execute("ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " LONGTEXT NOT NULL");
                System.out.println("[DB-FIX] " + tableName + "." + columnName + " -> LONGTEXT");
            } catch (Exception e) {
                System.out.println("[DB-FIX] skip alter " + tableName + "." + columnName + ": " + e.getMessage());
            }
        }
    }

    private String getColumnType(String tableName, String columnName) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    tableName,
                    columnName
            );
            if (rows.isEmpty()) {
                return null;
            }

            Object raw = rows.get(0).get("DATA_TYPE");
            return raw == null ? null : raw.toString().toLowerCase(Locale.ROOT);
        } catch (Exception e) {
            System.out.println("[DB-FIX] lookup failed for " + tableName + "." + columnName + ": " + e.getMessage());
            return null;
        }
    }
}
