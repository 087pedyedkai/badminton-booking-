package com.example.badminton.gui.admin;

import com.example.badminton.gui.Refreshable;
import com.example.badminton.gui.UiHelper;
import com.example.badminton.model.Payment;
import com.example.badminton.service.PaymentService;
import com.example.badminton.util.FormatHelper;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * แท็บ "รายการรับเงิน" (ผู้ดูแลระบบ): แสดง Payment ทุกรายการพร้อมเลขสลิป
 * ใช้ตรวจสอบว่ารายได้ในรายงานมาจากรายการไหน (แต่ละรายการอ้างอิงรายการต้นทางเพียงครั้งเดียว)
 */
public class PaymentsPanel extends JPanel implements Refreshable {

    private final PaymentService paymentService;
    private final DefaultTableModel tableModel = UiHelper.readOnlyModel(
            "รหัส", "วันที่รับเงิน", "หมวด", "อ้างอิง", "สมาชิก", "จำนวนเงิน (บาท)", "สลิป");
    private final JLabel totalLabel = new JLabel();

    public PaymentsPanel(PaymentService paymentService) {
        super(new BorderLayout(8, 8));
        this.paymentService = paymentService;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        totalLabel.setFont(UiHelper.font(Font.BOLD, 16));
        totalLabel.setForeground(UiHelper.PRIMARY_DARK);

        add(UiHelper.scroll(UiHelper.table(tableModel)), BorderLayout.CENTER);
        add(totalLabel, BorderLayout.SOUTH);
        refresh();
    }

    @Override
    public void refresh() {
        tableModel.setRowCount(0);
        double total = 0;
        for (Payment p : paymentService.getAllPayments()) {
            tableModel.addRow(new Object[]{
                    p.getPaymentId(), p.getPaymentDate(), p.getType().getThaiName(), p.getReferenceId(),
                    p.getMember() == null ? "ลูกค้าทั่วไป" : p.getMember().getMemberId() + " " + p.getMember().getFullName(),
                    String.format("%,.2f", p.getAmount()), p.getSlipReference()
            });
            total += p.getAmount();
        }
        totalLabel.setText("รวม " + tableModel.getRowCount() + " รายการ | ยอดเงินรวม " + FormatHelper.money(total));
    }
}
