package com.example.badminton.gui.cashier;

import com.example.badminton.gui.UiHelper;
import com.example.badminton.model.Booking;
import com.example.badminton.service.BookingService;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.BiConsumer;

/**
 * ตารางแสดงสถานะสนามทั้ง 12 สนามของวันที่เลือก (แถว = สนาม, คอลัมน์ = ชั่วโมง)
 *
 * สีของช่อง:
 * - เขียวอ่อน = ว่าง      - แดงอ่อน = จองแล้ว (จ่ายเงิน)   - ส้ม = จองด้วยแพ็กเกจ
 * - เทา = นอกเวลาทำการ   - เหลือง = ช่วงเวลาที่กำลังจะจอง   - แดงเข้ม = ช่วงที่เลือกชนกับการจองอื่น/นอกเวลา
 *
 * ข้อมูลทั้งหมดมาจาก BookingService (ตารางนี้แค่ "แสดงผล" ไม่ได้ตรวจกฎเอง)
 * เมื่อคลิกช่อง จะแจ้ง BookingPanel ผ่าน slotClickListener ว่าคลิกสนามไหน ชั่วโมงไหน
 */
public class CourtSchedulePanel extends JPanel {

    // ช่วงชั่วโมงที่แสดงในตาราง: เปิดเร็วสุด 10:00 (วันหยุด) ถึงปิดช้าสุด 21:00 (วันธรรมดา)
    public static final int FIRST_HOUR = 10;
    public static final int LAST_HOUR = 21;
    private static final int SLOT_COUNT = LAST_HOUR - FIRST_HOUR;

    private final BookingService bookingService;
    private final ScheduleTableModel model = new ScheduleTableModel();
    private final JTable table;
    private LocalDate date;

    // ช่วงที่ผู้ใช้กำลังเลือกในฟอร์ม (ใช้ระบายสีเหลือง)
    private int selectedCourt = -1;
    private int selectedStartHour = -1;
    private int selectedHours = 0;

    private BiConsumer<Integer, Integer> slotClickListener;

    public CourtSchedulePanel(BookingService bookingService) {
        super(new BorderLayout(0, 6));
        this.bookingService = bookingService;
        setOpaque(false);

        table = new JTable(model);
        table.setRowHeight(32);
        table.setRowSelectionAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(UiHelper.font(Font.BOLD, 13));
        table.getColumnModel().getColumn(0).setMinWidth(80);
        table.setDefaultRenderer(Object.class, new SlotRenderer());
        table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // คลิกช่องในตาราง -> แจ้งหมายเลขสนามและชั่วโมงไปยังผู้ฟัง (BookingPanel)
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());
                if (row >= 0 && column >= 1 && slotClickListener != null) {
                    slotClickListener.accept(row + 1, FIRST_HOUR + column - 1);
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(createLegend(), BorderLayout.SOUTH);
    }

    /** คำอธิบายสี */
    private JPanel createLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        legend.setOpaque(false);
        legend.add(legendItem(UiHelper.SLOT_FREE, "ว่าง"));
        legend.add(legendItem(UiHelper.SLOT_PAID, "จองแล้ว"));
        legend.add(legendItem(UiHelper.SLOT_PACKAGE, "จองด้วยแพ็กเกจ"));
        legend.add(legendItem(UiHelper.SLOT_CLOSED, "ปิด"));
        legend.add(legendItem(UiHelper.SLOT_SELECTED, "ช่วงที่เลือก"));
        legend.add(legendItem(UiHelper.SLOT_CONFLICT, "เลือกไม่ได้"));
        JLabel hint = new JLabel("  (คลิกช่องว่างเพื่อเลือกสนามและเวลาเริ่ม)");
        hint.setForeground(UiHelper.MUTED);
        legend.add(hint);
        return legend;
    }

    private JLabel legendItem(Color color, String text) {
        JLabel label = new JLabel(text);
        label.setIcon(new ColorSquareIcon(color));
        return label;
    }

    public void setSlotClickListener(BiConsumer<Integer, Integer> listener) {
        this.slotClickListener = listener;
    }

    /** แสดงตารางของวันที่ใหม่ */
    public void showDate(LocalDate date) {
        this.date = date;
        model.reload();
    }

    /** โหลดข้อมูลการจองล่าสุดของวันเดิม (เรียกหลังจองสำเร็จ) */
    public void reload() {
        model.reload();
    }

    /** กำหนดช่วงที่กำลังเลือก เพื่อระบายสีเหลืองในตาราง */
    public void setSelection(int court, int startHour, int hours) {
        selectedCourt = court;
        selectedStartHour = startHour;
        selectedHours = hours;
        table.repaint();
    }

    // =====================================================================
    // Table model: เก็บว่าช่องไหนมีการจองอะไร และชั่วโมงไหนเปิด/ปิด
    // =====================================================================
    private class ScheduleTableModel extends AbstractTableModel {

        private final Booking[][] slots = new Booking[BookingService.TOTAL_COURTS][SLOT_COUNT];
        private final boolean[] openSlots = new boolean[SLOT_COUNT];

        /** ดึงข้อมูลการจองของวันที่เลือกจาก BookingService มาใส่ในตาราง */
        void reload() {
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
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return BookingService.TOTAL_COURTS;
        }

        @Override
        public int getColumnCount() {
            return SLOT_COUNT + 1; // +1 สำหรับคอลัมน์ชื่อสนาม
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return "สนาม";
            }
            return String.format("%02d:00", FIRST_HOUR + column - 1);
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (column == 0) {
                return "สนาม " + (row + 1);
            }
            Booking booking = slots[row][column - 1];
            if (booking != null) {
                return booking.getBookingId();
            }
            return openSlots[column - 1] ? "" : "ปิด";
        }
    }

    // =====================================================================
    // Renderer: กำหนดสีและ tooltip ของแต่ละช่อง
    // =====================================================================
    private class SlotRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            setHorizontalAlignment(CENTER);
            setForeground(Color.DARK_GRAY);
            setToolTipText(null);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.WHITE));

            if (column == 0) {
                setBackground(new Color(0xE0E0E0));
                setFont(getFont().deriveFont(Font.BOLD));
                return this;
            }

            int slot = column - 1;
            int hour = FIRST_HOUR + slot;
            Booking booking = model.slots[row][slot];
            boolean open = model.openSlots[slot];
            boolean inSelection = (row + 1 == selectedCourt)
                    && hour >= selectedStartHour && hour < selectedStartHour + selectedHours;

            Color background;
            if (booking != null) {
                background = booking.isUsedPackage() ? UiHelper.SLOT_PACKAGE : UiHelper.SLOT_PAID;
                setToolTipText(UiHelper.html(booking.getBookingId() + " - " + booking.getMember().getFullName()
                        + "\n" + booking.getStartTime() + " - " + booking.getEndTime()
                        + (booking.isUsedPackage() ? " (แพ็กเกจ)" : "")));
            } else if (!open) {
                background = UiHelper.SLOT_CLOSED;
            } else {
                background = UiHelper.SLOT_FREE;
            }

            if (inSelection) {
                // ช่วงที่เลือก: ถ้าว่างเป็นสีเหลือง ถ้าชนการจองอื่นหรือนอกเวลาเป็นสีแดงเข้ม
                if (booking == null && open) {
                    background = UiHelper.SLOT_SELECTED;
                } else {
                    background = UiHelper.SLOT_CONFLICT;
                    setForeground(Color.WHITE);
                }
            }
            setBackground(background);
            return this;
        }
    }

    /** ไอคอนสี่เหลี่ยมสีเล็ก ๆ สำหรับคำอธิบายสี */
    private static class ColorSquareIcon implements javax.swing.Icon {
        private final Color color;

        ColorSquareIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, java.awt.Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x, y, 14, 14);
            g.setColor(Color.GRAY);
            g.drawRect(x, y, 14, 14);
        }

        @Override
        public int getIconWidth() {
            return 15;
        }

        @Override
        public int getIconHeight() {
            return 15;
        }
    }
}
