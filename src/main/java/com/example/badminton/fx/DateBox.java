package com.example.badminton.fx;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * ช่องเลือกวันที่ (เทียบเท่า DateField ของ Swing แต่มีปฏิทินให้กดเลือก)
 * ประกอบด้วย: ปุ่ม "<" (วันก่อนหน้า) | DatePicker (พิมพ์ ปี-เดือน-วัน หรือกดเลือกจากปฏิทิน)
 *            | ปุ่ม ">" (วันถัดไป) | ปุ่ม "วันนี้"
 * ถ้าพิมพ์ปีเป็น พ.ศ. (เช่น 2569) จะแปลงเป็น ค.ศ. ให้อัตโนมัติ
 *
 * ใช้ใน FXML ได้โดยตรง: <DateBox fx:id="dateBox"/> (เริ่มต้นที่วันนี้ เปลี่ยนด้วย setDate())
 * หน้าจออื่นสามารถ "ฟัง" การเปลี่ยนวันที่ได้ด้วย addChangeListener()
 */
public class DateBox extends HBox {

    private final DatePicker picker = new DatePicker();
    private final List<Runnable> changeListeners = new ArrayList<>();

    public DateBox() {
        super(4);
        setAlignment(Pos.CENTER_LEFT);

        // บังคับปฏิทินแบบ ค.ศ. (เครื่องที่ตั้งภาษาไทยจะแสดงเป็น พ.ศ. ถ้าไม่ตั้งค่านี้)
        picker.setChronology(IsoChronology.INSTANCE);
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : date.toString(); // รูปแบบ ปี-เดือน-วัน
            }

            @Override
            public LocalDate fromString(String text) {
                return parseOrNull(text);
            }
        });
        picker.setPrefWidth(150);
        picker.setTooltip(new Tooltip("รูปแบบ ปี-เดือน-วัน เช่น 2026-10-12"));
        picker.setValue(LocalDate.now());
        // ใส่ข้อความเองด้วย: DatePicker จะเติมข้อความในช่องก็ต่อเมื่อแสดงบนจอแล้ว
        // แต่ getDate() อ่านจากข้อความ และหน้าจออาจเรียกก่อนหน้าต่างแสดง (เช่น ใน initialize())
        picker.getEditor().setText(LocalDate.now().toString());

        Button previousButton = new Button("<");
        Button nextButton = new Button(">");
        Button todayButton = new Button("วันนี้");
        previousButton.setTooltip(new Tooltip("วันก่อนหน้า"));
        nextButton.setTooltip(new Tooltip("วันถัดไป"));

        previousButton.setOnAction(e -> shiftDays(-1));
        nextButton.setOnAction(e -> shiftDays(1));
        todayButton.setOnAction(e -> setDate(LocalDate.now()));
        // เลือกจากปฏิทิน หรือพิมพ์แล้วกด Enter -> ค่าเปลี่ยน
        picker.valueProperty().addListener((obs, oldValue, newValue) -> fireChanged());
        // ออกจากช่องพิมพ์ -> แจ้งด้วย (เผื่อพิมพ์รูปแบบผิดซึ่งค่าจะไม่เปลี่ยน)
        picker.getEditor().focusedProperty().addListener((obs, wasFocused, focused) -> {
            if (!focused) {
                fireChanged();
            }
        });

        getChildren().addAll(previousButton, picker, nextButton, todayButton);
    }

    /**
     * อ่านวันที่จากข้อความในช่อง (ตรรกะเดียวกับ DateField.getDate())
     * ถ้ารูปแบบผิด จะโยน IllegalArgumentException พร้อมข้อความภาษาไทย
     */
    public LocalDate getDate() {
        try {
            LocalDate date = LocalDate.parse(picker.getEditor().getText().trim());
            if (date.getYear() >= 2400) {
                date = date.minusYears(543); // แปลง พ.ศ. -> ค.ศ.
            }
            return date;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("รูปแบบวันที่ไม่ถูกต้อง ต้องเป็น ปี-เดือน-วัน เช่น 2026-10-12");
        }
    }

    /** อ่านวันที่ ถ้ารูปแบบผิดคืน null (ใช้ในจุดที่ไม่ต้องการแสดงข้อผิดพลาดทันที) */
    public LocalDate getDateOrNull() {
        try {
            return getDate();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setDate(LocalDate date) {
        picker.getEditor().setText(date.toString());
        if (date.equals(picker.getValue())) {
            fireChanged(); // ค่าเดิม DatePicker จะไม่แจ้ง จึงแจ้งเอง
        } else {
            picker.setValue(date); // valueProperty listener แจ้งให้
        }
    }

    private void shiftDays(int days) {
        LocalDate date = getDateOrNull();
        if (date == null) {
            date = LocalDate.now();
        }
        setDate(date.plusDays(days));
    }

    /** ใช้กับข้อความที่พิมพ์ใน DatePicker: แปลงได้คืนวันที่ (แปลง พ.ศ. ให้ด้วย) แปลงไม่ได้คืน null */
    private static LocalDate parseOrNull(String text) {
        try {
            LocalDate date = LocalDate.parse(text.trim());
            return date.getYear() >= 2400 ? date.minusYears(543) : date;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** ลงทะเบียนโค้ดที่จะให้ทำงานเมื่อวันที่เปลี่ยน */
    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    private void fireChanged() {
        for (Runnable listener : changeListeners) {
            listener.run();
        }
    }
}
