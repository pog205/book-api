# BookStation Backend

---

## 📌 Mục lục
- [1. Giới thiệu](#1-giới-thiệu)
- [2. Công nghệ sử dụng](#2-công-nghệ-sử-dụng)
- [3. Cấu trúc dự án](#3-cấu-trúc-dự-án)
- [4. Hướng dẫn chạy](#4-hướng-dẫn-chạy)
- [5. Quy tắc đặt tên (Naming Convention)](#5-quy-tắc-đặt-tên-naming-convention)
- [6. Thông tin khác](#6-thông-tin-khác)
- [🎪 Hệ thống quản lý sự kiện](#-hệ-thống-quản-lý-sự-kiện)

---

## 1. Giới thiệu
<!-- Để trống -->

## 2. Công nghệ sử dụng
<!-- Để trống -->

## 3. Cấu trúc dự án
<!-- Để trống -->

## 4. Hướng dẫn chạy
<!-- Để trống -->

## 5. Quy tắc đặt tên (Naming Convention)

### 5.1 Package
- Tất cả chữ thường, không dấu, không gạch dưới, không viết hoa.
- Nếu nhiều từ, viết liền hoặc tách bằng dấu chấm cho rõ nghĩa.
- **Ví dụ:**
  - `com.example.attendance`
  - `com.example.attendance.user.staff`
  - `com.example.attendance.dto.request`

### 5.2 Class & File
- Viết hoa chữ cái đầu mỗi từ (PascalCase/UpperCamelCase).
- Tên file trùng tên class.
- **Ví dụ:**
  - `UserController.java`
  - `AttendanceService.java`
  - `UserCreateRequest.java`

### 5.3 Interface
- Giống class, thường kết thúc bằng "able", "er", "Service", "Repository",...
- **Ví dụ:**
  - `UserRepository`
  - `AttendanceService`

### 5.4 Biến (Variable)
- camelCase, chữ thường, từ thứ 2 viết hoa chữ cái đầu.
- **Ví dụ:**
  - `userName`
  - `attendanceList`

### 5.5 Hằng số (Constant)
- Chữ in hoa, các từ cách nhau bằng dấu gạch dưới (_).
- **Ví dụ:**
  - `MAX_ATTENDANCE`
  - `DEFAULT_ROLE`

### 5.6 Method (Hàm)
- camelCase, động từ đứng đầu, rõ nghĩa.
- **Ví dụ:**
  - `getUserById()`
  - `calculateAttendanceRate()`

### 5.7 DTO (Data Transfer Object)
- Kết thúc bằng `Request` hoặc `Response`.
- Nếu nhiều loại, chia package con: `dto.request`, `dto.response`.
- **Ví dụ:**
  - `UserLoginRequest`
  - `UserLoginResponse`
  - `AttendanceSummaryResponse`

### 5.8 Đặt tên package nhiều từ
Đúng:
Sử dụng dấu gạch ngang (-) để phân tách các từ, giúp tên package rõ ràng và dễ đọc.
Ví dụ: user-management, order-processing.
Sai:
Không sử dụng gạch dưới (_), chữ hoa (CamelCase), hoặc ký tự không hợp lệ khác.
Ví dụ sai: user_management, userManagement, user-staff, user staff.
### 5.9 Lưu ý chung
- Không dùng tiếng Việt, không viết tắt khó hiểu.
- Tên phải rõ ràng, mô tả đúng chức năng.
- Không dùng ký tự đặc biệt, trừ dấu gạch dưới cho hằng số.

#### Ví dụ tổng hợp

com.example.attendance.dto.request.UserCreateRequest
com.example.attendance.dto.response.UserResponse
com.example.attendance.controller.UserController
com.example.attendance.service.AttendanceService
com.example.attendance.entity.User

---

## 6. Thông tin khác
<!-- Để trống -->

## 🎪 Hệ thống quản lý sự kiện

BookStation hiện đã được tích hợp **hệ thống quản lý sự kiện** hoàn chỉnh, hỗ trợ các loại sự kiện đa dạng như cuộc thi review, flash sale, gặp gỡ tác giả, v.v.

### 📋 **Tính năng chính:**
- ✅ **Quản lý danh mục sự kiện** - Phân loại theo chủ đề
- ✅ **Tạo sự kiện đa dạng** - Review, sale, offline event...  
- ✅ **Hệ thống quà tặng linh hoạt** - Voucher, sách, điểm, quà vật lý
- ✅ **Theo dõi người tham gia** - Trạng thái realtime
- ✅ **Xử lý claim quà** - Online/offline, nhiều phương thức
- ✅ **Audit trail đầy đủ** - Lịch sử mọi hoạt động

### 📁 **Tài liệu chi tiết:**
- [📊 Phân tích mục đích từng bảng](src/main/resources/sql/TABLE_PURPOSE_ANALYSIS.md)
- [📚 Giải thích đơn giản 6 bảng](src/main/resources/sql/DETAILED_TABLE_EXPLANATION.md)  
- [🔄 Workflow tổng hợp](src/main/resources/sql/COMPLETE_WORKFLOW_EXAMPLE.md)
- [🎯 Workflow đơn giản](src/main/resources/sql/SIMPLE_EVENT_WORKFLOW.md)
- [📈 Event workflow diagram](src/main/resources/sql/EVENT_WORKFLOW_DIAGRAM.md)

### 🗃️ **Database Schema:**
- [🏗️ Tạo bảng](src/main/resources/sql/create_event_tables.sql)
- [📊 Dữ liệu mẫu](src/main/resources/sql/event_sample_data.sql)  
- [🎯 Ví dụ thực tế](src/main/resources/sql/event_real_example.sql)

### 🎯 **Ví dụ sự kiện thực tế:**
```sql
-- Cuộc thi "Review Hay Nhận Quà" 
Event: Viết 3 review ≥ 100 từ trong tháng 7
Quà tặng: 
├─ 🎁 Voucher 100K (20 suất)
├─ 📚 Sách "Đắc Nhân Tâm" miễn phí (50 suất)  
└─ ⭐ 200 điểm thưởng (unlimited)
```
