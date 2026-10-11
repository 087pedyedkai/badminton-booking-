package com.example.badminton.model;

/**
 * คลาส SaleItem แทน "สินค้า 1 บรรทัดในใบเสร็จ" เช่น ลูกขนไก่ x 2 หลอด
 * เก็บ รหัสสินค้า ชื่อ ราคาต่อหน่วย จำนวน และคำนวณราคารวมของบรรทัดนี้ได้
 *
 * เหตุผลที่เก็บราคาต่อหน่วยไว้ในตัวเอง (unitPrice):
 * ถ้าผู้ดูแลระบบเปลี่ยนราคาสินค้าภายหลัง ยอดขายเก่าจะยังคงเป็นราคาเดิมตอนที่ขายจริง
 *
 * ความสัมพันธ์: Sale 1 รายการ มี SaleItem หลายบรรทัด และ SaleItem อ้างอิง Product
 */
public class SaleItem {

    private final Product product;
    private final double unitPrice;
    private final int quantity;

    public SaleItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("ไม่พบสินค้า");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("จำนวนสินค้าต้องมากกว่า 0");
        }
        this.product = product;
        this.unitPrice = product.getUnitPrice();
        this.quantity = quantity;
    }

    /** ราคารวมของบรรทัดนี้ = ราคาต่อหน่วย x จำนวน */
    public double getTotalPrice() {
        return unitPrice * quantity;
    }

    public Product getProduct() {
        return product;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return product.getProductId() + " " + product.getName()
                + " | " + String.format("%,.2f", unitPrice) + " x " + quantity
                + " = " + String.format("%,.2f บาท", getTotalPrice());
    }
}
