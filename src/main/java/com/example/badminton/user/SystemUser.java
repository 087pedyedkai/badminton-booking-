package com.example.badminton.user;

import com.example.badminton.model.Member;
import com.example.badminton.service.MemberService;
import com.example.badminton.util.InputHelper;

/**
 * abstract class SystemUser = แม่แบบของ "ผู้ใช้งานระบบ" (พนักงาน)
 *
 * ทำไมเป็น abstract class?
 * - ผู้ใช้ทุกประเภทมีข้อมูลร่วมกัน (รหัสพนักงาน, ชื่อ) และพฤติกรรมร่วมกัน
 *   (แสดงข้อความต้อนรับ, ค้นหาสมาชิกจากรหัส) -> เขียนไว้ที่นี่ครั้งเดียว
 * - แต่ "เมนู" และ "ชื่อบทบาท" ของแต่ละประเภทต่างกัน -> ประกาศเป็น abstract method
 *   บังคับให้คลาสลูก (Cashier, Administrator) ต้องเขียนเอง
 * - ไม่มีผู้ใช้ที่เป็น "SystemUser เฉย ๆ" จึงห้ามสร้างออบเจกต์ของคลาสนี้โดยตรง
 *
 * Inheritance:  Cashier extends SystemUser,  Administrator extends SystemUser
 * Polymorphism: ใน Main ตัวแปรชนิด SystemUser เก็บได้ทั้ง Cashier และ Administrator
 *               เมื่อเรียก user.showMenu() จะได้เมนูที่ถูกต้องตามประเภทจริงของออบเจกต์
 *
 * หมายเหตุ: เป็นโปรแกรมเพื่อการเรียนรู้ จึง "จำลอง" การเข้าสู่ระบบด้วยการเลือกบทบาทจากเมนูหลัก
 * ไม่มีระบบรหัสผ่านจริง (ถ้าใช้งานจริงควรเพิ่มการยืนยันตัวตน)
 */
public abstract class SystemUser {

    private final String userId;
    private final String name;

    // protected = คลาสลูกเข้าถึงได้ ใช้ร่วมกันทั้งแคชเชียร์และผู้ดูแลระบบ
    protected final MemberService memberService;

    protected SystemUser(String userId, String name, MemberService memberService) {
        this.userId = userId;
        this.name = name;
        this.memberService = memberService;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    /** ชื่อบทบาท เช่น "พนักงานแคชเชียร์" - คลาสลูกต้อง override */
    public abstract String getRoleName();

    /** แสดงเมนูของบทบาทนั้นและวนรับคำสั่ง - คลาสลูกต้อง override */
    public abstract void showMenu();

    /** พฤติกรรมร่วม: ข้อความต้อนรับ (เรียก getRoleName() ซึ่งแต่ละคลาสลูกคืนค่าไม่เหมือนกัน) */
    public void printWelcome() {
        System.out.println("\nเข้าสู่ระบบสำเร็จ: " + getRoleName() + " - " + name + " (" + userId + ")");
    }

    /**
     * พฤติกรรมร่วม: ถามรหัสสมาชิก แล้วค้นหา
     * คืน Member ถ้าพบ หรือคืน null พร้อมแจ้งข้อความ ถ้าไม่พบ (= ไม่ใช่สมาชิก)
     */
    protected Member askForMember() {
        String memberId = InputHelper.readText("กรอกรหัสสมาชิก (เช่น M001): ");
        Member member = memberService.findById(memberId);
        if (member == null) {
            System.out.println("  [ผิดพลาด] ไม่พบสมาชิกรหัส " + memberId
                    + " (ต้องสมัครสมาชิกก่อนจึงจะใช้บริการได้)");
        } else {
            System.out.println("  พบสมาชิก: " + member);
        }
        return member;
    }

    /** ตัวคั่นหัวข้อเมนูย่อย */
    protected void printSection(String title) {
        System.out.println("\n---------- " + title + " ----------");
    }
}
