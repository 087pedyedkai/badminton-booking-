package com.example.badminton.report;

import com.example.badminton.model.Booking;
import com.example.badminton.model.Member;
import com.example.badminton.service.PaymentService;
import com.example.badminton.util.FormatHelper;

/**
 * รายงาน "ประวัติการจองของสมาชิก" (สำหรับผู้ดูแลระบบ)
 * implements Reportable โดยตรง (ไม่ได้สืบทอด IncomeReport เพราะไม่ใช่รายงานรายได้)
 * แต่ยังถูกแสดงผลด้วย ReportService.showReport() ตัวเดียวกันได้ เพราะเป็น Reportable เหมือนกัน
 *
 * แสดง: วันที่ สนาม เวลา จำนวนชั่วโมง ยอดชำระของแต่ละการจอง
 *       ชั่วโมงสะสม (สำหรับส่วนลด) และชั่วโมงแพ็กเกจคงเหลือ
 */
public class MemberHistoryReport implements Reportable {

    private final Member member;

    public MemberHistoryReport(Member member) {
        this.member = member;
    }

    @Override
    public String getReportTitle() {
        return "ประวัติการจองของสมาชิก " + member.getMemberId() + " - " + member.getFullName();
    }

    @Override
    public String generateReport() {
        StringBuilder text = new StringBuilder();
        text.append("==================================================\n");
        text.append(getReportTitle()).append("\n");
        text.append("==================================================\n");
        if (member.getBookings().isEmpty()) {
            text.append("(ยังไม่มีประวัติการจอง)\n");
        }
        double totalPaid = 0;
        int totalHours = 0;
        for (Booking booking : member.getBookings()) {
            String payText = booking.isUsedPackage()
                    ? "ใช้แพ็กเกจ " + booking.getDurationHours() + " ชม."
                    : FormatHelper.money(booking.getCourtFee());
            text.append(booking.getBookingId())
                    .append(" | ").append(FormatHelper.formatDate(booking.getBookingDate()))
                    .append(" | สนาม ").append(booking.getCourt().getCourtNumber())
                    .append(" | ").append(booking.getStartTime()).append("-").append(booking.getEndTime())
                    .append(" | ").append(booking.getDurationHours()).append(" ชม.")
                    .append(" | ค่าสนาม: ").append(payText)
                    .append(booking.getShoeRentalFee() > 0 ? " | รองเท้า: " + FormatHelper.money(booking.getShoeRentalFee()) : "")
                    .append(" | รวมจ่าย: ").append(FormatHelper.money(booking.getTotalAmount())).append("\n");
            totalPaid += booking.getTotalAmount();
            totalHours += booking.getDurationHours();
        }
        text.append("--------------------------------------------------\n");
        text.append("จำนวนการจองทั้งหมด : ").append(member.getBookings().size())
                .append(" รายการ (").append(totalHours).append(" ชั่วโมง)\n");
        text.append("ยอดชำระจากการจองรวม : ").append(FormatHelper.money(totalPaid)).append("\n");
        int qualifying = member.getQualifyingHours();
        text.append("ชั่วโมงสะสม (นับเฉพาะชั่วโมงที่จ่ายเงินปกติ) : ").append(qualifying).append(" ชั่วโมง\n");
        // หาชั่วโมงถัดไปที่จะได้ส่วนลด เช่น สะสม 18 -> ชั่วโมงที่ 20
        int interval = PaymentService.DISCOUNT_HOUR_INTERVAL;
        int nextDiscountHour = (qualifying / interval + 1) * interval;
        text.append("ชั่วโมงที่จะได้ส่วนลด 30% ครั้งถัดไป : ชั่วโมงที่ ").append(nextDiscountHour)
                .append(" (อีก ").append(nextDiscountHour - qualifying).append(" ชั่วโมง)\n");
        text.append("ชั่วโมงแพ็กเกจคงเหลือ : ").append(member.getRemainingPackageHours()).append(" ชั่วโมง\n");
        text.append("==================================================");
        return text.toString();
    }
}
