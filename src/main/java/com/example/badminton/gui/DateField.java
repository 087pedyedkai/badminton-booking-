package com.example.badminton.gui;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * ช่องกรอกวันที่ (Swing ไม่มีปฏิทินให้ในตัว จึงทำแบบง่าย ๆ เอง)
 * ประกอบด้วย: ปุ่ม "<" (วันก่อนหน้า) | ช่องพิมพ์วันที่ ปี-เดือน-วัน | ปุ่ม ">" (วันถัดไป) | ปุ่ม "วันนี้"
 * ถ้าพิมพ์ปีเป็น พ.ศ. (เช่น 2569) จะแปลงเป็น ค.ศ. ให้อัตโนมัติ
 *
 * หน้าจออื่นสามารถ "ฟัง" การเปลี่ยนวันที่ได้ด้วย addChangeListener()
 */
public class DateField extends JPanel {

    private final JTextField textField = new JTextField(9);
    private final List<Runnable> changeListeners = new ArrayList<>();

    public DateField(LocalDate initialDate) {
        super(new FlowLayout(FlowLayout.LEFT, 4, 0));
        setOpaque(false);
        textField.setText(initialDate.toString());
        textField.setToolTipText("รูปแบบ ปี-เดือน-วัน เช่น 2026-10-12");

        JButton previousButton = new JButton("<");
        JButton nextButton = new JButton(">");
        JButton todayButton = new JButton("วันนี้");
        previousButton.setToolTipText("วันก่อนหน้า");
        nextButton.setToolTipText("วันถัดไป");

        previousButton.addActionListener(e -> shiftDays(-1));
        nextButton.addActionListener(e -> shiftDays(1));
        todayButton.addActionListener(e -> setDate(LocalDate.now()));
        textField.addActionListener(e -> fireChanged()); // กด Enter
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                fireChanged(); // ออกจากช่องพิมพ์
            }
        });

        add(previousButton);
        add(textField);
        add(nextButton);
        add(todayButton);
    }

    /**
     * อ่านวันที่จากช่องกรอก
     * ถ้ารูปแบบผิด จะโยน IllegalArgumentException พร้อมข้อความภาษาไทย
     */
    public LocalDate getDate() {
        try {
            LocalDate date = LocalDate.parse(textField.getText().trim());
            if (date.getYear() >= 2400) {
                date = date.minusYears(543); // แปลง พ.ศ. -> ค.ศ.
            }
            return date;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("รูปแบบวันที่ไม่ถูกต้อง ต้องเป็น ปี-เดือน-วัน เช่น 2026-10-12");
        }
    }

    /** อ่านวันที่ ถ้ารูปแบบผิดคืน null (ใช้ในจุดที่ไม่ต้องการแสดงข้อผิดพลาดทันที) */
    public LocalDate getDateOrNull() {
        try {
            return getDate();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setDate(LocalDate date) {
        textField.setText(date.toString());
        fireChanged();
    }

    private void shiftDays(int days) {
        LocalDate date = getDateOrNull();
        if (date == null) {
            date = LocalDate.now();
        }
        setDate(date.plusDays(days));
    }

    /** เปิด/ปิดการใช้งานทั้งช่องกรอกและปุ่มทั้งหมดพร้อมกัน */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (java.awt.Component child : getComponents()) {
            child.setEnabled(enabled);
        }
    }

    /** ลงทะเบียนโค้ดที่จะให้ทำงานเมื่อวันที่เปลี่ยน */
    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    private void fireChanged() {
        for (Runnable listener : changeListeners) {
            listener.run();
        }
    }
}
