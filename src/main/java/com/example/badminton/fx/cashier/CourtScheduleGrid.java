package com.example.badminton.fx.cashier;

import com.example.badminton.model.Booking;
import com.example.badminton.service.BookingService;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * ตารางแสดงสถานะสนามทั้ง 12 สนามของวันที่เลือก (แถว = สนาม, คอลัมน์ = ชั่วโมง)
 * เทียบเท่า CourtSchedulePanel ของ Swing
 *
 * สร้างจาก GridPane ที่มี Label 1 ตัวต่อ 1 ช่อง (แทน TableView) เพราะต้องระบายสีและรับคลิกทีละช่อง
 * สีของช่องกำหนดด้วย style class ใน style.css:
 * - slot-free    เขียวอ่อน = ว่าง          - slot-paid     แดงอ่อน = จองแล้ว (จ่ายเงิน)
 * - slot-package ส้ม = จองด้วยแพ็กเกจ       - slot-closed   เทา = นอกเวลาทำการ
 * - slot-selected เหลือง = ช่วงที่กำลังจะจอง - slot-conflict แดงเข้ม = ช่วงที่เลือกชนกับการจองอื่น/นอกเวลา
 *
 * ข้อมูลทั้งหมดมาจาก BookingService.getOpeningTime / getClosingTime / searchByDate
 * (ตารางนี้แค่ "แสดงผล" ไม่ได้ตรวจกฎเอง)
 * เมื่อคลิกช่อง จะแจ้ง BookingController ผ่าน slotClickListener ว่าคลิกสนามไหน ชั่วโมงไหน
 *
 * ใช้ใน FXML ได้: <CourtScheduleGrid fx:id="scheduleGrid"/> แล้วเรียก setBookingService() ก่อนใช้งาน
 */
public class CourtScheduleGrid extends VBox {

    // ช่วงชั่วโมงที่แสดงในตาราง: เปิดเร็วสุด 10:00 (วันหยุด) ถึงปิดช้าสุด 21:00 (วันธรรมดา)
    public static final int FIRST_HOUR = 10;
    public static final int LAST_HOUR = 21;
    private static final int SLOT_COUNT = LAST_HOUR - FIRST_HOUR;

    private static final List<String> SLOT_STATES = List.of(
            "slot-free", "slot-paid", "slot-package", "slot-closed", "slot-selected", "slot-conflict");

    private BookingService bookingService;
    private LocalDate date;

    // ข้อมูลของแต่ละช่อง: การจองที่อยู่ในช่องนั้น และชั่วโมงไหนเปิด/ปิด
    private final Booking[][] slots = new Booking[BookingService.TOTAL_COURTS][SLOT_COUNT];
    private final boolean[] openSlots = new boolean[SLOT_COUNT];
    private final Label[][] cells = new Label[BookingService.TOTAL_COURTS][SLOT_COUNT];

    // ช่วงที่ผู้ใช้กำลังเลือกในฟอร์ม (ใช้ระบายสีเหลือง)
    private int selectedCourt = -1;
    private int selectedStartHour = -1;
    private int selectedHours = 0;

    private BiConsumer<Integer, Integer> slotClickListener;

    public CourtScheduleGrid() {
        super(6);
        GridPane grid = createGrid();
        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        getChildren().addAll(scrollPane, createLegend());
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("schedule-grid");
        grid.getColumnConstraints().add(new ColumnConstraints(80));
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ColumnConstraints column = new ColumnConstraints(56, 70, Double.MAX_VALUE);
            column.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(column);
        }
        for (int row = 0; row <= BookingService.TOTAL_COURTS; row++) {
            grid.getRowConstraints().add(new RowConstraints(32));
        }

        // แถวหัวตาราง: "สนาม" + ชั่วโมง
        grid.add(cell("สนาม", true), 0, 0);
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            grid.add(cell(String.format("%02d:00", FIRST_HOUR + slot), true), slot + 1, 0);
        }

        for (int courtIndex = 0; courtIndex < BookingService.TOTAL_COURTS; courtIndex++) {
            grid.add(cell("สนาม " + (courtIndex + 1), true), 0, courtIndex + 1);
            for (int slot = 0; slot < SLOT_COUNT; slot++) {
                Label label = cell("", false);
                int court = courtIndex + 1;
                int hour = FIRST_HOUR + slot;
                // คลิกช่องในตาราง -> แจ้งหมายเลขสนามและชั่วโมงไปยังผู้ฟัง (BookingController)
                label.setOnMouseClicked(e -> {
                    if (slotClickListener != null) {
                        slotClickListener.accept(court, hour);
                    }
                });
                cells[courtIndex][slot] = label;
                grid.add(label, slot + 1, courtIndex + 1);
            }
        }
        return grid;
    }

    private Label cell(String text, boolean header) {
        Label label = new Label(text);
        label.getStyleClass().add("slot");
        if (header) {
            label.getStyleClass().add("slot-header");
        }
        label.setAlignment(Pos.CENTER);
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return label;
    }

    /** คำอธิบายสี */
    private FlowPane createLegend() {
        FlowPane legend = new FlowPane(12, 4);
        legend.getChildren().addAll(
                legendItem("slot-free", "ว่าง"),
                legendItem("slot-paid", "จองแล้ว"),
                legendItem("slot-package", "จองด้วยแพ็กเกจ"),
                legendItem("slot-closed", "ปิด"),
                legendItem("slot-selected", "ช่วงที่เลือก"),
                legendItem("slot-conflict", "เลือกไม่ได้"));
        Label hint = new Label("(คลิกช่องว่างเพื่อเลือกสนามและเวลาเริ่ม)");
        hint.getStyleClass().add("muted");
        legend.getChildren().add(hint);
        return legend;
    }

    private Label legendItem(String styleClass, String text) {
        Region box = new Region();
        box.getStyleClass().addAll("legend-box", styleClass);
        Label label = new Label(text, box);
        label.setGraphicTextGap(5);
        return label;
    }

    public void setBookingService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    public void setSlotClickListener(BiConsumer<Integer, Integer> listener) {
        this.slotClickListener = listener;
    }

    /** แสดงตารางของวันที่ใหม่ */
    public void showDate(LocalDate date) {
        this.date = date;
        reload();
    }

    /** ดึงข้อมูลการจองล่าสุดของวันที่เลือกจาก BookingService มาใส่ในตาราง (เรียกหลังจองสำเร็จด้วย) */
    public void reload() {
        for (int court = 0; court < BookingService.TOTAL_COURTS; court++) {
            for (int slot = 0; slot < SLOT_COUNT; slot++) {
                slots[court][slot] = null;
            }
        }
        if (date != null) {
            LocalTime open = bookingService.getOpeningTime(date);
            LocalTime close = bookingService.getClosingTime(date);
            for (int slot = 0; slot < SLOT_COUNT; slot++) {
                int hour = FIRST_HOUR + slot;
                openSlots[slot] = hour >= open.getHour() && hour < close.getHour();
            }
            for (Booking booking : bookingService.searchByDate(date)) {
                int courtIndex = booking.getCourt().getCourtNumber() - 1;
                int startHour = booking.getStartTime().getHour();
                for (int hour = startHour; hour < startHour + booking.getDurationHours(); hour++) {
                    int slot = hour - FIRST_HOUR;
                    if (slot >= 0 && slot < SLOT_COUNT) {
                        slots[courtIndex][slot] = booking;
                    }
                }
            }
        }
        updateCells();
    }

    /** กำหนดช่วงที่กำลังเลือก เพื่อระบายสีเหลืองในตาราง */
    public void setSelection(int court, int startHour, int hours) {
        selectedCourt = court;
        selectedStartHour = startHour;
        selectedHours = hours;
        updateCells();
    }

    /** กำหนดข้อความ สี และ tooltip ของทุกช่องใหม่ตามข้อมูลล่าสุด */
    private void updateCells() {
        for (int row = 0; row < BookingService.TOTAL_COURTS; row++) {
            for (int slot = 0; slot < SLOT_COUNT; slot++) {
                Label label = cells[row][slot];
                int hour = FIRST_HOUR + slot;
                Booking booking = slots[row][slot];
                boolean open = openSlots[slot];
                boolean inSelection = (row + 1 == selectedCourt)
                        && hour >= selectedStartHour && hour < selectedStartHour + selectedHours;

                String state;
                if (booking != null) {
                    state = booking.isUsedPackage() ? "slot-package" : "slot-paid";
                    label.setText(booking.getBookingId());
                    label.setTooltip(new Tooltip(booking.getBookingId() + " - " + booking.getMember().getFullName()
                            + "\n" + booking.getStartTime() + " - " + booking.getEndTime()
                            + (booking.isUsedPackage() ? " (แพ็กเกจ)" : "")));
                } else {
                    state = open ? "slot-free" : "slot-closed";
                    label.setText(open ? "" : "ปิด");
                    label.setTooltip(null);
                }

                if (inSelection) {
                    // ช่วงที่เลือก: ถ้าว่างเป็นสีเหลือง ถ้าชนการจองอื่นหรือนอกเวลาเป็นสีแดงเข้ม
                    state = (booking == null && open) ? "slot-selected" : "slot-conflict";
                }
                label.getStyleClass().removeAll(SLOT_STATES);
                label.getStyleClass().add(state);
            }
        }
    }
}
