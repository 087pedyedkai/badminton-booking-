package com.example.badminton.report;

/**
 * interface Reportable = "สัญญา" ว่าคลาสใดที่ implements interface นี้ ต้องสร้างรายงานได้
 *
 * ทำไมใช้ interface?
 * - ระบบมีรายงานหลายแบบ: รายได้รายวัน, รายได้รายเดือน, ประวัติการจองของสมาชิก
 * - แต่ละแบบมีเนื้อหาต่างกันมาก แต่ "วิธีเรียกใช้" เหมือนกัน คือ getReportTitle() และ generateReport()
 * - ReportService.showReport(Reportable report) จึงรับรายงานแบบไหนก็ได้
 *   โดยไม่ต้องรู้ว่าเป็นคลาสอะไร -> นี่คือ Polymorphism ผ่าน interface
 * - หน้าจอ GUI ก็เรียก generateReport() ตัวเดียวกันนี้ แล้วนำข้อความไปแสดงในหน้าต่าง
 *
 * คลาสที่ implements:
 * - IncomeReport (abstract) -> DailyIncomeReport, MonthlyIncomeReport
 * - MemberHistoryReport
 */
public interface Reportable {

    /** ชื่อหัวรายงาน */
    String getReportTitle();

    /** สร้างเนื้อหารายงานทั้งหมดเป็นข้อความ (คลาสที่ implements ต้องเขียนเอง) */
    String generateReport();

    /**
     * พิมพ์รายงานออกทาง Console
     * เป็น default method = เมธอดที่ interface "เขียนเนื้อหาไว้ให้แล้ว"
     * คลาสที่ implements จึงไม่ต้องเขียนซ้ำ เพราะทุกรายงานพิมพ์แบบเดียวกัน คือพิมพ์ผลของ generateReport()
     */
    default void printReport() {
        System.out.println(generateReport());
    }
}
