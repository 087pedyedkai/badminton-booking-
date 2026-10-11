package com.example.badminton.gui.admin;

import com.example.badminton.gui.DateField;
import com.example.badminton.gui.Refreshable;
import com.example.badminton.gui.UiHelper;
import com.example.badminton.service.BookingService;
import com.example.badminton.util.FormatHelper;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * แท็บ "วันหยุดนักขัตฤกษ์" (ผู้ดูแลระบบ)
 * วันที่อยู่ในรายการนี้จะใช้เวลาทำการแบบวันหยุด 10:00-20:00 (ดู BookingService.isWeekendOrHoliday())
 */
public class HolidayPanel extends JPanel implements Refreshable {

    private final BookingService bookingService;
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final DateField dateField = new DateField(LocalDate.now());

    public HolidayPanel(BookingService bookingService) {
        super(new BorderLayout(8, 8));
        this.bookingService = bookingService;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton addButton = UiHelper.primaryButton("เพิ่มวันหยุด");
        addButton.addActionListener(e -> addHoliday());
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        topBar.add(new JLabel("วันที่:"));
        topBar.add(dateField);
        topBar.add(addButton);

        JLabel note = new JLabel("วันหยุดนักขัตฤกษ์ เสาร์ และอาทิตย์ เปิด 10:00-20:00 | วันจันทร์-ศุกร์ปกติ เปิด 13:00-21:00");
        note.setForeground(UiHelper.MUTED);

        add(topBar, BorderLayout.NORTH);
        add(UiHelper.section("รายการวันหยุดนักขัตฤกษ์", UiHelper.scroll(new JList<>(listModel))), BorderLayout.CENTER);
        add(note, BorderLayout.SOUTH);
        refresh();
    }

    private void addHoliday() {
        try {
            LocalDate date = dateField.getDate();
            if (bookingService.addPublicHoliday(date)) {
                refresh();
            } else {
                UiHelper.showError(this, "วันที่ " + date + " เป็นวันหยุดอยู่แล้ว");
            }
        } catch (IllegalArgumentException e) {
            UiHelper.showError(this, e.getMessage());
        }
    }

    @Override
    public void refresh() {
        List<LocalDate> holidays = bookingService.getPublicHolidays();
        Collections.sort(holidays); // เรียงจากวันที่เก่าไปใหม่
        listModel.clear();
        for (LocalDate date : holidays) {
            listModel.addElement(FormatHelper.formatDate(date));
        }
    }
}
