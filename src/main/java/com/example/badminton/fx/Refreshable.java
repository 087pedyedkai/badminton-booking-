package com.example.badminton.fx;

/**
 * interface Refreshable = หน้าจอที่ "โหลดข้อมูลใหม่ได้" (ฉบับ JavaFX)
 *
 * ทำไมต้องมี?
 * ข้อมูลของแต่ละแท็บเปลี่ยนจากแท็บอื่นได้ เช่น จองสนามในแท็บ "จองสนาม" แล้ว
 * แท็บ "ค้นหาการจอง" ต้องเห็นรายการใหม่ด้วย
 * MainController จึงเรียก refresh() ทุกครั้งที่ผู้ใช้เปิดแท็บนั้น
 * โดยไม่ต้องรู้ว่าเป็น Controller ชนิดไหน (Polymorphism ผ่าน interface)
 *
 * (เป็นสำเนาของ gui.Refreshable เพื่อให้ลบแพ็กเกจ gui ของ Swing ทิ้งภายหลังได้โดยโค้ดส่วนนี้ไม่พัง)
 */
public interface Refreshable {
    void refresh();
}
