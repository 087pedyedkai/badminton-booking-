package com.example.badminton;

import com.example.badminton.model.Member;
import com.example.badminton.service.MemberService;
import com.example.badminton.user.SystemUser;
import com.example.badminton.util.InputHelper;

import java.time.LocalDate;

/**
 * Main = จุดเริ่มต้นของโปรแกรมแบบ "Console" (พิมพ์ตัวเลขเลือกเมนู)
 * (ถ้าต้องการแบบหน้าต่าง GUI ให้รัน com.example.badminton.gui.BadmintonApp แทน)
 *
 * ภาพรวมการเชื่อมต่อของคลาส:
 *
 *   Main
 *    ├── สร้าง BadmintonSystem ซึ่งภายในสร้าง Service ทั้งหมด ผู้ใช้ระบบ และข้อมูลตัวอย่าง
 *    │     Cashier       -> ใช้ Member/Booking/Payment/Product Service
 *    │     Administrator -> ใช้ ReportService และ Service อื่น ๆ
 *    └── แสดงเมนูหลัก ให้เลือกบทบาท แล้วเรียก user.showMenu() (Polymorphism)
 */
public class Main {

    public static void main(String[] args) {
        BadmintonSystem system = new BadmintonSystem();
        System.out.println("(โหลดข้อมูลตัวอย่างแล้ว: สมาชิก M001, M002 (สะสม 18 ชม.), M003 (มีแพ็กเกจ 15 ชม.))");

        // ตัวแปรชนิด SystemUser (คลาสแม่) แต่เก็บออบเจกต์ Cashier / Administrator
        SystemUser cashier = system.getCashier();
        SystemUser admin = system.getAdministrator();

        System.out.println("==================================================");
        System.out.println("   ระบบจัดการการจองสนามแบดมินตัน (12 สนาม)");
        System.out.println("   ค่าสนาม 180 บาท/ชม. | จ-ศ 13:00-21:00 | ส-อา/วันหยุด 10:00-20:00");
        System.out.println("   วันนี้: " + LocalDate.now());
        System.out.println("==================================================");

        while (true) {
            System.out.println("\n============== เมนูหลัก ==============");
            System.out.println("1. เข้าสู่ระบบพนักงานแคชเชียร์");
            System.out.println("2. เข้าสู่ระบบผู้ดูแลระบบ");
            System.out.println("3. สมัครสมาชิก");
            System.out.println("4. ออกจากโปรแกรม");
            int choice = InputHelper.readInt("เลือกเมนู: ", 1, 4);

            if (choice == 1) {
                openUserMenu(cashier);
            } else if (choice == 2) {
                openUserMenu(admin);
            } else if (choice == 3) {
                registerMember(system.getMemberService());
            } else {
                System.out.println("ขอบคุณที่ใช้บริการ ปิดโปรแกรม");
                break;
            }
        }
    }

    /**
     * ตัวอย่าง Polymorphism ที่ชัดที่สุดในโปรแกรม:
     * เมธอดนี้รับพารามิเตอร์ชนิด SystemUser (abstract class)
     * - ถ้าส่ง Cashier เข้ามา  -> user.showMenu() จะแสดงเมนูแคชเชียร์
     * - ถ้าส่ง Administrator    -> user.showMenu() จะแสดงเมนูผู้ดูแลระบบ
     * โค้ดบรรทัดเดียวกัน แต่ทำงานต่างกันตามชนิดจริงของออบเจกต์
     */
    private static void openUserMenu(SystemUser user) {
        user.showMenu();
    }

    /** เมนูสมัครสมาชิก: รับชื่อและเบอร์โทร แล้วให้ MemberService ออกรหัสสมาชิกที่ไม่ซ้ำ */
    private static void registerMember(MemberService memberService) {
        System.out.println("\n---------- สมัครสมาชิก ----------");
        String fullName = InputHelper.readText("ชื่อ-นามสกุล: ");
        while (true) {
            String phone = InputHelper.readText("เบอร์โทรศัพท์ (ตัวเลข 9-10 หลัก): ");
            try {
                Member member = memberService.registerMember(fullName, phone);
                System.out.println("สมัครสมาชิกสำเร็จ! รหัสสมาชิกของคุณคือ " + member.getMemberId());
                member.printInfo();
                return;
            } catch (IllegalArgumentException e) {
                // ข้อมูลไม่ถูกต้อง (เช่น เบอร์ไม่ใช่ตัวเลข หรือเบอร์ซ้ำ) -> แจ้งแล้วให้กรอกใหม่
                System.out.println("  [ผิดพลาด] " + e.getMessage());
            }
        }
    }
}
