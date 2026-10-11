package com.example.badminton.gui.admin;

import com.example.badminton.gui.Refreshable;
import com.example.badminton.gui.UiHelper;
import com.example.badminton.model.Product;
import com.example.badminton.service.ProductService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * แท็บ "จัดการสินค้า" (ผู้ดูแลระบบ): ดูรายการสินค้า เพิ่มสินค้าใหม่ และแก้ไขราคา
 * การตรวจสอบราคา (ต้องมากกว่า 0) ทำใน Product.setUnitPrice() -> ตัวอย่าง Encapsulation
 */
public class ProductManagePanel extends JPanel implements Refreshable {

    private final ProductService productService;
    private final DefaultTableModel tableModel = UiHelper.readOnlyModel("รหัส", "ชื่อสินค้า", "ราคา (บาท)");
    private final JTable table = UiHelper.table(tableModel);
    private final JTextField nameField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextField newPriceField = new JTextField();
    private List<Product> products = new ArrayList<>();

    public ProductManagePanel(ProductService productService) {
        super(new BorderLayout(10, 10));
        this.productService = productService;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- ฟอร์มเพิ่มสินค้า ----
        JPanel addForm = new JPanel(new GridBagLayout());
        UiHelper.addFormRow(addForm, 0, "ชื่อสินค้า:", nameField);
        UiHelper.addFormRow(addForm, 1, "ราคา (บาท):", priceField);
        JButton addButton = UiHelper.primaryButton("เพิ่มสินค้า");
        addButton.addActionListener(e -> addProduct());
        UiHelper.addFormWide(addForm, 2, addButton);

        // ---- ฟอร์มแก้ราคา ----
        JPanel editForm = new JPanel(new GridBagLayout());
        UiHelper.addFormRow(editForm, 0, "ราคาใหม่ (บาท):", newPriceField);
        JButton editButton = new JButton("แก้ราคาสินค้าที่เลือก");
        editButton.addActionListener(e -> changePrice());
        UiHelper.addFormWide(editForm, 1, editButton);

        JPanel side = new JPanel(new BorderLayout(0, 10));
        side.add(UiHelper.section("เพิ่มสินค้าใหม่", addForm), BorderLayout.NORTH);
        side.add(UiHelper.section("แก้ไขราคา (เลือกสินค้าในตารางก่อน)", editForm), BorderLayout.CENTER);
        JPanel sideWrapper = new JPanel(new BorderLayout());
        sideWrapper.add(side, BorderLayout.NORTH);
        sideWrapper.setPreferredSize(new Dimension(360, 0));

        add(UiHelper.scroll(table), BorderLayout.CENTER);
        add(sideWrapper, BorderLayout.EAST);
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

    private void addProduct() {
        try {
            Product product = productService.addProduct(nameField.getText(), parsePrice(priceField.getText()));
            nameField.setText("");
            priceField.setText("");
            refresh();
            UiHelper.showInfo(this, "เพิ่มสินค้าสำเร็จ", "เพิ่มสินค้าสำเร็จ: " + product);
        } catch (IllegalArgumentException e) {
            UiHelper.showError(this, e.getMessage());
        }
    }

    private void changePrice() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            UiHelper.showError(this, "กรุณาเลือกสินค้าในตารางก่อน");
            return;
        }
        Product product = products.get(table.convertRowIndexToModel(viewRow));
        try {
            product.setUnitPrice(parsePrice(newPriceField.getText()));
            newPriceField.setText("");
            refresh();
        } catch (IllegalArgumentException e) {
            UiHelper.showError(this, e.getMessage());
        }
    }

    @Override
    public void refresh() {
        products = productService.getAllProducts();
        tableModel.setRowCount(0);
        for (Product p : products) {
            tableModel.addRow(new Object[]{p.getProductId(), p.getName(), String.format("%,.2f", p.getUnitPrice())});
        }
    }
}
