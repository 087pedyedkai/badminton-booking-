// โมดูลของโปรแกรมจองสนามแบดมินตัน
module com.example.badminton {
    // java.desktop คือโมดูลของ Swing (หน้าจอ GUI แบบเดิมในแพ็กเกจ gui ยังใช้อยู่)
    requires java.desktop;

    // JavaFX: javafx.controls = ปุ่ม ตาราง แท็บ ฯลฯ, javafx.fxml = โหลดหน้าจอจากไฟล์ .fxml
    requires javafx.controls;
    requires javafx.fxml;

    // ให้ JavaFX (javafx.graphics) สร้างออบเจกต์ BadmintonFxApp ซึ่งเป็นคลาส Application ได้
    exports com.example.badminton.fx;

    // ให้ FXMLLoader เข้าถึง Controller และฟิลด์ @FXML (private) ในแพ็กเกจเหล่านี้ได้
    opens com.example.badminton.fx to javafx.fxml;
    opens com.example.badminton.fx.cashier to javafx.fxml;
    opens com.example.badminton.fx.admin to javafx.fxml;
}
