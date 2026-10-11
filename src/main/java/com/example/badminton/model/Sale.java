package com.example.badminton.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * คลาส Sale แทน "การขายอุปกรณ์ 1 ครั้ง (1 ใบเสร็จ)" ซึ่งอาจมีสินค้าหลายรายการ
 *
 * ความสัมพันธ์กับคลาสอื่น:
 * - Sale 1 ใบ มี SaleItem หลายบรรทัด (Composition: SaleItem เป็นส่วนหนึ่งของ Sale)
 * - Sale อาจผูกกับ Member (ถ้าลูกค้าเป็นสมาชิก) หรือไม่ผูกก็ได้ (ลูกค้าทั่วไป = null)
 * - ProductService เป็นผู้สร้าง Sale และส่งให้ PaymentService บันทึกรายได้หมวด "ขายอุปกรณ์"
 */
public class Sale {

    private final String saleId;
    private final LocalDate saleDate;
    private final Member member;
    private final List<SaleItem> items;
    private final String slipReference;

    public Sale(String saleId, LocalDate saleDate, Member member, List<SaleItem> items, String slipReference) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("ต้องมีสินค้าอย่างน้อย 1 รายการ");
        }
        this.saleId = saleId;
        this.saleDate = saleDate;
        this.member = member;
        this.items = new ArrayList<>(items); // คัดลอกเก็บไว้ ภายนอกแก้ List เดิมก็ไม่กระทบใบเสร็จนี้
        this.slipReference = slipReference;
    }

    /** ยอดรวมทั้งใบเสร็จ = ผลรวมราคาทุกบรรทัด */
    public double getTotalAmount() {
        double total = 0;
        for (SaleItem item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public String getSaleId() {
        return saleId;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public Member getMember() {
        return member;
    }

    public List<SaleItem> getItems() {
        return new ArrayList<>(items);
    }

    public String getSlipReference() {
        return slipReference;
    }

    /** สร้างข้อความใบเสร็จ (ใช้ได้ทั้ง Console และ GUI) */
    public String getReceiptText() {
        StringBuilder text = new StringBuilder();
        text.append("ใบเสร็จขายอุปกรณ์ เลขที่ ").append(saleId).append(" วันที่ ").append(saleDate).append("\n");
        text.append("ลูกค้า: ").append(member == null ? "ลูกค้าทั่วไป" : member.getMemberId() + " - " + member.getFullName()).append("\n");
        for (SaleItem item : items) {
            text.append("  - ").append(item).append("\n");
        }
        text.append("ยอดรวม: ").append(String.format("%,.2f บาท", getTotalAmount())).append(" | สลิป: ").append(slipReference);
        return text.toString();
    }

    /** พิมพ์ใบเสร็จทาง Console */
    public void printReceipt() {
        System.out.println(getReceiptText());
    }
}
