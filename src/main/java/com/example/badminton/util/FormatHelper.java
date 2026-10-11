package com.example.badminton.util;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * คลาสช่วยจัดรูปแบบการแสดงผล: วันที่ภาษาไทย (ชื่อวัน/ชื่อเดือน) และจำนวนเงิน
 * ใช้ร่วมกันหลายคลาส เช่น Booking (แสดงรายละเอียดการจอง) และรายงานรายได้
 */
public class FormatHelper {

    // ลำดับตรงกับ DayOfWeek.getValue() คือ 1 = จันทร์ ... 7 = อาทิตย์ (ตำแหน่ง 0 ไม่ใช้)
    private static final String[] THAI_DAY_NAMES = {
            "", "จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์", "เสาร์", "อาทิตย์"
    };

    // ลำดับตรงกับเดือน 1 = มกราคม ... 12 = ธันวาคม (ตำแหน่ง 0 ไม่ใช้)
    private static final String[] THAI_MONTH_NAMES = {
            "", "มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน",
            "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
    };

    /** คืนชื่อวันภาษาไทย เช่น "จันทร์" */
    public static String thaiDayName(LocalDate date) {
        return THAI_DAY_NAMES[date.getDayOfWeek().getValue()];
    }

    /** แสดงวันที่แบบ "2026-10-12 (จันทร์)" */
    public static String formatDate(LocalDate date) {
        return date + " (" + thaiDayName(date) + ")";
    }

    /** คืนชื่อเดือนภาษาไทย month = 1 ถึง 12 เช่น 10 -> "ตุลาคม" */
    public static String thaiMonthName(int month) {
        return THAI_MONTH_NAMES[month];
    }

    /** แสดงเดือนแบบ "ตุลาคม 2026 (พ.ศ. 2569)" */
    public static String formatYearMonth(YearMonth yearMonth) {
        return THAI_MONTH_NAMES[yearMonth.getMonthValue()] + " " + yearMonth.getYear()
                + " (พ.ศ. " + (yearMonth.getYear() + 543) + ")";
    }

    /** แสดงจำนวนเงินแบบมีคอมมาและทศนิยม 2 ตำแหน่ง เช่น "2,600.00 บาท" */
    public static String money(double amount) {
        return String.format("%,.2f บาท", amount);
    }
}
