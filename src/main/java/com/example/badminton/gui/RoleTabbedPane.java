package com.example.badminton.gui;

import javax.swing.JTabbedPane;
import java.awt.Component;
import java.awt.Font;

/**
 * แท็บเมนูของแต่ละบทบาท (แคชเชียร์ / ผู้ดูแลระบบ)
 * สืบทอดจาก JTabbedPane ของ Swing แล้วเพิ่มความสามารถ:
 * "เมื่อผู้ใช้เปลี่ยนแท็บ ให้หน้าจอของแท็บนั้นโหลดข้อมูลใหม่" (ถ้าหน้าจอนั้นเป็น Refreshable)
 */
public class RoleTabbedPane extends JTabbedPane {

    public RoleTabbedPane() {
        setFont(UiHelper.font(Font.BOLD, 14));
        addChangeListener(e -> refreshSelectedTab());
    }

    /** โหลดข้อมูลใหม่ให้แท็บที่กำลังเปิดอยู่ */
    public void refreshSelectedTab() {
        Component selected = getSelectedComponent();
        if (selected instanceof Refreshable) {
            ((Refreshable) selected).refresh();
        }
    }
}
