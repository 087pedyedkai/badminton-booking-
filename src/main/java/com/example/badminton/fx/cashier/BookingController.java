package com.example.badminton.fx.cashier;

import com.example.badminton.BadmintonSystem;
import com.example.badminton.fx.DateBox;
import com.example.badminton.fx.FxHelper;
import com.example.badminton.fx.Refreshable;
import com.example.badminton.model.Booking;
import com.example.badminton.model.Member;
import com.example.badminton.model.ShoeRental;
import com.example.badminton.service.BookingService;
import com.example.badminton.service.MemberService;
import com.example.badminton.service.PaymentService;
import com.example.badminton.util.FormatHelper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller แท็บ "จองสนาม" ของแคชเชียร์ (cashier/booking.fxml)
 *
 * หน้าจอแบ่ง 2 ส่วน:
 * - ซ้าย: ตารางสถานะสนาม (CourtScheduleGrid) ของวันที่เลือก
 * - ขวา: ฟอร์มจอง (สมาชิก สนาม เวลา ชั่วโมง แพ็กเกจ รองเท้า สลิป) + สรุปราคาแบบอัปเดตทันที
 *
 * ขั้นตอนการจองเหมือนเวอร์ชัน Console ทุกอย่าง และใช้ Service ชุดเดียวกัน:
 * 1) MemberService.findById()          -> ต้องเป็นสมาชิก
 * 2) BookingService.validateBooking()  -> ตรวจเวลาทำการ / ห้ามย้อนหลัง / สนามว่าง
 * 3) PaymentService.calculateCourtFee()-> คำนวณค่าสนามพร้อมส่วนลดชั่วโมงที่ 20 (countDiscountedHours)
 * 4) BookingService.createBooking()    -> บันทึก, หักแพ็กเกจ หรือเพิ่มชั่วโมงสะสม
 * 5) PaymentService.payCourtFee() และ rentShoes() -> บันทึกรายได้
 */
public class BookingController implements Refreshable {

    private final MemberService memberService;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    @FXML private Parent root;
    @FXML private DateBox dateBox;
    @FXML private Label openingHoursLabel;
    @FXML private CourtScheduleGrid scheduleGrid;

    @FXML private TextField memberIdField;
    @FXML private Label memberInfoLabel;
    @FXML private ComboBox<Integer> courtCombo;
    @FXML private ComboBox<String> startTimeCombo;
    @FXML private Spinner<Integer> hoursSpinner;
    @FXML private CheckBox usePackageCheck;
    @FXML private Spinner<Integer> shoeSpinner;
    @FXML private TextField slipField;
    @FXML private VBox summaryBox;
    @FXML private Button confirmButton;

    private Member currentMember;    // สมาชิกที่ตรวจสอบแล้ว (null = ยังไม่ได้เลือก)
    private boolean updatingCombo;   // กันไม่ให้ event ทำงานซ้อนระหว่างเติมรายการใน combo

    public BookingController(BadmintonSystem system) {
        this.memberService = system.getMemberService();
        this.bookingService = system.getBookingService();
        this.paymentService = system.getPaymentService();
    }

    @FXML
    private void initialize() {
        dateBox.setDate(LocalDate.now().plusDays(1));
        scheduleGrid.setBookingService(bookingService);
        scheduleGrid.setSlotClickListener(this::onSlotClicked);

        for (int i = 1; i <= BookingService.TOTAL_COURTS; i++) {
            courtCombo.getItems().add(i);
        }
        courtCombo.getSelectionModel().select(0);
        hoursSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 11, 1));
        shoeSpinner.setValueFactory(new IntegerSpinnerValueFactory(0, ShoeRental.MAX_PAIRS, 0));

        // เมื่อค่าในฟอร์มเปลี่ยน ให้คำนวณสรุปราคาใหม่ทันที
        dateBox.addChangeListener(this::onDateChanged);
        courtCombo.setOnAction(e -> updatePreview());
        startTimeCombo.setOnAction(e -> updatePreview());
        hoursSpinner.valueProperty().addListener((obs, oldValue, newValue) -> updatePreview());
        usePackageCheck.setOnAction(e -> updatePreview()); // onAction = เฉพาะตอนผู้ใช้คลิก
        shoeSpinner.valueProperty().addListener((obs, oldValue, newValue) -> updatePreview());

        onDateChanged();
    }

    // =====================================================================
    // การตอบสนองต่อผู้ใช้
    // =====================================================================

    /** เปลี่ยนวันที่ -> อัปเดตเวลาทำการ รายการเวลาเริ่ม และตารางสนาม */
    private void onDateChanged() {
        LocalDate date = dateBox.getDateOrNull();
        if (date == null) {
            openingHoursLabel.setText("รูปแบบวันที่ไม่ถูกต้อง (ปี-เดือน-วัน)");
            FxHelper.setTone(openingHoursLabel, FxHelper.ERROR);
            updatePreview();
            return;
        }
        LocalTime open = bookingService.getOpeningTime(date);
        LocalTime close = bookingService.getClosingTime(date);
        FxHelper.setTone(openingHoursLabel, FxHelper.OK);
        openingHoursLabel.setText("วัน" + FormatHelper.thaiDayName(date)
                + (bookingService.isPublicHoliday(date) ? " (วันหยุดนักขัตฤกษ์)" : "")
                + "  เปิด " + open + " - " + close);

        // เติมรายการเวลาเริ่มตามเวลาทำการของวันนั้น (พยายามคงเวลาที่เลือกไว้เดิม ถ้าไม่มีเลือกตัวแรก)
        String previous = startTimeCombo.getValue();
        updatingCombo = true;
        List<String> times = new ArrayList<>();
        for (int hour = open.getHour(); hour < close.getHour(); hour++) {
            times.add(String.format("%02d:00", hour));
        }
        startTimeCombo.getItems().setAll(times);
        if (previous != null && times.contains(previous)) {
            startTimeCombo.setValue(previous);
        } else {
            startTimeCombo.getSelectionModel().select(0);
        }
        updatingCombo = false;

        scheduleGrid.showDate(date);
        updatePreview();
    }

    /** คลิกช่องในตารางสนาม -> เลือกสนามและเวลาเริ่มให้ในฟอร์ม */
    private void onSlotClicked(int court, int hour) {
        courtCombo.setValue(court);
        String time = String.format("%02d:00", hour);
        if (startTimeCombo.getItems().contains(time)) { // เวลานอกเวลาทำการเลือกไม่ได้ (เหมือน Swing)
            startTimeCombo.setValue(time);
        }
        updatePreview();
    }

    /** ค้นหาสมาชิกจากรหัสที่กรอก */
    @FXML
    private void lookupMember() {
        String memberId = memberIdField.getText().trim();
        currentMember = memberId.isEmpty() ? null : memberService.findById(memberId);
        if (currentMember == null) {
            FxHelper.setTone(memberInfoLabel, FxHelper.ERROR);
            memberInfoLabel.setText(memberId.isEmpty() ? "กรุณากรอกรหัสสมาชิก"
                    : "ไม่พบสมาชิก " + memberId + " (ต้องสมัครสมาชิกก่อน)");
        } else {
            FxHelper.setTone(memberInfoLabel, FxHelper.OK);
            memberInfoLabel.setText(currentMember.getFullName()
                    + "\nชั่วโมงสะสม: " + currentMember.getQualifyingHours() + " ชม."
                    + " | แพ็กเกจคงเหลือ: " + currentMember.getRemainingPackageHours() + " ชม.");
        }
        updatePreview();
    }

    /** อ่านชั่วโมงเริ่มจาก combo เช่น "13:00" -> 13 (คืน -1 ถ้ายังไม่ได้เลือก) */
    private int getSelectedStartHour() {
        String item = startTimeCombo.getValue();
        if (item == null) {
            return -1;
        }
        return Integer.parseInt(item.substring(0, 2));
    }

    /**
     * คำนวณและแสดงสรุปการจองทันทีที่ข้อมูลในฟอร์มเปลี่ยน
     * ถ้าข้อมูลยังไม่ถูกต้อง จะแสดงเหตุผลเป็นสีแดง และปิดปุ่ม "ยืนยันการจอง"
     */
    private void updatePreview() {
        if (updatingCombo) {
            return;
        }
        LocalDate date = dateBox.getDateOrNull();
        int court = courtCombo.getValue();
        int startHour = getSelectedStartHour();
        int hours = hoursSpinner.getValue();
        scheduleGrid.setSelection(court, startHour, hours);

        // ช่องใช้แพ็กเกจ เลือกได้เฉพาะเมื่อสมาชิกมีชั่วโมงแพ็กเกจเหลือ
        boolean hasPackage = currentMember != null && currentMember.getRemainingPackageHours() > 0;
        usePackageCheck.setDisable(!hasPackage);
        if (!hasPackage) {
            usePackageCheck.setSelected(false);
        }

        if (currentMember == null) {
            showSummaryError("กรุณากรอกรหัสสมาชิกแล้วกด \"ตรวจสอบ\"\n(เฉพาะสมาชิกเท่านั้นที่จองสนามได้)");
            return;
        }
        if (date == null) {
            showSummaryError("รูปแบบวันที่ไม่ถูกต้อง");
            return;
        }
        String error = bookingService.validateBooking(court, date, startHour, hours);
        if (error != null) {
            showSummaryError(error);
            return;
        }
        boolean usePackage = usePackageCheck.isSelected();
        int remaining = currentMember.getRemainingPackageHours();
        if (usePackage && remaining < hours) {
            showSummaryError("ชั่วโมงแพ็กเกจไม่พอ (เหลือ " + remaining + " ชม. แต่ต้องการ " + hours + " ชม.)");
            return;
        }

        // ---- คำนวณราคา (แต่ละบรรทัดเป็น Label 1 ตัว) ----
        summaryBox.getChildren().clear();
        addSummaryLine("สนาม " + court + " | " + FormatHelper.formatDate(date), null);
        addSummaryLine(String.format("เวลา %02d:00 - %02d:00 (%d ชม.)", startHour, startHour + hours, hours), null);
        double courtFee = 0;
        if (usePackage) {
            addSummaryLine("ค่าสนาม: 0.00 บาท (ใช้แพ็กเกจ, คงเหลือหลังจอง " + (remaining - hours) + " ชม.)", null);
        } else {
            courtFee = paymentService.calculateCourtFee(currentMember, hours);
            int discounted = paymentService.countDiscountedHours(currentMember, hours);
            addSummaryLine("ค่าสนาม: " + FormatHelper.money(courtFee), null);
            if (discounted > 0) {
                addSummaryLine("ได้ส่วนลดชั่วโมงที่ 20: จ่าย 30% ("
                        + FormatHelper.money(paymentService.getDiscountedHourPrice())
                        + ") จำนวน " + discounted + " ชม.", "summary-discount");
            }
        }
        int pairs = shoeSpinner.getValue();
        double shoeFee = ShoeRental.calculateFee(pairs, 1);
        addSummaryLine("ค่าเช่ารองเท้า: " + FormatHelper.money(shoeFee) + " (" + pairs + " คู่ x 50)", null);
        addSummaryLine("ยอดชำระรวม: " + FormatHelper.money(courtFee + shoeFee), "summary-total");

        confirmButton.setDisable(false);
    }

    private void addSummaryLine(String text, String styleClass) {
        Label line = new Label(text);
        line.setWrapText(true);
        if (styleClass != null) {
            line.getStyleClass().add(styleClass);
        }
        summaryBox.getChildren().add(line);
    }

    private void showSummaryError(String message) {
        summaryBox.getChildren().clear();
        addSummaryLine(message, FxHelper.ERROR);
        confirmButton.setDisable(true);
    }

    /** บันทึกการจอง (หลังผ่านการตรวจสอบทั้งหมด) */
    @FXML
    private void confirmBooking() {
        LocalDate date;
        try {
            date = dateBox.getDate();
        } catch (IllegalArgumentException e) {
            FxHelper.showError(root, e.getMessage());
            return;
        }
        int court = courtCombo.getValue();
        int startHour = getSelectedStartHour();
        int hours = hoursSpinner.getValue();
        boolean usePackage = usePackageCheck.isSelected();
        int pairs = shoeSpinner.getValue();

        double courtFee = usePackage ? 0 : paymentService.calculateCourtFee(currentMember, hours);
        double total = courtFee + ShoeRental.calculateFee(pairs, 1);
        String slip = slipField.getText().trim();
        if (total > 0 && slip.isEmpty()) {
            FxHelper.showError(root, "กรุณากรอกเลขอ้างอิงสลิป หรือพิมพ์ CASH ถ้าชำระเงินสด");
            slipField.requestFocus();
            return;
        }
        if (total == 0) {
            slip = "-";
        }
        if (!FxHelper.confirm(root, "ยืนยันการจอง", "ยืนยันการจองสนาม " + court + " วันที่ " + date
                + String.format(" เวลา %02d:00-%02d:00", startHour, startHour + hours)
                + "\nยอดชำระรวม " + FormatHelper.money(total) + " ?")) {
            return;
        }

        try {
            Booking booking = bookingService.createBooking(currentMember, court, date, startHour, hours, usePackage);
            paymentService.payCourtFee(booking, slip);       // ใช้แพ็กเกจ = ไม่สร้างรายได้
            if (pairs > 0) {
                paymentService.rentShoes(booking, pairs, slip);
            }
            FxHelper.showText(root, "จองสำเร็จ", "จองสำเร็จ!\n\n" + booking.getDetailsText());
            slipField.setText("");
            shoeSpinner.getValueFactory().setValue(0);
            scheduleGrid.reload();
            lookupMember(); // อัปเดตชั่วโมงสะสม/แพ็กเกจคงเหลือของสมาชิก
        } catch (IllegalArgumentException e) {
            FxHelper.showError(root, e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        memberIdField.setText("");
        currentMember = null;
        FxHelper.setTone(memberInfoLabel, FxHelper.MUTED);
        memberInfoLabel.setText("ยังไม่ได้เลือกสมาชิก");
        hoursSpinner.getValueFactory().setValue(1);
        shoeSpinner.getValueFactory().setValue(0);
        slipField.setText("");
        usePackageCheck.setSelected(false);
        updatePreview();
    }

    /** เปิดแท็บนี้อีกครั้ง -> โหลดข้อมูลล่าสุด (อาจมีการเปลี่ยนแปลงจากแท็บอื่น) */
    @Override
    public void refresh() {
        if (currentMember != null) {
            lookupMember();
        }
        onDateChanged();
    }
}
