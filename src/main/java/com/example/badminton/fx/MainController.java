package com.example.badminton.fx;

import com.example.badminton.BadmintonSystem;
import com.example.badminton.fx.admin.HolidayController;
import com.example.badminton.fx.admin.IncomeReportController;
import com.example.badminton.fx.admin.MemberHistoryController;
import com.example.badminton.fx.admin.PaymentsController;
import com.example.badminton.fx.admin.ProductManageController;
import com.example.badminton.fx.cashier.BookingController;
import com.example.badminton.fx.cashier.PackageController;
import com.example.badminton.fx.cashier.SalesController;
import com.example.badminton.fx.cashier.ShoeRentalController;
import com.example.badminton.user.Cashier;
import com.example.badminton.user.SystemUser;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 * Controller ของหน้าต่างหลัก (main.fxml) - เทียบเท่า MainFrame + RoleTabbedPane ของ Swing
 *
 * โครงสร้างหน้าต่าง (BorderPane):
 * - top    = แถบหัวสีเขียว: ชื่อโปรแกรม + ผู้ใช้ที่เข้าสู่ระบบ + ปุ่มออกจากระบบ
 * - center = สลับได้ 3 หน้า (แทน CardLayout ด้วยการเปลี่ยน center):
 *     loginView   = หน้าเข้าสู่ระบบ (หน้าแรก)
 *     cashierTabs = แท็บงานของแคชเชียร์ 6 แท็บ
 *     adminTabs   = แท็บงานของผู้ดูแลระบบ 7 แท็บ
 *
 * แต่ละแท็บโหลดจากไฟล์ .fxml แยกด้วย <fx:include fx:id="xxx">
 * FXMLLoader จะใส่ Controller ของไฟล์นั้นให้ในฟิลด์ชื่อ xxxController อัตโนมัติ
 * แล้วผูก Tab กับ Controller (Refreshable) เพื่อโหลดข้อมูลใหม่ทุกครั้งที่เปิดแท็บ
 */
public class MainController {

    @FXML private BorderPane root;
    @FXML private Label userLabel;
    @FXML private Button logoutButton;
    @FXML private Parent loginView;
    @FXML private TabPane cashierTabs;
    @FXML private TabPane adminTabs;

    // Controller ของหน้าจอที่ fx:include เข้ามา (ชื่อฟิลด์ = fx:id + "Controller")
    @FXML private LoginController loginViewController;
    @FXML private BookingController cashierBookingController;
    @FXML private BookingSearchController cashierSearchController;
    @FXML private PackageController cashierPackageController;
    @FXML private ShoeRentalController cashierShoeController;
    @FXML private SalesController cashierSalesController;
    @FXML private MemberController cashierMemberController;
    @FXML private IncomeReportController adminIncomeController;
    @FXML private MemberHistoryController adminHistoryController;
    @FXML private BookingSearchController adminBookingsController;
    @FXML private PaymentsController adminPaymentsController;
    @FXML private MemberController adminMemberController;
    @FXML private ProductManageController adminProductController;
    @FXML private HolidayController adminHolidayController;

    public MainController(BadmintonSystem system) {
        // ไม่ต้องเก็บ system: แต่ละแท็บได้ BadmintonSystem ตัวเดียวกันผ่าน controllerFactory แล้ว
    }

    /** FXMLLoader เรียกเมธอดนี้หลังใส่ค่าฟิลด์ @FXML ครบแล้ว */
    @FXML
    private void initialize() {
        loginViewController.setMainController(this);
        adminBookingsController.setShowAllByDefault(); // ผู้ดูแลระบบ: เริ่มที่โหมดแสดงทั้งหมด

        // ลำดับต้องตรงกับลำดับแท็บใน main.fxml
        bindTabs(cashierTabs, cashierBookingController, cashierSearchController, cashierPackageController,
                cashierShoeController, cashierSalesController, cashierMemberController);
        bindTabs(adminTabs, adminIncomeController, adminHistoryController, adminBookingsController,
                adminPaymentsController, adminMemberController, adminProductController, adminHolidayController);
        logout();
    }

    /** ผูกแท็บแต่ละแท็บกับ Controller และให้โหลดข้อมูลใหม่ทุกครั้งที่ผู้ใช้เปลี่ยนแท็บ */
    private void bindTabs(TabPane tabPane, Refreshable... controllers) {
        for (int i = 0; i < controllers.length; i++) {
            tabPane.getTabs().get(i).setUserData(controllers[i]);
        }
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> refreshTab(newTab));
    }

    private void refreshTab(Tab tab) {
        if (tab != null && tab.getUserData() instanceof Refreshable refreshable) {
            refreshable.refresh();
        }
    }

    /**
     * เข้าสู่ระบบด้วยผู้ใช้ที่เลือก
     * Polymorphism: user.getRoleName() คืนค่า "พนักงานแคชเชียร์" หรือ "ผู้ดูแลระบบ"
     * ตามชนิดจริงของออบเจกต์ (Cashier / Administrator) ที่ override เมธอดนี้ไว้
     */
    public void login(SystemUser user) {
        userLabel.setText(user.getRoleName() + ": " + user.getName() + " (" + user.getUserId() + ")");
        logoutButton.setVisible(true);
        TabPane tabs = (user instanceof Cashier) ? cashierTabs : adminTabs;
        root.setCenter(tabs);
        tabs.getSelectionModel().select(0);
        refreshTab(tabs.getSelectionModel().getSelectedItem());
    }

    /** ออกจากระบบ กลับหน้าแรก */
    @FXML
    public void logout() {
        userLabel.setText("ยังไม่ได้เข้าสู่ระบบ");
        logoutButton.setVisible(false);
        root.setCenter(loginView);
    }
}
