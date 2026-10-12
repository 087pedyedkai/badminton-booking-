package com.example.badminton.gui.cashier;

import com.example.badminton.gui.Refreshable;
import com.example.badminton.gui.UiHelper;
import com.example.badminton.model.Member;
import com.example.badminton.model.MembershipPackage;
import com.example.badminton.service.MemberService;
import com.example.badminton.service.PaymentService;
import com.example.badminton.util.FormatHelper;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;

/**
 * แท็บ "แพ็กเกจชั่วโมง": ตรวจสอบชั่วโมงคงเหลือ และขายแพ็กเกจ 15 ชม. ราคา 2,600 บาท
 * - ตรวจสอบ: Member.getPackages() / getRemainingPackageHours()
 * - ขาย:     PaymentService.sellPackage() (บันทึกรายได้หมวด "ขายแพ็กเกจ" ครั้งเดียวตอนซื้อ)
 */
public class PackagePanel extends JPanel implements Refreshable {

    private final MemberService memberService;
    private final PaymentService paymentService;

    private final JTextField memberIdField = new JTextField(10);
    private final JLabel memberLabel = new JLabel("กรอกรหัสสมาชิกแล้วกด \"ตรวจสอบ\"");
    private final JLabel remainingLabel = new JLabel("-");
    private final DefaultTableModel tableModel = UiHelper.readOnlyModel(
            "รหัสแพ็กเกจ", "วันที่ซื้อ", "ชั่วโมงทั้งหมด", "ใช้ไป", "คงเหลือ");
    private final JTextField slipField = new JTextField(18);
    private final JButton buyButton = UiHelper.primaryButton("ซื้อแพ็กเกจ");
    private Member currentMember;

    public PackagePanel(MemberService memberService, PaymentService paymentService) {
        super(new BorderLayout(10, 10));
        this.memberService = memberService;
        this.paymentService = paymentService;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- แถบค้นหาสมาชิก ----
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton findButton = new JButton("ตรวจสอบ");
        findButton.addActionListener(e -> lookupMember());
        memberIdField.addActionListener(e -> lookupMember());
        topBar.add(new JLabel("รหัสสมาชิก:"));
        topBar.add(memberIdField);
        topBar.add(findButton);
        topBar.add(memberLabel);

        // ---- ส่วนแสดงชั่วโมงคงเหลือ + ตารางแพ็กเกจ ----
        remainingLabel.setFont(UiHelper.font(Font.BOLD, 28));
        remainingLabel.setForeground(UiHelper.PRIMARY_DARK);
        JPanel remainingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        remainingPanel.add(new JLabel("ชั่วโมงแพ็กเกจคงเหลือรวม: "));
        remainingPanel.add(remainingLabel);
        JPanel center = new JPanel(new BorderLayout(6, 6));
        center.add(remainingPanel, BorderLayout.NORTH);
        center.add(UiHelper.scroll(UiHelper.table(tableModel)), BorderLayout.CENTER);

        // ---- ส่วนขายแพ็กเกจ ----
        JPanel buyForm = new JPanel(new GridBagLayout());
        JLabel priceLabel = new JLabel(UiHelper.html("<b>แพ็กเกจ " + MembershipPackage.PACKAGE_HOURS + " ชั่วโมง</b>\nราคา "
                + FormatHelper.money(MembershipPackage.PACKAGE_PRICE)
                + "\n(เฉลี่ย " + FormatHelper.money(MembershipPackage.PACKAGE_PRICE / MembershipPackage.PACKAGE_HOURS)
                + "/ชม. จากปกติ 180 บาท)"));
        priceLabel.setFont(UiHelper.font(Font.PLAIN, 15));
        UiHelper.addFormWide(buyForm, 0, priceLabel);
        UiHelper.addFormRow(buyForm, 1, "เลขอ้างอิงสลิป:", slipField);
        UiHelper.addFormWide(buyForm, 2, buyButton);
        buyButton.addActionListener(e -> buyPackage());
        buyButton.setEnabled(false);
        JPanel buyWrapper = new JPanel(new BorderLayout());
        buyWrapper.add(UiHelper.section("ขายแพ็กเกจ", buyForm), BorderLayout.NORTH);

        add(topBar, BorderLayout.NORTH);
        add(UiHelper.section("แพ็กเกจของสมาชิก", center), BorderLayout.CENTER);
        add(buyWrapper, BorderLayout.EAST);
    }

    private void lookupMember() {
        String memberId = memberIdField.getText().trim();
        currentMember = memberId.isEmpty() ? null : memberService.findById(memberId);
        showMember();
        if (currentMember == null && !memberId.isEmpty()) {
            UiHelper.showError(this, "ไม่พบสมาชิกรหัส " + memberId + " (ต้องสมัครสมาชิกก่อนจึงจะซื้อแพ็กเกจได้)");
        }
    }

    /** แสดงข้อมูลแพ็กเกจของสมาชิกปัจจุบัน */
    private void showMember() {
        tableModel.setRowCount(0);
        buyButton.setEnabled(currentMember != null);
        if (currentMember == null) {
            memberLabel.setText("กรอกรหัสสมาชิกแล้วกด \"ตรวจสอบ\"");
            memberLabel.setForeground(UiHelper.MUTED);
            remainingLabel.setText("-");
            return;
        }
        memberLabel.setText(currentMember.toString());
        memberLabel.setForeground(UiHelper.PRIMARY_DARK);
        remainingLabel.setText(currentMember.getRemainingPackageHours() + " ชั่วโมง");
        for (MembershipPackage p : currentMember.getPackages()) {
            tableModel.addRow(new Object[]{
                    p.getPackageId(), p.getPurchaseDate(), p.getTotalHours(), p.getUsedHours(), p.getRemainingHours()
            });
        }
    }

    private void buyPackage() {
        if (currentMember.getFreePackagesAvailable() > 0) { // มีสิทธิ์ -> รับฟรี ไม่ต้องใช้สลิป
            if (UiHelper.confirm(this, "รับแพ็กเกจฟรี", "สมาชิกมีสิทธิ์รับแพ็กเกจ "
                    + MembershipPackage.PACKAGE_HOURS + " ชม. ฟรี\nให้ " + currentMember.getFullName() + " ?")) {
                MembershipPackage free = paymentService.claimFreePackage(currentMember);
                showMember();
                UiHelper.showInfo(this, "รับแพ็กเกจฟรีสำเร็จ", "ได้รับแพ็กเกจฟรี: " + free.getPackageId());
            }
            return;
        }
        String slip = slipField.getText().trim();
        if (slip.isEmpty()) {
            UiHelper.showError(this, "กรุณากรอกเลขอ้างอิงสลิป หรือพิมพ์ CASH ถ้าชำระเงินสด");
            return;
        }
        if (!UiHelper.confirm(this, "ยืนยันการซื้อแพ็กเกจ", "ขายแพ็กเกจ " + MembershipPackage.PACKAGE_HOURS
                + " ชม. ราคา " + FormatHelper.money(MembershipPackage.PACKAGE_PRICE)
                + "\nให้ " + currentMember.getFullName() + " ?")) {
            return;
        }
        try {
            MembershipPackage newPackage = paymentService.sellPackage(currentMember, slip);
            slipField.setText("");
            showMember();
            UiHelper.showInfo(this, "ซื้อแพ็กเกจสำเร็จ", "ซื้อแพ็กเกจสำเร็จ: " + newPackage.getPackageId()
                    + "\nชั่วโมงคงเหลือรวม " + currentMember.getRemainingPackageHours() + " ชั่วโมง");
        } catch (IllegalArgumentException e) {
            UiHelper.showError(this, e.getMessage());
        }
    }

    @Override
    public void refresh() {
        showMember(); // ชั่วโมงอาจถูกใช้ไปจากแท็บจองสนาม
    }
}
