package com.example.badminton.fx;

import com.example.badminton.BadmintonSystem;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * FxHelper = รวมเครื่องมือเล็ก ๆ ที่ทุกหน้าจอ JavaFX ใช้ร่วมกัน (เทียบเท่า UiHelper ของ Swing)
 * - โหลดไฟล์ FXML พร้อมส่ง BadmintonSystem ให้ Controller
 * - ฟอนต์ภาษาไทย + style.css (สีและหน้าตาทั้งโปรแกรมอยู่ใน style.css ที่เดียว)
 * - กล่องข้อความแจ้งเตือน (Alert) และตัวช่วยตาราง
 */
public final class FxHelper {

    /** คลาสสีของข้อความที่สลับไปมาได้ (ดู style.css) */
    public static final String MUTED = "muted";
    public static final String ERROR = "error-text";
    public static final String OK = "ok-text";

    private static final String STYLESHEET = FxHelper.class.getResource("style.css").toExternalForm();
    private static String fontStyle = "";

    private FxHelper() {
        // คลาสนี้มีแต่เมธอด static จึงไม่ให้สร้างออบเจกต์
    }

    // ---------------- โหลดหน้าจอ ----------------

    /**
     * โหลดไฟล์ FXML (ตำแหน่งเทียบกับแพ็กเกจ fx เช่น "main.fxml" หรือ "cashier/booking.fxml")
     *
     * setControllerFactory: ทุก Controller มี constructor ที่รับ BadmintonSystem
     * FXMLLoader จึงสร้าง Controller ด้วย new XxxController(system) แทน constructor เปล่า
     * (fx:include ที่อยู่ในไฟล์นั้นก็ใช้ factory เดียวกันอัตโนมัติ)
     */
    public static FXMLLoader load(String fxml, BadmintonSystem system) {
        FXMLLoader loader = new FXMLLoader(FxHelper.class.getResource(fxml));
        loader.setControllerFactory(type -> {
            try {
                return type.getConstructor(BadmintonSystem.class).newInstance(system);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("สร้าง Controller " + type.getName() + " ไม่ได้", e);
            }
        });
        try {
            loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException("โหลดหน้าจอ " + fxml + " ไม่ได้", e);
        }
        return loader;
    }

    // ---------------- ฟอนต์และ style ----------------

    /**
     * เลือกฟอนต์ภาษาไทยตัวแรกที่มีในเครื่อง (ลำดับเดียวกับ UiHelper ของ Swing)
     * ต้องเลือกในโค้ด เพราะ CSS ของ JavaFX ไม่รองรับการเขียนรายการฟอนต์สำรองหลายตัว
     */
    public static void pickThaiFont() {
        String[] candidates = {"Leelawadee UI", "Tahoma", "Leelawadee", "Noto Sans Thai", "Sarabun"};
        List<String> available = Font.getFamilies();
        for (String name : candidates) {
            if (available.contains(name)) {
                fontStyle = "-fx-font-family: \"" + name + "\";";
                return;
            }
        }
    }

    /** ใส่ style.css และฟอนต์ไทยให้หน้าจอ (ใช้กับ root ของทุกหน้าต่าง) */
    public static void applyTheme(Parent root) {
        root.getStylesheets().add(STYLESHEET);
        root.setStyle(fontStyle);
    }

    /** เปลี่ยนสีข้อความของ Label เป็น MUTED / ERROR / OK (เอาสีเดิมออกก่อน) */
    public static void setTone(Label label, String toneClass) {
        label.getStyleClass().removeAll(MUTED, ERROR, OK);
        label.getStyleClass().add(toneClass);
    }

    // ---------------- ตาราง ----------------

    /**
     * กำหนดว่าคอลัมน์แต่ละคอลัมน์ (ตามลำดับใน FXML) ดึงค่าอะไรจากออบเจกต์ของแถว
     * เช่น setColumnValues(table, Member::getMemberId, Member::getFullName)
     * -> คอลัมน์ที่ 1 แสดงรหัส คอลัมน์ที่ 2 แสดงชื่อ
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> void setColumnValues(TableView<T> table, Function<T, ?>... getters) {
        for (int i = 0; i < getters.length; i++) {
            Function<T, ?> getter = getters[i];
            TableColumn<T, Object> column = (TableColumn<T, Object>) table.getColumns().get(i);
            column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(getter.apply(cell.getValue())));
        }
        table.setPlaceholder(new Label("ไม่มีข้อมูล"));
    }

    // ---------------- กล่องข้อความแจ้งเตือน ----------------

    public static void showError(Node owner, String message) {
        alert(owner, Alert.AlertType.ERROR, "เกิดข้อผิดพลาด", message).showAndWait();
    }

    public static void showInfo(Node owner, String title, String message) {
        alert(owner, Alert.AlertType.INFORMATION, title, message).showAndWait();
    }

    /** แสดงข้อความยาวหลายบรรทัด (เช่น ใบเสร็จ รายละเอียดการจอง) ในกล่องที่เลื่อนอ่านได้ */
    public static void showText(Node owner, String title, String text) {
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefColumnCount(48);
        area.setPrefRowCount(Math.min(16, text.split("\n").length + 1));
        Alert alert = alert(owner, Alert.AlertType.INFORMATION, title, null);
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }

    /** ถามยืนยัน คืน true ถ้ากด "Yes" */
    public static boolean confirm(Node owner, String title, String message) {
        Alert alert = alert(owner, Alert.AlertType.CONFIRMATION, title, message);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> answer = alert.showAndWait();
        return answer.isPresent() && answer.get() == ButtonType.YES;
    }

    private static Alert alert(Node owner, Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (owner != null && owner.getScene() != null) {
            alert.initOwner(owner.getScene().getWindow());
        }
        applyTheme(alert.getDialogPane());
        // ให้กล่องสูงพอแสดงข้อความหลายบรรทัดได้ครบ (ค่าเริ่มต้นของ JavaFX อาจตัดข้อความ)
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        return alert;
    }
}
