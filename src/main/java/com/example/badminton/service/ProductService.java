package com.example.badminton.service;

import com.example.badminton.model.Member;
import com.example.badminton.model.PaymentType;
import com.example.badminton.model.Product;
import com.example.badminton.model.Sale;
import com.example.badminton.model.SaleItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductService ดูแล "สินค้าอุปกรณ์แบดมินตัน" และ "การขาย"
 *
 * ความสัมพันธ์กับคลาสอื่น:
 * - เก็บ Product (รายการสินค้า) และ Sale (ประวัติการขาย)
 * - เมื่อขายสินค้า จะเรียก PaymentService.recordPayment() เพื่อบันทึกรายได้หมวด "ขายอุปกรณ์"
 *   ทำให้รายงานรายได้เห็นยอดขายอุปกรณ์แยกจากค่าเช่ารองเท้า
 */
public class ProductService {

    private final List<Product> products = new ArrayList<>();
    private final List<Sale> sales = new ArrayList<>();
    private final PaymentService paymentService;
    private int nextProductNumber = 1;
    private int nextSaleNumber = 1;

    public ProductService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** เพิ่มสินค้าใหม่ ออกรหัสอัตโนมัติ (P01, P02, ...) ราคาต้องมากกว่า 0 */
    public Product addProduct(String name, double unitPrice) {
        String productId = String.format("P%02d", nextProductNumber);
        Product product = new Product(productId, name, unitPrice); // constructor ตรวจราคาให้
        products.add(product);
        nextProductNumber++;
        return product;
    }

    public Product findProductById(String productId) {
        for (Product product : products) {
            if (product.getProductId().equalsIgnoreCase(productId.trim())) {
                return product;
            }
        }
        return null;
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    /**
     * บันทึกการขาย 1 ใบเสร็จ
     * ขั้นตอน: สร้าง Sale จากรายการสินค้า -> บันทึกรายได้ผ่าน PaymentService -> เก็บประวัติการขาย
     * member เป็น null ได้ (ลูกค้าทั่วไปซื้ออุปกรณ์ได้ ไม่จำเป็นต้องเป็นสมาชิก)
     */
    public Sale createSale(Member member, List<SaleItem> items, String slipReference) {
        String saleId = String.format("S%03d", nextSaleNumber);
        Sale sale = new Sale(saleId, LocalDate.now(), member, items, slipReference);
        paymentService.recordPayment(PaymentType.EQUIPMENT_SALE, sale.getTotalAmount(), member, saleId, slipReference);
        sales.add(sale);
        nextSaleNumber++;
        return sale;
    }

    public List<Sale> getAllSales() {
        return new ArrayList<>(sales);
    }
}
