package com.example.badminton.gui;

import com.example.badminton.BadmintonSystem;

import javax.swing.SwingUtilities;

/**
 * จุดเริ่มต้นของโปรแกรมแบบ "หน้าต่าง GUI" (Java Swing)
 * (เวอร์ชัน Console ยังใช้ได้ตามเดิมที่ com.example.badminton.Main)
 *
 * ความสัมพันธ์: BadmintonApp -> สร้าง BadmintonSystem (Service + ข้อมูลตัวอย่าง)
 *              -> ส่งให้ MainFrame ซึ่งสร้างหน้าจอทุกแท็บจาก Service ชุดนั้น
 */
public class BadmintonApp {

    public static void main(String[] args) {
        // Swing กำหนดให้สร้างและแก้ไขหน้าจอบน "Event Dispatch Thread" เท่านั้น
        // SwingUtilities.invokeLater() คือการส่งงานไปทำบน thread นั้นอย่างถูกต้อง
        SwingUtilities.invokeLater(() -> {
            UiHelper.installTheme();
            BadmintonSystem system = new BadmintonSystem();
            MainFrame frame = new MainFrame(system);
            frame.setVisible(true);
        });
    }
}
