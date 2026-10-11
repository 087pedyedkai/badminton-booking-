package com.example.badminton.gui.cashier;

import com.example.badminton.gui.Refreshable;
import com.example.badminton.gui.UiHelper;
import com.example.badminton.model.Booking;
import com.example.badminton.model.ShoeRental;
import com.example.badminton.service.BookingService;
import com.example.badminton.service.PaymentService;
import com.example.badminton.util.FormatHelper;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;

/**
 * แท็บ "เช่ารองเท้า": เพิ่มการเช่ารองเท้าให้การจองที่มีอยู่แล้ว
 * ค่าเช่า = จำนวนคู่ x 1 วัน (การจอง 1 รายการ = 1 วัน) x 50 บาท -> PaymentService.rentShoes()
 * (ส่วนการ "ซื้อ" รองเท้าอยู่ในแท็บขายอุปกรณ์ แยกหมวดรายได้กัน)
 */
public class ShoeRentalPanel extends JPanel implements Refreshable {

    private final BookingService bookingService;
    private final PaymentService paymentService;

    private final JTextField bookingIdField = new JTextField(8);
    private final JLabel bookingLabel = new JLabel("กรอกรหัสการจองแล้วกด \"ค้นหา\"");
    private final JSpinner pairsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, ShoeRental.MAX_PAIRS, 1));
    private final JLabel feeLabel = new JLabel();
    private final JTextField slipField = new JTextField();
    private final JButton rentButton = UiHelper.primaryButton("ยืนยันการเช่า");
    private final DefaultTableModel tableModel = UiHelper.readOnlyModel(
            "รหัสเช่า", "รหัสการจอง", "สมาชิก", "วันที่ใช้", "จำนวนคู่", "ค่าเช่า (บาท)", "สลิป");
    private Booking currentBooking;

    public ShoeRentalPanel(BookingService bookingService, PaymentService paymentService) {
        super(new BorderLayout(10, 10));
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        JPanel bookingRow = new JPanel(new BorderLayout(4, 0));
        JButton findButton = new JButton("ค้นหา");
        findButton.addActionListener(e -> lookupBooking());
        bookingIdField.addActionListener(e -> lookupBooking());
        bookingRow.add(bookingIdField, BorderLayout.CENTER);
        bookingRow.add(findButton, BorderLayout.EAST);
        UiHelper.addFormRow(form, 0, "รหัสการจอง:", bookingRow);
        bookingLabel.setForeground(UiHelper.MUTED);
        UiHelper.addFormWide(form, 1, bookingLabel);
        UiHelper.addFormRow(form, 2, "จำนวนรองเท้า (คู่):", pairsSpinner);
        feeLabel.setFont(UiHelper.font(Font.BOLD, 16));
        UiHelper.addFormWide(form, 3, feeLabel);
        UiHelper.addFormRow(form, 4, "เลขอ้างอิงสลิป:", slipField);
        UiHelper.addFormWide(form, 5, rentButton);

        pairsSpinner.addChangeListener(e -> updateFee());
        rentButton.addActionListener(e -> rentShoes());
        rentButton.setEnabled(false);
        updateFee();

        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.add(UiHelper.section("เช่ารองเท้า (50 บาท/คู่/วันที่จอง)", form), BorderLayout.NORTH);
        formWrapper.setPreferredSize(new Dimension(400, 0));

        add(formWrapper, BorderLayout.WEST);
        add(UiHelper.section("ประวัติการเช่ารองเท้าทั้งหมด", UiHelper.scroll(UiHelper.table(tableModel))),
                BorderLayout.CENTER);
        refresh();
    }

    private void lookupBooking() {
        String bookingId = bookingIdField.getText().trim();
        currentBooking = bookingId.isEmpty() ? null : bookingService.findById(bookingId);
        rentButton.setEnabled(currentBooking != null);
        if (currentBooking == null) {
            bookingLabel.setForeground(UiHelper.ERROR);
            bookingLabel.setText("ไม่พบการจองรหัสนี้ (ต้องจองสนามก่อนจึงจะเช่ารองเท้าได้)");
        } else {
            bookingLabel.setForeground(UiHelper.PRIMARY_DARK);
            bookingLabel.setText(UiHelper.html(currentBooking.getMember().getFullName()
                    + "\nสนาม " + currentBooking.getCourt().getCourtNumber() + " | "
                    + FormatHelper.formatDate(currentBooking.getBookingDate()) + " | "
                    + currentBooking.getStartTime() + "-" + currentBooking.getEndTime()));
        }
    }

    private void updateFee() {
        int pairs = (Integer) pairsSpinner.getValue();
        feeLabel.setText("ค่าเช่า: " + pairs + " คู่ x 50 = " + FormatHelper.money(ShoeRental.calculateFee(pairs, 1)));
    }

    private void rentShoes() {
        int pairs = (Integer) pairsSpinner.getValue();
        String slip = slipField.getText().trim();
        if (slip.isEmpty()) {
            UiHelper.showError(this, "กรุณากรอกเลขอ้างอิงสลิป หรือพิมพ์ CASH ถ้าชำระเงินสด");
            return;
        }
        if (!UiHelper.confirm(this, "ยืนยันการเช่า", "เช่ารองเท้า " + pairs + " คู่ ให้การจอง "
                + currentBooking.getBookingId() + "\nค่าเช่า " + FormatHelper.money(ShoeRental.calculateFee(pairs, 1)) + " ?")) {
            return;
        }
        try {
            ShoeRental rental = paymentService.rentShoes(currentBooking, pairs, slip);
            slipField.setText("");
            refresh();
            UiHelper.showInfo(this, "เช่ารองเท้าสำเร็จ", "เช่ารองเท้าสำเร็จ: " + rental.getRentalId()
                    + "\nยอดรวมของการจอง " + currentBooking.getBookingId() + " ตอนนี้ "
                    + FormatHelper.money(currentBooking.getTotalAmount()));
        } catch (IllegalArgumentException e) {
            UiHelper.showError(this, e.getMessage());
        }
    }

    @Override
    public void refresh() {
        tableModel.setRowCount(0);
        for (ShoeRental r : paymentService.getAllShoeRentals()) {
            tableModel.addRow(new Object[]{
                    r.getRentalId(), r.getBooking().getBookingId(),
                    r.getBooking().getMember().getMemberId() + " " + r.getBooking().getMember().getFullName(),
                    r.getRentalDate(), r.getPairs(), String.format("%,.2f", r.getRentalFee()), r.getSlipReference()
            });
        }
    }
}
