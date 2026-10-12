# คำถามแก้โค้ดที่อาจารย์อาจสั่ง (พร้อมเฉลย)

> ใช้เตรียมตัวกรณีอาจารย์ให้ "แก้โค้ดสด" เพื่อวัดความเข้าใจ
> อ้างอิงโค้ดเวอร์ชันล่าสุดในโฟลเดอร์ `badminton-booking-` (commit `21ba98b`: มี JavaFX + Swing + Console และมีแก้ไข/ยกเลิกการจองแล้ว)

## ก่อนอ่าน

- **ทุกเฉลยที่มีโค้ดผ่านการทดสอบจริงแล้ว**: นำไปใส่ในสำเนาโปรเจกต์แยก (ไม่ได้แก้ไฟล์จริงของคุณ) คอมไพล์ทั้งโปรเจกต์ (Console + Swing + JavaFX) และรันทดสอบ ผลที่เขียนไว้ในหัวข้อ "ผลทดสอบ" คือผลที่รันได้จริง
  - ข้อจำกัด: เครื่องทดสอบใช้ JDK 21 + JavaFX 21 (โปรเจกต์จริงใช้ 25) แต่ API ที่ใช้ในเฉลยเหมือนกัน
  - หน้าจอ JavaFX ทดสอบโดยเปิดโปรแกรมบนจอเสมือน แล้วสั่งงาน Controller ด้วยโค้ด (ไม่ได้ใช้เมาส์คลิก)
- ป้าย `[A1]`, `[B4]` ในโค้ดเฉลยใช้บอกว่าบรรทัดนั้นมาจากข้อไหน เวลาแก้จริงไม่ต้องใส่
- ตำแหน่งไฟล์ย่อ: `.../` = `src/main/java/com/example/badminton/` และ `res/` = `src/main/resources/com/example/badminton/fx/`
- โปรเจกต์มี **3 หน้าตา** (JavaFX, Swing, Console) ถ้าแก้ "กฎ" ใน Service จะมีผลกับทั้ง 3 แบบพร้อมกัน แต่ถ้าแก้ "หน้าจอ" ต้องแก้แยกแต่ละแบบ ในเฉลยจะเน้น JavaFX (หน้าจอที่ใช้นำเสนอ) และบอกจุดของ Swing/Console ไว้ด้วย

## หลักคิด 4 ข้อเวลาโดนสั่งแก้

1. **หาก่อนว่ากฎนี้อยู่ที่ไหน**: ราคา/เวลา/ส่วนลด → `service/` หรือค่าคงที่ใน `model/`, ข้อความ/ปุ่ม/ตาราง → `fx/` + `res/*.fxml`
2. **แก้ที่ต้นทางที่เดียว** ถ้าเป็นค่าคงที่ (เช่น `Court.STANDARD_PRICE_PER_HOUR`) แล้วค้นหาข้อความที่พิมพ์ตัวเลขนั้นไว้ตรง ๆ (Ctrl+Shift+F ใน IntelliJ) เพราะป้ายบนหน้าจอบางอันไม่ได้อ่านจากค่าคงที่
3. **ดูว่าใครเรียกเมธอดนี้** (คลิกชื่อเมธอด → Alt+F7 "Find Usages") ก่อนเปลี่ยนพารามิเตอร์
4. **คอมไพล์ + รัน + ลองกรณีปกติและกรณีผิด** อย่าบอกว่าเสร็จจนกว่าจะลองแล้ว

---

## สารบัญคำถาม

| ข้อ | เรื่อง | ระดับ | ไฟล์หลักที่แก้ |
|---|---|---|---|
| **A1** | เปลี่ยนราคาสนามเป็น 200 บาท | ง่าย | `Court` |
| **A2** | เพิ่มสนามเป็น 15 สนาม | ง่าย | `BookingService` |
| **A3** | วันธรรมดาเปิด 12:00–22:00 | ง่าย (มีกับดัก) | `BookingService`, `CourtScheduleGrid` |
| **A4** | แพ็กเกจเป็น 20 ชม. 3,200 บาท | ง่าย | `MembershipPackage` |
| **A5** | ค่าเช่ารองเท้าเป็น 60 บาท | ง่าย (มีกับดัก) | `ShoeRental` + ข้อความหน้าจอ |
| **A6** | เช่ารองเท้าได้สูงสุด 10 คู่ | ง่าย | `ShoeRental` |
| **A7** | เพิ่มสินค้า "เทปพันด้าม" | ง่าย | `BadmintonSystem` |
| **A8** | เปลี่ยนรหัสสมาชิกเป็น MEM0001 | ง่าย | `MemberService` |
| **A9** | เปลี่ยนสีช่อง "ว่าง" / สีปุ่ม | ง่าย | `style.css` |
| **A10** | หน้าจองให้เริ่มที่ "วันนี้" | ง่าย | `BookingController` |
| **B1** | ส่วนลดชั่วโมงที่ 20 ได้ครั้งเดียว | กลาง | `PaymentService`, `MemberHistoryReport` |
| **B2** | ส่วนลดเป็น "ลด 30%" (จ่าย 70%) | กลาง | `PaymentService` |
| **B3** | นับชั่วโมงแพ็กเกจเป็นชั่วโมงสะสมด้วย | กลาง (มีกับดัก) | `BookingService` |
| **B4** | ห้ามจองชั่วโมงที่ผ่านไปแล้วของวันนี้ | กลาง | `BookingService` |
| **B5** | จองล่วงหน้าได้ไม่เกิน 30 วัน | กลาง | `BookingService` |
| **B6** | จองได้ครั้งละไม่เกิน 4 ชม. | กลาง (มีกับดัก) | `BookingService`, `BadmintonSystem` |
| **B7** | สมาชิกคนเดียวห้ามจองเวลาซ้อนกัน (คนละสนาม) | กลาง–ยาก | `Booking`, `BookingService`, `BookingController` |
| **B8** | เบอร์โทรต้องขึ้นต้นด้วย 0 และมี 10 หลัก | กลาง | `Member` |
| **B9** | ราคาสินค้าห้ามเป็น NaN/Infinity | กลาง | `Product` |
| **B10** | ค้นหาการจองด้วยวันที่ปี พ.ศ. ได้ | กลาง | `BookingSearchController` |
| **B11** | แก้ไข/ยกเลิกได้ก่อนเวลาเริ่มอย่างน้อย 2 ชม. | กลาง | `BookingService` |
| **B12** | ยกเลิกแล้วคืนเงิน 50% | กลาง–ยาก | `BookingService`, `PaymentType` |
| **B13** | ปุ่มลบวันหยุดนักขัตฤกษ์ | กลาง | `BookingService`, `HolidayController`, `holiday.fxml` |
| **B14** | ค้นหาการจองตามหมายเลขสนาม | กลาง (มีกับดัก) | `BookingService`, `BookingSearchController` |
| **B15** | แสดง "อีกกี่ชั่วโมงจะได้ส่วนลด" ในหน้าจอง | กลาง | `BookingController` |
| **B16** | แก้ไขชื่อ/เบอร์โทรของสมาชิก | กลาง | `MemberService` |
| **B17** | เพิ่มคอลัมน์ "เบอร์โทร" ในตารางค้นหาการจอง | กลาง (มีกับดัก) | `booking-search.fxml`, `BookingSearchController` |
| **C1** | รายงานรายได้รายปี | ยาก (OOP) | `report/` ไฟล์ใหม่, `ReportService`, `Administrator` |
| **C2** | รายงานรายได้ตามช่วงวันที่ | ยาก (OOP) | `report/` ไฟล์ใหม่ |
| **C3** | เพิ่มหมวดรายได้ "ค่าเช่าไม้แบด" | ยาก (enum) | `PaymentType`, `PaymentService` |
| **C4** | เพิ่มบทบาท "ผู้จัดการ" | ยาก (OOP) | `user/Manager` ใหม่, `BadmintonSystem`, `Main` |
| **C5** | แพ็กเกจมีอายุ 90 วัน | ยาก | `MembershipPackage`, `Member` |
| **C6** | ราคาช่วงเวลา (วันธรรมดาก่อน 17:00 = 150 บาท) | ยาก (Overloading) | `PaymentService` + ผู้เรียกทุกจุด |
| **C7** | เปลี่ยนสถานะการจองจาก String เป็น enum | ยาก | `BookingStatus` ใหม่, `Booking` |
| **C8** | สร้าง interface `Payable` ให้ Booking และ Sale | ยาก (Interface) | `Payable` ใหม่, `Booking`, `Sale` |
| **C9** | เพิ่มสต็อกสินค้า | ยาก | `Product`, `ProductService` |
| **C10** | แพ็กเกจไม่พอ ให้ใช้เท่าที่มีแล้วจ่ายส่วนที่เหลือ | ยาก (อธิบายแนวทาง) | หลายไฟล์ |
| **D1–D8** | อาจารย์ลบเนื้อเมธอดแล้วให้เขียนใหม่ | กลาง | ดูหัวข้อ D |
| **E1–E10** | "ถ้าแก้แบบนี้จะเกิดอะไรขึ้น" | คิดวิเคราะห์ | ดูหัวข้อ E |

---

## หมวด A: เปลี่ยนค่าคงที่และหน้าตา (ระดับง่าย)

> จุดที่อาจารย์อยากเห็นในหมวดนี้: รู้ว่าค่าอยู่ที่ไหน แก้ที่เดียวแล้วทั้งระบบเปลี่ยนตาม และรู้ว่ามีข้อความบนหน้าจอที่พิมพ์ตัวเลขไว้ตรง ๆ ต้องตามแก้เอง

### A1. เปลี่ยนราคาสนามจาก 180 เป็น 200 บาท/ชม.

**ต้องแก้ที่:** `.../model/Court.java`

```java
// เดิม
public static final double STANDARD_PRICE_PER_HOUR = 180.0;
// ใหม่
public static final double STANDARD_PRICE_PER_HOUR = 200.0; // [A1]
```

**ผลที่เปลี่ยนตามเองโดยไม่ต้องแก้:**
- `PaymentService.calculateCourtFee()` ใช้ค่านี้คิดราคาทุกชั่วโมง
- ราคาชั่วโมงส่วนลดคำนวณจาก `getDiscountedHourPrice()` = 200 × 30 / 100 = **60 บาท** (เดิม 54)

**ต้องตามแก้ข้อความเอง (พิมพ์ "180" ไว้ตรง ๆ):**
- `res/login.fxml` ป้าย "12 สนาม | ค่าสนาม 180 บาท/ชั่วโมง"
- `.../Main.java` บรรทัดหัวโปรแกรม Console
- `.../fx/cashier/PackageController.java` ข้อความ "จากปกติ 180 บาท" (และ `gui/cashier/PackagePanel.java`, `gui/LoginPanel.java` ฝั่ง Swing)

**ทดสอบ:** จอง 2 ชม. ให้สมาชิกใหม่ → 400 บาท; M002 (สะสม 18) จอง 3 ชม. → 200 + 60 + 200 = 460 บาท

**พูดอธิบาย:** "ราคาเก็บเป็นค่าคงที่ `static final` ที่ `Court` จุดเดียว ทุกที่คำนวณจากค่านี้ รวมถึงราคาส่วนลด 30% ด้วย"

### A2. เพิ่มสนามเป็น 15 สนาม

**ต้องแก้ที่:** `.../service/BookingService.java`

```java
public static final int TOTAL_COURTS = 15; // [A2] เดิม 12
```

**ทำไมแก้ที่เดียวพอ:** constructor ของ `BookingService` วน `for (int i = 1; i <= TOTAL_COURTS; i++)` สร้าง `Court` ส่วนหน้าจอก็ใช้ค่านี้ทั้งหมด ได้แก่ combo เลือกสนามใน `BookingController`, ขนาด array และจำนวนแถวใน `CourtScheduleGrid` และ `InputHelper.readInt(..., 1, TOTAL_COURTS)` ใน Console

**ตามแก้ข้อความ:** "12 สนาม" ใน `res/login.fxml`, `Main.java`

**ผลทดสอบ:** `validateBooking(15, ...)` ผ่าน, สนาม 16 ได้ข้อความ "หมายเลขสนามไม่ถูกต้อง (มีสนามที่ 1 ถึง 15)" เปิด JavaFX ได้ปกติ

### A3. วันธรรมดาเปิด 12:00–22:00

**ต้องแก้ที่:** `.../service/BookingService.java`

```java
private static final LocalTime WEEKDAY_OPEN = LocalTime.of(12, 0);  // [A3] เดิม 13
private static final LocalTime WEEKDAY_CLOSE = LocalTime.of(22, 0); // [A3] เดิม 21
```

**กับดัก:** ตารางสนามแสดงชั่วโมง 10:00–20:00 ตายตัว (`CourtScheduleGrid.LAST_HOUR = 21`) ถ้าไม่แก้ จะจอง 21:00 ได้ (เพราะ combo เวลาเริ่มอ่านจากเวลาเปิด-ปิด) แต่ **ตารางไม่มีคอลัมน์ 21:00 ให้เห็น** ต้องแก้เพิ่ม:

```java
// .../fx/cashier/CourtScheduleGrid.java (และ gui/cashier/CourtSchedulePanel.java ฝั่ง Swing)
public static final int LAST_HOUR = 22; // [A3] ปิดช้าสุด 22:00 (FIRST_HOUR = 10 เท่าเดิม เพราะวันหยุดยังเปิด 10:00)
```

**ตามแก้ข้อความ:** `res/login.fxml`, `res/admin/holiday.fxml`, `Main.java`

**ผลทดสอบ:** วันธรรมดาจอง 21:00–22:00 ผ่าน

### A4. แพ็กเกจเป็น 20 ชม. ราคา 3,200 บาท

**ต้องแก้ที่:** `.../model/MembershipPackage.java`

```java
public static final double PACKAGE_PRICE = 3200.0; // [A4]
public static final int PACKAGE_HOURS = 20;        // [A4]
```

**เปลี่ยนตามเอง:** `sellPackage()` บันทึกรายได้ 3,200, แพ็กเกจใหม่ได้ 20 ชม., หน้าจอแพ็กเกจ JavaFX คำนวณราคาเฉลี่ยจากค่าคงที่ (3,200 / 20 = 160 บาท/ชม.)

**ตามแก้ข้อความ:** `Cashier.java` เมนู "4. ซื้อแพ็กเกจชั่วโมง (15 ชม. 2,600 บาท)" และ `Main.java` "(มีแพ็กเกจ 15 ชม.)"

**คำถามต่อที่อาจโดน:** "แพ็กเกจที่ขายไปแล้วเปลี่ยนไหม?" → ไม่เปลี่ยน เพราะ `totalHours` ถูกเก็บในออบเจกต์ตอนสร้าง (แต่ข้อมูลอยู่ในหน่วยความจำ เปิดโปรแกรมใหม่ก็สร้างใหม่หมดอยู่แล้ว)

### A5. ค่าเช่ารองเท้าเป็น 60 บาท/คู่

**ต้องแก้ที่:** `.../model/ShoeRental.java`

```java
public static final double PRICE_PER_PAIR_PER_DAY = 60.0; // [A5]
```

**กับดัก:** การคำนวณเปลี่ยนถูกต้อง แต่ข้อความหน้าจอยังเขียน "x 50" อยู่ 4 จุด: `BookingController.updatePreview()`, `ShoeRentalController` (ป้ายค่าเช่า), `Cashier.bookCourt()` และ `Cashier.rentShoes()` วิธีที่ดีกว่าการพิมพ์ 60 ทับคือให้อ่านจากค่าคงที่:

```java
// ตัวอย่างใน BookingController.updatePreview()
addSummaryLine("ค่าเช่ารองเท้า: " + FormatHelper.money(shoeFee) + " (" + pairs + " คู่ x "
        + (int) ShoeRental.PRICE_PER_PAIR_PER_DAY + ")", null); // [A5] ไม่พิมพ์ตัวเลขตายตัว
```

**พูดอธิบาย:** "ถ้าป้ายอ่านจากค่าคงที่ ครั้งหน้าเปลี่ยนราคาก็แก้แค่บรรทัดเดียว"

### A6. เช่ารองเท้าได้สูงสุด 10 คู่ต่อครั้ง

```java
// .../model/ShoeRental.java
public static final int MAX_PAIRS = 10; // [A6] เดิม 20
```

เปลี่ยนตามเองทั้งหมด: constructor ของ `ShoeRental` ตรวจ 1–10, Spinner ใน `BookingController` และ `ShoeRentalController` ใช้ `ShoeRental.MAX_PAIRS` เป็นค่าสูงสุด, Console ก็ใช้ค่านี้ใน `readInt`

### A7. เพิ่มสินค้า "เทปพันด้าม (แบบผ้า)" ราคา 90 บาท ในข้อมูลตัวอย่าง

**ต้องแก้ที่:** `.../BadmintonSystem.java` ในเมธอด `loadSampleData()` ต่อจากสินค้าตัวสุดท้าย

```java
productService.addProduct("ถุงเท้ากีฬา", 120);
productService.addProduct("เทปพันด้าม (แบบผ้า)", 90); // [A7] ได้รหัส P09 อัตโนมัติ
```

**พูดอธิบาย:** "`ProductService.addProduct()` ออกรหัสจากตัวนับ `nextProductNumber` ให้เอง ไม่ต้องกำหนดรหัสเอง" (หรือเพิ่มตอนโปรแกรมรันผ่านแท็บ "จัดการสินค้า" ของผู้ดูแลก็ได้)

### A8. เปลี่ยนรูปแบบรหัสสมาชิกเป็น MEM0001

```java
// .../service/MemberService.java ใน registerMember()
String newId = String.format("MEM%04d", nextMemberNumber); // [A8] %04d = เลข 4 หลักเติม 0 ข้างหน้า
```

**สิ่งที่ต้องรู้:** `findById()` เทียบด้วย `equalsIgnoreCase` จึงพิมพ์ `mem0002` ก็หาเจอ; ข้อความตัวอย่าง "เช่น M001" ในหน้าจอเป็นแค่คำแนะนำ ควรแก้ให้ตรง

### A9. เปลี่ยนสีช่อง "ว่าง" ในตารางสนามเป็นสีฟ้า / เปลี่ยนสีปุ่มหลัก

**ต้องแก้ที่:** `res/style.css` (JavaFX ไม่ต้องแก้ Java เลย)

```css
.slot-free     { -fx-background-color: #BBDEFB; }  /* [A9] เดิม #E8F5E9 */

.primary-button {
    -fx-background-color: #1565C0;                  /* [A9] เดิม #2E7D32 */
    -fx-text-fill: white;
}
```

คำอธิบายสี (legend) ใต้ตารางเปลี่ยนตามเอง เพราะใช้ style class เดียวกัน (`legendItem("slot-free", ...)`) ฝั่ง Swing แก้ที่ `gui/UiHelper.java` (`SLOT_FREE`, `PRIMARY`)

**พูดอธิบาย:** "JavaFX แยกหน้าตาไว้ใน CSS โค้ด Java แค่ใส่ชื่อ class เช่น `slot-free` ให้ Label"

### A10. หน้าจองสนามให้เริ่มที่ "วันนี้" แทนพรุ่งนี้

```java
// .../fx/cashier/BookingController.java ใน initialize()
dateBox.setDate(LocalDate.now()); // [A10] เดิม LocalDate.now().plusDays(1)
```

ฝั่ง Swing: `gui/cashier/BookingPanel.java` → `new DateField(LocalDate.now())`

---

## หมวด B: แก้กฎทางธุรกิจและหน้าจอ (ระดับกลาง)

### B1. ส่วนลดชั่วโมงที่ 20 ให้ "ครั้งเดียว" (ไม่ใช่ทุก 20 ชม.)

**ต้องแก้ที่:** `.../service/PaymentService.java` เมธอด `isDiscountHour()`

```java
// เดิม: ได้ทุกชั่วโมงที่ 20, 40, 60, ...
return hourNumber % DISCOUNT_HOUR_INTERVAL == 0;
// ใหม่: ได้เฉพาะชั่วโมงที่ 20
return hourNumber == DISCOUNT_HOUR_INTERVAL; // [B1]
```

**ต้องตามแก้:** `.../report/MemberHistoryReport.java` ส่วนที่บอก "ชั่วโมงที่จะได้ส่วนลดครั้งถัดไป" (เดิมคำนวณรอบถัดไปเป็น 40, 60 ซึ่งจะผิด)

```java
int interval = PaymentService.DISCOUNT_HOUR_INTERVAL;
if (qualifying < interval) { // [B1] ยังไม่ถึงชั่วโมงที่ 20
    text.append("ชั่วโมงที่จะได้ส่วนลด 30% : ชั่วโมงที่ ").append(interval)
            .append(" (อีก ").append(interval - qualifying).append(" ชั่วโมง)\n");
} else {
    text.append("ใช้สิทธิ์ส่วนลดชั่วโมงที่ 20 ไปแล้ว\n");
}
```

**ผลทดสอบ:** สมาชิกใหม่จอง 8 ชม. 5 ครั้ง → 1,440 / 1,440 / **1,314** / 1,440 / 1,440 (ครั้งที่ 5 ครอบคลุมชั่วโมงที่ 33–40 ไม่ได้ส่วนลดแล้ว ถ้าเป็นโค้ดเดิมครั้งที่ 5 จะเป็น 1,314) และรายงานประวัติแสดง "ใช้สิทธิ์ส่วนลดชั่วโมงที่ 20 ไปแล้ว"

**พูดอธิบาย:** "กฎส่วนลดอยู่ในเมธอดเล็ก ๆ `isDiscountHour()` เมธอดเดียว ทั้ง `calculateCourtFee()` และ `countDiscountedHours()` เรียกเมธอดนี้ จึงแก้จุดเดียว"

### B2. เปลี่ยนเป็น "ลด 30%" (จ่าย 70% = 126 บาท)

```java
// .../service/PaymentService.java
public static final int DISCOUNT_PERCENT_TO_PAY = 70; // [B2] เดิม 30 -> 180 x 70 / 100 = 126
```

**ตามแก้ข้อความ** "จ่าย 30%" / "จ่ายเพียง 30%" ใน `BookingController.updatePreview()`, `Cashier.bookCourt()`, `Booking.getDetailsText()` ("รวมส่วนลด 30%"), `MemberHistoryReport` (ฝั่ง Swing: `BookingPanel`)

**พูดอธิบาย:** "ชื่อค่าคงที่คือ 'เปอร์เซ็นต์ที่ต้องจ่าย' ไม่ใช่ 'เปอร์เซ็นต์ที่ลด' จึงต้องใส่ 70 ไม่ใช่ 30"

### B3. ให้ชั่วโมงที่จองด้วยแพ็กเกจนับเป็นชั่วโมงสะสมด้วย

**ต้องแก้ที่:** `.../service/BookingService.java` 2 จุด (จุดที่ 2 คือกับดัก)

```java
// จุดที่ 1: createBooking()
if (usePackage) {
    member.usePackageHours(hours);      // หักชั่วโมงแพ็กเกจ (ไม่มีรายได้ใหม่)
}
member.addQualifyingHours(hours);       // [B3] นับชั่วโมงสะสมทุกการจอง รวมแพ็กเกจด้วย

// จุดที่ 2: cancelBooking() ต้องแก้ให้ "ย้อนกลับ" ตรงกัน
if (booking.isUsedPackage()) {
    member.returnPackageHours(booking.getDurationHours());
}
member.removeQualifyingHours(booking.getDurationHours()); // [B3] ต้องแก้คู่กับ createBooking()
```

**กับดัก:** ถ้าแก้แค่จุดที่ 1 การยกเลิกการจองแบบแพ็กเกจจะคืนชั่วโมงแพ็กเกจแต่ไม่ลดชั่วโมงสะสม → ชั่วโมงสะสมเกินจริง

**ผลทดสอบ:** M003 จองแพ็กเกจ 4 ชม. → สะสม 4, แพ็กเกจ 11; ยกเลิก → สะสม 0, แพ็กเกจ 15

**คำถามต่อที่อาจโดน:** "ถ้าชั่วโมงที่ 20 ตกอยู่ในการจองแพ็กเกจล่ะ?" → ชั่วโมงนั้นฟรีอยู่แล้ว (ค่าสนาม 0) สิทธิ์ส่วนลดจึง "ถูกใช้ไป" โดยสมาชิกไม่ได้ประโยชน์ ควรเสนอให้อาจารย์ตัดสินว่าต้องการแบบไหน

### B4. ห้ามจองชั่วโมงที่ผ่านไปแล้วของวันนี้ (แก้บั๊กที่พบในการวิเคราะห์)

**ต้องแก้ที่:** `.../service/BookingService.java` เมธอด private `validate()` (ตัวที่ `validateBooking()` และ `validateUpdate()` เรียก) ใส่ก่อนบรรทัด `LocalTime start = LocalTime.of(startHour, 0);`

```java
// [B4] ห้ามจองชั่วโมงที่ผ่านไปแล้วของวันนี้
if (date.equals(LocalDate.now()) && startHour <= LocalTime.now().getHour()) {
    return "เวลา " + String.format("%02d:00", startHour) + " ของวันนี้ผ่านไปแล้ว กรุณาเลือกเวลาถัดไป";
}
```

**ทำไมใช้ `<=`:** ถ้าตอนนี้ 17:41 ชั่วโมง 17:00 เริ่มไปแล้ว ต้องห้ามด้วย จองได้ตั้งแต่ 18:00

**ทำไมใส่ใน `validate()`:** หน้าจอ JavaFX, Swing, Console, `createBooking()` และการแก้ไขการจอง เรียกผ่านเมธอดนี้ทั้งหมด จึงแก้ครั้งเดียวได้ทุกที่ และหน้าจอจองแสดงเหตุผลสีแดงพร้อมปิดปุ่มยืนยันให้เอง

**ผลทดสอบ** (จำลองเวลา 17:41 วันจันทร์): จอง 13:00 วันนี้ → "เวลา 13:00 ของวันนี้ผ่านไปแล้ว..."; จอง 18:00 → ผ่าน

### B5. จองล่วงหน้าได้ไม่เกิน 30 วัน

```java
// .../service/BookingService.java
public static final int MAX_ADVANCE_DAYS = 30; // [B5]

// ใน validate() ต่อจาก B4
if (date.isAfter(LocalDate.now().plusDays(MAX_ADVANCE_DAYS))) {
    return "จองล่วงหน้าได้ไม่เกิน " + MAX_ADVANCE_DAYS + " วัน";
}
```

**ผลทดสอบ:** วันนี้ + 30 วัน → ผ่าน, + 31 วัน → ปฏิเสธ (ข้อมูลตัวอย่างจองไว้ +7 ถึง +9 วัน จึงไม่กระทบ)

### B6. จองได้ครั้งละไม่เกิน 4 ชั่วโมง

**ขั้นที่ 1:** เพิ่มกฎใน `.../service/BookingService.java`

```java
public static final int MAX_HOURS_PER_BOOKING = 4; // [B6]

// ใน validate() ต่อจาก if (hours <= 0) {...}
if (hours > MAX_HOURS_PER_BOOKING) {
    return "จองได้ครั้งละไม่เกิน " + MAX_HOURS_PER_BOOKING + " ชั่วโมง";
}
```

**กับดักใหญ่:** แก้แค่นี้แล้วรัน → **โปรแกรมพังตั้งแต่เปิด** เพราะ `loadSampleData()` จอง M002 ครั้งละ 6 ชม. (ทดสอบแล้ว: `IllegalArgumentException` ที่ `BadmintonSystem.loadSampleData`)

**ขั้นที่ 2:** แก้ข้อมูลตัวอย่างใน `.../BadmintonSystem.java` ให้ยังได้ 18 ชม. เท่าเดิม

```java
// [B6] จองได้ครั้งละไม่เกิน 4 ชม. จึงเปลี่ยนเป็น 6 ครั้ง ครั้งละ 3 ชม. (รวมยัง 18 ชม. เท่าเดิม)
for (int i = 7; i <= 12; i++) {
    Booking booking = bookingService.createBooking(m2, 12, LocalDate.now().plusDays(i), 13, 3, false);
    paymentService.payCourtFee(booking, "slips/sample-" + booking.getBookingId() + ".jpg");
}
```

**ขั้นที่ 3 (หน้าจอ):** ให้ช่องกรอกจำกัดตามกฎ
```java
// .../fx/cashier/BookingController.java
hoursSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, BookingService.MAX_HOURS_PER_BOOKING, 1));
// .../user/Cashier.java ใน bookCourt()
int hours = InputHelper.readInt("จำนวนชั่วโมงที่ต้องการจอง (1-" + BookingService.MAX_HOURS_PER_BOOKING + "): ",
        1, BookingService.MAX_HOURS_PER_BOOKING); // [B6]
```

**ผลทดสอบ:** หลังแก้ เปิดได้ทั้ง Console และ JavaFX; M002 มี 6 การจอง รวม 18 ชม.

**พูดอธิบาย:** "กฎจริงอยู่ใน Service ส่วน Spinner แค่ช่วยไม่ให้ผู้ใช้เลือกผิด ถึงจะแก้แค่ Spinner ก็ยังต้องมีกฎใน Service เพราะ Console และโค้ดอื่นเรียก `createBooking()` ได้โดยตรง"

### B7. สมาชิกคนเดียวห้ามจองเวลาซ้อนกัน แม้จะคนละสนาม

**แนวคิด:** `overlapsWith()` เดิมเช็ก "สนามเดียวกัน" ด้วย จึงต้องมีเมธอดใหม่ที่เช็กแค่ "วันเดียวกัน + เวลาทับ" แล้ววนการจองของสมาชิกคนนั้น

**ขั้นที่ 1:** `.../model/Booking.java`
```java
/** [B7] ทับเวลากับช่วงที่ถามมาหรือไม่ โดยไม่สนสนาม (ใช้ตรวจว่าสมาชิกคนเดียวจองซ้อนเวลา) */
public boolean overlapsTime(LocalDate otherDate, LocalTime otherStart, LocalTime otherEnd) {
    return bookingDate.equals(otherDate)
            && startTime.isBefore(otherEnd) && otherStart.isBefore(getEndTime());
}
```

**ขั้นที่ 2:** `.../service/BookingService.java`
```java
/**
 * [B7] หา "การจองอื่นของสมาชิกคนนี้" ที่เวลาทับกัน (ไม่สนว่าสนามไหน)
 * คืนข้อความ error หรือ null ถ้าไม่ทับ / ignored = การจองที่ไม่ต้องนับ (ใช้ตอนแก้ไขการจอง)
 */
public String checkMemberTimeConflict(Member member, LocalDate date, int startHour, int hours, Booking ignored) {
    LocalTime start = LocalTime.of(startHour, 0);
    LocalTime end = start.plusHours(hours);
    for (Booking other : member.getBookings()) {
        if (other != ignored && other.overlapsTime(date, start, end)) {
            return "สมาชิก " + member.getMemberId() + " มีการจอง " + other.getBookingId()
                    + " (สนาม " + other.getCourt().getCourtNumber() + ") ในช่วงเวลานี้อยู่แล้ว";
        }
    }
    return null;
}

// ใน createBooking() ต่อจาก validateBooking()
String conflict = checkMemberTimeConflict(member, date, startHour, hours, null); // [B7]
if (conflict != null) {
    throw new IllegalArgumentException(conflict);
}

// ใน updateBooking() ก่อน booking.reschedule(...)
String conflict = checkMemberTimeConflict(booking.getMember(), date, startHour,
        booking.getDurationHours(), booking); // [B7] ไม่นับตัวเอง
if (conflict != null) {
    throw new IllegalArgumentException(conflict);
}
```

**ขั้นที่ 3 (หน้าจอแสดงทันที):** `.../fx/cashier/BookingController.java` ใน `updatePreview()`
```java
String error = bookingService.validateBooking(court, date, startHour, hours);
if (error == null) { // [B7] สมาชิกคนนี้มีการจองเวลาทับกันอยู่แล้วหรือไม่
    error = bookingService.checkMemberTimeConflict(currentMember, date, startHour, hours, null);
}
```

**ผลทดสอบ:** M002 มี B001 (สนาม 12, 13:00–19:00 วันที่ +7) → ขอสนาม 3 เวลา 14:00 วันเดียวกัน ถูกปฏิเสธ "สมาชิก M002 มีการจอง B001 (สนาม 12) ในช่วงเวลานี้อยู่แล้ว" หน้าจอ JavaFX แสดงข้อความนี้และปิดปุ่มยืนยัน; แก้ไข B001 ไปสนาม 1 เวลาเดิม → ผ่าน (เพราะไม่นับตัวเอง)

**ทำไมต้องมีพารามิเตอร์ `ignored`:** ตอนแก้ไขการจอง ถ้าไม่ยกเว้นตัวเอง จะชนกับเวลาเดิมของตัวเองเสมอ (เป็นหลักเดียวกับ `isCourtAvailable(..., ignored)` ที่มีอยู่แล้วในโค้ด)

### B8. เบอร์โทรต้องขึ้นต้นด้วย 0 และมี 10 หลัก

```java
// .../model/Member.java ใน setPhoneNumber()
if (phoneNumber == null || !phoneNumber.matches("0\\d{9}")) { // [B8] 0 ตามด้วยตัวเลข 9 ตัว
    throw new IllegalArgumentException("เบอร์โทรศัพท์ต้องขึ้นต้นด้วย 0 และมี 10 หลัก");
}
```

**อธิบาย regex:** `0` = ตัวอักษร 0 ตรง ๆ, `\\d` = ตัวเลข 1 ตัว (ใน Java ต้องเขียน `\\` สองตัว), `{9}` = 9 ครั้งพอดี, `matches()` ต้องตรงทั้งข้อความ

**ผลทดสอบ:** `081234567` (9 หลัก) ✗, `1812345678` ✗, `0811111111` ✓ เบอร์ในข้อมูลตัวอย่างผ่านทั้งหมด

**พูดอธิบาย:** "ตรวจใน setter ของ `Member` และ constructor เรียก setter นี้ จึงเป็น Encapsulation ข้อมูลผิดเข้ามาไม่ได้ทุกช่องทาง"

### B9. ราคาสินค้าห้ามเป็น NaN หรือ Infinity

```java
// .../model/Product.java ใน setUnitPrice()
if (!Double.isFinite(unitPrice) || unitPrice <= 0) { // [B9] กัน NaN และ Infinity
    throw new IllegalArgumentException("ราคาสินค้าต้องมากกว่า 0");
}
```

**ทำไมเดิมหลุด:** `Double.parseDouble("NaN")` ได้ค่า NaN และ `NaN <= 0` เป็น false จึงผ่านการตรวจเดิม

**ผลทดสอบ:** NaN และ Infinity ถูกปฏิเสธทั้งคู่

### B10. ค้นหาการจองด้วยวันที่ที่พิมพ์เป็นปี พ.ศ. ได้

```java
// .../fx/BookingSearchController.java ในเมธอด search() ส่วนค้นด้วยวันที่
try {
    LocalDate date = LocalDate.parse(keyword);
    if (date.getYear() >= 2400) {
        date = date.minusYears(543); // [B10] พิมพ์ปี พ.ศ. มา -> แปลงเป็น ค.ศ.
    }
    results = bookingService.searchByDate(date);
} catch (DateTimeParseException e) {
```

**ผลทดสอบ:** พิมพ์วันที่ของ B001 เป็นปี พ.ศ. (เช่น `2569-10-19`) → พบ 1 รายการ

### B11. แก้ไข/ยกเลิกการจองได้ก่อนเวลาเริ่มอย่างน้อย 2 ชั่วโมง

```java
// .../service/BookingService.java
public static final int MIN_HOURS_BEFORE_CHANGE = 2; // [B11]

// ใน checkModifiable() ต่อจากการตรวจ "วันที่ผ่านมาแล้ว" (ต้อง import java.time.LocalDateTime)
LocalDateTime startAt = booking.getBookingDate().atTime(booking.getStartTime());
if (LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_CHANGE).isAfter(startAt)) {
    return "ต้องแก้ไขหรือยกเลิกก่อนเวลาเริ่มอย่างน้อย " + MIN_HOURS_BEFORE_CHANGE + " ชั่วโมง";
}
```

**สิ่งที่ได้เพิ่มโดยไม่ต้องแก้:** `checkModifiable()` ถูกเรียกทั้งตอนแก้ไขและยกเลิก ในทั้ง JavaFX (`BookingSearchController`), Swing และ Console ทุกหน้าจอจึงได้กฎนี้พร้อมกัน

**ผลทดสอบ** (จำลองเวลา 17:43): การจองวันนี้ 18:00 → "ต้องแก้ไขหรือยกเลิกก่อนเวลาเริ่มอย่างน้อย 2 ชั่วโมง"; การจองวันที่ +7 → แก้ได้

### B12. ยกเลิกการจองแล้วคืนเงินแค่ 50% (ร้านเก็บค่าธรรมเนียม 50%)

**แนวคิด:** ตอนนี้ `PaymentService.cancelBookingPayments()` ลบ Payment ของการจองออกทั้งหมด (รายได้หายทั้งก้อน) ถ้าร้านเก็บ 50% ต้อง **บันทึกเงินส่วนที่ร้านเก็บเป็นรายได้ใหม่** ไม่เช่นนั้นรายงานรายได้จะน้อยกว่าเงินจริง

**ขั้นที่ 1:** เพิ่มหมวดรายได้ใน `.../model/PaymentType.java`
```java
EQUIPMENT_SALE("ขายอุปกรณ์แบดมินตัน"),
CANCELLATION_FEE("ค่าธรรมเนียมยกเลิกการจอง"); // [B12]
```

**ขั้นที่ 2:** `.../service/BookingService.java` (import `PaymentType`)
```java
public static final int CANCELLATION_FEE_PERCENT = 50; // [B12]

// ใน cancelBooking() แทนบรรทัด double refund = paymentService.cancelBookingPayments(booking);
double paid = paymentService.cancelBookingPayments(booking);       // ยอดที่สมาชิกเคยจ่าย
double fee = paid * CANCELLATION_FEE_PERCENT / 100.0;              // [B12] ร้านเก็บไว้ 50%
if (fee > 0) {
    paymentService.recordPayment(PaymentType.CANCELLATION_FEE, fee, member,
            booking.getBookingId(), "หักจากยอดคืนเงิน");
}
double refund = paid - fee;
```

**ทำไม `recordPayment()` ไม่ฟ้องว่าบันทึกซ้ำ:** `isAlreadyPaid()` เช็ก "หมวด + รหัสอ้างอิง" ซึ่งหมวด `CANCELLATION_FEE` ของ B00x ยังไม่เคยมี (และ Payment ค่าสนามเดิมถูกลบไปแล้ว)

**ตามแก้ข้อความ:** กล่องยืนยันใน `BookingSearchController.cancelClicked()` และ `Cashier.cancelBooking()` ยังเขียน "คืนเงิน" เป็นยอดเต็ม ควรเปลี่ยนเป็นครึ่งหนึ่ง

**ผลทดสอบ:** จอง 2 ชม. + รองเท้า 2 คู่ จ่าย 460 → ยกเลิก คืน 230, รายได้วันนี้เหลือเพิ่ม 230 อยู่ในหมวด "ค่าธรรมเนียมยกเลิกการจอง" รายงาน JavaFX แสดงหมวดใหม่เองเพราะวน `PaymentType.values()`

### B13. เพิ่มปุ่ม "ลบวันหยุดที่เลือก" (ผู้ดูแลระบบ)

**ขั้นที่ 1:** `.../service/BookingService.java`
```java
/** [B13] ลบวันหยุดนักขัตฤกษ์ คืน false ถ้าไม่มีวันนี้ในรายการ */
public boolean removePublicHoliday(LocalDate date) {
    return publicHolidays.remove(date); // List.remove(Object) คืน true/false
}
```

**ขั้นที่ 2:** `res/admin/holiday.fxml` เพิ่มปุ่มถัดจากปุ่มเพิ่มวันหยุด
```xml
<Button text="เพิ่มวันหยุด" styleClass="primary-button" onAction="#addHoliday"/>
<Button text="ลบวันหยุดที่เลือก" onAction="#removeHoliday"/> <!-- [B13] -->
```

**ขั้นที่ 3:** `.../fx/admin/HolidayController.java` (import `java.util.ArrayList`)
```java
@FXML private ListView<String> holidayList;
private List<LocalDate> shownHolidays = new ArrayList<>(); // [B13] วันที่จริงของแต่ละแถวใน ListView

/** [B13] ลบวันหยุดที่เลือกในรายการ */
@FXML
private void removeHoliday() {
    int index = holidayList.getSelectionModel().getSelectedIndex();
    if (index < 0) {
        FxHelper.showError(root, "กรุณาเลือกวันหยุดในรายการก่อน");
        return;
    }
    LocalDate date = shownHolidays.get(index);
    if (FxHelper.confirm(root, "ยืนยันการลบ", "ลบวันหยุด " + FormatHelper.formatDate(date) + " ?")) {
        bookingService.removePublicHoliday(date);
        refresh();
    }
}

// ใน refresh() หลัง Collections.sort(holidays);
shownHolidays = holidays;   // [B13] ลำดับตรงกับแถวใน ListView
```

**ทำไมต้องมี `shownHolidays`:** ListView เก็บแค่ข้อความ เช่น "2026-10-13 (อังคาร)" ต้องมี List ของ `LocalDate` ที่ลำดับตรงกันไว้หาวันที่จริง

**กับดัก:** ชื่อใน `onAction="#removeHoliday"` ต้องตรงกับชื่อเมธอดทุกตัวอักษร ไม่เช่นนั้นโปรแกรมพังตอนเปิด (ดูข้อ E10)

**ผลทดสอบ:** วันหยุด 21 → 20 วัน; เปิดหน้าจอ JavaFX ได้ปกติ

**ข้อควรระวังที่ควรพูด:** ถ้าลบวันหยุดที่มีคนจอง 10:00–13:00 ไว้แล้ว วันนั้นจะกลับเป็นวันธรรมดา (เปิด 13:00) การจองเดิมจะอยู่นอกเวลาทำการ

### B14. ค้นหาการจองตามหมายเลขสนาม

**ขั้นที่ 1:** `.../service/BookingService.java`
```java
/** [B14] ค้นหาการจองตามหมายเลขสนาม */
public List<Booking> searchByCourt(int courtNumber) {
    List<Booking> result = new ArrayList<>();
    for (Booking booking : bookings) {
        if (booking.getCourt().getCourtNumber() == courtNumber) {
            result.add(booking);
        }
    }
    return result;
}
```

**ขั้นที่ 2:** `.../fx/BookingSearchController.java`
```java
private static final String[] SEARCH_TYPES = {
        "รหัสการจอง", "รหัสสมาชิก", "ชื่อสมาชิก", "วันที่ใช้สนาม (ปี-เดือน-วัน)",
        "หมายเลขสนาม", // [B14] เพิ่มก่อน "แสดงทั้งหมด"
        "แสดงทั้งหมด"
};
private static final int BY_COURT = 4;   // [B14]
private static final int SHOW_ALL = 5;   // [B14] เลื่อนจาก 4 เป็น 5

// ใน search() เพิ่มก่อน else สุดท้าย (ที่ค้นด้วยวันที่)
} else if (type == BY_COURT) { // [B14]
    try {
        results = bookingService.searchByCourt(Integer.parseInt(keyword));
    } catch (NumberFormatException e) {
        if (showErrors) {
            FxHelper.showError(root, "หมายเลขสนามต้องเป็นตัวเลข เช่น 5");
        }
        return;
    }
} else {
```

**กับดัก:** โค้ดใช้ "ลำดับในรายการ" เป็นตัวเลข ถ้าแทรกตัวเลือกใหม่แต่ไม่แก้ `SHOW_ALL` จาก 4 เป็น 5 → เลือก "หมายเลขสนาม" แล้วจะกลายเป็นแสดงทั้งหมด และเลือก "แสดงทั้งหมด" จะไปตกที่ค้นด้วยวันที่

**ผลทดสอบ:** ตัวเลือกครบ 6 แบบ ค้นสนาม 12 → พบ 3 รายการ (B001–B003)

### B15. แสดง "อีกกี่ชั่วโมงจะได้ส่วนลด" เมื่อกดตรวจสอบสมาชิกในหน้าจอง

```java
// .../fx/cashier/BookingController.java ใน lookupMember() ส่วนที่พบสมาชิก
FxHelper.setTone(memberInfoLabel, FxHelper.OK);
int qualifying = currentMember.getQualifyingHours();
int interval = PaymentService.DISCOUNT_HOUR_INTERVAL;
int nextDiscountHour = (qualifying / interval + 1) * interval; // [B15] เช่น สะสม 18 -> 20
memberInfoLabel.setText(currentMember.getFullName()
        + "\nชั่วโมงสะสม: " + qualifying + " ชม."
        + " | แพ็กเกจคงเหลือ: " + currentMember.getRemainingPackageHours() + " ชม."
        + "\nอีก " + (nextDiscountHour - qualifying) + " ชม. จะถึงชั่วโมงส่วนลด (ชั่วโมงที่ "
        + nextDiscountHour + ")");
```

**อธิบายสูตร:** `qualifying / 20` เป็นการหารจำนวนเต็ม (ตัดเศษ) เช่น 18/20 = 0 → (0+1)×20 = 20; สะสม 21 → 21/20 = 1 → (1+1)×20 = 40 (สูตรเดียวกับที่ใช้ใน `MemberHistoryReport`)

**ผลทดสอบ:** M002 → "อีก 2 ชม. จะถึงชั่วโมงส่วนลด (ชั่วโมงที่ 20)"

### B16. แก้ไขชื่อและเบอร์โทรของสมาชิก

```java
// .../service/MemberService.java
/**
 * [B16] แก้ไขชื่อและเบอร์โทรของสมาชิก
 * ตรวจทุกอย่างก่อนเปลี่ยนจริง เพื่อไม่ให้ข้อมูลถูกแก้ไปครึ่งเดียว
 */
public void updateMember(Member member, String newFullName, String newPhone) {
    if (member == null) {
        throw new IllegalArgumentException("ไม่พบสมาชิก");
    }
    Member owner = findByPhone(newPhone);
    if (owner != null && owner != member) {
        throw new IllegalArgumentException("เบอร์โทรศัพท์นี้เป็นของสมาชิก " + owner.getMemberId() + " แล้ว");
    }
    if (newFullName == null || newFullName.isBlank()) {
        throw new IllegalArgumentException("ชื่อ-นามสกุลห้ามว่าง");
    }
    member.setPhoneNumber(newPhone);  // ถ้ารูปแบบเบอร์ผิดจะโยน exception ตรงนี้ ก่อนเปลี่ยนชื่อ
    member.setFullName(newFullName);
}
```

**จุดที่ควรอธิบาย:**
- `owner != member` → ยอมให้ใช้เบอร์เดิมของตัวเองได้ (ไม่ชนกับตัวเอง)
- ลำดับ: ตรวจชื่อก่อน แล้ว `setPhoneNumber()` (อาจโยน exception) ก่อน `setFullName()` → ถ้าเบอร์ผิด ชื่อจะไม่ถูกเปลี่ยนไปครึ่งเดียว
- setter ของ `Member` มีอยู่แล้วและตรวจข้อมูลในตัว (Encapsulation) จึงไม่ต้องเขียนตรวจรูปแบบเบอร์ซ้ำ

**ผลทดสอบ:** ใช้เบอร์ของ M002 → ปฏิเสธ; เบอร์ `abc` → ปฏิเสธ และชื่อไม่เปลี่ยน; ข้อมูลถูก → เปลี่ยนสำเร็จ

(ถ้าอาจารย์ให้ทำหน้าจอด้วย: เพิ่มปุ่มในแท็บสมาชิก `member.fxml` + เมธอดใน `MemberController` ที่เปิด `TextInputDialog` แล้วเรียก `updateMember()` ตามรูปแบบเดียวกับ B13)

### B17. เพิ่มคอลัมน์ "เบอร์โทร" ในตารางค้นหาการจอง (JavaFX)

**ต้องแก้ 2 ไฟล์ให้ลำดับตรงกัน**

```xml
<!-- res/booking-search.fxml -->
<TableColumn text="สมาชิก" prefWidth="150"/>
<TableColumn text="เบอร์โทร" prefWidth="100"/> <!-- [B17] -->
<TableColumn text="สนาม" prefWidth="55"/>
```

```java
// .../fx/BookingSearchController.java ใน initialize()
FxHelper.setColumnValues(table,
        Booking::getBookingId,
        b -> b.getMember().getMemberId() + " " + b.getMember().getFullName(),
        b -> b.getMember().getPhoneNumber(), // [B17] ต้องอยู่ลำดับเดียวกับคอลัมน์ใน FXML
        b -> b.getCourt().getCourtNumber(),
        ...
```

**กับดัก:** `FxHelper.setColumnValues()` จับคู่ "คอลัมน์ที่ i ใน FXML" กับ "getter ตัวที่ i" ตามลำดับ ถ้าเพิ่มที่ FXML แต่ไม่เพิ่ม getter ตำแหน่งเดียวกัน ข้อมูลทุกคอลัมน์หลังจากนั้นจะเลื่อนผิดช่อง

**ผลทดสอบ:** ตารางมี 11 คอลัมน์ คอลัมน์ที่ 3 แสดง `0898765432` และคอลัมน์ที่ 4 แสดงหมายเลขสนามถูกต้อง

---

## หมวด C: เพิ่มความสามารถใหม่ที่วัดความเข้าใจ OOP (ระดับยาก)

### C1. เพิ่มรายงานรายได้ "รายปี"

**แนวคิด:** ใช้ Inheritance เหมือน `DailyIncomeReport` / `MonthlyIncomeReport` สร้างคลาสลูกใหม่ของ `IncomeReport` เขียนเพิ่มแค่ 2 เมธอด (`isInPeriod()`, `getReportTitle()`) ส่วนสูตรรวมเงินได้จากคลาสแม่ทั้งหมด

**ขั้นที่ 1:** สร้างไฟล์ใหม่ `.../report/YearlyIncomeReport.java`
```java
package com.example.badminton.report;

import com.example.badminton.model.Booking;
import com.example.badminton.model.Payment;

import java.time.LocalDate;
import java.util.List;

/**
 * [C1] รายงานรายได้ "รายปี"
 * สืบทอด IncomeReport เหมือนรายวัน/รายเดือน เขียนเพิ่มแค่ 2 เมธอดที่ต่างกัน
 */
public class YearlyIncomeReport extends IncomeReport {

    private final int year;

    public YearlyIncomeReport(List<Payment> payments, List<Booking> bookings, int year) {
        super(payments, bookings);
        this.year = year;
    }

    /** Override: นับเฉพาะรายการที่อยู่ในปีเดียวกับรายงาน */
    @Override
    protected boolean isInPeriod(LocalDate date) {
        return date.getYear() == year;
    }

    @Override
    public String getReportTitle() {
        return "รายงานรายได้ประจำปี " + year + " (พ.ศ. " + (year + 543) + ")";
    }
}
```

**ขั้นที่ 2:** `.../service/ReportService.java` (import `YearlyIncomeReport`)
```java
/** [C1] รายงานรายปี */
public IncomeReport createYearlyReport(int year) {
    return new YearlyIncomeReport(paymentService.getAllPayments(), bookingService.getAllBookings(), year);
}
```

**ขั้นที่ 3 (เมนู Console ผู้ดูแล):** `.../user/Administrator.java`
```java
System.out.println("9. ดูรายงานรายได้รายปี");                      // [C1] เพิ่มบรรทัดเมนู
int choice = InputHelper.readInt("เลือกเมนู: ", 0, 9);             // [C1] 8 -> 9
case 9 -> viewYearlyReport();                                       // [C1] เพิ่มใน switch

/** [C1] ตัวแปรชนิด Reportable เก็บ YearlyIncomeReport -> ใช้ showReport() เดิมได้เลย */
private void viewYearlyReport() {
    printSection("รายงานรายได้รายปี");
    int year = InputHelper.readInt("ปี ค.ศ. (เช่น 2026): ", 2000, 2100);
    Reportable report = reportService.createYearlyReport(year);
    reportService.showReport(report);
}
```

**ถ้าให้ทำในหน้าจอ JavaFX ด้วย:** ใน `res/admin/income-report.fxml` เพิ่ม `RadioButton fx:id="yearlyRadio"` ใน `toggleGroup="$typeGroup"` เดียวกัน แล้วใน `IncomeReportController.showReport()` เพิ่มเงื่อนไข `else if (yearlyRadio.isSelected()) report = reportService.createYearlyReport(yearSpinner.getValue());` (ส่วนแสดงตารางไม่ต้องแก้ เพราะเรียกผ่านชนิด `IncomeReport` อยู่แล้ว)

**ผลทดสอบ:** หัวรายงาน "รายงานรายได้ประจำปี 2026 (พ.ศ. 2569)" ยอดรวมถูกต้อง เมนู Console ข้อ 9 ใช้ได้

**พูดอธิบาย:** "ไม่ต้องแก้ `IncomeReport`, `Reportable` หรือ `showReport()` เลย แค่เพิ่มคลาสลูก นี่คือประโยชน์ของ Inheritance + Polymorphism คือเพิ่มของใหม่โดยไม่แก้ของเดิม"

### C2. เพิ่มรายงานรายได้ "ตามช่วงวันที่"

ไฟล์ใหม่ `.../report/DateRangeIncomeReport.java`
```java
package com.example.badminton.report;

import com.example.badminton.model.Booking;
import com.example.badminton.model.Payment;
import com.example.badminton.util.FormatHelper;

import java.time.LocalDate;
import java.util.List;

/** [C2] รายงานรายได้ "ตามช่วงวันที่" (รวมวันเริ่มและวันสิ้นสุด) */
public class DateRangeIncomeReport extends IncomeReport {

    private final LocalDate from;
    private final LocalDate to;

    public DateRangeIncomeReport(List<Payment> payments, List<Booking> bookings, LocalDate from, LocalDate to) {
        super(payments, bookings);
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("วันเริ่มต้องไม่หลังวันสิ้นสุด");
        }
        this.from = from;
        this.to = to;
    }

    /** Override: from <= date <= to */
    @Override
    protected boolean isInPeriod(LocalDate date) {
        return !date.isBefore(from) && !date.isAfter(to);
    }

    @Override
    public String getReportTitle() {
        return "รายงานรายได้ " + FormatHelper.formatDate(from) + " ถึง " + FormatHelper.formatDate(to);
    }
}
```

และใน `ReportService`:
```java
/** [C2] รายงานช่วงวันที่ */
public IncomeReport createRangeReport(LocalDate from, LocalDate to) {
    return new DateRangeIncomeReport(paymentService.getAllPayments(), bookingService.getAllBookings(), from, to);
}
```

**อธิบาย:** "ไม่ก่อน from และไม่หลัง to" = อยู่ในช่วงแบบรวมขอบ ใช้ `!isBefore` / `!isAfter` แทน `>=` / `<=` เพราะ `LocalDate` เป็นออบเจกต์ ใช้เครื่องหมายเปรียบเทียบตรง ๆ ไม่ได้

**ผลทดสอบ:** ช่วง เมื่อวาน–วันนี้ ได้ยอดถูก; ใส่วันเริ่มหลังวันสิ้นสุด → "วันเริ่มต้องไม่หลังวันสิ้นสุด"

### C3. เพิ่มหมวดรายได้ใหม่ "ค่าเช่าไม้แบด" (30 บาท/อัน)

**ขั้นที่ 1:** `.../model/PaymentType.java`
```java
EQUIPMENT_SALE("ขายอุปกรณ์แบดมินตัน"),
RACKET_RENTAL("ค่าเช่าไม้แบด"); // [C3] อย่าลืมเปลี่ยน ; ของตัวก่อนหน้าเป็น ,
```

**ขั้นที่ 2:** `.../service/PaymentService.java`
```java
private int nextRacketNumber = 1;                          // [C3]
public static final double RACKET_PRICE_PER_PIECE = 30.0;  // [C3]

/** [C3] เช่าไม้แบดให้การจอง บันทึกรายได้หมวดใหม่ RACKET_RENTAL */
public Payment rentRacket(Booking booking, int pieces, String slipReference) {
    if (booking == null) {
        throw new IllegalArgumentException("ไม่พบการจอง ต้องเช่าไม้คู่กับการจองสนาม");
    }
    if (pieces <= 0) {
        throw new IllegalArgumentException("จำนวนไม้ต้องมากกว่า 0");
    }
    String racketId = String.format("RK%03d", nextRacketNumber);
    Payment payment = recordPayment(PaymentType.RACKET_RENTAL, pieces * RACKET_PRICE_PER_PIECE,
            booking.getMember(), racketId, slipReference);
    nextRacketNumber++;
    return payment;
}
```

**สิ่งที่ได้ฟรี:** รายงานรายได้ทั้ง Console และ JavaFX วน `for (PaymentType type : PaymentType.values())` จึงมีแถว "ค่าเช่าไม้แบด" เพิ่มเองโดยไม่ต้องแก้รายงาน

**ผลทดสอบ:** เช่า 2 อัน → รายงานวันนี้หมวด "ค่าเช่าไม้แบด" = 60 บาท, enum มี 5 หมวด (6 ถ้าทำ B12 ด้วย)

**ข้อควรรู้:** ถ้ายกเลิกการจอง เงินค่าเช่าไม้จะ **ไม่ถูกคืน** เพราะ `cancelBookingPayments()` คืนเฉพาะหมวดค่าสนามและค่าเช่ารองเท้า ถ้าต้องการคืนด้วยต้องเก็บรายการเช่าไม้ไว้ใน `Booking` แบบเดียวกับ `ShoeRental`

### C4. เพิ่มบทบาท "ผู้จัดการ" (ดูรายงานได้อย่างเดียว)

**ขั้นที่ 1:** ไฟล์ใหม่ `.../user/Manager.java`
```java
package com.example.badminton.user;

import com.example.badminton.service.MemberService;
import com.example.badminton.service.ReportService;
import com.example.badminton.util.InputHelper;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * [C4] Manager = ผู้จัดการ (บทบาทใหม่) สืบทอดจาก SystemUser
 * ต้อง override 2 เมธอด abstract: getRoleName() และ showMenu() ไม่เช่นนั้นคอมไพล์ไม่ผ่าน
 */
public class Manager extends SystemUser {

    private final ReportService reportService;

    public Manager(String userId, String name, MemberService memberService, ReportService reportService) {
        super(userId, name, memberService);
        this.reportService = reportService;
    }

    @Override
    public String getRoleName() {
        return "ผู้จัดการ";
    }

    @Override
    public void showMenu() {
        printWelcome();
        while (true) {
            System.out.println("\n========== เมนูผู้จัดการ ==========");
            System.out.println("1. ดูรายงานรายได้วันนี้");
            System.out.println("2. ดูรายงานรายได้เดือนนี้");
            System.out.println("0. กลับเมนูหลัก");
            int choice = InputHelper.readInt("เลือกเมนู: ", 0, 2);
            switch (choice) {
                case 1 -> reportService.showReport(reportService.createDailyReport(LocalDate.now()));
                case 2 -> reportService.showReport(reportService.createMonthlyReport(YearMonth.now()));
                case 0 -> {
                    return;
                }
            }
        }
    }
}
```

**ขั้นที่ 2:** `.../BadmintonSystem.java`
```java
private final SystemUser manager; // [C4] ประกาศเป็นชนิดคลาสแม่

// ใน constructor ก่อน loadSampleData();
manager = new Manager("MG01", "สมพร จัดการเก่ง", memberService, reportService); // [C4]

public SystemUser getManager() { // [C4]
    return manager;
}
```

**ขั้นที่ 3:** `.../Main.java`
```java
SystemUser manager = system.getManager(); // [C4]
...
System.out.println("4. เข้าสู่ระบบผู้จัดการ"); // [C4]
System.out.println("5. ออกจากโปรแกรม");
int choice = InputHelper.readInt("เลือกเมนู: ", 1, 5);
...
} else if (choice == 4) {
    openUserMenu(manager); // [C4] เมธอดเดิม ไม่ต้องแก้ -> Polymorphism
} else {
```

**ผลทดสอบ (Console):** เมนู 4 → "เข้าสู่ระบบสำเร็จ: ผู้จัดการ - สมพร จัดการเก่ง (MG01)" → ดูรายงานวันนี้ได้

**ถ้าอาจารย์ถามเรื่อง JavaFX:** `MainController.login()` ใช้ `(user instanceof Cashier) ? cashierTabs : adminTabs` ผู้จัดการจะตกไปที่แท็บผู้ดูแล ต้องเพิ่ม `TabPane` ชุดใหม่ใน `main.fxml` และปุ่มใน `login.fxml` + `LoginController` นี่คือจุดที่ GUI ยังผูกกับชนิดคลาสโดยตรง ถ้าจะให้ดีกว่านี้ อาจเพิ่มเมธอด abstract ใน `SystemUser` ให้แต่ละบทบาทบอกเองว่าใช้หน้าจอไหน

**พูดอธิบาย:** "`Main.openUserMenu(SystemUser user)` ไม่ต้องแก้สักบรรทัด เพราะเรียก `user.showMenu()` แล้ว Java เลือกเมนูของ `Manager` ให้เอง"

### C5. แพ็กเกจมีอายุ 90 วันนับจากวันซื้อ

**ขั้นที่ 1:** `.../model/MembershipPackage.java`
```java
public static final int VALID_DAYS = 90; // [C5]

/** [C5] วันหมดอายุ = วันซื้อ + 90 วัน */
public LocalDate getExpiryDate() {
    return purchaseDate.plusDays(VALID_DAYS);
}

/** [C5] หมดอายุแล้วหรือยัง ณ วันที่ onDate */
public boolean isExpired(LocalDate onDate) {
    return onDate.isAfter(getExpiryDate());
}
```

**ขั้นที่ 2:** `.../model/Member.java` 2 เมธอดต้องข้ามแพ็กเกจที่หมดอายุ
```java
// getRemainingPackageHours()
for (MembershipPackage p : packages) {
    if (!p.isExpired(LocalDate.now())) { // [C5] ไม่นับแพ็กเกจที่หมดอายุ
        total += p.getRemainingHours();
    }
}

// usePackageHours() ในลูป ต่อจาก if (hoursLeftToDeduct == 0) break;
if (p.isExpired(LocalDate.now())) { // [C5] ข้ามแพ็กเกจที่หมดอายุ
    continue;
}
```

**ทำไมต้องแก้ทั้ง 2 เมธอด:** ถ้าแก้แค่ `getRemainingPackageHours()` การตรวจ "ชั่วโมงพอไหม" จะถูก แต่ตอนหักจริงอาจไปหักจากแพ็กเกจที่หมดอายุ

**ผลทดสอบ:** แพ็กเกจที่ซื้อเมื่อ 100 วันก่อน → `isExpired = true`, ชั่วโมงใช้ได้ = 0 และจองแบบแพ็กเกจถูกปฏิเสธ "ชั่วโมงแพ็กเกจไม่พอ"; แพ็กเกจของ M003 (ซื้อวันนี้) ยังใช้ได้ 15

**คำถามต่อ:** "เช็กหมดอายุด้วยวันนี้ หรือวันที่จะมาใช้สนาม?" → เฉลยนี้ใช้วันที่ทำรายการ (วันนี้) ถ้าต้องการใช้วันที่ใช้สนาม ต้องส่งวันที่จองเข้าไปใน `usePackageHours()` ด้วย

### C6. ราคาตามช่วงเวลา: วันธรรมดาก่อน 17:00 ชั่วโมงละ 150 บาท (หลัง 17:00 และวันหยุด 180 บาท)

**แนวคิด:** เมธอดเดิม `calculateCourtFee(member, hours)` ไม่รู้ว่าจองกี่โมงหรือวันอะไร จึงสร้างเมธอด **ชื่อเดียวกันแต่พารามิเตอร์ต่างกัน (Overloading)** แล้วเปลี่ยนผู้เรียกทุกจุด

**ขั้นที่ 1:** `.../service/PaymentService.java`
```java
public static final double OFF_PEAK_PRICE = 150.0;  // [C6]
public static final int OFF_PEAK_END_HOUR = 17;     // [C6] วันธรรมดาก่อน 17:00

/** [C6] ราคาปกติของชั่วโมงที่เริ่มเวลา hour (ยังไม่รวมส่วนลดชั่วโมงที่ 20) */
public double getHourPrice(int hour, boolean weekendOrHoliday) {
    if (!weekendOrHoliday && hour < OFF_PEAK_END_HOUR) {
        return OFF_PEAK_PRICE;
    }
    return Court.STANDARD_PRICE_PER_HOUR;
}

/** [C6] Overloading: ชื่อเดียวกับ calculateCourtFee(member, hours) แต่รับพารามิเตอร์ต่างกัน */
public double calculateCourtFee(Member member, int startHour, int hours, boolean weekendOrHoliday) {
    double total = 0;
    int hoursBefore = member.getQualifyingHours();
    for (int i = 1; i <= hours; i++) {
        int hour = startHour + i - 1;                 // ชั่วโมงที่ i เริ่มกี่โมง
        if (isDiscountHour(hoursBefore + i)) {
            total += getDiscountedHourPrice();         // ชั่วโมงที่ 20 ยังคิด 30% ของราคาปกติ 180
        } else {
            total += getHourPrice(hour, weekendOrHoliday);
        }
    }
    return total;
}
```

**ทำไมรับ `boolean weekendOrHoliday` แทน `LocalDate`:** รายการวันหยุดนักขัตฤกษ์อยู่ใน `BookingService` ไม่ใช่ `PaymentService` จึงให้ผู้เรียกส่งผล `bookingService.isWeekendOrHoliday(date)` มา

**ขั้นที่ 2: เปลี่ยนผู้เรียกทุกจุด** (หาด้วย Alt+F7 ที่ชื่อเมธอด) มี 6 จุด:

| ไฟล์ | จุด | แก้เป็น |
|---|---|---|
| `service/BookingService.java` | `createBooking()` | `paymentService.calculateCourtFee(member, startHour, hours, isWeekendOrHoliday(date))` |
| `fx/cashier/BookingController.java` | `updatePreview()` และ `confirmBooking()` | `paymentService.calculateCourtFee(currentMember, startHour, hours, bookingService.isWeekendOrHoliday(date))` |
| `gui/cashier/BookingPanel.java` (Swing) | `updatePreview()` และ `confirmBooking()` | เหมือน JavaFX |
| `user/Cashier.java` (Console) | `bookCourt()` | `paymentService.calculateCourtFee(member, startHour, hours, bookingService.isWeekendOrHoliday(date))` |

**กับดัก:** ถ้าเปลี่ยนแค่ `createBooking()` ราคาที่ "แสดงในหน้าจอก่อนยืนยัน" จะไม่ตรงกับราคาที่ "บันทึกจริง"

**ผลทดสอบ:** สมาชิกใหม่ วันพฤหัส 13–15 = 300, 16–18 = 150 + 180 = 330, เสาร์ 13–15 = 360; M002 (สะสม 18) วันธรรมดา 13–16 = 150 + **54** + 150 = 354

**พูดอธิบาย:** "เมธอดเดิมยังอยู่ (Overloading) โค้ดที่ยังไม่ได้แก้ก็ยังคอมไพล์ผ่าน แต่จะคิดราคาแบบเดิม จึงต้องหาผู้เรียกให้ครบ"

### C7. เปลี่ยนสถานะการจองจาก String เป็น enum

**ขั้นที่ 1:** ไฟล์ใหม่ `.../model/BookingStatus.java`
```java
package com.example.badminton.model;

/** [C7] สถานะการชำระเงินของการจอง (แทนค่าคงที่ String เดิม) */
public enum BookingStatus {
    UNPAID("รอชำระเงิน"),
    PAID("ชำระเงินแล้ว"),
    PACKAGE("ใช้ชั่วโมงแพ็กเกจ (ไม่ต้องชำระค่าสนาม)");

    private final String thaiName;

    BookingStatus(String thaiName) {
        this.thaiName = thaiName;
    }

    public String getThaiName() {
        return thaiName;
    }
}
```

**ขั้นที่ 2:** `.../model/Booking.java`
```java
// ลบค่าคงที่ STATUS_UNPAID, STATUS_PAID, STATUS_PACKAGE ออก
private BookingStatus paymentStatus; // [C7] String -> enum

// constructor
this.paymentStatus = usedPackage ? BookingStatus.PACKAGE : BookingStatus.UNPAID;
// markCourtFeePaid()
this.paymentStatus = BookingStatus.PAID;
// isCourtFeePaid()
return paymentStatus == BookingStatus.PAID; // enum เทียบด้วย == ได้

/** คืนเป็นข้อความเหมือนเดิม หน้าจอที่เรียกเมธอดนี้จึงไม่ต้องแก้ */
public String getPaymentStatus() {
    return paymentStatus.getThaiName();
}

public BookingStatus getStatus() {
    return paymentStatus;
}

// getDetailsText(): .append(paymentStatus) -> .append(paymentStatus.getThaiName())
```

**จุดที่ควรอธิบาย:** `getPaymentStatus()` ยังคืน `String` เหมือนเดิม จึงไม่ต้องแก้ตารางใน JavaFX/Swing/Console เลย (เป็นการเปลี่ยนภายในคลาสโดยภายนอกไม่รู้ = Encapsulation) และ enum ทำให้ "พิมพ์สถานะผิด" ไม่ได้อีก

**ผลทดสอบ:** คอมไพล์ผ่านทั้งโปรเจกต์ `getStatus()` = `PAID`, `getPaymentStatus()` = "ชำระเงินแล้ว"

### C8. สร้าง interface `Payable` ให้ทั้ง Booking และ Sale

**ไฟล์ใหม่** `.../model/Payable.java`
```java
package com.example.badminton.model;

import java.util.List;

/** [C8] interface Payable = รายการที่ "มียอดเงินต้องชำระ" (การจอง และใบเสร็จขายอุปกรณ์) */
public interface Payable {

    /** รหัสของรายการ เช่น B001 หรือ S001 */
    String getReferenceId();

    /** ยอดเงินรวมของรายการ */
    double getTotalAmount();

    /** static method ใน interface: รวมยอดของรายการชนิดไหนก็ได้ที่เป็น Payable */
    static double sumTotal(List<? extends Payable> items) {
        double total = 0;
        for (Payable item : items) {
            total += item.getTotalAmount(); // Polymorphism: เรียกเมธอดของ Booking หรือ Sale ตามชนิดจริง
        }
        return total;
    }
}
```

**แก้คลาสเดิม** (ทั้งสองคลาสมี `getTotalAmount()` อยู่แล้ว แค่เพิ่ม `implements`, `@Override` และ `getReferenceId()`)
```java
// .../model/Booking.java
public class Booking implements Payable { // [C8]
    @Override
    public String getReferenceId() { return bookingId; }
    @Override
    public double getTotalAmount() { ... เดิม ... }

// .../model/Sale.java
public class Sale implements Payable { // [C8]
    @Override
    public String getReferenceId() { return saleId; }
    @Override
    public double getTotalAmount() { ... เดิม ... }
```

**ตัวอย่างการใช้:**
```java
List<Payable> items = new ArrayList<>(member.getBookings()); // ใส่ Booking
items.add(sale);                                             // ใส่ Sale ใน List เดียวกันได้
double total = Payable.sumTotal(items);
```

**ผลทดสอบ:** การจอง 3 รายการของ M002 (3,240) + ใบเสร็จลูกขนไก่ 650 → `sumTotal` = 3,890

**`List<? extends Payable>` คืออะไร:** รับ `List<Booking>` หรือ `List<Sale>` หรือ `List<Payable>` ได้ทั้งหมด (ถ้าเขียน `List<Payable>` จะส่ง `List<Booking>` เข้ามาไม่ได้)

### C9. เพิ่มสต็อกสินค้า (ขายเกินจำนวนคงเหลือไม่ได้)

**ขั้นที่ 1:** `.../model/Product.java`
```java
private int stock;                             // [C9] จำนวนคงเหลือ
public static final int DEFAULT_STOCK = 20;    // [C9] สต็อกเริ่มต้น

/** constructor เดิม (3 ค่า) ยังใช้ได้ -> เรียก constructor ใหม่ด้วยสต็อกเริ่มต้น (Overloading) */
public Product(String productId, String name, double unitPrice) {
    this(productId, name, unitPrice, DEFAULT_STOCK);
}

public Product(String productId, String name, double unitPrice, int stock) {
    if (stock < 0) {
        throw new IllegalArgumentException("สต็อกห้ามติดลบ");
    }
    ... ตรวจชื่อ/ราคาเหมือนเดิม ...
    this.stock = stock;
}

public int getStock() {
    return stock;
}

/** [C9] รับสินค้าเข้า */
public void addStock(int quantity) {
    if (quantity <= 0) {
        throw new IllegalArgumentException("จำนวนที่รับเข้าต้องมากกว่า 0");
    }
    stock += quantity;
}

/** [C9] ตัดสต็อก ห้ามเกินจำนวนคงเหลือ */
public void reduceStock(int quantity) {
    if (quantity <= 0 || quantity > stock) {
        throw new IllegalArgumentException("สินค้า " + name + " คงเหลือ " + stock + " ชิ้น ไม่พอ");
    }
    stock -= quantity;
}
```

**ขั้นที่ 2:** `.../service/ProductService.java` ใน `createSale()` (import `LinkedHashMap`, `Map`)
```java
// [C9] รวมจำนวนต่อสินค้าก่อน (ตะกร้าอาจมีสินค้าเดียวกัน 2 บรรทัด) แล้วตรวจสต็อกให้ครบก่อนขาย
Map<Product, Integer> needed = new LinkedHashMap<>();
for (SaleItem item : items) {
    needed.merge(item.getProduct(), item.getQuantity(), Integer::sum);
}
for (Map.Entry<Product, Integer> entry : needed.entrySet()) {
    if (entry.getKey().getStock() < entry.getValue()) {
        throw new IllegalArgumentException("สินค้า " + entry.getKey().getName() + " คงเหลือ "
                + entry.getKey().getStock() + " ชิ้น แต่ต้องการ " + entry.getValue() + " ชิ้น");
    }
}
// ... สร้าง Sale และ recordPayment เหมือนเดิม ...
for (Map.Entry<Product, Integer> entry : needed.entrySet()) {
    entry.getKey().reduceStock(entry.getValue()); // [C9] ตัดสต็อกหลังบันทึกการขายสำเร็จ
}
```

**กับดัก 2 อย่าง:**
1. ตะกร้ามี P01 × 15 และ P01 × 10 (เพิ่ม 2 ครั้ง) ถ้าตรวจทีละบรรทัด แต่ละบรรทัดผ่าน (≤ 20) แต่รวมกัน 25 เกินสต็อก จึงต้องรวมก่อนด้วย `Map.merge()`
2. ต้อง "ตรวจให้ครบทุกชิ้นก่อน แล้วค่อยตัด" ถ้าตัดไปทีละชิ้นแล้วชิ้นที่ 3 ไม่พอ สต็อกชิ้นแรกจะถูกตัดไปแล้วทั้งที่ขายไม่สำเร็จ

**ผลทดสอบ:** P01 สต็อก 20 ขาย 15+10 → ปฏิเสธ สต็อกยัง 20; ขาย 15 → สำเร็จ เหลือ 5

### C10. แพ็กเกจไม่พอ ให้ใช้เท่าที่มีแล้วจ่ายเงินส่วนที่เหลือ (คำถามให้อธิบายแนวทาง)

ข้อนี้กระทบโครงสร้างข้อมูล อาจารย์มักถามเพื่อดูว่า "รู้ไหมว่าต้องแก้อะไรบ้าง" มากกว่าให้เขียนจนเสร็จ

1. `Booking` ตอนนี้มี `boolean usedPackage` (ทั้งการจองใช้แพ็กเกจหรือไม่ใช้) ต้องเปลี่ยนเป็น `int packageHoursUsed` (ใช้แพ็กเกจกี่ชั่วโมง)
2. `BookingService.createBooking()`: `packageHours = Math.min(คงเหลือ, hours)`, `paidHours = hours - packageHours`, ค่าสนาม = `calculateCourtFee(member, paidHours)`, หักแพ็กเกจ `packageHours`, เพิ่มชั่วโมงสะสม `paidHours`
3. `cancelBooking()`: คืนแพ็กเกจ `packageHoursUsed` และลดชั่วโมงสะสมเฉพาะ `paidHours`
4. `PaymentService.payCourtFee()`: ใช้เงื่อนไข `getCourtFee() > 0` แทน `isUsedPackage()`
5. ทุกที่ที่เรียก `isUsedPackage()` (ตารางสี `CourtScheduleGrid`, รายงาน `IncomeReport.getPackageHoursUsed()`, `MemberHistoryReport`, ข้อความรายละเอียด) ต้องตัดสินใจใหม่ว่าการจองแบบผสมแสดงอย่างไร
6. หน้าจอ: ช่องติ๊ก "ใช้แพ็กเกจ" ต้องไม่ปฏิเสธเมื่อชั่วโมงไม่พอ แต่แสดงว่า "ใช้แพ็กเกจ x ชม. + จ่าย y ชม."

**พูดอธิบาย:** "หาจุดที่ต้องแก้ด้วย Find Usages ของ `isUsedPackage()` เพราะเป็นตัวแปรที่ความหมายเปลี่ยน"

---

## หมวด D: อาจารย์ลบเนื้อเมธอดออก แล้วให้เขียนใหม่

> เฉลยในหมวดนี้คือโค้ดจริงในโปรเจกต์ ควรฝึกเขียนเองโดยไม่ดูเฉลย แล้วอธิบายทีละบรรทัดได้

### D1. `Booking.overlapsWith()` — ตรวจการจองซ้อน

```java
public boolean overlapsWith(int otherCourtNumber, LocalDate otherDate,
                            LocalTime otherStart, LocalTime otherEnd) {
    if (court.getCourtNumber() != otherCourtNumber) {
        return false;                       // ต่างสนาม = ไม่ทับ
    }
    if (!bookingDate.equals(otherDate)) {
        return false;                       // ต่างวัน = ไม่ทับ
    }
    return startTime.isBefore(otherEnd) && otherStart.isBefore(getEndTime()); // A < D และ C < B
}
```
**จำ:** ช่วง [A, B) กับ [C, D) ทับกันเมื่อ **A < D และ C < B** (13–15 กับ 15–16 ไม่ทับ)

### D2. `PaymentService.calculateCourtFee()` — ค่าสนามพร้อมส่วนลดชั่วโมงที่ 20

```java
public double calculateCourtFee(Member member, int hours) {
    double total = 0;
    int hoursBefore = member.getQualifyingHours();
    for (int i = 1; i <= hours; i++) {
        int hourNumber = hoursBefore + i;            // ลำดับชั่วโมงสะสมของชั่วโมงนี้
        if (isDiscountHour(hourNumber)) {
            total += getDiscountedHourPrice();       // 54
        } else {
            total += Court.STANDARD_PRICE_PER_HOUR;  // 180
        }
    }
    return total;
}
```
**จำ:** เมธอดนี้ "แค่คำนวณ" ไม่เปลี่ยนชั่วโมงสะสม (การเพิ่มชั่วโมงสะสมอยู่ใน `createBooking()` หลังคำนวณ)

### D3. `Member.usePackageHours()` — หักชั่วโมงแพ็กเกจจากแพ็กเกจเก่าสุดก่อน

```java
public boolean usePackageHours(int hours) {
    if (hours <= 0 || hours > getRemainingPackageHours()) {
        return false;                                // ไม่พอ -> ไม่หักเลย ชั่วโมงจึงไม่ติดลบ
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
```

### D4. `Member.returnPackageHours()` — คืนชั่วโมงแพ็กเกจเมื่อยกเลิก (ย้อนลำดับการหัก)

```java
public void returnPackageHours(int hours) {
    int hoursLeftToReturn = hours;
    for (int i = packages.size() - 1; i >= 0 && hoursLeftToReturn > 0; i--) { // วนจากแพ็กเกจใหม่สุด
        MembershipPackage p = packages.get(i);
        int hoursToThisPackage = Math.min(p.getUsedHours(), hoursLeftToReturn);
        if (hoursToThisPackage > 0) {
            p.returnHours(hoursToThisPackage);
            hoursLeftToReturn -= hoursToThisPackage;
        }
    }
}
```
**ทำไมวนถอยหลัง:** ตอนหักเริ่มจากแพ็กเกจเก่า ตอนคืนจึงคืนจากแพ็กเกจใหม่ก่อน และคืนได้ไม่เกินที่ใช้ไป (`getUsedHours()`) ชั่วโมงจึงไม่เกิน 15

### D5. `BookingService.isWeekendOrHoliday()` และเวลาเปิด-ปิด

```java
public boolean isWeekendOrHoliday(LocalDate date) {
    DayOfWeek day = date.getDayOfWeek();
    return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY || isPublicHoliday(date);
}

public LocalTime getOpeningTime(LocalDate date) {
    return isWeekendOrHoliday(date) ? WEEKEND_OPEN : WEEKDAY_OPEN;
}
```

### D6. `IncomeReport.getIncomeByType()` — รวมรายได้ตามหมวดในช่วงเวลา

```java
public double getIncomeByType(PaymentType type) {
    double total = 0;
    for (Payment payment : payments) {
        if (payment.getType() == type && isInPeriod(payment.getPaymentDate())) {
            total += payment.getAmount();
        }
    }
    return total;
}
```
**จำ:** `isInPeriod()` เป็น abstract ตรงนี้ไม่รู้ว่ารายวันหรือรายเดือน Java เลือกเมธอดของคลาสลูกให้ตอนรัน

### D7. `PaymentService.isAlreadyPaid()` — กันบันทึกเงินซ้ำ

```java
public boolean isAlreadyPaid(PaymentType type, String referenceId) {
    for (Payment payment : payments) {
        if (payment.getType() == type && payment.getReferenceId().equals(referenceId)) {
            return true;
        }
    }
    return false;
}
```
**จำ:** enum เทียบด้วย `==` ได้ แต่ `String` ต้องใช้ `.equals()`

### D8. `MonthlyIncomeReport` — เขียนคลาสลูกทั้งคลาส

```java
public class MonthlyIncomeReport extends IncomeReport {

    private final YearMonth reportMonth;

    public MonthlyIncomeReport(List<Payment> payments, List<Booking> bookings, YearMonth reportMonth) {
        super(payments, bookings);   // ต้องเรียก constructor ของคลาสแม่ก่อน
        this.reportMonth = reportMonth;
    }

    @Override
    protected boolean isInPeriod(LocalDate date) {
        return YearMonth.from(date).equals(reportMonth);
    }

    @Override
    public String getReportTitle() {
        return "รายงานรายได้ประจำเดือน " + FormatHelper.formatYearMonth(reportMonth);
    }
}
```

---

## หมวด E: "ถ้าแก้แบบนี้ จะเกิดอะไรขึ้น?" (วัดความเข้าใจโดยไม่ต้องเขียนโค้ด)

| ข้อ | อาจารย์ถาม | คำตอบ | หลักฐาน |
|---|---|---|---|
| **E1** | ใน `createBooking()` ถ้าย้าย `member.addQualifyingHours(hours)` ไปไว้ **ก่อน** `calculateCourtFee()` | ส่วนลดคลาดเคลื่อน: M002 สะสม 18 จอง 3 ชม. ระบบจะนับเป็นชั่วโมงที่ 22–24 ได้ **540** บาท (ที่ถูกคือ 414) ลูกค้าเสียสิทธิ์ส่วนลด | ทดสอบรันจริง |
| **E2** | ใน `overlapsWith()` ถ้าเปลี่ยน `<` เป็น `<=` (`!isAfter`) | จองต่อเวลากันไม่ได้: B001 จบ 19:00 แล้วจอง 19:00–20:00 สนามเดียวกันจะถูกปฏิเสธว่า "ถูกจองแล้ว" | ทดสอบรันจริง |
| **E3** | ลบคำว่า `abstract` ออกจาก `IncomeReport` | **คอมไพล์ไม่ผ่าน**: "IncomeReport is not abstract and does not override abstract method isInPeriod(LocalDate)" เพราะคลาสธรรมดามี abstract method ไม่ได้ | ทดสอบคอมไพล์จริง |
| **E4** | `Member.getBookings()` คืน `bookings` ตรง ๆ แทน `new ArrayList<>(bookings)` | คอมไพล์และทำงานปกติ แต่โค้ดภายนอกเรียก `member.getBookings().clear()` หรือ `.add()` ได้ ข้ามการตรวจของ `BookingService` (เสีย Encapsulation) | อ่านจากโค้ด |
| **E5** | เปลี่ยน `remainingHours` ใน `MembershipPackage` เป็น `public` | ใครก็เขียน `p.remainingHours = -5` ได้ ข้ามการตรวจใน `useHours()` ชั่วโมงติดลบได้ | อ่านจากโค้ด |
| **E6** | ลบการเช็ก `isAlreadyPaid()` ใน `recordPayment()` | เรียก `payCourtFee()` ซ้ำกับการจองเดิมได้ รายได้นับซ้ำ: รายได้วันนี้จาก 5,840 กลายเป็น **6,920** (นับ B001 สองครั้ง) | ทดสอบรันจริง |
| **E7** | สลับลำดับ Controller ใน `bindTabs(...)` ของ `MainController` ให้ไม่ตรงกับลำดับแท็บใน `main.fxml` | เปิดแท็บหนึ่งแต่ไปสั่ง `refresh()` ของอีกแท็บ แท็บที่เปิดจะแสดงข้อมูลเก่า (ไม่ error) | อ่านจากโค้ด |
| **E8** | ลบบรรทัด `opens com.example.badminton.fx.cashier to javafx.fxml;` ใน `module-info.java` | คอมไพล์ผ่าน แต่ **เปิดโปรแกรม JavaFX แล้วพังทันที** (`LoadException` ตอนโหลด `main.fxml`) เพราะ FXMLLoader เข้าถึงฟิลด์ `@FXML private` ของ Controller ในแพ็กเกจ cashier ไม่ได้ | ทดสอบรันจริง |
| **E9** | ลบ `fx:id="openingHoursLabel"` ใน `booking.fxml` แต่ยังมีฟิลด์ `@FXML private Label openingHoursLabel` | ฟิลด์เป็น `null` → เปิดโปรแกรมแล้ว `NullPointerException` ใน `BookingController.onDateChanged()` | ทดสอบรันจริง |
| **E10** | เปลี่ยนชื่อเมธอด `lookupMember()` ใน `BookingController` แต่ไม่แก้ `onAction="#lookupMember"` ใน FXML | เปิดโปรแกรมแล้วพัง: "Error resolving onAction='#lookupMember'" ชื่อใน FXML ต้องตรงกับชื่อเมธอด | ทดสอบรันจริง |
| **E11** | ลบ `super(userId, name, memberService);` ใน constructor ของ `Cashier` | **คอมไพล์ไม่ผ่าน**: Java จะเรียก `super()` แบบไม่มีพารามิเตอร์ให้อัตโนมัติ แต่ `SystemUser` ไม่มี constructor แบบนั้น | ทดสอบคอมไพล์จริง |
| **E12** | ให้แต่ละ Controller สร้าง `new BadmintonSystem()` ของตัวเอง | ข้อมูลไม่เชื่อมกัน: จองที่แท็บจองสนามแล้ว แท็บรายงานไม่เห็น เพราะแต่ละตัวมี Service และ `ArrayList` คนละชุด (ตอนนี้ `FxHelper.load()` ส่ง `BadmintonSystem` ตัวเดียวให้ทุก Controller ผ่าน `setControllerFactory`) | อ่านจากโค้ด |
| **E13** | ใน `BookingService.createBooking()` ถ้าลบการเรียก `validateBooking()` ออก เพราะ "หน้าจอตรวจแล้ว" | หน้าจอ JavaFX ยังกันได้ แต่ Console/โค้ดอื่นที่เรียก `createBooking()` ตรงจะจองซ้อนหรือนอกเวลาได้ และถ้ามีคนจองตัดหน้าระหว่างกรอกฟอร์มก็ไม่ถูกตรวจซ้ำ | อ่านจากโค้ด |

---

## เทคนิคตอนถูกสั่งแก้สดใน IntelliJ IDEA

| ต้องการ | วิธี |
|---|---|
| หาข้อความ/ตัวเลขทั้งโปรเจกต์ เช่น "180" | Ctrl + Shift + F |
| ดูว่าใครเรียกเมธอด/ค่าคงที่นี้ | คลิกชื่อ → Alt + F7 (Find Usages) |
| กระโดดไปที่ประกาศของเมธอด/คลาส | Ctrl + คลิก หรือ Ctrl + B |
| เปลี่ยนชื่อเมธอดให้ตามแก้ทุกที่ (Java) | Shift + F6 (Rename) — **ระวัง:** ชื่อใน FXML (`onAction="#..."`) อาจไม่ถูกเปลี่ยนตาม ต้องเช็กเอง |
| คอมไพล์ทั้งโปรเจกต์ | Ctrl + F9 (Build Project) |
| รัน JavaFX | รัน `BadmintonFxApp` หรือคำสั่ง `mvnw javafx:run` |
| รัน Console | รัน `Main` |

**ลำดับที่ควรพูดตอนแก้:** "กฎนี้อยู่ที่ ... → แก้ที่ ... → มีที่อื่นเรียกใช้ไหม (Find Usages) → คอมไพล์ → ลองกรณีปกติ → ลองกรณีที่ควรถูกปฏิเสธ"
