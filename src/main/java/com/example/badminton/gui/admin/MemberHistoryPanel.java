package com.example.badminton.gui.admin;

import com.example.badminton.gui.Refreshable;
import com.example.badminton.gui.UiHelper;
import com.example.badminton.model.Member;
import com.example.badminton.report.Reportable;
import com.example.badminton.service.MemberService;
import com.example.badminton.service.ReportService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * แท็บ "ประวัติการจองของสมาชิก" (ผู้ดูแลระบบ)
 *
 * ตัวอย่าง Polymorphism ผ่าน interface:
 * reportService.createMemberHistoryReport() คืนค่าเป็นชนิด Reportable
 * หน้าจอนี้เรียกแค่ report.generateReport() โดยไม่ต้องรู้ว่าข้างในเป็น MemberHistoryReport
 */
public class MemberHistoryPanel extends JPanel implements Refreshable {

    private final MemberService memberService;
    private final ReportService reportService;
    private final JComboBox<String> memberCombo = new JComboBox<>();
    private final JTextArea reportArea = UiHelper.outputArea();
    private List<Member> members;

    public MemberHistoryPanel(MemberService memberService, ReportService reportService) {
        super(new BorderLayout(8, 8));
        this.memberService = memberService;
        this.reportService = reportService;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton showButton = UiHelper.primaryButton("แสดงประวัติ");
        showButton.addActionListener(e -> showHistory());
        memberCombo.addActionListener(e -> showHistory());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        topBar.add(new JLabel("เลือกสมาชิก:"));
        topBar.add(memberCombo);
        topBar.add(showButton);
        reportArea.setLineWrap(false);

        add(topBar, BorderLayout.NORTH);
        add(UiHelper.section("ประวัติการจอง", UiHelper.scroll(reportArea)), BorderLayout.CENTER);
        refresh();
    }

    private void showHistory() {
        int index = memberCombo.getSelectedIndex();
        if (index < 0) {
            reportArea.setText("");
            return;
        }
        Reportable report = reportService.createMemberHistoryReport(members.get(index));
        reportArea.setText(report.generateReport());
        reportArea.setCaretPosition(0);
    }

    /** โหลดรายชื่อสมาชิกใหม่ (อาจมีสมาชิกสมัครเพิ่ม) โดยพยายามคงสมาชิกที่เลือกไว้ */
    @Override
    public void refresh() {
        int previous = memberCombo.getSelectedIndex();
        members = memberService.getAllMembers();
        memberCombo.removeAllItems();
        for (Member m : members) {
            memberCombo.addItem(m.getMemberId() + " - " + m.getFullName());
        }
        if (previous >= 0 && previous < members.size()) {
            memberCombo.setSelectedIndex(previous);
        }
        showHistory();
    }
}
