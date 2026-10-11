package com.example.badminton.fx;

import com.example.badminton.BadmintonSystem;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * จุดเริ่มต้นของโปรแกรมแบบ "หน้าต่าง GUI" ด้วย JavaFX
 * (เวอร์ชัน Console ยังใช้ได้ที่ com.example.badminton.Main และ Swing ที่ gui.BadmintonApp)
 *
 * ความสัมพันธ์: BadmintonFxApp -> สร้าง BadmintonSystem (Service + ข้อมูลตัวอย่าง) ตัวเดียว
 *              -> FxHelper.load("main.fxml", system) สร้าง MainController และหน้าจอทุกแท็บ
 *                 โดยทุก Controller ได้ BadmintonSystem ตัวเดียวกันนี้ ข้อมูลจึงเชื่อมกันทั้งหมด
 *
 * โครงสร้างแบบ JavaFX: หน้าตาอยู่ในไฟล์ .fxml (resources) + การทำงานอยู่ใน Controller + สีอยู่ใน style.css
 */
public class BadmintonFxApp extends Application {

    /**
     * JavaFX เรียก start() บน "JavaFX Application Thread" ให้เอง
     * (เทียบกับ Swing ที่ต้องเรียก SwingUtilities.invokeLater() เอง)
     */
    @Override
    public void start(Stage stage) {
        FxHelper.pickThaiFont();
        BadmintonSystem system = new BadmintonSystem();

        FXMLLoader loader = FxHelper.load("main.fxml", system);
        Parent root = loader.getRoot();
        FxHelper.applyTheme(root);

        stage.setTitle("ระบบจัดการการจองสนามแบดมินตัน");
        stage.setScene(new Scene(root, 1280, 800));
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.centerOnScreen(); // เปิดกลางจอ
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
