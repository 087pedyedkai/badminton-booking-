package com.example.badminton.fx.admin;

import com.example.badminton.BadmintonSystem;
import com.example.badminton.fx.Refreshable;
import com.example.badminton.model.Member;
import com.example.badminton.report.Reportable;
import com.example.badminton.service.MemberService;
import com.example.badminton.service.ReportService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller แท็บ "ประวัติการจองของสมาชิก" ของผู้ดูแลระบบ (admin/member-history.fxml)
 * Service ที่เรียก: MemberService.getAllMembers(), ReportService.createMemberHistoryReport()
 *
 * ตัวอย่าง Polymorphism ผ่าน interface:
 * reportService.createMemberHistoryReport() คืนค่าเป็นชนิด Reportable
 * หน้าจอนี้เรียกแค่ report.generateReport() โดยไม่ต้องรู้ว่าข้างในเป็น MemberHistoryReport
 */
public class MemberHistoryController implements Refreshable {

    private final MemberService memberService;
    private final ReportService reportService;

    @FXML private ComboBox<String> memberCombo;
    @FXML private TextArea reportArea;

    private List<Member> members = new ArrayList<>();

    public MemberHistoryController(BadmintonSystem system) {
        this.memberService = system.getMemberService();
        this.reportService = system.getReportService();
    }

    @FXML
    private void initialize() {
        refresh();
    }

    @FXML
    private void showHistory() {
        int index = memberCombo.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= members.size()) {
            reportArea.setText("");
            return;
        }
        Reportable report = reportService.createMemberHistoryReport(members.get(index));
        reportArea.setText(report.generateReport());
        reportArea.positionCaret(0);
    }

    /** โหลดรายชื่อสมาชิกใหม่ (อาจมีสมาชิกสมัครเพิ่ม) โดยพยายามคงสมาชิกที่เลือกไว้ (ไม่มีให้เลือกคนแรก) */
    @Override
    public void refresh() {
        int previous = memberCombo.getSelectionModel().getSelectedIndex();
        members = memberService.getAllMembers();
        List<String> names = new ArrayList<>();
        for (Member m : members) {
            names.add(m.getMemberId() + " - " + m.getFullName());
        }
        memberCombo.getItems().setAll(names);
        if (previous >= 0 && previous < members.size()) {
            memberCombo.getSelectionModel().select(previous);
        } else if (!members.isEmpty()) {
            memberCombo.getSelectionModel().select(0);
        }
        showHistory();
    }
}
