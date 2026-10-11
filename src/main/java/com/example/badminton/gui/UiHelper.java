package com.example.badminton.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * UiHelper = รวมเครื่องมือเล็ก ๆ ที่ทุกหน้าจอใช้ร่วมกัน
 * เช่น สี ฟอนต์ภาษาไทย ปุ่ม ตาราง กล่องข้อความแจ้งเตือน และการจัดฟอร์ม
 *
 * ทำไมต้องรวมไว้ที่เดียว?
 * - ทุกหน้าจอจะมีหน้าตาเหมือนกัน (สีเดียวกัน ฟอนต์เดียวกัน)
 * - ถ้าอยากเปลี่ยนสีทั้งโปรแกรม แก้ที่นี่ที่เดียว
 * - โค้ดในหน้าจอต่าง ๆ สั้นลงและอ่านง่ายขึ้น
 */
public final class UiHelper {

    // ---------------- สีหลักของโปรแกรม (โทนเขียวสนามแบดมินตัน) ----------------
    public static final Color PRIMARY = new Color(0x2E7D32);
    public static final Color PRIMARY_DARK = new Color(0x1B5E20);
    public static final Color BACKGROUND = new Color(0xF4F6F4);
    public static final Color ERROR = new Color(0xC62828);
    public static final Color SUCCESS = new Color(0x2E7D32);
    public static final Color MUTED = new Color(0x616161);

    // สีของช่องตารางสนาม
    public static final Color SLOT_FREE = new Color(0xE8F5E9);
    public static final Color SLOT_PAID = new Color(0xEF9A9A);
    public static final Color SLOT_PACKAGE = new Color(0xFFCC80);
    public static final Color SLOT_CLOSED = new Color(0xD6D6D6);
    public static final Color SLOT_SELECTED = new Color(0xFFF176);
    public static final Color SLOT_CONFLICT = new Color(0xE53935);

    private static String thaiFontName = Font.DIALOG;

    private UiHelper() {
        // คลาสนี้มีแต่เมธอด static จึงไม่ให้สร้างออบเจกต์
    }

    /**
     * ตั้งค่าหน้าตาของโปรแกรม (ต้องเรียกก่อนสร้างหน้าต่างใด ๆ)
     * 1) ใช้ Look and Feel แบบ "Nimbus" ที่มากับ Java (ดูทันสมัยกว่าแบบเริ่มต้น)
     * 2) เลือกฟอนต์ที่แสดงภาษาไทยได้ และตั้งเป็นฟอนต์เริ่มต้นของทุก component
     */
    public static void installTheme() {
        thaiFontName = pickThaiFont();
        Font baseFont = new Font(thaiFontName, Font.PLAIN, 14);

        UIManager.put("nimbusSelectionBackground", PRIMARY);
        UIManager.put("nimbusFocus", PRIMARY);
        UIManager.put("control", BACKGROUND);
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // ถ้าใช้ Nimbus ไม่ได้ ก็ใช้หน้าตาเริ่มต้นของ Java ต่อไป (โปรแกรมยังทำงานได้ปกติ)
        }

        // Nimbus อ่านฟอนต์จาก "defaultFont" ส่วน Look and Feel อื่นต้องตั้งทีละ key
        UIManager.getLookAndFeelDefaults().put("defaultFont", baseFont);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, new FontUIResource(baseFont));
            }
        }
    }

    /** เลือกฟอนต์ภาษาไทยตัวแรกที่มีในเครื่อง (Windows มี Leelawadee UI และ Tahoma) */
    private static String pickThaiFont() {
        String[] candidates = {"Leelawadee UI", "Tahoma", "Leelawadee", "Noto Sans Thai", "Sarabun"};
        Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String name : candidates) {
            if (available.contains(name)) {
                return name;
            }
        }
        return Font.DIALOG;
    }

    public static Font font(int style, int size) {
        return new Font(thaiFontName, style, size);
    }

    // ---------------- ตัวช่วยสร้าง component ----------------

    /** ข้อความหัวข้อตัวใหญ่ */
    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.BOLD, 20));
        label.setForeground(PRIMARY_DARK);
        return label;
    }

    /** ปุ่มหลัก (สีเขียว) สำหรับคำสั่งสำคัญ เช่น "ยืนยันการจอง" */
    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(font(Font.BOLD, 14));
        return button;
    }

    /** ตารางที่ผู้ใช้แก้ไขข้อมูลในช่องไม่ได้ (ใช้แสดงผลอย่างเดียว) */
    public static DefaultTableModel readOnlyModel(String... columnNames) {
        return new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /** สร้าง JTable จาก model พร้อมตั้งค่าให้อ่านภาษาไทยง่าย และกดหัวตารางเพื่อเรียงข้อมูลได้ */
    public static JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(font(Font.BOLD, 14));
        table.setFillsViewportHeight(true);
        return table;
    }

    /** กล่องข้อความหลายบรรทัดแบบอ่านอย่างเดียว (ใช้แสดงรายละเอียด/รายงาน) */
    public static JTextArea outputArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMargin(new Insets(8, 8, 8, 8));
        return area;
    }

    public static JScrollPane scroll(Component component) {
        return new JScrollPane(component);
    }

    /** ห่อ component ด้วยกรอบที่มีหัวข้อ */
    public static JPanel section(String title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout());
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(font(Font.BOLD, 14));
        border.setTitleColor(PRIMARY_DARK);
        panel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(4, 6, 6, 6)));
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    /**
     * เพิ่ม 1 แถวในฟอร์มแบบ "ป้ายชื่อ : ช่องกรอก" (panel ต้องใช้ GridBagLayout)
     * row = ลำดับแถว เริ่มที่ 0
     */
    public static void addFormRow(JPanel panel, int row, String label, JComponent field) {
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = row;
        left.anchor = GridBagConstraints.WEST;
        left.insets = new Insets(4, 4, 4, 8);
        panel.add(new JLabel(label), left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = row;
        right.fill = GridBagConstraints.HORIZONTAL;
        right.weightx = 1;
        right.insets = new Insets(4, 0, 4, 4);
        panel.add(field, right);
    }

    /** เพิ่ม component เต็มความกว้างของฟอร์ม (2 คอลัมน์) */
    public static void addFormWide(JPanel panel, int row, JComponent component) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(6, 4, 6, 4);
        panel.add(component, c);
    }

    // ---------------- กล่องข้อความแจ้งเตือน ----------------

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "เกิดข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /** แสดงข้อความยาวหลายบรรทัด (เช่น ใบเสร็จ รายละเอียดการจอง) ในกล่องที่เลื่อนอ่านได้ */
    public static void showText(Component parent, String title, String text) {
        JTextArea area = outputArea();
        area.setText(text);
        area.setColumns(48);
        area.setRows(Math.min(16, text.split("\n").length + 1));
        JOptionPane.showMessageDialog(parent, new JScrollPane(area), title, JOptionPane.INFORMATION_MESSAGE);
    }

    /** ถามยืนยัน คืน true ถ้ากด "Yes" */
    public static boolean confirm(Component parent, String title, String message) {
        int answer = JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return answer == JOptionPane.YES_OPTION;
    }

    /** แปลงข้อความเป็น HTML เพื่อให้ JLabel ขึ้นบรรทัดใหม่ได้ ("\n" -> "<br>") */
    public static String html(String text) {
        return "<html>" + text.replace("\n", "<br>") + "</html>";
    }

    /** เหมือน html() แต่กำหนดความกว้าง ข้อความที่ยาวเกินจะขึ้นบรรทัดใหม่อัตโนมัติ */
    public static String html(String text, int widthPixels) {
        return "<html><div style='width:" + widthPixels + "px'>" + text.replace("\n", "<br>") + "</div></html>";
    }
}
