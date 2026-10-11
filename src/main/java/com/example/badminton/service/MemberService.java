package com.example.badminton.service;

import com.example.badminton.model.Member;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * MemberService ดูแลเรื่อง "สมาชิก" ทั้งหมด: สมัครสมาชิก และค้นหาสมาชิก
 *
 * ความสัมพันธ์กับคลาสอื่น:
 * - เก็บ Member ทุกคนไว้ใน ArrayList (เก็บในหน่วยความจำ ปิดโปรแกรมแล้วข้อมูลหาย)
 * - Main เรียก registerMember() เมื่อเลือกเมนู "สมัครสมาชิก"
 * - Cashier / Administrator เรียก findById() เพื่อดึงสมาชิกก่อนจอง ขายแพ็กเกจ หรือดูประวัติ
 *   ถ้าหาไม่เจอ (คืน null) แปลว่า "ยังไม่ได้เป็นสมาชิก" -> ระบบจะไม่อนุญาตให้จอง
 */
public class MemberService {

    private final List<Member> members = new ArrayList<>();
    private int nextMemberNumber = 1; // ตัวนับสำหรับออกรหัสสมาชิกถัดไป

    /**
     * สมัครสมาชิกใหม่
     * ขั้นตอน: ตรวจเบอร์ซ้ำ -> ออกรหัสใหม่ (M001, M002, ...) -> สร้าง Member -> เก็บลง List
     * รหัสสมาชิกมาจากตัวนับที่เพิ่มขึ้นทุกครั้ง จึง "ไม่ซ้ำกันแน่นอน"
     * (ถ้าข้อมูลไม่ถูกต้อง constructor ของ Member จะโยน IllegalArgumentException)
     */
    public Member registerMember(String fullName, String phoneNumber) {
        if (findByPhone(phoneNumber) != null) {
            throw new IllegalArgumentException("เบอร์โทรศัพท์นี้ถูกใช้สมัครสมาชิกแล้ว");
        }
        String newId = String.format("M%03d", nextMemberNumber);
        Member member = new Member(newId, fullName, phoneNumber, LocalDate.now());
        members.add(member);
        nextMemberNumber++; // เพิ่มตัวนับหลังสร้างสำเร็จเท่านั้น
        return member;
    }

    /** ค้นหาสมาชิกจากรหัส (ไม่สนตัวพิมพ์เล็ก/ใหญ่) คืน null ถ้าไม่พบ */
    public Member findById(String memberId) {
        for (Member member : members) {
            if (member.getMemberId().equalsIgnoreCase(memberId.trim())) {
                return member;
            }
        }
        return null;
    }

    /** ค้นหาสมาชิกจากเบอร์โทร คืน null ถ้าไม่พบ */
    public Member findByPhone(String phoneNumber) {
        for (Member member : members) {
            if (member.getPhoneNumber().equals(phoneNumber)) {
                return member;
            }
        }
        return null;
    }

    /** ค้นหาสมาชิกจากชื่อ (พิมพ์บางส่วนของชื่อก็ได้) อาจได้หลายคน */
    public List<Member> searchByName(String keyword) {
        List<Member> result = new ArrayList<>();
        String lowerKeyword = keyword.trim().toLowerCase();
        for (Member member : members) {
            if (member.getFullName().toLowerCase().contains(lowerKeyword)) {
                result.add(member);
            }
        }
        return result;
    }

    public List<Member> getAllMembers() {
        return new ArrayList<>(members);
    }
}
