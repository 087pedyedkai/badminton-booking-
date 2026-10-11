package com.example.badminton.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * คลาสช่วยรับข้อมูลจากคีย์บอร์ด (Console)
 *
 * เหตุผลที่แยกออกมาเป็นคลาสของตัวเอง:
 * - ทุกเมนูต้องรับตัวเลข วันที่ และข้อความเหมือน ๆ กัน ถ้าเขียนซ้ำทุกที่โค้ดจะยาวและแก้ยาก
 * - ทุกเมธอดจะ "วนถามซ้ำ" จนกว่าผู้ใช้จะพิมพ์ข้อมูลถูกต้อง โปรแกรมจึงไม่ crash
 *   เมื่อผู้ใช้พิมพ์ตัวอักษรแทนตัวเลข หรือพิมพ์วันที่ผิดรูปแบบ
 *
 * ทุกเมธอดเป็น static จึงเรียกใช้ได้ทันที เช่น InputHelper.readInt(...)
 * โดยไม่ต้องสร้างออบเจกต์ (ใช้ Scanner ตัวเดียวร่วมกันทั้งโปรแกรม)
 */
public class InputHelper {

    // Scanner ตัวเดียวสำหรับทั้งโปรแกรม (ไม่ควรสร้าง Scanner ของ System.in หลายตัว)
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * อ่านข้อความ 1 บรรทัด (อาจเป็นค่าว่างได้)
     * ถ้าไม่มีข้อมูลเข้ามาอีกแล้ว (เช่น ปิด input) ให้จบโปรแกรมอย่างสุภาพแทนการ error
     */
    public static String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println("\nไม่มีข้อมูลนำเข้าเพิ่มเติม ปิดโปรแกรม");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    /** อ่านข้อความที่ "ห้ามว่าง" วนถามจนกว่าจะพิมพ์อะไรบางอย่าง */
    public static String readText(String prompt) {
        while (true) {
            String text = readLine(prompt);
            if (!text.isEmpty()) {
                return text;
            }
            System.out.println("  [ผิดพลาด] กรุณากรอกข้อมูล ห้ามเว้นว่าง");
        }
    }

    /**
     * อ่านจำนวนเต็มที่อยู่ในช่วง min ถึง max
     * ขั้นตอน: อ่านข้อความ -> แปลงเป็น int -> ตรวจช่วง -> ถ้าผิดให้ถามใหม่
     */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            String text = readLine(prompt);
            try {
                int value = Integer.parseInt(text);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("  [ผิดพลาด] กรุณากรอกตัวเลขระหว่าง " + min + " ถึง " + max);
            } catch (NumberFormatException e) {
                // เกิดเมื่อผู้ใช้พิมพ์สิ่งที่ไม่ใช่ตัวเลข เช่น "abc"
                System.out.println("  [ผิดพลาด] กรุณากรอกเป็นตัวเลขจำนวนเต็มเท่านั้น");
            }
        }
    }

    /** อ่านจำนวนทศนิยมที่ต้องมากกว่า 0 (ใช้กับราคาสินค้า) */
    public static double readPositiveDouble(String prompt) {
        while (true) {
            String text = readLine(prompt);
            try {
                double value = Double.parseDouble(text);
                if (value > 0) {
                    return value;
                }
                System.out.println("  [ผิดพลาด] ค่าต้องมากกว่า 0");
            } catch (NumberFormatException e) {
                System.out.println("  [ผิดพลาด] กรุณากรอกเป็นตัวเลข");
            }
        }
    }

    /** ถามคำถามแบบ ใช่/ไม่ใช่ รับ y หรือ n เท่านั้น */
    public static boolean readYesNo(String prompt) {
        while (true) {
            String text = readLine(prompt).toLowerCase();
            if (text.equals("y")) {
                return true;
            }
            if (text.equals("n")) {
                return false;
            }
            System.out.println("  [ผิดพลาด] กรุณาพิมพ์ y (ใช่) หรือ n (ไม่ใช่)");
        }
    }

    /**
     * อ่านวันที่รูปแบบ yyyy-MM-dd เช่น 2026-10-12
     * ถ้าผู้ใช้พิมพ์ปีเป็น พ.ศ. (เช่น 2569) โปรแกรมจะแปลงเป็น ค.ศ. ให้อัตโนมัติ (ลบ 543)
     */
    public static LocalDate readDate(String prompt) {
        while (true) {
            String text = readLine(prompt);
            try {
                LocalDate date = LocalDate.parse(text);
                if (date.getYear() >= 2400) {
                    date = date.minusYears(543);
                    System.out.println("  (แปลงปี พ.ศ. เป็น ค.ศ. ให้แล้ว: " + date + ")");
                }
                return date;
            } catch (DateTimeParseException e) {
                System.out.println("  [ผิดพลาด] รูปแบบวันที่ไม่ถูกต้อง ต้องเป็น ปี-เดือน-วัน เช่น 2026-10-12");
            }
        }
    }

    /** อ่านเดือนและปีรูปแบบ yyyy-MM เช่น 2026-10 (ใช้กับรายงานรายเดือน) */
    public static YearMonth readYearMonth(String prompt) {
        while (true) {
            String text = readLine(prompt);
            try {
                YearMonth yearMonth = YearMonth.parse(text);
                if (yearMonth.getYear() >= 2400) {
                    yearMonth = yearMonth.minusYears(543);
                    System.out.println("  (แปลงปี พ.ศ. เป็น ค.ศ. ให้แล้ว: " + yearMonth + ")");
                }
                return yearMonth;
            } catch (DateTimeParseException e) {
                System.out.println("  [ผิดพลาด] รูปแบบไม่ถูกต้อง ต้องเป็น ปี-เดือน เช่น 2026-10");
            }
        }
    }
}
