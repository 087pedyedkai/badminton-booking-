package com.example.badminton.fx.cashier;

import com.example.badminton.BadmintonSystem;
import com.example.badminton.fx.FxHelper;
import com.example.badminton.fx.Refreshable;
import com.example.badminton.model.Member;
import com.example.badminton.model.MembershipPackage;
import com.example.badminton.service.MemberService;
import com.example.badminton.service.PaymentService;
import com.example.badminton.util.FormatHelper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controller แท็บ "แพ็กเกจชั่วโมง" (cashier/package.fxml):
 * ตรวจสอบชั่วโมงคงเหลือ และขายแพ็กเกจ 15 ชม. ราคา 2,600 บาท
 * - ตรวจสอบ: MemberService.findById() -> Member.getPackages() / getRemainingPackageHours()
 * - ขาย:     PaymentService.sellPackage() (บันทึกรายได้หมวด "ขายแพ็กเกจ" ครั้งเดียวตอนซื้อ)
 */
public class PackageController implements Refreshable {

    private final MemberService memberService;
    private final PaymentService paymentService;

    @FXML private Parent root;
    @FXML private TextField memberIdField;
    @FXML private Label memberLabel;
    @FXML private Label remainingLabel;
    @FXML private TableView<MembershipPackage> table;
    @FXML private Label packageTitleLabel;
    @FXML private Label priceLabel;
    @FXML private TextField slipField;
    @FXML private Button buyButton;

    private Member currentMember;

    public PackageController(BadmintonSystem system) {
        this.memberService = system.getMemberService();
        this.paymentService = system.getPaymentService();
    }

    @FXML
    private void initialize() {
        FxHelper.setColumnValues(table,
                MembershipPackage::getPackageId, MembershipPackage::getPurchaseDate, MembershipPackage::getTotalHours,
                MembershipPackage::getUsedHours, MembershipPackage::getRemainingHours);

        packageTitleLabel.setText("แพ็กเกจ " + MembershipPackage.PACKAGE_HOURS + " ชั่วโมง");
        priceLabel.setText("ราคา " + FormatHelper.money(MembershipPackage.PACKAGE_PRICE)
                + "\n(เฉลี่ย " + FormatHelper.money(MembershipPackage.PACKAGE_PRICE / MembershipPackage.PACKAGE_HOURS)
                + "/ชม. จากปกติ 180 บาท)");
    }

    @FXML
    private void lookupMember() {
        String memberId = memberIdField.getText().trim();
        currentMember = memberId.isEmpty() ? null : memberService.findById(memberId);
        showMember();
        if (currentMember == null && !memberId.isEmpty()) {
            FxHelper.showError(root, "ไม่พบสมาชิกรหัส " + memberId + " (ต้องสมัครสมาชิกก่อนจึงจะซื้อแพ็กเกจได้)");
        }
    }

    /** แสดงข้อมูลแพ็กเกจของสมาชิกปัจจุบัน */
    private void showMember() {
        table.getItems().clear();
        buyButton.setDisable(currentMember == null);
        if (currentMember == null) {
            memberLabel.setText("กรอกรหัสสมาชิกแล้วกด \"ตรวจสอบ\"");
            FxHelper.setTone(memberLabel, FxHelper.MUTED);
            remainingLabel.setText("-");
            return;
        }
        memberLabel.setText(currentMember.toString());
        FxHelper.setTone(memberLabel, FxHelper.OK);
        remainingLabel.setText(currentMember.getRemainingPackageHours() + " ชั่วโมง");
        table.getItems().setAll(currentMember.getPackages());
    }

    @FXML
    private void buyPackage() {
        if (currentMember.getFreePackagesAvailable() > 0) { // มีสิทธิ์ -> รับฟรี ไม่ต้องใช้สลิป
            if (FxHelper.confirm(root, "รับแพ็กเกจฟรี", "สมาชิกมีสิทธิ์รับแพ็กเกจ "
                    + MembershipPackage.PACKAGE_HOURS + " ชม. ฟรี\nให้ " + currentMember.getFullName() + " ?")) {
                MembershipPackage free = paymentService.claimFreePackage(currentMember);
                showMember();
                FxHelper.showInfo(root, "รับแพ็กเกจฟรีสำเร็จ", "ได้รับแพ็กเกจฟรี: " + free.getPackageId());
            }
            return;
        }
        String slip = slipField.getText().trim();
        if (slip.isEmpty()) {
            FxHelper.showError(root, "กรุณากรอกเลขอ้างอิงสลิป หรือพิมพ์ CASH ถ้าชำระเงินสด");
            return;
        }
        if (!FxHelper.confirm(root, "ยืนยันการซื้อแพ็กเกจ", "ขายแพ็กเกจ " + MembershipPackage.PACKAGE_HOURS
                + " ชม. ราคา " + FormatHelper.money(MembershipPackage.PACKAGE_PRICE)
                + "\nให้ " + currentMember.getFullName() + " ?")) {
            return;
        }
        try {
            MembershipPackage newPackage = paymentService.sellPackage(currentMember, slip);
            slipField.setText("");
            showMember();
            FxHelper.showInfo(root, "ซื้อแพ็กเกจสำเร็จ", "ซื้อแพ็กเกจสำเร็จ: " + newPackage.getPackageId()
                    + "\nชั่วโมงคงเหลือรวม " + currentMember.getRemainingPackageHours() + " ชั่วโมง");
        } catch (IllegalArgumentException e) {
            FxHelper.showError(root, e.getMessage());
        }
    }

    @Override
    public void refresh() {
        showMember(); // ชั่วโมงอาจถูกใช้ไปจากแท็บจองสนาม
    }
}
