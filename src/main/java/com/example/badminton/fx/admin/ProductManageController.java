package com.example.badminton.fx.admin;

import com.example.badminton.BadmintonSystem;
import com.example.badminton.fx.FxHelper;
import com.example.badminton.fx.Refreshable;
import com.example.badminton.model.Product;
import com.example.badminton.service.ProductService;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controller แท็บ "จัดการสินค้า" ของผู้ดูแลระบบ (admin/product-manage.fxml)
 * ดูรายการสินค้า (ProductService.getAllProducts) เพิ่มสินค้าใหม่ (ProductService.addProduct) และแก้ไขราคา
 * การตรวจสอบราคา (ต้องมากกว่า 0) ทำใน Product.setUnitPrice() -> ตัวอย่าง Encapsulation
 */
public class ProductManageController implements Refreshable {

    private final ProductService productService;

    @FXML private Parent root;
    @FXML private TableView<Product> table;
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField newPriceField;

    public ProductManageController(BadmintonSystem system) {
        this.productService = system.getProductService();
    }

    @FXML
    private void initialize() {
        FxHelper.setColumnValues(table,
                Product::getProductId, Product::getName, p -> String.format("%,.2f", p.getUnitPrice()));
        refresh();
    }

    /** แปลงข้อความเป็นราคา ถ้าไม่ใช่ตัวเลขโยน IllegalArgumentException */
    private double parsePrice(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ราคาต้องเป็นตัวเลข");
        }
    }

    @FXML
    private void addProduct() {
        try {
            Product product = productService.addProduct(nameField.getText(), parsePrice(priceField.getText()));
            nameField.setText("");
            priceField.setText("");
            refresh();
            FxHelper.showInfo(root, "เพิ่มสินค้าสำเร็จ", "เพิ่มสินค้าสำเร็จ: " + product);
        } catch (IllegalArgumentException e) {
            FxHelper.showError(root, e.getMessage());
        }
    }

    @FXML
    private void changePrice() {
        Product product = table.getSelectionModel().getSelectedItem();
        if (product == null) {
            FxHelper.showError(root, "กรุณาเลือกสินค้าในตารางก่อน");
            return;
        }
        try {
            product.setUnitPrice(parsePrice(newPriceField.getText()));
            newPriceField.setText("");
            refresh();
        } catch (IllegalArgumentException e) {
            FxHelper.showError(root, e.getMessage());
        }
    }

    @Override
    public void refresh() {
        table.getItems().setAll(productService.getAllProducts());
        table.refresh(); // บังคับวาดใหม่ เพราะแก้ราคาในออบเจกต์เดิม (รายการเดิมแต่ค่าเปลี่ยน)
    }
}
