package com.example.badminton.fx;

import com.example.badminton.BadmintonSystem;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;

/**
 * Controller ของหน้าเข้าสู่ระบบ (login.fxml) - เทียบเท่าเมนูหลักของเวอร์ชัน Console
 * ปุ่ม: เข้าสู่ระบบแคชเชียร์ / เข้าสู่ระบบผู้ดูแลระบบ / สมัครสมาชิก / ออกจากโปรแกรม
 *
 * หมายเหตุ: เป็นการ "จำลอง" การเข้าสู่ระบบ (กดปุ่มเลือกบทบาทได้เลย ไม่มีรหัสผ่าน)
 * เพราะเป็นโปรแกรมเพื่อการเรียนรู้ ถ้าใช้งานจริงควรเพิ่มการยืนยันตัวตน
 */
public class LoginController {

    private final BadmintonSystem system;
    private MainController mainController;

    @FXML private Parent root;

    public LoginController(BadmintonSystem system) {
        this.system = system;
    }

    /** MainController ส่งตัวเองมาให้ เพื่อให้ปุ่มเข้าสู่ระบบสั่งเปิดหน้าจอของบทบาทนั้นได้ */
    void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // ส่งออบเจกต์ผู้ใช้ (ชนิด SystemUser) ไปให้ MainController เปิดหน้าจอของบทบาทนั้น
    @FXML
    private void loginCashier() {
        mainController.login(system.getCashier());
    }

    @FXML
    private void loginAdmin() {
        mainController.login(system.getAdministrator());
    }

    @FXML
    private void register() {
        RegisterMemberController.showDialog(root, system);
    }

    @FXML
    private void exit() {
        Platform.exit();
    }
}
