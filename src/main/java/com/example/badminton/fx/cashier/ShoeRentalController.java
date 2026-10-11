package com.example.badminton.fx.cashier;

import com.example.badminton.BadmintonSystem;
import com.example.badminton.fx.FxHelper;
import com.example.badminton.fx.Refreshable;
import com.example.badminton.model.Booking;
import com.example.badminton.model.ShoeRental;
import com.example.badminton.service.BookingService;
import com.example.badminton.service.PaymentService;
import com.example.badminton.util.FormatHelper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controller แท็บ "เช่ารองเท้า" (cashier/shoe-rental.fxml): เพิ่มการเช่ารองเท้าให้การจองที่มีอยู่แล้ว
 * ค่าเช่า = จำนวนคู่ x 1 วัน (การจอง 1 รายการ = 1 วัน) x 50 บาท
 * Service ที่เรียก: BookingService.findById(), PaymentService.rentShoes() / getAllShoeRentals()
 * (ส่วนการ "ซื้อ" รองเท้าอยู่ในแท็บขายอุปกรณ์ แยกหมวดรายได้กัน)
 */
public class ShoeRentalController implements Refreshable {

    private final BookingService bookingService;
    private final PaymentService paymentService;

    @FXML private Parent root;
    @FXML private TextField bookingIdField;
    @FXML private Label bookingLabel;
    @FXML private Spinner<Integer> pairsSpinner;
    @FXML private Label feeLabel;
    @FXML private TextField slipField;
    @FXML private Button rentButton;
    @FXML private TableView<ShoeRental> table;

    private Booking currentBooking;

    public ShoeRentalController(BadmintonSystem system) {
        this.bookingService = system.getBookingService();
        this.paymentService = system.getPaymentService();
    }

    @FXML
    private void initialize() {
        pairsSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, ShoeRental.MAX_PAIRS, 1));
        pairsSpinner.valueProperty().addListener((obs, oldValue, newValue) -> updateFee());
        updateFee();

        FxHelper.setColumnValues(table,
                ShoeRental::getRentalId,
                r -> r.getBooking().getBookingId(),
                r -> r.getBooking().getMember().getMemberId() + " " + r.getBooking().getMember().getFullName(),
                ShoeRental::getRentalDate,
                ShoeRental::getPairs,
                r -> String.format("%,.2f", r.getRentalFee()),
                ShoeRental::getSlipReference);
        refresh();
    }

    @FXML
    private void lookupBooking() {
        String bookingId = bookingIdField.getText().trim();
        currentBooking = bookingId.isEmpty() ? null : bookingService.findById(bookingId);
        rentButton.setDisable(currentBooking == null);
        if (currentBooking == null) {
            FxHelper.setTone(bookingLabel, FxHelper.ERROR);
            bookingLabel.setText("ไม่พบการจองรหัสนี้ (ต้องจองสนามก่อนจึงจะเช่ารองเท้าได้)");
        } else {
            FxHelper.setTone(bookingLabel, FxHelper.OK);
            bookingLabel.setText(currentBooking.getMember().getFullName()
                    + "\nสนาม " + currentBooking.getCourt().getCourtNumber() + " | "
                    + FormatHelper.formatDate(currentBooking.getBookingDate()) + " | "
                    + currentBooking.getStartTime() + "-" + currentBooking.getEndTime());
        }
    }

    private void updateFee() {
        int pairs = pairsSpinner.getValue();
        feeLabel.setText("ค่าเช่า: " + pairs + " คู่ x 50 = " + FormatHelper.money(ShoeRental.calculateFee(pairs, 1)));
    }

    @FXML
    private void rentShoes() {
        int pairs = pairsSpinner.getValue();
        String slip = slipField.getText().trim();
        if (slip.isEmpty()) {
            FxHelper.showError(root, "กรุณากรอกเลขอ้างอิงสลิป หรือพิมพ์ CASH ถ้าชำระเงินสด");
            return;
        }
        if (!FxHelper.confirm(root, "ยืนยันการเช่า", "เช่ารองเท้า " + pairs + " คู่ ให้การจอง "
                + currentBooking.getBookingId() + "\nค่าเช่า " + FormatHelper.money(ShoeRental.calculateFee(pairs, 1)) + " ?")) {
            return;
        }
        try {
            ShoeRental rental = paymentService.rentShoes(currentBooking, pairs, slip);
            slipField.setText("");
            refresh();
            FxHelper.showInfo(root, "เช่ารองเท้าสำเร็จ", "เช่ารองเท้าสำเร็จ: " + rental.getRentalId()
                    + "\nยอดรวมของการจอง " + currentBooking.getBookingId() + " ตอนนี้ "
                    + FormatHelper.money(currentBooking.getTotalAmount()));
        } catch (IllegalArgumentException e) {
            FxHelper.showError(root, e.getMessage());
        }
    }

    @Override
    public void refresh() {
        table.getItems().setAll(paymentService.getAllShoeRentals());
    }
}
