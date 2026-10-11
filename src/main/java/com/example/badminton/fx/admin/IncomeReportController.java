package com.example.badminton.fx.admin;

import com.example.badminton.BadmintonSystem;
import com.example.badminton.fx.DateBox;
import com.example.badminton.fx.FxHelper;
import com.example.badminton.fx.Refreshable;
import com.example.badminton.model.PaymentType;
import com.example.badminton.report.IncomeReport;
import com.example.badminton.service.ReportService;
import com.example.badminton.util.FormatHelper;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller แท็บ "รายงานรายได้" ของผู้ดูแลระบบ (admin/income-report.fxml) เลือกได้ทั้งรายวันและรายเดือน
 *
 * การทำงาน:
 * - รายวัน   -> ReportService.createDailyReport(date)       ได้ DailyIncomeReport
 * - รายเดือน -> ReportService.createMonthlyReport(yearMonth) ได้ MonthlyIncomeReport
 * ทั้งสองแบบเก็บในตัวแปรชนิด IncomeReport (abstract class) ตัวเดียวกัน
 * แล้วเรียก getIncomeByType() / getTotalIncome() ได้เหมือนกัน -> Polymorphism
 *
 * กติกา: นับเฉพาะเงินที่รับจริง (Payment) ตามวันที่รับเงิน การใช้ชั่วโมงแพ็กเกจไม่นับเป็นรายได้ซ้ำ
 */
public class IncomeReportController implements Refreshable {

    /** ข้อมูล 1 แถวของตารางรายงาน (หมวด, ยอดเงิน, สัดส่วน) เป็นข้อความที่จัดรูปแบบแล้ว */
    public record IncomeRow(String typeName, String amount, String percent) {
    }

    private final ReportService reportService;

    @FXML private RadioButton dailyRadio;
    @FXML private RadioButton monthlyRadio;
    @FXML private DateBox dateBox;
    @FXML private ComboBox<String> monthCombo;
    @FXML private Spinner<Integer> yearSpinner;
    @FXML private Label titleLabel;
    @FXML private TableView<IncomeRow> table;
    @FXML private Label totalLabel;
    @FXML private Label noteLabel;

    public IncomeReportController(BadmintonSystem system) {
        this.reportService = system.getReportService();
    }

    @FXML
    private void initialize() {
        for (int month = 1; month <= 12; month++) {
            monthCombo.getItems().add(month + " - " + FormatHelper.thaiMonthName(month));
        }
        monthCombo.getSelectionModel().select(LocalDate.now().getMonthValue() - 1);
        // IntegerSpinnerValueFactory แสดงตัวเลขแบบไม่มีจุลภาค (2026 ไม่ใช่ 2,026) อยู่แล้ว
        yearSpinner.setValueFactory(new IntegerSpinnerValueFactory(2000, 2100, LocalDate.now().getYear()));

        FxHelper.setColumnValues(table, IncomeRow::typeName, IncomeRow::amount, IncomeRow::percent);
        dateBox.addChangeListener(this::showReport);
        updateInputs();
    }

    /** เปิด/ปิดช่องกรอกตามประเภทรายงานที่เลือก แล้วแสดงรายงานใหม่ */
    @FXML
    private void updateInputs() {
        boolean daily = dailyRadio.isSelected();
        dateBox.setDisable(!daily);
        monthCombo.setDisable(daily);
        yearSpinner.setDisable(daily);
        showReport();
    }

    @FXML
    private void showReport() {
        IncomeReport report;
        if (dailyRadio.isSelected()) {
            LocalDate date = dateBox.getDateOrNull();
            if (date == null) {
                titleLabel.setText("รูปแบบวันที่ไม่ถูกต้อง (ปี-เดือน-วัน)");
                return;
            }
            report = reportService.createDailyReport(date);
        } else {
            YearMonth yearMonth = YearMonth.of(yearSpinner.getValue(), monthCombo.getSelectionModel().getSelectedIndex() + 1);
            report = reportService.createMonthlyReport(yearMonth);
        }

        titleLabel.setText(report.getReportTitle()); // ชื่อหัวรายงานต่างกันตามชนิดจริง (override)
        double total = report.getTotalIncome();
        List<IncomeRow> rows = new ArrayList<>();
        for (PaymentType type : PaymentType.values()) {
            double amount = report.getIncomeByType(type);
            String percent = total > 0 ? String.format("%.1f%%", amount * 100 / total) : "-";
            rows.add(new IncomeRow(type.getThaiName(), String.format("%,.2f", amount), percent));
        }
        table.getItems().setAll(rows);
        totalLabel.setText("รายได้รวมทั้งสิ้น: " + FormatHelper.money(total));
        noteLabel.setText("จำนวนรายการรับเงิน " + report.getTransactionCount() + " รายการ | ชั่วโมงแพ็กเกจที่ถูกใช้จอง "
                + report.getPackageHoursUsed() + " ชม. (ไม่นับเป็นรายได้ซ้ำ เพราะนับแล้วตอนขายแพ็กเกจ)");
    }

    @Override
    public void refresh() {
        showReport();
    }
}
