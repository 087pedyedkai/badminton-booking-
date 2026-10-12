package com.example.badminton.gui;

import com.example.badminton.model.Booking;
import com.example.badminton.service.BookingService;
import com.example.badminton.util.FormatHelper;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * หน้าจอค้นหาและดูรายการจอง (ใช้ร่วมกัน 2 บทบาท)
 * - แคชเชียร์: "ค้นหาข้อมูลการจอง" ด้วยรหัสการจอง รหัสสมาชิก ชื่อสมาชิก หรือวันที่
 * - ผู้ดูแลระบบ: "ดูข้อมูลการจองทั้งหมด" (เริ่มต้นที่โหมดแสดงทั้งหมด)
 *
 * คลิกแถวในตาราง -> แสดงรายละเอียดเต็มของการจองนั้นด้านล่าง (Booking.getDetailsText())
 * ปุ่ม "แก้ไขการจอง" -> EditBookingDialog (BookingService.updateBooking)
 * ปุ่ม "ยกเลิกการจอง" -> BookingService.cancelBooking (คืนชั่วโมงแพ็กเกจ / คืนเงิน)
 */
public class BookingSearchPanel extends JPanel implements Refreshable {

    private static final String[] SEARCH_TYPES = {
            "รหัสการจอง", "รหัสสมาชิก", "ชื่อสมาชิก", "วันที่ใช้สนาม (ปี-เดือน-วัน)", "แสดงทั้งหมด"
    };
    private static final int SHOW_ALL = 4;

    private final BookingService bookingService;
    private final JComboBox<String> searchTypeCombo = new JComboBox<>(SEARCH_TYPES);
    private final JTextField keywordField = new JTextField(16);
    private final JLabel resultLabel = new JLabel(" ");
    private final DefaultTableModel tableModel = UiHelper.readOnlyModel(
            "รหัส", "สมาชิก", "สนาม", "วันที่ใช้สนาม", "เวลา", "ชม.", "ค่าสนาม", "ค่าเช่ารองเท้า", "ยอดรวม", "สถานะ");
    private final JTable table = UiHelper.table(tableModel);
    private final JTextArea detailsArea = UiHelper.outputArea();
    private final JButton editButton = new JButton("แก้ไขการจอง");
    private final JButton cancelButton = new JButton("ยกเลิกการจอง");
    private List<Booking> currentResults = new ArrayList<>();

    public BookingSearchPanel(BookingService bookingService, boolean showAllByDefault) {
        super(new BorderLayout(8, 8));
        this.bookingService = bookingService;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton searchButton = UiHelper.primaryButton("ค้นหา");
        searchBar.add(new JLabel("ค้นหาด้วย:"));
        searchBar.add(searchTypeCombo);
        searchBar.add(keywordField);
        searchBar.add(searchButton);
        searchBar.add(resultLabel);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        cancelButton.setBackground(UiHelper.ERROR);
        cancelButton.setForeground(Color.WHITE);
        editButton.setEnabled(false);
        cancelButton.setEnabled(false);
        actionBar.add(editButton);
        actionBar.add(cancelButton);
        editButton.addActionListener(e -> editSelected());
        cancelButton.addActionListener(e -> cancelSelected());
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(searchBar, BorderLayout.CENTER);
        topBar.add(actionBar, BorderLayout.EAST);

        searchButton.addActionListener(e -> search(true));
        keywordField.addActionListener(e -> search(true));
        searchTypeCombo.addActionListener(e -> {
            keywordField.setEnabled(searchTypeCombo.getSelectedIndex() != SHOW_ALL);
            if (searchTypeCombo.getSelectedIndex() == SHOW_ALL) {
                search(false);
            }
        });

        // เลือกแถว -> แสดงรายละเอียด (ต้องแปลงลำดับแถว เพราะผู้ใช้อาจกดเรียงตารางใหม่)
        table.getSelectionModel().addListSelectionListener(e -> {
            Booking booking = getSelectedBooking();
            if (booking != null) {
                detailsArea.setText(booking.getDetailsText());
                detailsArea.setCaretPosition(0); // เลื่อนกลับไปบรรทัดแรก
            }
            editButton.setEnabled(booking != null);
            cancelButton.setEnabled(booking != null);
        });
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(9).setPreferredWidth(220);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                UiHelper.scroll(table), UiHelper.section("รายละเอียดการจอง (คลิกแถวในตาราง)", UiHelper.scroll(detailsArea)));
        split.setResizeWeight(0.65);

        add(topBar, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        if (showAllByDefault) {
            searchTypeCombo.setSelectedIndex(SHOW_ALL);
        }
    }

    /**
     * ค้นหาตามประเภทที่เลือก แล้วแสดงผลในตาราง
     * showErrors = true เมื่อผู้ใช้กดค้นหาเอง (แจ้งข้อผิดพลาด), false เมื่อระบบโหลดใหม่อัตโนมัติ
     */
    private void search(boolean showErrors) {
        int type = searchTypeCombo.getSelectedIndex();
        String keyword = keywordField.getText().trim();
        List<Booking> results;

        if (type == SHOW_ALL) {
            results = bookingService.getAllBookings();
        } else if (keyword.isEmpty()) {
            if (showErrors) {
                UiHelper.showError(this, "กรุณากรอกคำค้นหา");
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
                    UiHelper.showError(this, "รูปแบบวันที่ไม่ถูกต้อง ต้องเป็น ปี-เดือน-วัน เช่น 2026-10-12");
                }
                return;
            }
        }
        showResults(results);
    }

    /** การจองของแถวที่เลือก (ต้องแปลงลำดับแถว เพราะผู้ใช้อาจกดเรียงตารางใหม่) หรือ null */
    private Booking getSelectedBooking() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return currentResults.get(table.convertRowIndexToModel(viewRow));
    }

    /** แก้ไขการจองที่เลือก (เลื่อนสนาม/วันที่/เวลา) */
    private void editSelected() {
        Booking booking = getSelectedBooking();
        String error = bookingService.checkModifiable(booking);
        if (error != null) {
            UiHelper.showError(this, error);
            return;
        }
        if (new EditBookingDialog(this, bookingService, booking).showAndSave()) {
            UiHelper.showText(this, "แก้ไขสำเร็จ", "แก้ไขการจองสำเร็จ!\n\n" + booking.getDetailsText());
            search(false);
        }
    }

    /** ยกเลิก (ลบ) การจองที่เลือก */
    private void cancelSelected() {
        Booking booking = getSelectedBooking();
        String error = bookingService.checkModifiable(booking);
        if (error != null) {
            UiHelper.showError(this, error);
            return;
        }
        String effect = booking.isUsedPackage()
                ? "คืนชั่วโมงแพ็กเกจ " + booking.getDurationHours() + " ชม. ให้สมาชิก"
                : "ลดชั่วโมงสะสม " + booking.getDurationHours() + " ชม.";
        if (!UiHelper.confirm(this, "ยืนยันการยกเลิก", "ยกเลิกการจอง " + booking.getBookingId()
                + " ของ " + booking.getMember().getFullName() + " ?\n"
                + "- " + effect + "\n- คืนเงิน " + FormatHelper.money(booking.getTotalAmount())
                + " (ลบรายการรับเงินของการจองนี้)")) {
            return;
        }
        try {
            double refund = bookingService.cancelBooking(booking);
            UiHelper.showInfo(this, "ยกเลิกสำเร็จ", "ยกเลิกการจอง " + booking.getBookingId()
                    + " แล้ว\nยอดเงินที่ต้องคืนสมาชิก: " + FormatHelper.money(refund));
            search(false);
        } catch (IllegalArgumentException e) {
            UiHelper.showError(this, e.getMessage());
        }
    }

    private void showResults(List<Booking> results) {
        currentResults = results;
        tableModel.setRowCount(0);
        for (Booking b : results) {
            tableModel.addRow(new Object[]{
                    b.getBookingId(),
                    b.getMember().getMemberId() + " " + b.getMember().getFullName(),
                    b.getCourt().getCourtNumber(),
                    FormatHelper.formatDate(b.getBookingDate()),
                    b.getStartTime() + "-" + b.getEndTime(),
                    b.getDurationHours(),
                    b.isUsedPackage() ? "แพ็กเกจ" : String.format("%,.2f", b.getCourtFee()),
                    String.format("%,.2f", b.getShoeRentalFee()),
                    String.format("%,.2f", b.getTotalAmount()),
                    b.getPaymentStatus()
            });
        }
        detailsArea.setText("");
        resultLabel.setText("พบ " + results.size() + " รายการ");
        resultLabel.setForeground(results.isEmpty() ? UiHelper.ERROR : UiHelper.PRIMARY_DARK);
    }

    @Override
    public void refresh() {
        search(false); // ค้นหาซ้ำด้วยเงื่อนไขเดิม เพื่อให้เห็นการจองใหม่ล่าสุด
    }
}
