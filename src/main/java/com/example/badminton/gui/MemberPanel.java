package com.example.badminton.gui;

import com.example.badminton.model.Member;
import com.example.badminton.service.MemberService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * หน้าจอข้อมูลสมาชิก (ใช้ร่วมกันทั้งแคชเชียร์และผู้ดูแลระบบ)
 * - ตารางรายชื่อสมาชิกทั้งหมด พิมพ์ค้นหาด้วยรหัสหรือชื่อแล้วกรองทันที
 * - คลิกแถว -> ดูข้อมูลละเอียดของสมาชิก (Member.getInfoText())
 * - ปุ่ม "สมัครสมาชิกใหม่" เปิด RegisterMemberDialog
 */
public class MemberPanel extends JPanel implements Refreshable {

    private final MemberService memberService;
    private final JTextField filterField = new JTextField(18);
    private final DefaultTableModel tableModel = UiHelper.readOnlyModel(
            "รหัส", "ชื่อ-นามสกุล", "เบอร์โทร", "วันที่สมัคร", "จำนวนการจอง", "ชั่วโมงสะสม", "แพ็กเกจคงเหลือ (ชม.)");
    private final JTable table = UiHelper.table(tableModel);
    private final JTextArea detailsArea = UiHelper.outputArea();
    private List<Member> shownMembers = new ArrayList<>();

    public MemberPanel(MemberService memberService) {
        super(new BorderLayout(8, 8));
        this.memberService = memberService;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton registerButton = UiHelper.primaryButton("+ สมัครสมาชิกใหม่");
        registerButton.addActionListener(e -> registerMember());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        topBar.add(new JLabel("ค้นหา (รหัส/ชื่อ):"));
        topBar.add(filterField);
        topBar.add(registerButton);

        // พิมพ์ในช่องค้นหา -> กรองตารางทันที
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                refresh();
            }

            public void removeUpdate(DocumentEvent e) {
                refresh();
            }

            public void changedUpdate(DocumentEvent e) {
                refresh();
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow >= 0) {
                detailsArea.setText(shownMembers.get(table.convertRowIndexToModel(viewRow)).getInfoText());
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                UiHelper.scroll(table), UiHelper.section("ข้อมูลสมาชิก", UiHelper.scroll(detailsArea)));
        split.setResizeWeight(0.7);

        add(topBar, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        refresh();
    }

    private void registerMember() {
        Member member = RegisterMemberDialog.showDialog(this, memberService);
        if (member != null) {
            filterField.setText(member.getMemberId()); // กรองให้เห็นสมาชิกใหม่ทันที
            refresh();
        }
    }

    /** โหลดรายชื่อสมาชิกใหม่ (กรองตามคำค้นหาถ้ามี) */
    @Override
    public void refresh() {
        String keyword = filterField.getText().trim().toLowerCase();
        shownMembers = new ArrayList<>();
        for (Member member : memberService.getAllMembers()) {
            if (keyword.isEmpty()
                    || member.getMemberId().toLowerCase().contains(keyword)
                    || member.getFullName().toLowerCase().contains(keyword)) {
                shownMembers.add(member);
            }
        }
        tableModel.setRowCount(0);
        for (Member m : shownMembers) {
            tableModel.addRow(new Object[]{
                    m.getMemberId(), m.getFullName(), m.getPhoneNumber(), m.getRegisterDate(),
                    m.getBookings().size(), m.getQualifyingHours(), m.getRemainingPackageHours()
            });
        }
        detailsArea.setText("");
    }
}
