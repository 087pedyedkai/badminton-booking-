package com.example.badminton.fx;

import com.example.badminton.BadmintonSystem;
import com.example.badminton.model.Booking;
import com.example.badminton.service.BookingService;
import com.example.badminton.util.FormatHelper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller หน้าจอค้นหาและดูรายการจอง (booking-search.fxml) ใช้ร่วมกัน 2 บทบาท
 * - แคชเชียร์: "ค้นหาข้อมูลการจอง" ด้วยรหัสการจอง รหัสสมาชิก ชื่อสมาชิก หรือวันที่
 * - ผู้ดูแลระบบ: "ดูข้อมูลการจองทั้งหมด" (MainController เรียก setShowAllByDefault())
 *
 * Service ที่เรียก: BookingService.findById / searchByMemberId / searchByMemberName / searchByDate / getAllBookings
 * คลิกแถวในตาราง -> แสดงรายละเอียดเต็มของการจองนั้นด้านล่าง (Booking.getDetailsText())
 * ปุ่ม "แก้ไขการจอง" -> EditBookingDialog (BookingService.updateBooking)
 * ปุ่ม "ยกเลิกการจอง" -> BookingService.cancelBooking (คืนชั่วโมงแพ็กเกจ / คืนเงิน)
 */
public class BookingSearchController implements Refreshable {

    private static final String[] SEARCH_TYPES = {
            "รหัสการจอง", "รหัสสมาชิก", "ชื่อสมาชิก", "วันที่ใช้สนาม (ปี-เดือน-วัน)", "แสดงทั้งหมด"
    };
    private static final int SHOW_ALL = 4;

    private final BookingService bookingService;

    @FXML private Parent root;
    @FXML private ComboBox<String> searchTypeCombo;
    @FXML private TextField keywordField;
    @FXML private Label resultLabel;
    @FXML private TableView<Booking> table;
    @FXML private TextArea detailsArea;
    @FXML private Button editButton;
    @FXML private Button cancelButton;

    public BookingSearchController(BadmintonSystem system) {
        this.bookingService = system.getBookingService();
    }

    @FXML
    private void initialize() {
        searchTypeCombo.getItems().setAll(SEARCH_TYPES);
        searchTypeCombo.getSelectionModel().select(0);

        FxHelper.setColumnValues(table,
                Booking::getBookingId,
                b -> b.getMember().getMemberId() + " " + b.getMember().getFullName(),
                b -> b.getCourt().getCourtNumber(),
                b -> FormatHelper.formatDate(b.getBookingDate()),
                b -> b.getStartTime() + "-" + b.getEndTime(),
                Booking::getDurationHours,
                b -> b.isUsedPackage() ? "แพ็กเกจ" : String.format("%,.2f", b.getCourtFee()),
                b -> String.format("%,.2f", b.getShoeRentalFee()),
                b -> String.format("%,.2f", b.getTotalAmount()),
                Booking::getPaymentStatus);

        // เลือกแถว -> แสดงรายละเอียด (getSelectedItem() ได้ออบเจกต์ของแถวโดยตรง แม้ผู้ใช้กดเรียงตารางใหม่)
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldBooking, booking) -> {
            if (booking != null) {
                detailsArea.setText(booking.getDetailsText());
                detailsArea.positionCaret(0); // เลื่อนกลับไปบรรทัดแรก
            }
            editButton.setDisable(booking == null);
            cancelButton.setDisable(booking == null);
        });
    }

    /** ให้หน้าจอนี้เริ่มที่โหมด "แสดงทั้งหมด" (ใช้ในแท็บของผู้ดูแลระบบ) */
    public void setShowAllByDefault() {
        searchTypeCombo.getSelectionModel().select(SHOW_ALL); // จะเรียก searchTypeChanged() ต่อเอง
    }

    @FXML
    private void searchTypeChanged() {
        boolean showAll = searchTypeCombo.getSelectionModel().getSelectedIndex() == SHOW_ALL;
        keywordField.setDisable(showAll);
        if (showAll) {
            search(false);
        }
    }

    @FXML
    private void searchClicked() {
        search(true);
    }

    /**
     * ค้นหาตามประเภทที่เลือก แล้วแสดงผลในตาราง
     * showErrors = true เมื่อผู้ใช้กดค้นหาเอง (แจ้งข้อผิดพลาด), false เมื่อระบบโหลดใหม่อัตโนมัติ
     */
    private void search(boolean showErrors) {
        int type = searchTypeCombo.getSelectionModel().getSelectedIndex();
        String keyword = keywordField.getText().trim();
        List<Booking> results;

        if (type == SHOW_ALL) {
            results = bookingService.getAllBookings();
        } else if (keyword.isEmpty()) {
            if (showErrors) {
                FxHelper.showError(root, "กรุณากรอกคำค้นหา");
            }
            return;
        } else if (type == 0) {
            results = new ArrayList<>();
            Booking booking = bookingService.findById(keyword);
            if (booking != null) {
                results.add(booking);
            }
        } else if (type == 1) {
            results = bookingService.searchByMemberId(keyword);
        } else if (type == 2) {
            results = bookingService.searchByMemberName(keyword);
        } else {
            try {
                results = bookingService.searchByDate(LocalDate.parse(keyword));
            } catch (DateTimeParseException e) {
                if (showErrors) {
                    FxHelper.showError(root, "รูปแบบวันที่ไม่ถูกต้อง ต้องเป็น ปี-เดือน-วัน เช่น 2026-10-12");
                }
                return;
            }
        }
        showResults(results);
    }

    /** แก้ไขการจองที่เลือก (เลื่อนสนาม/วันที่/เวลา) */
    @FXML
    private void editClicked() {
        Booking booking = table.getSelectionModel().getSelectedItem();
        String error = bookingService.checkModifiable(booking);
        if (error != null) {
            FxHelper.showError(root, error);
            return;
        }
        try {
            if (new EditBookingDialog(root, bookingService, booking).showAndSave()) {
                FxHelper.showText(root, "แก้ไขสำเร็จ", "แก้ไขการจองสำเร็จ!\n\n" + booking.getDetailsText());
                search(false);
            }
        } catch (IllegalArgumentException e) {
            FxHelper.showError(root, e.getMessage());
        }
    }

    /** ยกเลิก (ลบ) การจองที่เลือก */
    @FXML
    private void cancelClicked() {
        Booking booking = table.getSelectionModel().getSelectedItem();
        String error = bookingService.checkModifiable(booking);
        if (error != null) {
            FxHelper.showError(root, error);
            return;
        }
        String effect = booking.isUsedPackage()
                ? "คืนชั่วโมงแพ็กเกจ " + booking.getDurationHours() + " ชม. ให้สมาชิก"
                : "ลดชั่วโมงสะสม " + booking.getDurationHours() + " ชม.";
        if (!FxHelper.confirm(root, "ยืนยันการยกเลิก", "ยกเลิกการจอง " + booking.getBookingId()
                + " ของ " + booking.getMember().getFullName() + " ?\n"
                + "- " + effect + "\n- คืนเงิน " + FormatHelper.money(booking.getTotalAmount())
                + " (ลบรายการรับเงินของการจองนี้)")) {
            return;
        }
        try {
            double refund = bookingService.cancelBooking(booking);
            FxHelper.showInfo(root, "ยกเลิกสำเร็จ", "ยกเลิกการจอง " + booking.getBookingId()
                    + " แล้ว\nยอดเงินที่ต้องคืนสมาชิก: " + FormatHelper.money(refund));
            search(false);
        } catch (IllegalArgumentException e) {
            FxHelper.showError(root, e.getMessage());
        }
    }

    private void showResults(List<Booking> results) {
        table.getItems().setAll(results);
        detailsArea.setText("");
        resultLabel.setText("พบ " + results.size() + " รายการ");
        FxHelper.setTone(resultLabel, results.isEmpty() ? FxHelper.ERROR : FxHelper.OK);
    }

    @Override
    public void refresh() {
        search(false); // ค้นหาซ้ำด้วยเงื่อนไขเดิม เพื่อให้เห็นการจองใหม่ล่าสุด
    }
}
