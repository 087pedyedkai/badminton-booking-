package com.example.badminton.fx.admin;

import com.example.badminton.BadmintonSystem;
import com.example.badminton.fx.DateBox;
import com.example.badminton.fx.FxHelper;
import com.example.badminton.fx.Refreshable;
import com.example.badminton.service.BookingService;
import com.example.badminton.util.FormatHelper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ListView;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Controller แท็บ "วันหยุดนักขัตฤกษ์" ของผู้ดูแลระบบ (admin/holiday.fxml)
 * Service ที่เรียก: BookingService.addPublicHoliday() / getPublicHolidays()
 * วันที่อยู่ในรายการนี้จะใช้เวลาทำการแบบวันหยุด 10:00-20:00 (ดู BookingService.isWeekendOrHoliday())
 */
public class HolidayController implements Refreshable {

    private final BookingService bookingService;

    @FXML private Parent root;
    @FXML private DateBox dateBox;
    @FXML private ListView<String> holidayList;

    public HolidayController(BadmintonSystem system) {
        this.bookingService = system.getBookingService();
    }

    @FXML
    private void initialize() {
        refresh();
    }

    @FXML
    private void addHoliday() {
        try {
            LocalDate date = dateBox.getDate();
            if (bookingService.addPublicHoliday(date)) {
                refresh();
            } else {
                FxHelper.showError(root, "วันที่ " + date + " เป็นวันหยุดอยู่แล้ว");
            }
        } catch (IllegalArgumentException e) {
            FxHelper.showError(root, e.getMessage());
        }
    }

    @Override
    public void refresh() {
        List<LocalDate> holidays = bookingService.getPublicHolidays();
        Collections.sort(holidays); // เรียงจากวันที่เก่าไปใหม่
        holidayList.getItems().clear();
        for (LocalDate date : holidays) {
            holidayList.getItems().add(FormatHelper.formatDate(date));
        }
    }
}
