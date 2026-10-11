package com.example.badminton.model;

/**
 * คลาส Product แทน "สินค้าอุปกรณ์แบดมินตัน" ที่ขายในร้าน เช่น ไม้แบด ลูกขนไก่ รองเท้า
 * (ราคากำหนดเองได้ตามที่โจทย์อนุญาต ดูตัวอย่างใน BadmintonSystem.loadSampleData())
 *
 * ความสัมพันธ์กับคลาสอื่น:
 * - ProductService เก็บรายการสินค้าทั้งหมด
 * - SaleItem อ้างอิง Product เพื่อรู้ว่าขายสินค้าอะไร
 */
public class Product {

    private final String productId;
    private final String name;
    private double unitPrice;

    public Product(String productId, String name, double unitPrice) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("ชื่อสินค้าห้ามว่าง");
        }
        this.productId = productId;
        this.name = name.trim();
        setUnitPrice(unitPrice);
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    /** Encapsulation: เปลี่ยนราคาได้ แต่ต้องผ่านการตรวจว่าราคามากกว่า 0 */
    public void setUnitPrice(double unitPrice) {
        if (unitPrice <= 0) {
            throw new IllegalArgumentException("ราคาสินค้าต้องมากกว่า 0");
        }
        this.unitPrice = unitPrice;
    }

    @Override
    public String toString() {
        return productId + " | " + name + " | " + String.format("%,.2f บาท", unitPrice);
    }
}
