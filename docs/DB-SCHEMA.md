## DB 스키마

### Users

| 컬럼 | 타입 | PK | NN | FK | UNIQUE | Default |
|------|------|----|----|----|--------|----------|
| user_seq | BIGINT | O | O |  |  | AUTO_INCREMENT |
| user_id | VARCHAR(30) |  | O |  | O |  |
| user_pw | VARCHAR(255) |  | O |  |  |  |
| user_type | VARCHAR(20) |  | O |  |  |  |
| user_create_date | DATETIME |  | O |  |  |  |
| user_update_date | DATETIME |  | O |  |  |  |
| user_deleted | BOOLEAN |  | O |  |  | FALSE |

### user_type
- CREATOR: 강사
- CLASSMATE: 수강생

---

### Classes

| 컬럼 | 타입 | PK | NN | FK | UNIQUE | Default |
|------|------|----|----|----|--------|----------|
| class_seq | BIGINT | O | O |  |  | AUTO_INCREMENT |
| class_title | VARCHAR(255) |  | O |  |  |  |
| class_content | TEXT |  | O |  |  |  |
| class_price | DECIMAL(10,0) |  | O |  |  |  |
| class_max_cap | INT |  | O |  |  |  |
| class_curr_apps | INT |  | O |  |  | 0 |
| class_start_date | DATETIME |  | O |  |  |  |
| class_end_date | DATETIME |  | O |  |  |  |
| class_state | VARCHAR(20) |  | O |  |  |  |
| class_create_date | DATETIME |  | O |  |  |  |
| class_update_date | DATETIME |  | O |  |  |  |
| class_deleted | BOOLEAN |  | O |  |  | FALSE |
| user_seq | BIGINT |  | O | O |  |  |

### class_state
- DRAFT: 초안 (신청 불가)
- OPEN: 모집 중 (신청 가능)
- CLOSED: 모집 마감 (신청 불가)

---

### Enrollments

| 컬럼 | 타입 | PK | NN | FK | UNIQUE | Default |
|------|------|----|----|----|--------|----------|
| enroll_seq | BIGINT | O | O |  |  | AUTO_INCREMENT |
| enroll_state | VARCHAR(20) |  | O |  |  |  |
| enroll_create_date | DATETIME |  | O |  |  |  |
| enroll_update_date | DATETIME |  | O |  |  |  |
| user_seq | BIGINT |  | O | O | 복합 UNIQUE |  |
| class_seq | BIGINT |  | O | O | 복합 UNIQUE |  |

> **복합 UNIQUE:** `(user_seq, class_seq)`

### enroll_state
- PENDING: 신청 완료, 결제 대기
- CONFIRMED: 결제 완료, 수강 확정
- CANCELLED: 취소됨
