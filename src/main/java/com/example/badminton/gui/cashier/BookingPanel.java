package com.example.badminton.gui.cashier;

import com.example.badminton.gui.DateField;
import com.example.badminton.gui.Refreshable;
import com.example.badminton.gui.UiHelper;
import com.example.badminton.model.Booking;
import com.example.badminton.model.Member;
import com.example.badminton.model.ShoeRental;
import com.example.badminton.service.BookingService;
import com.example.badminton.service.MemberService;
import com.example.badminton.service.PaymentService;
import com.example.badminton.util.FormatHelper;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * แท็บ "จองสนาม" ของแคชเชียร์
 *
 * หน้าจอแบ่ง 2 ส่วน:
 * - ซ้าย: ตารางสถานะสนาม (CourtSchedulePanel) ของวันที่เลือก
 * - ขวา: ฟอร์มจอง (สมาชิก สนาม เวลา ชั่วโมง แพ็กเกจ รองเท้า สลิป) + สรุปราคาแบบอัปเดตทันที
 *
 * ขั้นตอนการจองเหมือนเวอร์ชัน Console ทุกอย่าง และใช้ Service ชุดเดียวกัน:
 * 1) MemberService.findById()          -> ต้องเป็นสมาชิก
 * 2) BookingService.validateBooking()  -> ตรวจเวลาทำการ / ห้ามย้อนหลัง / สนามว่าง
 * 3) PaymentService.calculateCourtFee()-> คำนวณค่าสนามพร้อมส่วนลดชั่วโมงที่ 20
 * 4) BookingService.createBooking()    -> บันทึก, หักแพ็กเกจ หรือเพิ่มชั่วโมงสะสม
 * 5) PaymentService.payCourtFee() และ rentShoes() -> บันทึกรายได้
 */
public class BookingPanel extends JPanel implements Refreshable {

    private static final int SUMMARY_WIDTH = 280; // ความกว้างข้อความสรุป (pixel) ข้อความยาวจะขึ้นบรรทัดใหม่เอง

    private final MemberService memberService;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    private final DateField dateField = new DateField(LocalDate.now().plusDays(1));
    private final JLabel openingHoursLabel = new JLabel();
    private final CourtSchedulePanel schedulePanel;

    private final JTextField memberIdField = new JTextField(8);
    private final JLabel memberInfoLabel = new JLabel("ยังไม่ได้เลือกสมาชิก");
    private final JComboBox<Integer> courtCombo = new JComboBox<>();
    private final JComboBox<String> startTimeCombo = new JComboBox<>();
    private final JSpinner hoursSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 11, 1));
    private final JCheckBox usePackageCheck = new JCheckBox("ใช้ชั่วโมงแพ็กเกจ (ไม่มีค่าสนาม)");
    private final JSpinner shoeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, ShoeRental.MAX_PAIRS, 1));
    private final JTextField slipField = new JTextField();
    private final JLabel summaryLabel = new JLabel();
    private final JButton confirmButton = UiHelper.primaryButton("ยืนยันการจอง");

    private Member currentMember;    // สมาชิกที่ตรวจสอบแล้ว (null = ยังไม่ได้เลือก)
    private boolean updatingCombo;   // กันไม่ให้ event ทำงานซ้อนระหว่างเติมรายการใน combo

    public BookingPanel(MemberService memberService, BookingService bookingService, PaymentService paymentService) {
        super(new BorderLayout(10, 10));
        this.memberService = memberService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        schedulePanel = new CourtSchedulePanel(bookingService);
        schedulePanel.setSlotClickListener(this::onSlotClicked);

        add(createTopBar(), BorderLayout.NORTH);
        add(schedulePanel, BorderLayout.CENTER);
        add(createForm(), BorderLayout.EAST);

        // เมื่อค่าในฟอร์มเปลี่ยน ให้คำนวณสรุปราคาใหม่ทันที
        dateField.addChangeListener(this::onDateChanged);
        courtCombo.addActionListener(e -> updatePreview());
        startTimeCombo.addActionListener(e -> updatePreview());
        hoursSpinner.addChangeListener(e -> updatePreview());
        usePackageCheck.addActionListener(e -> updatePreview());
        shoeSpinner.addChangeListener(e -> updatePreview());
        confirmButton.addActionListener(e -> confirmBooking());

        onDateChanged();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        JLabel label = new JLabel("วันที่ใช้สนาม:");
        label.setFont(UiHelper.font(Font.BOLD, 15));
        bar.add(label);
        bar.add(dateField);
        bar.add(openingHoursLabel);
        return bar;
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new GridBagLayout());
        int row = 0;

        // ---- สมาชิก ----
        JPanel memberRow = new JPanel(new BorderLayout(4, 0));
        memberRow.setOpaque(false);
        JButton findMemberButton = new JButton("ตรวจสอบ");
        findMemberButton.addActionListener(e -> lookupMember());
        memberIdField.addActionListener(e -> lookupMember());
        memberIdField.setToolTipText("เช่น M001 แล้วกด Enter");
        memberRow.add(memberIdField, BorderLayout.CENTER);
        memberRow.add(findMemberButton, BorderLayout.EAST);
        UiHelper.addFormRow(form, row++, "รหัสสมาชิก:", memberRow);
        memberInfoLabel.setForeground(UiHelper.MUTED);
        UiHelper.addFormWide(form, row++, memberInfoLabel);

        // ---- สนามและเวลา ----
        for (int i = 1; i <= BookingService.TOTAL_COURTS; i++) {
            courtCombo.addItem(i);
        }
        UiHelper.addFormRow(form, row++, "สนาม:", courtCombo);
        UiHelper.addFormRow(form, row++, "เวลาเริ่ม:", startTimeCombo);
        UiHelper.addFormRow(form, row++, "จำนวนชั่วโมง:", hoursSpinner);
        UiHelper.addFormWide(form, row++, usePackageCheck);
        UiHelper.addFormRow(form, row++, "เช่ารองเท้า (คู่):", shoeSpinner);
        UiHelper.addFormRow(form, row++, "เลขอ้างอิงสลิป:", slipField);
        slipField.setToolTipText("เช่น slips/0001.jpg หรือ CASH (ไม่ต้องกรอกถ้ายอดชำระเป็น 0)");

        // ---- สรุปราคา ----
        summaryLabel.setVerticalAlignment(JLabel.TOP);
        UiHelper.addFormWide(form, row++, UiHelper.section("สรุปการจอง", summaryLabel));

        JButton clearButton = new JButton("ล้างฟอร์ม");
        clearButton.addActionListener(e -> clearForm());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        buttons.add(clearButton);
        buttons.add(confirmButton);
        UiHelper.addFormWide(form, row, buttons);

        // วางฟอร์มชิดบน และใส่ใน JScrollPane: ถ้าหน้าจอเตี้ยกว่าฟอร์ม จะเลื่อนดูได้ (ช่องกรอกไม่ถูกบีบ)
        JPanel topAligned = new WidthTrackingPanel();
        topAligned.add(form, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(topAligned);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel wrapper = UiHelper.section("ข้อมูลการจอง", scrollPane);
        wrapper.setPreferredSize(new Dimension(400, 0));
        return wrapper;
    }

    // =====================================================================
    // การตอบสนองต่อผู้ใช้
    // =====================================================================

    /** เปลี่ยนวันที่ -> อัปเดตเวลาทำการ รายการเวลาเริ่ม และตารางสนาม */
    private void onDateChanged() {
        LocalDate date = dateField.getDateOrNull();
        if (date == null) {
            openingHoursLabel.setText("รูปแบบวันที่ไม่ถูกต้อง (ปี-เดือน-วัน)");
            openingHoursLabel.setForeground(UiHelper.ERROR);
            updatePreview();
            return;
        }
        LocalTime open = bookingService.getOpeningTime(date);
        LocalTime close = bookingService.getClosingTime(date);
        openingHoursLabel.setForeground(UiHelper.PRIMARY_DARK);
        openingHoursLabel.setText("วัน" + FormatHelper.thaiDayName(date)
                + (bookingService.isPublicHoliday(date) ? " (วันหยุดนักขัตฤกษ์)" : "")
                + "  เปิด " + open + " - " + close);

        // เติมรายการเวลาเริ่มตามเวลาทำการของวันนั้น (พยายามคงเวลาที่เลือกไว้เดิม)
        Object previous = startTimeCombo.getSelectedItem();
        updatingCombo = true;
        startTimeCombo.removeAllItems();
        for (int hour = open.getHour(); hour < close.getHour(); hour++) {
            startTimeCombo.addItem(String.format("%02d:00", hour));
        }
        if (previous != null) {
            startTimeCombo.setSelectedItem(previous);
        }
        updatingCombo = false;

        schedulePanel.showDate(date);
        updatePreview();
    }

    /** คลิกช่องในตารางสนาม -> เลือกสนามและเวลาเริ่มให้ในฟอร์ม */
    private void onSlotClicked(int court, int hour) {
        courtCombo.setSelectedItem(court);
        startTimeCombo.setSelectedItem(String.format("%02d:00", hour));
        updatePreview();
    }

    /** ค้นหาสมาชิกจากรหัสที่กรอก */
    private void lookupMember() {
        String memberId = memberIdField.getText().trim();
        currentMember = memberId.isEmpty() ? null : memberService.findById(memberId);
        if (currentMember == null) {
            memberInfoLabel.setForeground(UiHelper.ERROR);
            memberInfoLabel.setText(memberId.isEmpty() ? "กรุณากรอกรหัสสมาชิก"
                    : "ไม่พบสมาชิก " + memberId + " (ต้องสมัครสมาชิกก่อน)");
        } else {
            memberInfoLabel.setForeground(UiHelper.PRIMARY_DARK);
            memberInfoLabel.setText(UiHelper.html("<b>" + currentMember.getFullName() + "</b>"
                    + "\nชั่วโมงสะสม: " + currentMember.getQualifyingHours() + " ชม."
                    + " | แพ็กเกจคงเหลือ: " + currentMember.getRemainingPackageHours() + " ชม."));
        }
        updatePreview();
    }

    /** อ่านชั่วโมงเริ่มจาก combo เช่น "13:00" -> 13 (คืน -1 ถ้ายังไม่ได้เลือก) */
    private int getSelectedStartHour() {
        Object item = startTimeCombo.getSelectedItem();
        if (item == null) {
            return -1;
        }
        return Integer.parseInt(item.toString().substring(0, 2));
    }

    /**
     * คำนวณและแสดงสรุปการจองทันทีที่ข้อมูลในฟอร์มเปลี่ยน
     * ถ้าข้อมูลยังไม่ถูกต้อง จะแสดงเหตุผลเป็นสีแดง และปิดปุ่ม "ยืนยันการจอง"
     */
    private void updatePreview() {
        if (updatingCombo) {
            return;
        }
        LocalDate date = dateField.getDateOrNull();
        int court = (Integer) courtCombo.getSelectedItem();
        int startHour = getSelectedStartHour();
        int hours = (Integer) hoursSpinner.getValue();
        schedulePanel.setSelection(court, startHour, hours);

        // ช่องใช้แพ็กเกจ เลือกได้เฉพาะเมื่อสมาชิกมีชั่วโมงแพ็กเกจเหลือ
        boolean hasPackage = currentMember != null && currentMember.getRemainingPackageHours() > 0;
        usePackageCheck.setEnabled(hasPackage);
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

        // ---- คำนวณราคา ----
        StringBuilder text = new StringBuilder();
        text.append("สนาม ").append(court).append(" | ").append(FormatHelper.formatDate(date)).append("\n");
        text.append(String.format("เวลา %02d:00 - %02d:00 (%d ชม.)", startHour, startHour + hours, hours)).append("\n");
        double courtFee = 0;
        if (usePackage) {
            text.append("ค่าสนาม: 0.00 บาท (ใช้แพ็กเกจ, คงเหลือหลังจอง ")
                    .append(remaining - hours).append(" ชม.)\n");
        } else {
            courtFee = paymentService.calculateCourtFee(currentMember, hours);
            int discounted = paymentService.countDiscountedHours(currentMember, hours);
            text.append("ค่าสนาม: ").append(FormatHelper.money(courtFee)).append("\n");
            if (discounted > 0) {
                text.append("<font color='#2E7D32'>ได้ส่วนลดชั่วโมงที่ 20: จ่าย 30% (")
                        .append(FormatHelper.money(paymentService.getDiscountedHourPrice()))
                        .append(") จำนวน ").append(discounted).append(" ชม.</font>\n");
            }
        }
        int pairs = (Integer) shoeSpinner.getValue();
        double shoeFee = ShoeRental.calculateFee(pairs, 1);
        text.append("ค่าเช่ารองเท้า: ").append(FormatHelper.money(shoeFee))
                .append(" (").append(pairs).append(" คู่ x 50)\n");
        text.append("<b><font size='+1'>ยอดชำระรวม: ").append(FormatHelper.money(courtFee + shoeFee))
                .append("</font></b>");

        summaryLabel.setForeground(java.awt.Color.BLACK);
        summaryLabel.setText(UiHelper.html(text.toString(), SUMMARY_WIDTH));
        confirmButton.setEnabled(true);
    }

    private void showSummaryError(String message) {
        summaryLabel.setForeground(UiHelper.ERROR);
        summaryLabel.setText(UiHelper.html(message, SUMMARY_WIDTH));
        confirmButton.setEnabled(false);
    }

    /** บันทึกการจอง (หลังผ่านการตรวจสอบทั้งหมด) */
    private void confirmBooking() {
        LocalDate date;
        try {
            date = dateField.getDate();
        } catch (IllegalArgumentException e) {
            UiHelper.showError(this, e.getMessage());
            return;
        }
        int court = (Integer) courtCombo.getSelectedItem();
        int startHour = getSelectedStartHour();
        int hours = (Integer) hoursSpinner.getValue();
        boolean usePackage = usePackageCheck.isSelected();
        int pairs = (Integer) shoeSpinner.getValue();

        double courtFee = usePackage ? 0 : paymentService.calculateCourtFee(currentMember, hours);
        double total = courtFee + ShoeRental.calculateFee(pairs, 1);
        String slip = slipField.getText().trim();
        if (total > 0 && slip.isEmpty()) {
            UiHelper.showError(this, "กรุณากรอกเลขอ้างอิงสลิป หรือพิมพ์ CASH ถ้าชำระเงินสด");
            slipField.requestFocus();
            return;
        }
        if (total == 0) {
            slip = "-";
        }
        if (!UiHelper.confirm(this, "ยืนยันการจอง", "ยืนยันการจองสนาม " + court + " วันที่ " + date
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
            UiHelper.showText(this, "จองสำเร็จ", "จองสำเร็จ!\n\n" + booking.getDetailsText());
            slipField.setText("");
            shoeSpinner.setValue(0);
            schedulePanel.reload();
            lookupMember(); // อัปเดตชั่วโมงสะสม/แพ็กเกจคงเหลือของสมาชิก
        } catch (IllegalArgumentException e) {
            UiHelper.showError(this, e.getMessage());
        }
    }

    private void clearForm() {
        memberIdField.setText("");
        currentMember = null;
        memberInfoLabel.setForeground(UiHelper.MUTED);
        memberInfoLabel.setText("ยังไม่ได้เลือกสมาชิก");
        hoursSpinner.setValue(1);
        shoeSpinner.setValue(0);
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

    /**
     * Panel ที่ "กว้างเท่ากับพื้นที่ของ JScrollPane เสมอ" (เลื่อนได้เฉพาะแนวตั้ง)
     * ถ้าไม่ทำแบบนี้ ฟอร์มที่กว้างกว่าพื้นที่จะถูกตัดขอบขวาทิ้ง
     */
    private static class WidthTrackingPanel extends JPanel implements Scrollable {
        WidthTrackingPanel() {
            super(new BorderLayout());
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true; // กว้างเท่าพื้นที่แสดงผลเสมอ
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false; // สูงตามเนื้อหา (เลื่อนแนวตั้งได้)
        }
    }
}
