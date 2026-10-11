package com.example.badminton.fx;

import com.example.badminton.BadmintonSystem;
import com.example.badminton.model.Member;
import com.example.badminton.service.MemberService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller ของหน้าต่างสมัครสมาชิก (register-member.fxml)
 * เปิดได้ทั้งจากหน้าเข้าสู่ระบบ และแท็บ "สมาชิก"
 *
 * การทำงาน: กรอกชื่อและเบอร์โทร -> กด "สมัครสมาชิก"
 * -> เรียก MemberService.registerMember() ซึ่งตรวจข้อมูลและออกรหัสสมาชิกที่ไม่ซ้ำ
 * -> ถ้าข้อมูลผิด (เบอร์ไม่ใช่ตัวเลข 9-10 หลัก หรือเบอร์ซ้ำ) จะแสดงข้อความสีแดง และยังไม่ปิดหน้าต่าง
 */
public class RegisterMemberController {

    private final MemberService memberService;
    private Member registeredMember; // สมาชิกที่สมัครสำเร็จ (null ถ้ากดยกเลิก)

    @FXML private Parent root;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private Label errorLabel;

    public RegisterMemberController(BadmintonSystem system) {
        this.memberService = system.getMemberService();
    }

    @FXML
    private void register() {
        try {
            registeredMember = memberService.registerMember(nameField.getText(), phoneField.getText().trim());
            FxHelper.showInfo(root, "สมัครสมาชิกสำเร็จ",
                    "สมัครสมาชิกสำเร็จ!\nรหัสสมาชิกคือ " + registeredMember.getMemberId() + "\n\n"
                            + registeredMember.getInfoText());
            close();
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        close();
    }

    private void close() {
        ((Stage) root.getScene().getWindow()).close();
    }

    /**
     * เปิดหน้าต่างสมัครสมาชิก แล้วรอจนกว่าจะปิด
     * คืนค่า Member ที่สมัครสำเร็จ หรือ null ถ้ายกเลิก
     */
    public static Member showDialog(Node owner, BadmintonSystem system) {
        FXMLLoader loader = FxHelper.load("register-member.fxml", system);
        Parent root = loader.getRoot();
        FxHelper.applyTheme(root);

        Stage stage = new Stage();
        stage.setTitle("สมัครสมาชิกใหม่");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner.getScene().getWindow());
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait(); // หน้าต่างแบบ modal: โค้ดจะหยุดรอที่บรรทัดนี้จนกว่าจะปิดหน้าต่าง

        RegisterMemberController controller = loader.getController();
        return controller.registeredMember;
    }
}
