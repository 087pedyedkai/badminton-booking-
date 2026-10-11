package com.example.badminton.gui;

import com.example.badminton.BadmintonSystem;
import com.example.badminton.gui.admin.HolidayPanel;
import com.example.badminton.gui.admin.IncomeReportPanel;
import com.example.badminton.gui.admin.MemberHistoryPanel;
import com.example.badminton.gui.admin.PaymentsPanel;
import com.example.badminton.gui.admin.ProductManagePanel;
import com.example.badminton.gui.cashier.BookingPanel;
import com.example.badminton.gui.cashier.PackagePanel;
import com.example.badminton.gui.cashier.SalesPanel;
import com.example.badminton.gui.cashier.ShoeRentalPanel;
import com.example.badminton.user.Cashier;
import com.example.badminton.user.SystemUser;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * หน้าต่างหลักของโปรแกรม GUI
 *
 * โครงสร้างหน้าต่าง:
 * - แถบหัว (สีเขียว): ชื่อโปรแกรม + ผู้ใช้ที่เข้าสู่ระบบ + ปุ่มออกจากระบบ
 * - เนื้อหา ใช้ CardLayout สลับ 3 หน้า (เหมือนสำรับไพ่ที่แสดงทีละใบ):
 *     "login"   = LoginPanel (หน้าแรก)
 *     "cashier" = แท็บงานของแคชเชียร์
 *     "admin"   = แท็บงานของผู้ดูแลระบบ
 *
 * ทุกหน้าจอได้ Service มาจาก BadmintonSystem ตัวเดียวกัน ข้อมูลจึงเชื่อมกันทั้งหมด
 * เช่น แคชเชียร์จองสนาม -> ผู้ดูแลระบบเห็นรายได้ในรายงานทันที
 */
public class MainFrame extends JFrame {

    private static final String LOGIN_CARD = "login";
    private static final String CASHIER_CARD = "cashier";
    private static final String ADMIN_CARD = "admin";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final JLabel userLabel = new JLabel();
    private final JButton logoutButton = new JButton("ออกจากระบบ");
    private final RoleTabbedPane cashierTabs = new RoleTabbedPane();
    private final RoleTabbedPane adminTabs = new RoleTabbedPane();

    public MainFrame(BadmintonSystem system) {
        super("ระบบจัดการการจองสนามแบดมินตัน");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1280, 800);
        setLocationRelativeTo(null); // เปิดกลางจอ

        // ---- แท็บของแคชเชียร์ ----
        cashierTabs.addTab("จองสนาม", new BookingPanel(
                system.getMemberService(), system.getBookingService(), system.getPaymentService()));
        cashierTabs.addTab("ค้นหาการจอง", new BookingSearchPanel(system.getBookingService(), false));
        cashierTabs.addTab("แพ็กเกจชั่วโมง", new PackagePanel(system.getMemberService(), system.getPaymentService()));
        cashierTabs.addTab("เช่ารองเท้า", new ShoeRentalPanel(system.getBookingService(), system.getPaymentService()));
        cashierTabs.addTab("ขายอุปกรณ์", new SalesPanel(system.getMemberService(), system.getProductService()));
        cashierTabs.addTab("สมาชิก", new MemberPanel(system.getMemberService()));

        // ---- แท็บของผู้ดูแลระบบ ----
        adminTabs.addTab("รายงานรายได้", new IncomeReportPanel(system.getReportService()));
        adminTabs.addTab("ประวัติการจองของสมาชิก",
                new MemberHistoryPanel(system.getMemberService(), system.getReportService()));
        adminTabs.addTab("การจองทั้งหมด", new BookingSearchPanel(system.getBookingService(), true));
        adminTabs.addTab("รายการรับเงิน", new PaymentsPanel(system.getPaymentService()));
        adminTabs.addTab("สมาชิก", new MemberPanel(system.getMemberService()));
        adminTabs.addTab("จัดการสินค้า", new ProductManagePanel(system.getProductService()));
        adminTabs.addTab("วันหยุดนักขัตฤกษ์", new HolidayPanel(system.getBookingService()));

        cardPanel.add(new LoginPanel(this, system), LOGIN_CARD);
        cardPanel.add(cashierTabs, CASHIER_CARD);
        cardPanel.add(adminTabs, ADMIN_CARD);

        setLayout(new BorderLayout());
        add(createHeader(), BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
        logout();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiHelper.PRIMARY_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel appName = new JLabel("ระบบจองสนามแบดมินตัน");
        appName.setFont(UiHelper.font(Font.BOLD, 20));
        appName.setForeground(Color.WHITE);

        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(UiHelper.font(Font.PLAIN, 15));
        logoutButton.addActionListener(e -> logout());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(userLabel);
        right.add(logoutButton);

        header.add(appName, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    /**
     * เข้าสู่ระบบด้วยผู้ใช้ที่เลือก
     * Polymorphism: user.getRoleName() คืนค่า "พนักงานแคชเชียร์" หรือ "ผู้ดูแลระบบ"
     * ตามชนิดจริงของออบเจกต์ (Cashier / Administrator) ที่ override เมธอดนี้ไว้
     */
    public void login(SystemUser user) {
        userLabel.setText(user.getRoleName() + ": " + user.getName() + " (" + user.getUserId() + ")");
        logoutButton.setVisible(true);
        if (user instanceof Cashier) {
            cardLayout.show(cardPanel, CASHIER_CARD);
            cashierTabs.setSelectedIndex(0);
            cashierTabs.refreshSelectedTab();
        } else {
            cardLayout.show(cardPanel, ADMIN_CARD);
            adminTabs.setSelectedIndex(0);
            adminTabs.refreshSelectedTab();
        }
    }

    /** ออกจากระบบ กลับหน้าแรก */
    public void logout() {
        userLabel.setText("ยังไม่ได้เข้าสู่ระบบ");
        logoutButton.setVisible(false);
        cardLayout.show(cardPanel, LOGIN_CARD);
    }
}
