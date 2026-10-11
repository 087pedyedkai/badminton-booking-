package com.example.badminton.fx.admin;

import com.example.badminton.BadmintonSystem;
import com.example.badminton.fx.FxHelper;
import com.example.badminton.fx.Refreshable;
import com.example.badminton.model.Payment;
import com.example.badminton.service.PaymentService;
import com.example.badminton.util.FormatHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.util.List;

/**
 * Controller แท็บ "รายการรับเงิน" ของผู้ดูแลระบบ (admin/payments.fxml)
 * แสดง Payment ทุกรายการพร้อมเลขสลิป จาก PaymentService.getAllPayments()
 * ใช้ตรวจสอบว่ารายได้ในรายงานมาจากรายการไหน (แต่ละรายการอ้างอิงรายการต้นทางเพียงครั้งเดียว)
 */
public class PaymentsController implements Refreshable {

    private final PaymentService paymentService;

    @FXML private TableView<Payment> table;
    @FXML private Label totalLabel;

    public PaymentsController(BadmintonSystem system) {
        this.paymentService = system.getPaymentService();
    }

    @FXML
    private void initialize() {
        FxHelper.setColumnValues(table,
                Payment::getPaymentId, Payment::getPaymentDate, p -> p.getType().getThaiName(), Payment::getReferenceId,
                p -> p.getMember() == null ? "ลูกค้าทั่วไป" : p.getMember().getMemberId() + " " + p.getMember().getFullName(),
                p -> String.format("%,.2f", p.getAmount()), Payment::getSlipReference);
        refresh();
    }

    @Override
    public void refresh() {
        List<Payment> payments = paymentService.getAllPayments();
        table.getItems().setAll(payments);
        double total = 0;
        for (Payment p : payments) {
            total += p.getAmount();
        }
        totalLabel.setText("รวม " + payments.size() + " รายการ | ยอดเงินรวม " + FormatHelper.money(total));
    }
}
