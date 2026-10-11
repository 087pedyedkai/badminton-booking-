package com.example.badminton.service;

import com.example.badminton.model.Member;
import com.example.badminton.report.DailyIncomeReport;
import com.example.badminton.report.IncomeReport;
import com.example.badminton.report.MemberHistoryReport;
import com.example.badminton.report.MonthlyIncomeReport;
import com.example.badminton.report.Reportable;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * ReportService ทำหน้าที่ "สร้าง" และ "แสดง" รายงานต่าง ๆ ให้ผู้ดูแลระบบ
 *
 * ความสัมพันธ์กับคลาสอื่น:
 * - ดึงข้อมูลการชำระเงินจาก PaymentService และข้อมูลการจองจาก BookingService
 * - สร้างออบเจกต์รายงาน (DailyIncomeReport / MonthlyIncomeReport / MemberHistoryReport)
 * - Administrator เรียกใช้เมธอดของคลาสนี้จากเมนู
 *
 * จุดสำคัญเรื่อง Polymorphism:
 * รายงานทุกแบบเป็น Reportable (interface) และ showReport() รับ Reportable
 * -> ใช้ได้กับรายงานทุกแบบด้วยโค้ดชุดเดียว
 * (รายงานรายวัน/รายเดือนคืนค่าเป็น IncomeReport ซึ่ง "เป็น" Reportable เช่นกัน
 *  แต่หน้าจอ GUI สามารถเรียก getIncomeByType() เพื่อนำยอดแต่ละหมวดไปแสดงเป็นตาราง/กราฟได้)
 */
public class ReportService {

    private final PaymentService paymentService;
    private final BookingService bookingService;

    public ReportService(PaymentService paymentService, BookingService bookingService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
    }

    public IncomeReport createDailyReport(LocalDate date) {
        return new DailyIncomeReport(paymentService.getAllPayments(), bookingService.getAllBookings(), date);
    }

    public IncomeReport createMonthlyReport(YearMonth yearMonth) {
        return new MonthlyIncomeReport(paymentService.getAllPayments(), bookingService.getAllBookings(), yearMonth);
    }

    public Reportable createMemberHistoryReport(Member member) {
        return new MemberHistoryReport(member);
    }

    /**
     * แสดงรายงานใดก็ได้ที่เป็น Reportable
     * ตอนเขียนโค้ดเราไม่รู้ว่า report เป็นรายวัน รายเดือน หรือประวัติสมาชิก
     * แต่เมื่อเรียก report.printReport() Java จะเรียกเมธอดของคลาสจริงให้เอง (Dynamic binding)
     */
    public void showReport(Reportable report) {
        report.printReport();
    }
}
