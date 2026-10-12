package com.example.badminton.gui;

import com.example.badminton.model.Booking;
import com.example.badminton.service.BookingService;
import com.example.badminton.util.FormatHelper;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * กล่องแก้ไขการจอง (เลื่อนสนาม / วันที่ / เวลาเริ่ม) ใช้จาก BookingSearchPanel
 *
 * จำนวนชั่วโมงคงเดิม ค่าสนามจึงไม่เปลี่ยน (ถ้าต้องการเปลี่ยนชั่วโมง ให้ยกเลิกแล้วจองใหม่)
 * ทุกครั้งที่ค่าในฟอร์มเปลี่ยน จะตรวจด้วย BookingService.validateUpdate() ทันที
 * ถ้าไม่ผ่าน แสดงเหตุผลสีแดงและปิดปุ่ม "บันทึก"
 */
public class EditBookingDialog extends JDialog {

    private final BookingService bookingService;
    private final Booking booking;

    private final DateField dateField;
    private final JComboBox<Integer> courtCombo = new JComboBox<>();
    private final JComboBox<String> startTimeCombo = new JComboBox<>();
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton saveButton = UiHelper.primaryButton("บันทึก");
    private boolean updatingCombo; // กันไม่ให้ event ทำงานซ้อนระหว่างเติมรายการใน combo
    private boolean saved;

    public EditBookingDialog(Component parent, BookingService bookingService, Booking booking) {
        super(SwingUtilities.getWindowAncestor(parent), "แก้ไขการจอง " + booking.getBookingId(),
                Dialog.ModalityType.APPLICATION_MODAL);
        this.bookingService = bookingService;
        this.booking = booking;
        this.dateField = new DateField(booking.getBookingDate());

        for (int i = 1; i <= BookingService.TOTAL_COURTS; i++) {
            courtCombo.addItem(i);
        }
        courtCombo.setSelectedItem(booking.getCourt().getCourtNumber());
        fillStartTimes(String.format("%02d:00", booking.getStartTime().getHour()));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        UiHelper.addFormRow(form, 0, "สมาชิก:",
                new JLabel(booking.getMember().getMemberId() + " " + booking.getMember().getFullName()));
        UiHelper.addFormRow(form, 1, "เดิม:", new JLabel("สนาม " + booking.getCourt().getCourtNumber() + " | "
                + FormatHelper.formatDate(booking.getBookingDate()) + " | "
                + booking.getStartTime() + "-" + booking.getEndTime()));
        UiHelper.addFormRow(form, 2, "วันที่ใหม่:", dateField);
        UiHelper.addFormRow(form, 3, "สนาม:", courtCombo);
        UiHelper.addFormRow(form, 4, "เวลาเริ่ม:", startTimeCombo);
        UiHelper.addFormRow(form, 5, "จำนวนชั่วโมง:", new JLabel(booking.getDurationHours() + " ชม. (คงเดิม)"));
        UiHelper.addFormWide(form, 6, statusLabel);

        JButton closeButton = new JButton("ปิด");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(closeButton);
        buttons.add(saveButton);

        dateField.addChangeListener(() -> {
            fillStartTimes((String) startTimeCombo.getSelectedItem());
            validateForm();
        });
        courtCombo.addActionListener(e -> validateForm());
        startTimeCombo.addActionListener(e -> validateForm());
        closeButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> save());

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        validateForm();
        pack();
        setLocationRelativeTo(parent);
    }

    /** แสดงกล่อง คืน true ถ้าบันทึกการแก้ไขสำเร็จ */
    public boolean showAndSave() {
        setVisible(true); // modal: รอจนกว่ากล่องจะถูกปิด
        return saved;
    }

    private void save() {
        try {
            bookingService.updateBooking(booking, (Integer) courtCombo.getSelectedItem(),
                    dateField.getDate(), getSelectedStartHour());
            saved = true;
            dispose();
        } catch (IllegalArgumentException e) {
            UiHelper.showError(this, e.getMessage());
        }
    }

    /** เติมรายการเวลาเริ่มตามเวลาทำการของวันที่เลือก (คงเวลาที่เลือกไว้ถ้ายังอยู่ในรายการ) */
    private void fillStartTimes(String previous) {
        LocalDate date = dateField.getDateOrNull();
        if (date == null) {
            return;
        }
        LocalTime open = bookingService.getOpeningTime(date);
        LocalTime close = bookingService.getClosingTime(date);
        updatingCombo = true;
        startTimeCombo.removeAllItems();
        for (int hour = open.getHour(); hour < close.getHour(); hour++) {
            startTimeCombo.addItem(String.format("%02d:00", hour));
        }
        startTimeCombo.setSelectedIndex(0);
        if (previous != null) {
            startTimeCombo.setSelectedItem(previous); // ถ้าไม่มีในรายการ จะคงตัวแรกไว้
        }
        updatingCombo = false;
    }

    private int getSelectedStartHour() {
        String item = (String) startTimeCombo.getSelectedItem();
        return item == null ? -1 : Integer.parseInt(item.substring(0, 2));
    }

    private void validateForm() {
        if (updatingCombo) {
            return;
        }
        LocalDate date = dateField.getDateOrNull();
        int court = (Integer) courtCombo.getSelectedItem();
        String error = (date == null) ? "รูปแบบวันที่ไม่ถูกต้อง (ปี-เดือน-วัน)"
                : bookingService.validateUpdate(booking, court, date, getSelectedStartHour());
        if (error == null) {
            int start = getSelectedStartHour();
            statusLabel.setText(String.format("ว่าง: สนาม %d เวลา %02d:00-%02d:00 ค่าสนามเท่าเดิม",
                    court, start, start + booking.getDurationHours()));
            statusLabel.setForeground(UiHelper.SUCCESS);
        } else {
            statusLabel.setText(UiHelper.html(error, 320));
            statusLabel.setForeground(UiHelper.ERROR);
        }
        saveButton.setEnabled(error == null);
    }
}
