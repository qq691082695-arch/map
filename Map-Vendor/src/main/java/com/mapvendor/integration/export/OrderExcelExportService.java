package com.mapvendor.integration.export;

import com.mapvendor.common.error.BusinessException;
import com.mapvendor.module.order.domain.BusinessType;
import com.mapvendor.module.order.domain.OrderStatus;
import com.mapvendor.module.order.repository.AdminOrderMapper;
import com.mapvendor.module.order.repository.AdminOrderRow;
import com.mapvendor.module.order.service.AdminOrderQueryService;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderExcelExportService {
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");
    private static final String[] HEADERS = {"订单号", "服务日期", "状态", "服务类型", "服务商ID", "服务商名称",
            "联系人", "联系电话", "人数", "车辆规格", "车辆数量", "服务方式", "房型规格", "房间数量",
            "用餐时段", "取消来源", "取消原因", "创建时间", "确认时间", "取消时间"};

    private final AdminOrderMapper mapper;
    private final AdminOrderQueryService queryService;
    private final OrderExportProperties properties;

    public OrderExcelExportService(AdminOrderMapper mapper, AdminOrderQueryService queryService,
            OrderExportProperties properties) {
        this.mapper = mapper;
        this.queryService = queryService;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public long validateAndCount(LocalDate from, LocalDate to, OrderStatus status, BusinessType type, Long businessId) {
        queryService.validateFilter(from, to);
        validateRange(from, to);
        String statusValue = status == null ? null : status.name();
        String typeValue = type == null ? null : type.name();
        long total = mapper.count(from, to, statusValue, typeValue, businessId);
        if (total > properties.getMaxRows()) {
            audit("REJECTED_ROW_LIMIT", from, to, statusValue, typeValue, businessId, total);
            throw new BusinessException("EXPORT_ROW_LIMIT_EXCEEDED",
                    "导出结果超过最大行数 " + properties.getMaxRows() + "，请缩小筛选范围", HttpStatus.BAD_REQUEST);
        }
        return total;
    }

    @Transactional(readOnly = true)
    public void export(LocalDate from, LocalDate to, OrderStatus status, BusinessType type, Long businessId,
            long total, OutputStream output) throws IOException {
        String statusValue = status == null ? null : status.name();
        String typeValue = type == null ? null : type.name();
        audit("STARTED", from, to, statusValue, typeValue, businessId, total);
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        try {
            Sheet sheet = workbook.createSheet("订单");
            writeHeader(workbook, sheet);
            int rowIndex = 1;
            long offset = 0;
            while (offset < total) {
                List<AdminOrderRow> rows = mapper.selectPage(from, to, statusValue, typeValue, businessId,
                        offset, Math.max(1, Math.min(properties.getFetchSize(), properties.getMaxRows())));
                if (rows.isEmpty()) break;
                for (AdminOrderRow order : rows) writeRow(sheet.createRow(rowIndex++), order);
                offset += rows.size();
            }
            setColumnWidths(sheet);
            workbook.write(output);
            audit("SUCCEEDED", from, to, statusValue, typeValue, businessId, rowIndex - 1L);
        } catch (IOException ex) {
            audit("FAILED", from, to, statusValue, typeValue, businessId, total);
            throw ex;
        } finally {
            workbook.dispose();
            workbook.close();
        }
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (properties.getMaxRows() < 1 || properties.getMaxDateRangeDays() < 1) {
            throw new IllegalStateException("Export limits must be positive");
        }
        if (from == null || to == null) {
            throw new BusinessException("EXPORT_DATE_RANGE_REQUIRED", "导出必须填写服务日期起止值", HttpStatus.BAD_REQUEST);
        }
        long days = ChronoUnit.DAYS.between(from, to) + 1L;
        if (days > properties.getMaxDateRangeDays()) {
            throw new BusinessException("EXPORT_DATE_RANGE_EXCEEDED",
                    "导出服务日期范围不能超过 " + properties.getMaxDateRangeDays() + " 天", HttpStatus.BAD_REQUEST);
        }
    }

    private void writeHeader(SXSSFWorkbook workbook, Sheet sheet) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        Row row = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(style);
        }
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, HEADERS.length - 1));
    }

    private void writeRow(Row row, AdminOrderRow o) {
        int c = 0;
        text(row, c++, o.getOrderNo()); text(row, c++, value(o.getServiceDate())); text(row, c++, o.getStatus());
        text(row, c++, o.getServiceType()); number(row, c++, o.getBusinessId()); text(row, c++, o.getBusinessNameSnapshot());
        text(row, c++, o.getContactName()); text(row, c++, o.getContactPhone()); number(row, c++, o.getPeopleNum());
        text(row, c++, o.getCarSpecSnapshot()); number(row, c++, o.getCarQuantity()); text(row, c++, o.getServiceMode());
        text(row, c++, o.getRoomSpecSnapshot()); number(row, c++, o.getRoomQuantity()); text(row, c++, o.getMealPeriod());
        text(row, c++, o.getCancelSource()); text(row, c++, o.getCancelReason()); text(row, c++, value(o.getCreatedAt()));
        text(row, c++, value(o.getConfirmedAt())); text(row, c, value(o.getCancelledAt()));
    }

    private void text(Row row, int column, String value) { row.createCell(column).setCellValue(value == null ? "" : value); }
    private void number(Row row, int column, Number value) {
        if (value == null) row.createCell(column).setBlank(); else row.createCell(column).setCellValue(value.doubleValue());
    }
    private String value(Object value) { return value == null ? "" : value.toString(); }
    private void setColumnWidths(Sheet sheet) {
        int[] widths = {22,12,12,12,12,24,14,16,8,24,10,12,24,10,12,12,30,20,20,20};
        for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i] * 256);
    }
    private void audit(String result, LocalDate from, LocalDate to, String status, String type, Long businessId, long rows) {
        AUDIT_LOG.info("admin_order_export result={} requestId={} serviceDateFrom={} serviceDateTo={} status={} type={} businessId={} rows={}",
                result, MDC.get("requestId"), from, to, status, type, businessId, rows);
    }
}
