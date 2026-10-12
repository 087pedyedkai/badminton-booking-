package com.example.badminton.fx;

import com.example.badminton.model.Booking;
import com.example.badminton.service.BookingService;
import com.example.badminton.util.FormatHelper;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

/**
 * กล่องแก้ไขการจอง (เลื่อนสนาม / วันที่ / เวลาเริ่ม) ใช้จาก BookingSearchController
 *
 * จำนวนชั่วโมงคงเดิม ค่าสนามจึงไม่เปลี่ยน (ถ้าต้องการเปลี่ยนชั่วโมง ให้ยกเลิกแล้วจองใหม่)
 * ทุกครั้งที่ค่าในฟอร์มเปลี่ยน จะตรวจด้วย BookingService.validateUpdate() ทันที
 * ถ้าไม่ผ่าน แสดงเหตุผลสีแดงและปิดปุ่ม "บันทึก"
 */
public class EditBookingDialog extends Dialog<ButtonType> {

    private final BookingService bookingService;
    private final Booking booking;

    private final DateBox dateBox = new DateBox();
    private final ComboBox<Integer> courtCombo = new ComboBox<>();
    private final ComboBox<String> startTimeCombo = new ComboBox<>();
    private final Label statusLabel = new Label();
    private boolean updatingCombo; // กันไม่ให้ event ทำงานซ้อนระหว่างเติมรายการใน combo

    public EditBookingDialog(Node owner, BookingService bookingService, Booking booking) {
        this.bookingService = bookingService;
        this.booking = booking;

        setTitle("แก้ไขการจอง " + booking.getBookingId());
        if (owner != null && owner.getScene() != null) {
            initOwner(owner.getScene().getWindow());
        }
        FxHelper.applyTheme(getDialogPane());
        getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) getDialogPane().lookupButton(ButtonType.OK)).setText("บันทึก");
        ((Button) getDialogPane().lookupButton(ButtonType.CANCEL)).setText("ปิด");

        for (int i = 1; i <= BookingService.TOTAL_COURTS; i++) {
            courtCombo.getItems().add(i);
        }
        courtCombo.setMaxWidth(Double.MAX_VALUE);
        startTimeCombo.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(360);

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.addRow(0, new Label("สมาชิก:"),
                new Label(booking.getMember().getMemberId() + " " + booking.getMember().getFullName()));
        form.addRow(1, new Label("เดิม:"), new Label("สนาม " + booking.getCourt().getCourtNumber() + " | "
                + FormatHelper.formatDate(booking.getBookingDate()) + " | "
                + booking.getStartTime() + "-" + booking.getEndTime()));
        form.addRow(2, new Label("วันที่ใหม่:"), dateBox);
        form.addRow(3, new Label("สนาม:"), courtCombo);
        form.addRow(4, new Label("เวลาเริ่ม:"), startTimeCombo);
        form.addRow(5, new Label("จำนวนชั่วโมง:"), new Label(booking.getDurationHours() + " ชม. (คงเดิม)"));
        form.add(statusLabel, 0, 6, 2, 1);
        getDialogPane().setContent(form);

        // ใส่ค่าเดิมของการจอง แล้วค่อยผูก listener
        courtCombo.setValue(booking.getCourt().getCourtNumber());
        dateBox.setDate(booking.getBookingDate());
        fillStartTimes(String.format("%02d:00", booking.getStartTime().getHour()));
        dateBox.addChangeListener(() -> {
            fillStartTimes(startTimeCombo.getValue());
            validateForm();
        });
        courtCombo.setOnAction(e -> validateForm());
        startTimeCombo.setOnAction(e -> validateForm());
        validateForm();
    }

    /** แสดงกล่อง คืน true ถ้าบันทึกการแก้ไขสำเร็จ */
    public boolean showAndSave() {
        Optional<ButtonType> answer = showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return false;
        }
        bookingService.updateBooking(booking, courtCombo.getValue(), dateBox.getDate(), getSelectedStartHour());
        return true;
    }

    /** เติมรายการเวลาเริ่มตามเวลาทำการของวันที่เลือก (คงเวลาที่เลือกไว้ถ้ายังอยู่ในรายการ) */
    private void fillStartTimes(String previous) {
        LocalDate date = dateBox.getDateOrNull();
        if (date == null) {
            return;
        }
        LocalTime open = bookingService.getOpeningTime(date);
        LocalTime close = bookingService.getClosingTime(date);
        updatingCombo = true;
        startTimeCombo.getItems().clear();
        for (int hour = open.getHour(); hour < close.getHour(); hour++) {
            startTimeCombo.getItems().add(String.format("%02d:00", hour));
        }
        if (previous != null && startTimeCombo.getItems().contains(previous)) {
            startTimeCombo.setValue(previous);
        } else {
            startTimeCombo.getSelectionModel().select(0);
        }
        updatingCombo = false;
    }

    private int getSelectedStartHour() {
        String item = startTimeCombo.getValue();
        return item == null ? -1 : Integer.parseInt(item.substring(0, 2));
    }

    private void validateForm() {
        if (updatingCombo) {
            return;
        }
        LocalDate date = dateBox.getDateOrNull();
        String error = (date == null) ? "รูปแบบวันที่ไม่ถูกต้อง (ปี-เดือน-วัน)"
                : bookingService.validateUpdate(booking, courtCombo.getValue(), date, getSelectedStartHour());
        if (error == null) {
            int start = getSelectedStartHour();
            statusLabel.setText(String.format("ว่าง: สนาม %d เวลา %02d:00-%02d:00 ค่าสนามเท่าเดิม",
                    courtCombo.getValue(), start, start + booking.getDurationHours()));
            FxHelper.setTone(statusLabel, FxHelper.OK);
        } else {
            statusLabel.setText(error);
            FxHelper.setTone(statusLabel, FxHelper.ERROR);
        }
        getDialogPane().lookupButton(ButtonType.OK).setDisable(error != null);
    }
}
