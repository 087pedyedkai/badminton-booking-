# โน้ตทบทวนก่อนนำเสนอ: ระบบจองสนามแบดมินตัน

> ฉบับอ่านเร็ว 5–10 นาที รายละเอียดเต็มและหลักฐานจากโค้ดอยู่ใน `PROGRAM_ANALYSIS.md`

---

## 1. พูดให้ได้ใน 30 วินาที

ระบบจองสนามแบดมินตัน 12 สนาม เขียนด้วย Java OOP มี 2 หน้าตาคือ **GUI (Java Swing)** และ **Console** ใช้ตรรกะชุดเดียวกัน แคชเชียร์จองสนามให้สมาชิก รับเงินพร้อมเลขสลิป ขายแพ็กเกจ เช่ารองเท้า ขายอุปกรณ์ ส่วนผู้ดูแลระบบดูประวัติการจองและรายได้รายวัน/รายเดือน ข้อมูลเก็บในหน่วยความจำ

> **ข้อควรระวัง:** ใช้ **Swing ไม่ใช่ JavaFX** — ไม่มีไฟล์ FXML/CSS

---

## 2. โครงสร้าง 3 ชั้น

```
หน้าจอ (gui/*, Main, Cashier/Administrator.showMenu)
   ↓ เรียกเมธอด
Service (MemberService, BookingService, PaymentService, ProductService, ReportService)  ← กฎทั้งหมดอยู่ที่นี่
   ↓ สร้าง/แก้ไข
Model (Member, Booking, Court, Payment, Product, Sale, SaleItem, ShoeRental, MembershipPackage, PaymentType)
```

`BadmintonSystem` สร้างทุกอย่าง: `PaymentService` ก่อน → ส่งให้ `BookingService`, `ProductService`, `ReportService` → สร้าง `Cashier`, `Administrator` → `loadSampleData()`

---

## 3. คลาสที่ต้องจำ

| คลาส | หน้าที่ | เมธอดเด่น |
|---|---|---|
| `BookingService` | เวลาทำการ, วันหยุด, การจอง | `validateBooking()`, `createBooking()`, `isWeekendOrHoliday()` |
| `PaymentService` | เงินทั้งหมด | `calculateCourtFee()`, `recordPayment()`, `payCourtFee()`, `sellPackage()`, `rentShoes()` |
| `Booking` | การจอง 1 รายการ | `overlapsWith()`, `getTotalAmount()` |
| `Member` | สมาชิก | `usePackageHours()`, `addQualifyingHours()` |
| `MembershipPackage` | แพ็กเกจ 15 ชม. | `useHours()` (ห้ามติดลบ) |
| `SystemUser` (abstract) | แม่ของผู้ใช้ | `getRoleName()`*, `showMenu()`* |
| `IncomeReport` (abstract) | แม่ของรายงานรายได้ | `isInPeriod()`*, `getIncomeByType()` |
| `Reportable` (interface) | สัญญาของรายงาน | `getReportTitle()`, `generateReport()`, `default printReport()` |

`*` = abstract method ที่คลาสลูกต้อง override

---

## 4. ขั้นตอนจองสนาม (จำลำดับนี้)

1. ตรวจสมาชิก `memberService.findById()` — ไม่พบ = จองไม่ได้
2. `validateBooking()` ตรวจ 5 ข้อ: สนาม 1–12 → วันที่ไม่ย้อนหลัง → ชั่วโมง > 0 → อยู่ในเวลาทำการ → สนามว่าง
3. คำนวณค่าสนาม `calculateCourtFee()` (หรือ 0 ถ้าใช้แพ็กเกจ)
4. `createBooking()` → validate ซ้ำ → `new Booking` → หักแพ็กเกจ **หรือ** เพิ่มชั่วโมงสะสม
5. `payCourtFee()` → สร้าง `Payment` (ไม่สร้างถ้าใช้แพ็กเกจ)
6. `rentShoes()` ถ้าเช่ารองเท้า

**จองซ้อน:** สนามเดียวกัน + วันเดียวกัน + `start1 < end2 && start2 < end1` → 13–15 กับ 15–16 **ไม่ทับ**

---

## 5. สูตรที่ต้องจำ

| เรื่อง | สูตร | ตัวอย่าง |
|---|---|---|
| ค่าสนาม | 180 × ชม. (ยกเว้นชั่วโมงส่วนลด) | 2 ชม. = 360 |
| ส่วนลด | ชั่วโมงสะสมลำดับที่ `% 20 == 0` จ่าย 180 × 30% = **54** | สะสม 18 จอง 3 ชม. = 180 + 54 + 180 = **414** |
| แพ็กเกจ | 2,600 บาท ได้ 15 ชม. ค่าสนาม 0 | มี 15 จอง 4 → เหลือ 11 |
| รองเท้า | คู่ × 1 วัน × 50 | 3 คู่ = 150 |
| สินค้า | ราคาต่อหน่วย × จำนวน | P03 650×2 + P05 1,800 = 3,100 |
| ยอดการจอง | ค่าสนาม + ค่ารองเท้า | 360 + 100 = 460 |
| รายได้ | Σ `Payment` ตามหมวด + **วันที่รับเงิน** | ข้อมูลตัวอย่าง = 3,240 + 2,600 = 5,840 |

**การตีความของผู้พัฒนา (โจทย์ไม่ได้ระบุ — ควรบอกอาจารย์):**

- ส่วนลดให้ **ทุก 20 ชม.** (20, 40, 60...) ไม่ใช่ครั้งเดียว
- ชั่วโมงที่ใช้แพ็กเกจ **ไม่นับ** เป็นชั่วโมงสะสม
- แพ็กเกจไม่พอ = จองแบบแพ็กเกจไม่ได้ (ไม่ผสมกับจ่ายเงิน)
- รายได้แพ็กเกจนับครั้งเดียวตอนขาย

---

## 6. OOP — ประโยคพร้อมตอบ

| แนวคิด | ตอบสั้น ๆ | ไฟล์ |
|---|---|---|
| Encapsulation | field เป็น `private`, `useHours()` ห้ามหักเกิน → ไม่ติดลบ, getter คืนสำเนา List | `MembershipPackage`, `Member` |
| Inheritance | `Cashier`/`Administrator extends SystemUser`; `Daily`/`MonthlyIncomeReport extends IncomeReport` | `user/`, `report/` |
| Polymorphism | `Main.openUserMenu(SystemUser u)` → `u.showMenu()`; `ReportService.showReport(Reportable r)` | `Main`, `ReportService` |
| Abstract class | `SystemUser` (มีข้อมูลร่วม + เมนูบังคับ), `IncomeReport` (สูตรร่วม + ช่วงเวลาบังคับ) | |
| Interface | `Reportable` (รายงานทุกแบบ), `Refreshable` (11 หน้าจอ GUI) | |
| Overriding | `isInPeriod()` รายวันเทียบวัน รายเดือนเทียบเดือน | `report/` |
| Overloading | `UiHelper.html(text)` / `html(text, width)` | `gui/UiHelper` |
| Composition | `Sale` มี `SaleItem` (จดราคาตอนขาย) | `model/` |

**Interface vs Abstract class:** `SystemUser` ต้องเป็น abstract class เพราะมี field และ constructor ร่วม ส่วน `Reportable` เป็น interface เพราะรายงานต่างชนิดมีแค่ "ความสามารถ" ร่วม (`MemberHistoryReport` ไม่ใช่รายงานรายได้ จึงไม่ extends `IncomeReport` แต่ implements `Reportable`)

---

## 7. สถานะเทียบโจทย์

| ทำแล้ว | ทำบางส่วน | ยังไม่พบ |
|---|---|---|
| 12 สนาม, 180 บาท, เวลาเปิด-ปิด, วันหยุด | ล็อกอิน (ไม่มีรหัสผ่าน) | บันทึกข้อมูลถาวร |
| ต้องเป็นสมาชิก, ข้อมูลการจองครบ | แนบสลิป (เป็นข้อความเลขอ้างอิง) | ยกเลิก/แก้ไขการจอง |
| กันจองซ้อน, แพ็กเกจ, ส่วนลดชม.ที่ 20 | ห้ามจองย้อนหลัง (ชั่วโมงที่ผ่านไปของวันนี้ยังจองได้) | |
| เช่ารองเท้า, ขายอุปกรณ์, ค้นหา | | |
| ประวัติสมาชิก, รายได้รายวัน/เดือน | | |
| Interface + Abstract class | | |

**ต้องทดสอบเพิ่ม:** หน้าจอ GUI ทุกปุ่ม — ยังไม่ได้เปิดดูจริงในการวิเคราะห์ (ตรรกะส่วน service รันทดสอบแล้ว)

---

## 8. จุดอ่อนที่ควรพูดเองก่อนถูกถาม

1. ปิดโปรแกรมแล้วข้อมูลหาย (`ArrayList`)
2. เข้าสู่ระบบแบบจำลอง ไม่มีรหัสผ่าน
3. สลิปเป็นข้อความ ยังไม่แนบไฟล์จริง
4. ยกเลิกการจองไม่ได้
5. จองชั่วโมงที่ผ่านไปแล้วของวันนี้ได้ (ตรวจแค่วันที่)
6. เพิ่มวันหยุดภายหลังไม่ตรวจการจองเดิม

**แนวทางพัฒนา:** ตรวจเวลาปัจจุบันใน `validateBooking()`, เพิ่มล็อกอิน, บันทึกไฟล์/DB, `JFileChooser` แนบสลิป, ยกเลิกการจอง, JUnit test

---

## 9. คำถามที่ต้องตอบให้คล่อง

1. **ใช้ JavaFX ไหม?** → ไม่ ใช้ Swing (`module-info` require แค่ `java.desktop`)
2. **Polymorphism อยู่ไหน?** → `user.showMenu()` ใน `Main.openUserMenu()`, `showReport(Reportable)`
3. **ทำไม SystemUser เป็น abstract class?** → มีข้อมูลร่วม + บังคับเมนู + ห้ามสร้างตรง
4. **ส่วนลดคิดอย่างไร?** → 18 + 3 ชม. = ชั่วโมง 19, 20, 21 → 414 บาท ไม่ได้ซ้ำเพราะชั่วโมงสะสมเพิ่มทันที
5. **แพ็กเกจนับรายได้ซ้ำไหม?** → ไม่ นับตอนขาย 2,600 ครั้งเดียว ตอนใช้ไม่สร้าง `Payment`
6. **กันจองซ้อนอย่างไร?** → `Booking.overlapsWith()` สูตร `A < D && C < B`
7. **กันรับเงินซ้ำอย่างไร?** → `isAlreadyPaid(type, referenceId)` ใน `recordPayment()`
8. **ข้อมูลเก็บที่ไหน?** → `ArrayList` ใน Service ไม่มีฐานข้อมูล
9. **ถ้าเปลี่ยนราคาสนาม?** → แก้ `Court.STANDARD_PRICE_PER_HOUR` ที่เดียว (ส่วนลดเปลี่ยนตาม) + ข้อความป้ายที่พิมพ์ 180 ไว้
10. **ทดสอบอย่างไร?** → ไม่มี JUnit test ในโปรเจกต์; ทดสอบด้วยมือตาม scenario ใน README และรันทดสอบตรรกะ service แล้ว

---

## 10. เช็กลิสต์ก่อนขึ้นนำเสนอ

- [ ] เปิด `BadmintonApp` ได้ ตัวอักษรไทยแสดงถูก (ถ้าไม่ได้ ตั้ง File Encoding เป็น UTF-8 ใน IntelliJ)
- [ ] ตรวจว่า IntelliJ ใช้ JDK 25 (ตามที่ `pom.xml` กำหนด)
- [ ] ซ้อมสาธิต: สมัคร → จอง M002 (414 บาท) → จอง M003 ด้วยแพ็กเกจ → เช่ารองเท้า → ขายอุปกรณ์ → ค้นหา → รายงาน → ประวัติสมาชิก
- [ ] เตรียม `Main` (Console) เป็นแผนสำรอง
- [ ] เปิดไฟล์ `BookingService`, `PaymentService`, `SystemUser`, `Reportable`, `IncomeReport` ไว้ในแท็บ IDE พร้อมชี้โค้ด
