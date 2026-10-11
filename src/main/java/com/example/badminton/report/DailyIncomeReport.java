package com.example.badminton.report;

import com.example.badminton.model.Booking;
import com.example.badminton.model.Payment;
import com.example.badminton.util.FormatHelper;

import java.time.LocalDate;
import java.util.List;

/**
 * รายงานรายได้ "รายวัน"
 * สืบทอด (extends) จาก IncomeReport จึงได้เมธอดคำนวณและพิมพ์รายงานมาใช้ทันที
 * เขียนเพิ่มเฉพาะส่วนที่ต่าง: นับเฉพาะวันที่ตรงกับวันที่เลือก และชื่อหัวรายงาน
 */
public class DailyIncomeReport extends IncomeReport {

    private final LocalDate reportDate;

    public DailyIncomeReport(List<Payment> payments, List<Booking> bookings, LocalDate reportDate) {
        super(payments, bookings); // ส่งข้อมูลให้ constructor ของคลาสแม่ (IncomeReport)
        this.reportDate = reportDate;
    }

    /** Override: นับเฉพาะรายการที่วันที่ตรงกับวันที่ของรายงาน */
    @Override
    protected boolean isInPeriod(LocalDate date) {
        return date.equals(reportDate);
    }

    @Override
    public String getReportTitle() {
        return "รายงานรายได้ประจำวันที่ " + FormatHelper.formatDate(reportDate);
    }
}
