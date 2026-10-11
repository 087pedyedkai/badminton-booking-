package com.example.badminton.gui.cashier;

import com.example.badminton.gui.Refreshable;
import com.example.badminton.gui.UiHelper;
import com.example.badminton.model.Member;
import com.example.badminton.model.Product;
import com.example.badminton.model.Sale;
import com.example.badminton.model.SaleItem;
import com.example.badminton.service.MemberService;
import com.example.badminton.service.ProductService;
import com.example.badminton.util.FormatHelper;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * แท็บ "ขายอุปกรณ์": เลือกสินค้า ใส่จำนวน เพิ่มลงตะกร้า แล้วยืนยันการขาย
 * - ซ้าย: รายการสินค้า (ProductService.getAllProducts())
 * - ขวา: ตะกร้า (List<SaleItem>) พร้อมยอดรวม -> ProductService.createSale() บันทึกรายได้หมวด "ขายอุปกรณ์"
 */
public class SalesPanel extends JPanel implements Refreshable {

    private final MemberService memberService;
    private final ProductService productService;

    private final DefaultTableModel productModel = UiHelper.readOnlyModel("รหัส", "สินค้า", "ราคา (บาท)");
    private final JTable productTable = UiHelper.table(productModel);
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    private final DefaultTableModel cartModel = UiHelper.readOnlyModel("รหัส", "สินค้า", "ราคา/หน่วย", "จำนวน", "รวม (บาท)");
    private final JTable cartTable = UiHelper.table(cartModel);
    private final JLabel totalLabel = new JLabel();
    private final JTextField memberIdField = new JTextField(8);
    private final JTextField slipField = new JTextField(14);

    private List<Product> products = new ArrayList<>();
    private final List<SaleItem> cart = new ArrayList<>();

    public SalesPanel(MemberService memberService, ProductService productService) {
        super(new BorderLayout(8, 8));
        this.memberService = memberService;
        this.productService = productService;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cartTable.setAutoCreateRowSorter(false); // ตะกร้าไม่ต้องเรียง เพื่อให้ลำดับตรงกับ List

        // ---- ฝั่งสินค้า ----
        JButton addButton = UiHelper.primaryButton("เพิ่มลงตะกร้า >");
        addButton.addActionListener(e -> addToCart());
        JPanel addBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addBar.add(new JLabel("จำนวน:"));
        addBar.add(quantitySpinner);
        addBar.add(addButton);
        JPanel productSide = new JPanel(new BorderLayout(4, 4));
        productSide.add(UiHelper.scroll(productTable), BorderLayout.CENTER);
        productSide.add(addBar, BorderLayout.SOUTH);

        // ---- ฝั่งตะกร้า ----
        JButton removeButton = new JButton("ลบรายการที่เลือก");
        JButton clearButton = new JButton("ล้างตะกร้า");
        removeButton.addActionListener(e -> removeSelected());
        clearButton.addActionListener(e -> {
            cart.clear();
            updateCart();
        });
        JPanel cartButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cartButtons.add(removeButton);
        cartButtons.add(clearButton);

        JPanel checkout = new JPanel(new GridBagLayout());
        totalLabel.setFont(UiHelper.font(Font.BOLD, 22));
        totalLabel.setForeground(UiHelper.PRIMARY_DARK);
        UiHelper.addFormWide(checkout, 0, totalLabel);
        memberIdField.setToolTipText("เว้นว่างได้ ถ้าเป็นลูกค้าทั่วไป");
        UiHelper.addFormRow(checkout, 1, "รหัสสมาชิก (ไม่บังคับ):", memberIdField);
        UiHelper.addFormRow(checkout, 2, "เลขอ้างอิงสลิป:", slipField);
        JButton sellButton = UiHelper.primaryButton("ยืนยันการขาย");
        sellButton.addActionListener(e -> confirmSale());
        UiHelper.addFormWide(checkout, 3, sellButton);

        JPanel cartSide = new JPanel(new BorderLayout(4, 4));
        cartSide.add(cartButtons, BorderLayout.NORTH);
        cartSide.add(UiHelper.scroll(cartTable), BorderLayout.CENTER);
        cartSide.add(checkout, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                UiHelper.section("สินค้า (เลือกแถวแล้วกดเพิ่มลงตะกร้า หรือดับเบิลคลิก)", productSide),
                UiHelper.section("ตะกร้าสินค้า", cartSide));
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

        // ดับเบิลคลิกสินค้า = เพิ่มลงตะกร้า
        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addToCart();
                }
            }
        });

        refresh();
        updateCart();
    }

    private void addToCart() {
        int viewRow = productTable.getSelectedRow();
        if (viewRow < 0) {
            UiHelper.showError(this, "กรุณาเลือกสินค้าในตารางก่อน");
            return;
        }
        Product product = products.get(productTable.convertRowIndexToModel(viewRow));
        int quantity = (Integer) quantitySpinner.getValue();
        try {
            cart.add(new SaleItem(product, quantity)); // SaleItem ตรวจว่าจำนวนต้องมากกว่า 0
            quantitySpinner.setValue(1);
            updateCart();
        } catch (IllegalArgumentException e) {
            UiHelper.showError(this, e.getMessage());
        }
    }

    private void removeSelected() {
        int row = cartTable.getSelectedRow();
        if (row >= 0) {
            cart.remove(row);
            updateCart();
        }
    }

    /** แสดงตะกร้าและยอดรวมใหม่ */
    private void updateCart() {
        cartModel.setRowCount(0);
        double total = 0;
        for (SaleItem item : cart) {
            cartModel.addRow(new Object[]{
                    item.getProduct().getProductId(), item.getProduct().getName(),
                    String.format("%,.2f", item.getUnitPrice()), item.getQuantity(),
                    String.format("%,.2f", item.getTotalPrice())
            });
            total += item.getTotalPrice();
        }
        totalLabel.setText("ยอดรวม: " + FormatHelper.money(total));
    }

    private void confirmSale() {
        if (cart.isEmpty()) {
            UiHelper.showError(this, "ยังไม่มีสินค้าในตะกร้า");
            return;
        }
        Member member = null;
        String memberId = memberIdField.getText().trim();
        if (!memberId.isEmpty()) {
            member = memberService.findById(memberId);
            if (member == null) {
                UiHelper.showError(this, "ไม่พบสมาชิกรหัส " + memberId + " (เว้นว่างได้ถ้าเป็นลูกค้าทั่วไป)");
                return;
            }
        }
        String slip = slipField.getText().trim();
        if (slip.isEmpty()) {
            UiHelper.showError(this, "กรุณากรอกเลขอ้างอิงสลิป หรือพิมพ์ CASH ถ้าชำระเงินสด");
            return;
        }
        if (!UiHelper.confirm(this, "ยืนยันการขาย", totalLabel.getText() + "\nยืนยันการขายหรือไม่?")) {
            return;
        }
        try {
            Sale sale = productService.createSale(member, cart, slip);
            UiHelper.showText(this, "ขายสำเร็จ", sale.getReceiptText());
            cart.clear();
            memberIdField.setText("");
            slipField.setText("");
            updateCart();
        } catch (IllegalArgumentException e) {
            UiHelper.showError(this, e.getMessage());
        }
    }

    /** โหลดรายการสินค้าใหม่ (ผู้ดูแลระบบอาจเพิ่มสินค้าหรือแก้ราคา) */
    @Override
    public void refresh() {
        products = productService.getAllProducts();
        productModel.setRowCount(0);
        for (Product p : products) {
            productModel.addRow(new Object[]{p.getProductId(), p.getName(), String.format("%,.2f", p.getUnitPrice())});
        }
    }
}
