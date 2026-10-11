package com.example.badminton.fx;

import com.example.badminton.BadmintonSystem;
import com.example.badminton.model.Member;
import com.example.badminton.service.MemberService;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller หน้าจอข้อมูลสมาชิก (member.fxml) ใช้ร่วมกันทั้งแคชเชียร์และผู้ดูแลระบบ
 * - ตารางรายชื่อสมาชิกทั้งหมด พิมพ์ค้นหาด้วยรหัสหรือชื่อแล้วกรองทันที
 * - คลิกแถว -> ดูข้อมูลละเอียดของสมาชิก (Member.getInfoText())
 * - ปุ่ม "สมัครสมาชิกใหม่" เปิดหน้าต่างสมัครสมาชิก (RegisterMemberController)
 *
 * Service ที่เรียก: MemberService.getAllMembers()
 */
public class MemberController implements Refreshable {

    private final BadmintonSystem system;
    private final MemberService memberService;

    @FXML private Parent root;
    @FXML private TextField filterField;
    @FXML private TableView<Member> table;
    @FXML private TextArea detailsArea;

    public MemberController(BadmintonSystem system) {
        this.system = system;
        this.memberService = system.getMemberService();
    }

    @FXML
    private void initialize() {
        FxHelper.setColumnValues(table,
                Member::getMemberId, Member::getFullName, Member::getPhoneNumber, Member::getRegisterDate,
                m -> m.getBookings().size(), Member::getQualifyingHours, Member::getRemainingPackageHours);

        // พิมพ์ในช่องค้นหา -> กรองตารางทันที
        filterField.textProperty().addListener((obs, oldText, newText) -> refresh());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldMember, member) -> {
            if (member != null) {
                detailsArea.setText(member.getInfoText());
            }
        });
        refresh();
    }

    @FXML
    private void registerMember() {
        Member member = RegisterMemberController.showDialog(root, system);
        if (member != null) {
            filterField.setText(member.getMemberId()); // กรองให้เห็นสมาชิกใหม่ทันที
            refresh();
        }
    }

    /** โหลดรายชื่อสมาชิกใหม่ (กรองตามคำค้นหาถ้ามี) */
    @Override
    public void refresh() {
        String keyword = filterField.getText().trim().toLowerCase();
        List<Member> shownMembers = new ArrayList<>();
        for (Member member : memberService.getAllMembers()) {
            if (keyword.isEmpty()
                    || member.getMemberId().toLowerCase().contains(keyword)
                    || member.getFullName().toLowerCase().contains(keyword)) {
                shownMembers.add(member);
            }
        }
        table.getItems().setAll(shownMembers);
        detailsArea.setText("");
    }
}
