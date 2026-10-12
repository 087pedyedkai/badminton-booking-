package com.example.badminton.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * คลาส Member แทน "สมาชิก" ของสนามแบดมินตัน
 * ตามโจทย์: เฉพาะสมาชิกเท่านั้นที่จองสนามได้ และสมาชิกแต่ละคนต้องมีรหัสไม่ซ้ำกัน
 *
 * ความสัมพันธ์กับคลาสอื่น:
 * - MemberService เป็นผู้สร้างและเก็บ Member ทั้งหมด (และออกรหัสสมาชิกที่ไม่ซ้ำ)
 * - Member 1 คน มี Booking ได้หลายรายการ (เก็บใน bookings) -> ใช้ดูประวัติการจอง
 * - Member 1 คน มี MembershipPackage ได้หลายแพ็กเกจ (เก็บใน packages)
 * - Booking ก็อ้างอิงกลับมาที่ Member เช่นกัน จึงรู้ว่าการจองนั้นเป็นของใคร
 */
public class Member {

    private final String memberId;
    private String fullName;
    private String phoneNumber;
    private final LocalDate registerDate;

    /**
     * ชั่วโมงสะสมสำหรับคิดส่วนลด (qualifying hours)
     * กติกาที่เลือกใช้ (โจทย์ไม่ได้ระบุชัด):
     * - นับเฉพาะชั่วโมงที่ "จ่ายเงินค่าสนามแบบปกติ" เท่านั้น
     * - ชั่วโมงที่ใช้แพ็กเกจ "ไม่นับ" เพราะแพ็กเกจเป็นราคาพิเศษอยู่แล้ว
     * รายละเอียดการคิดส่วนลดดูที่ PaymentService.calculateCourtFee()
     */
    private int qualifyingHours;

    private final List<MembershipPackage> packages = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();

    public Member(String memberId, String fullName, String phoneNumber, LocalDate registerDate) {
        this.memberId = memberId;
        setFullName(fullName);       // ใช้ setter เพื่อให้ผ่านการตรวจสอบข้อมูลด้วย
        setPhoneNumber(phoneNumber);
        this.registerDate = registerDate;
        this.qualifyingHours = 0;
    }

    // ---------------- Getter / Setter (Encapsulation) ----------------

    public String getMemberId() {
        return memberId;
    }

    public String getFullName() {
        return fullName;
    }

    /** ตั้งชื่อใหม่ พร้อมตรวจว่าไม่เป็นค่าว่าง */
    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("ชื่อ-นามสกุลห้ามว่าง");
        }
        this.fullName = fullName.trim();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    /** ตั้งเบอร์โทร พร้อมตรวจว่าเป็นตัวเลข 9-10 หลัก */
    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches("\\d{9,10}")) {
            throw new IllegalArgumentException("เบอร์โทรศัพท์ต้องเป็นตัวเลข 9-10 หลัก");
        }
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getRegisterDate() {
        return registerDate;
    }

    public int getQualifyingHours() {
        return qualifyingHours;
    }

    /** เพิ่มชั่วโมงสะสม (เรียกโดย BookingService หลังบันทึกการจองแบบจ่ายเงินปกติสำเร็จ) */
    public void addQualifyingHours(int hours) {
        if (hours <= 0) {
            throw new IllegalArgumentException("ชั่วโมงสะสมที่เพิ่มต้องมากกว่า 0");
        }
        qualifyingHours += hours;
    }

    /**
     * ลดชั่วโมงสะสม (เมื่อยกเลิกการจองแบบจ่ายเงินปกติ) ไม่ให้ติดลบ
     * ผลคือการจองครั้งถัดไปจะนับชั่วโมงที่ 20 เหมือนการจองที่ยกเลิกไม่เคยเกิดขึ้น
     */
    public void removeQualifyingHours(int hours) {
        if (hours <= 0) {
            throw new IllegalArgumentException("ชั่วโมงสะสมที่ลดต้องมากกว่า 0");
        }
        qualifyingHours = Math.max(0, qualifyingHours - hours);
    }

    // ---------------- การจัดการแพ็กเกจ ----------------

    public void addPackage(MembershipPackage membershipPackage) {
        packages.add(membershipPackage);
    }

    /** คืนสำเนารายการแพ็กเกจ (คืนเป็น List ใหม่ ภายนอกจึงแก้ไข List ตัวจริงไม่ได้) */
    public List<MembershipPackage> getPackages() {
        return new ArrayList<>(packages);
    }

    /** รวมชั่วโมงคงเหลือจากทุกแพ็กเกจ */
    public int getRemainingPackageHours() {
        int total = 0;
        for (MembershipPackage p : packages) {
            total += p.getRemainingHours();
        }
        return total;
    }

    /**
     * หักชั่วโมงแพ็กเกจเมื่อจองสนามแบบใช้แพ็กเกจ
     * ขั้นตอน:
     * 1) ถ้าชั่วโมงที่ขอใช้มากกว่าชั่วโมงคงเหลือรวม -> ไม่อนุญาต (คืน false) ชั่วโมงจึงไม่ติดลบ
     * 2) ไล่หักจากแพ็กเกจที่ซื้อก่อน (เก่าสุด) ไปจนครบจำนวนที่ต้องการ
     */
    public boolean usePackageHours(int hours) {
        if (hours <= 0 || hours > getRemainingPackageHours()) {
            return false;
        }
        int hoursLeftToDeduct = hours;
        for (MembershipPackage p : packages) {
            if (hoursLeftToDeduct == 0) {
                break;
            }
            int hoursFromThisPackage = Math.min(p.getRemainingHours(), hoursLeftToDeduct);
            if (hoursFromThisPackage > 0) {
                p.useHours(hoursFromThisPackage);
                hoursLeftToDeduct -= hoursFromThisPackage;
            }
        }
        return true;
    }

    /**
     * คืนชั่วโมงแพ็กเกจเมื่อยกเลิกการจองที่ใช้แพ็กเกจ
     * usePackageHours() หักจากแพ็กเกจเก่าสุดก่อน จึงคืนกลับจากแพ็กเกจใหม่สุดก่อน (ย้อนลำดับการหัก)
     */
    public void returnPackageHours(int hours) {
        int hoursLeftToReturn = hours;
        for (int i = packages.size() - 1; i >= 0 && hoursLeftToReturn > 0; i--) {
            MembershipPackage p = packages.get(i);
            int hoursToThisPackage = Math.min(p.getUsedHours(), hoursLeftToReturn);
            if (hoursToThisPackage > 0) {
                p.returnHours(hoursToThisPackage);
                hoursLeftToReturn -= hoursToThisPackage;
            }
        }
    }

    // ---------------- ประวัติการจอง ----------------

    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking);
    }

    public List<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }

    /** สร้างข้อความข้อมูลสมาชิกแบบละเอียด (ใช้ได้ทั้ง Console และ GUI) */
    public String getInfoText() {
        return "รหัสสมาชิก        : " + memberId + "\n"
                + "ชื่อ-นามสกุล       : " + fullName + "\n"
                + "เบอร์โทรศัพท์      : " + phoneNumber + "\n"
                + "วันที่สมัคร         : " + registerDate + "\n"
                + "จำนวนการจอง       : " + bookings.size() + " รายการ\n"
                + "ชั่วโมงสะสม (ส่วนลด): " + qualifyingHours + " ชั่วโมง\n"
                + "ชั่วโมงแพ็กเกจคงเหลือ: " + getRemainingPackageHours() + " ชั่วโมง";
    }

    /** แสดงข้อมูลสมาชิกทาง Console (ใช้ในเมนูแคชเชียร์ "ดูข้อมูลสมาชิก") */
    public void printInfo() {
        System.out.println(getInfoText());
    }

    @Override
    public String toString() {
        return memberId + " - " + fullName + " (โทร " + phoneNumber + ")";
    }
}
