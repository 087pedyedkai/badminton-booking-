package com.example.badminton.report;

import com.example.badminton.model.Booking;
import com.example.badminton.model.Payment;
import com.example.badminton.util.FormatHelper;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * รายงานรายได้ "รายเดือน"
 * สืบทอดจาก IncomeReport เหมือน DailyIncomeReport ต่างกันแค่การเลือกช่วงเวลา
 * (นับทุกวันที่อยู่ในเดือนและปีที่เลือก) -> ตัวอย่าง Polymorphism ที่เห็นภาพชัด
 */
public class MonthlyIncomeReport extends IncomeReport {

    private final YearMonth reportMonth;

    public MonthlyIncomeReport(List<Payment> payments, List<Booking> bookings, YearMonth reportMonth) {
        super(payments, bookings);
        this.reportMonth = reportMonth;
    }

    /** Override: นับรายการที่อยู่ในเดือน/ปีเดียวกับรายงาน */
    @Override
    protected boolean isInPeriod(LocalDate date) {
        return YearMonth.from(date).equals(reportMonth);
    }

    @Override
    public String getReportTitle() {
        return "รายงานรายได้ประจำเดือน " + FormatHelper.formatYearMonth(reportMonth);
    }
}
