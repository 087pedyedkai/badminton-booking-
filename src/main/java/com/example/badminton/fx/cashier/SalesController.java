package com.example.badminton.fx.cashier;

import com.example.badminton.BadmintonSystem;
import com.example.badminton.fx.FxHelper;
import com.example.badminton.fx.Refreshable;
import com.example.badminton.model.Member;
import com.example.badminton.model.Product;
import com.example.badminton.model.Sale;
import com.example.badminton.model.SaleItem;
import com.example.badminton.service.MemberService;
import com.example.badminton.service.ProductService;
import com.example.badminton.util.FormatHelper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller แท็บ "ขายอุปกรณ์" (cashier/sales.fxml): เลือกสินค้า ใส่จำนวน เพิ่มลงตะกร้า แล้วยืนยันการขาย
 * - ซ้าย: รายการสินค้า (ProductService.getAllProducts())
 * - ขวา: ตะกร้า (List<SaleItem>) พร้อมยอดรวม -> ProductService.createSale() บันทึกรายได้หมวด "ขายอุปกรณ์"
 * (รหัสสมาชิกไม่บังคับ ถ้ากรอกจะตรวจด้วย MemberService.findById())
 */
public class SalesController implements Refreshable {

    private final MemberService memberService;
    private final ProductService productService;

    @FXML private Parent root;
    @FXML private TableView<Product> productTable;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private TableView<SaleItem> cartTable;
    @FXML private Label totalLabel;
    @FXML private TextField memberIdField;
    @FXML private TextField slipField;

    private final List<SaleItem> cart = new ArrayList<>();

    public SalesController(BadmintonSystem system) {
        this.memberService = system.getMemberService();
        this.productService = system.getProductService();
    }

    @FXML
    private void initialize() {
        quantitySpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 100, 1));
        FxHelper.setColumnValues(productTable,
                Product::getProductId, Product::getName, p -> String.format("%,.2f", p.getUnitPrice()));
        FxHelper.setColumnValues(cartTable,
                item -> item.getProduct().getProductId(),
                item -> item.getProduct().getName(),
                item -> String.format("%,.2f", item.getUnitPrice()),
                SaleItem::getQuantity,
                item -> String.format("%,.2f", item.getTotalPrice()));

        // ดับเบิลคลิกสินค้า = เพิ่มลงตะกร้า (ใช้ row factory เพื่อให้นับเฉพาะคลิกบนแถวที่มีสินค้า)
        productTable.setRowFactory(table -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    addToCart();
                }
            });
            return row;
        });

        refresh();
        updateCart();
    }

    @FXML
    private void addToCart() {
        Product product = productTable.getSelectionModel().getSelectedItem();
        if (product == null) {
            FxHelper.showError(root, "กรุณาเลือกสินค้าในตารางก่อน");
            return;
        }
        int quantity = quantitySpinner.getValue();
        try {
            cart.add(new SaleItem(product, quantity)); // SaleItem ตรวจว่าจำนวนต้องมากกว่า 0
            quantitySpinner.getValueFactory().setValue(1);
            updateCart();
        } catch (IllegalArgumentException e) {
            FxHelper.showError(root, e.getMessage());
        }
    }

    @FXML
    private void removeSelected() {
        int row = cartTable.getSelectionModel().getSelectedIndex();
        if (row >= 0) {
            cart.remove(row); // ตะกร้าเรียงไม่ได้ ลำดับแถวจึงตรงกับลำดับใน List
            updateCart();
        }
    }

    @FXML
    private void clearCart() {
        cart.clear();
        updateCart();
    }

    /** แสดงตะกร้าและยอดรวมใหม่ */
    private void updateCart() {
        cartTable.getItems().setAll(cart);
        double total = 0;
        for (SaleItem item : cart) {
            total += item.getTotalPrice();
        }
        totalLabel.setText("ยอดรวม: " + FormatHelper.money(total));
    }

    @FXML
    private void confirmSale() {
        if (cart.isEmpty()) {
            FxHelper.showError(root, "ยังไม่มีสินค้าในตะกร้า");
            return;
        }
        Member member = null;
        String memberId = memberIdField.getText().trim();
        if (!memberId.isEmpty()) {
            member = memberService.findById(memberId);
            if (member == null) {
                FxHelper.showError(root, "ไม่พบสมาชิกรหัส " + memberId + " (เว้นว่างได้ถ้าเป็นลูกค้าทั่วไป)");
                return;
            }
        }
        String slip = slipField.getText().trim();
        if (slip.isEmpty()) {
            FxHelper.showError(root, "กรุณากรอกเลขอ้างอิงสลิป หรือพิมพ์ CASH ถ้าชำระเงินสด");
            return;
        }
        if (!FxHelper.confirm(root, "ยืนยันการขาย", totalLabel.getText() + "\nยืนยันการขายหรือไม่?")) {
            return;
        }
        try {
            Sale sale = productService.createSale(member, cart, slip);
            FxHelper.showText(root, "ขายสำเร็จ", sale.getReceiptText());
            cart.clear();
            memberIdField.setText("");
            slipField.setText("");
            updateCart();
        } catch (IllegalArgumentException e) {
            FxHelper.showError(root, e.getMessage());
        }
    }

    /** โหลดรายการสินค้าใหม่ (ผู้ดูแลระบบอาจเพิ่มสินค้าหรือแก้ราคา) */
    @Override
    public void refresh() {
        productTable.getItems().setAll(productService.getAllProducts());
        productTable.refresh(); // บังคับวาดใหม่ เผื่อราคาของออบเจกต์เดิมถูกแก้
    }
}
