package com.example.badminton.gui;

/**
 * interface Refreshable = หน้าจอที่ "โหลดข้อมูลใหม่ได้"
 *
 * ทำไมต้องมี?
 * ข้อมูลของแต่ละแท็บเปลี่ยนจากแท็บอื่นได้ เช่น จองสนามในแท็บ "จองสนาม" แล้ว
 * แท็บ "ค้นหาการจอง" ต้องเห็นรายการใหม่ด้วย
 * RoleTabbedPane จึงเรียก refresh() ทุกครั้งที่ผู้ใช้เปิดแท็บนั้น
 * โดยไม่ต้องรู้ว่าเป็นหน้าจอชนิดไหน (Polymorphism ผ่าน interface อีกตัวอย่างหนึ่ง)
 */
public interface Refreshable {
    void refresh();
}
