package com.example.badminton.gui;

import com.example.badminton.BadmintonSystem;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;

/**
 * หน้าเข้าสู่ระบบ (หน้าแรกของโปรแกรม) - เทียบเท่าเมนูหลักของเวอร์ชัน Console
 * ปุ่ม: เข้าสู่ระบบแคชเชียร์ / เข้าสู่ระบบผู้ดูแลระบบ / สมัครสมาชิก / ออกจากโปรแกรม
 *
 * หมายเหตุ: เป็นการ "จำลอง" การเข้าสู่ระบบ (กดปุ่มเลือกบทบาทได้เลย ไม่มีรหัสผ่าน)
 * เพราะเป็นโปรแกรมเพื่อการเรียนรู้ ถ้าใช้งานจริงควรเพิ่มการยืนยันตัวตน
 */
public class LoginPanel extends JPanel {

    public LoginPanel(MainFrame frame, BadmintonSystem system) {
        super(new GridBagLayout()); // GridBagLayout ที่มี component เดียว = จัดให้อยู่กึ่งกลาง
        setBackground(UiHelper.BACKGROUND);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xC8E6C9), 2),
                BorderFactory.createEmptyBorder(28, 48, 28, 48)));

        JLabel title = UiHelper.title("ระบบจองสนามแบดมินตัน");
        title.setFont(UiHelper.font(Font.BOLD, 28));
        JLabel info1 = new JLabel("12 สนาม | ค่าสนาม 180 บาท/ชั่วโมง");
        JLabel info2 = new JLabel("จันทร์-ศุกร์ 13:00-21:00 | เสาร์-อาทิตย์ และวันหยุดนักขัตฤกษ์ 10:00-20:00");
        info1.setForeground(UiHelper.MUTED);
        info2.setForeground(UiHelper.MUTED);

        JButton cashierButton = bigButton("เข้าสู่ระบบพนักงานแคชเชียร์", true);
        JButton adminButton = bigButton("เข้าสู่ระบบผู้ดูแลระบบ", true);
        JButton registerButton = bigButton("สมัครสมาชิก", false);
        JButton exitButton = bigButton("ออกจากโปรแกรม", false);

        // ส่งออบเจกต์ผู้ใช้ (ชนิด SystemUser) ไปให้ MainFrame เปิดหน้าจอของบทบาทนั้น
        cashierButton.addActionListener(e -> frame.login(system.getCashier()));
        adminButton.addActionListener(e -> frame.login(system.getAdministrator()));
        registerButton.addActionListener(e -> RegisterMemberDialog.showDialog(this, system.getMemberService()));
        exitButton.addActionListener(e -> frame.dispose());

        JLabel note = new JLabel("* จำลองการเข้าสู่ระบบเพื่อการเรียนรู้ (ไม่มีรหัสผ่าน)");
        note.setForeground(UiHelper.MUTED);
        note.setFont(UiHelper.font(Font.PLAIN, 12));

        addCentered(card, title);
        card.add(Box.createVerticalStrut(6));
        addCentered(card, info1);
        addCentered(card, info2);
        card.add(Box.createVerticalStrut(24));
        addCentered(card, cashierButton);
        card.add(Box.createVerticalStrut(10));
        addCentered(card, adminButton);
        card.add(Box.createVerticalStrut(10));
        addCentered(card, registerButton);
        card.add(Box.createVerticalStrut(10));
        addCentered(card, exitButton);
        card.add(Box.createVerticalStrut(18));
        addCentered(card, note);

        add(card);
    }

    private JButton bigButton(String text, boolean primary) {
        JButton button = primary ? UiHelper.primaryButton(text) : new JButton(text);
        button.setFont(UiHelper.font(Font.BOLD, 16));
        Dimension size = new Dimension(360, 46);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        return button;
    }

    private void addCentered(JPanel panel, Component component) {
        ((javax.swing.JComponent) component).setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(component);
    }
}
