package com.example.badminton.gui.admin;

import com.example.badminton.gui.DateField;
import com.example.badminton.gui.Refreshable;
import com.example.badminton.gui.UiHelper;
import com.example.badminton.model.PaymentType;
import com.example.badminton.report.IncomeReport;
import com.example.badminton.service.ReportService;
import com.example.badminton.util.FormatHelper;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * แท็บ "รายงานรายได้" (ผู้ดูแลระบบ) เลือกได้ทั้งรายวันและรายเดือน
 *
 * การทำงาน:
 * - รายวัน   -> reportService.createDailyReport(date)       ได้ DailyIncomeReport
 * - รายเดือน -> reportService.createMonthlyReport(yearMonth) ได้ MonthlyIncomeReport
 * ทั้งสองแบบเก็บในตัวแปรชนิด IncomeReport (abstract class) ตัวเดียวกัน
 * แล้วเรียก getIncomeByType() / getTotalIncome() ได้เหมือนกัน -> Polymorphism
 *
 * กติกา: นับเฉพาะเงินที่รับจริง (Payment) ตามวันที่รับเงิน การใช้ชั่วโมงแพ็กเกจไม่นับเป็นรายได้ซ้ำ
 */
public class IncomeReportPanel extends JPanel implements Refreshable {

    private final ReportService reportService;

    private final JRadioButton dailyRadio = new JRadioButton("รายวัน", true);
    private final JRadioButton monthlyRadio = new JRadioButton("รายเดือน");
    private final DateField dateField = new DateField(LocalDate.now());
    private final JComboBox<String> monthCombo = new JComboBox<>();
    private final JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1));

    private final JLabel titleLabel = UiHelper.title(" ");
    private final DefaultTableModel tableModel = UiHelper.readOnlyModel("หมวดรายได้", "ยอดเงิน (บาท)", "สัดส่วน");
    private final JLabel totalLabel = new JLabel();
    private final JLabel noteLabel = new JLabel();

    public IncomeReportPanel(ReportService reportService) {
        super(new BorderLayout(8, 8));
        this.reportService = reportService;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ButtonGroup group = new ButtonGroup();
        group.add(dailyRadio);
        group.add(monthlyRadio);
        for (int month = 1; month <= 12; month++) {
            monthCombo.addItem(month + " - " + FormatHelper.thaiMonthName(month));
        }
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        yearSpinner.setEditor(new JSpinner.NumberEditor(yearSpinner, "#")); // แสดง 2026 ไม่ใช่ 2,026

        JButton showButton = UiHelper.primaryButton("แสดงรายงาน");
        showButton.addActionListener(e -> showReport());
        dailyRadio.addActionListener(e -> updateInputs());
        monthlyRadio.addActionListener(e -> updateInputs());
        dateField.addChangeListener(this::showReport);

        // ---- แถบเลือกช่วงเวลา (2 บรรทัด: รายวัน / รายเดือน) ----
        JPanel dailyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        dailyRow.add(dailyRadio);
        dailyRow.add(dateField);
        JPanel monthlyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        monthlyRow.add(monthlyRadio);
        monthlyRow.add(new JLabel("เดือน:"));
        monthlyRow.add(monthCombo);
        monthlyRow.add(new JLabel("ปี (ค.ศ.):"));
        monthlyRow.add(yearSpinner);
        monthlyRow.add(showButton);
        JPanel selector = new JPanel(new GridLayout(2, 1, 0, 4));
        selector.add(dailyRow);
        selector.add(monthlyRow);

        // ---- ผลรายงาน ----
        totalLabel.setFont(UiHelper.font(Font.BOLD, 24));
        totalLabel.setForeground(UiHelper.PRIMARY_DARK);
        noteLabel.setForeground(UiHelper.MUTED);
        JPanel bottom = new JPanel(new GridLayout(2, 1));
        bottom.add(totalLabel);
        bottom.add(noteLabel);

        JPanel result = new JPanel(new BorderLayout(6, 6));
        result.add(titleLabel, BorderLayout.NORTH);
        result.add(UiHelper.scroll(UiHelper.table(tableModel)), BorderLayout.CENTER);
        result.add(bottom, BorderLayout.SOUTH);

        add(UiHelper.section("เลือกช่วงเวลา", selector), BorderLayout.NORTH);
        add(result, BorderLayout.CENTER);
        updateInputs();
    }

    /** เปิด/ปิดช่องกรอกตามประเภทรายงานที่เลือก แล้วแสดงรายงานใหม่ */
    private void updateInputs() {
        boolean daily = dailyRadio.isSelected();
        dateField.setEnabled(daily);
        monthCombo.setEnabled(!daily);
        yearSpinner.setEnabled(!daily);
        showReport();
    }

    private void showReport() {
        IncomeReport report;
        if (dailyRadio.isSelected()) {
            LocalDate date = dateField.getDateOrNull();
            if (date == null) {
                titleLabel.setText("รูปแบบวันที่ไม่ถูกต้อง (ปี-เดือน-วัน)");
                return;
            }
            report = reportService.createDailyReport(date);
        } else {
            YearMonth yearMonth = YearMonth.of((Integer) yearSpinner.getValue(), monthCombo.getSelectedIndex() + 1);
            report = reportService.createMonthlyReport(yearMonth);
        }

        titleLabel.setText(report.getReportTitle()); // ชื่อหัวรายงานต่างกันตามชนิดจริง (override)
        double total = report.getTotalIncome();
        tableModel.setRowCount(0);
        for (PaymentType type : PaymentType.values()) {
            double amount = report.getIncomeByType(type);
            String percent = total > 0 ? String.format("%.1f%%", amount * 100 / total) : "-";
            tableModel.addRow(new Object[]{type.getThaiName(), String.format("%,.2f", amount), percent});
        }
        totalLabel.setText("รายได้รวมทั้งสิ้น: " + FormatHelper.money(total));
        noteLabel.setText("จำนวนรายการรับเงิน " + report.getTransactionCount() + " รายการ | ชั่วโมงแพ็กเกจที่ถูกใช้จอง "
                + report.getPackageHoursUsed() + " ชม. (ไม่นับเป็นรายได้ซ้ำ เพราะนับแล้วตอนขายแพ็กเกจ)");
    }

    @Override
    public void refresh() {
        showReport();
    }
}
