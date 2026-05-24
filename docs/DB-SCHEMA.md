## DB 스키마

### Users

| 컬럼 | 타입 | PK | NN | FK | Default |
|------|------|----|----|----|----------|
| user_seq | long | O | O |  |  |
| user_id | varchar(30) |  | O |  |  |
| user_pw | varchar(255) |  | O |  |  |
| user_type | varchar(20) |  | O |  |  |
| user_create_date | datetime |  | O |  |  |
| user_update_date | datetime |  | O |  |  |
| user_deleted | boolean |  | O |  | FALSE |

### user_type
- CREATOR: 강사
- CLASSMATE: 수강생

---

### Classes
| 컬럼 | 타입 | PK | NN | FK | Default |
|------|------|----|----|----|----------|
| class_seq | long | O | O |  | auto_increment |
| class_title | varchar |  | O |  |  |
| class_content | text |  | O |  |  |
| class_price | decimal |  | O |  |  |
| class_max_cap | int |  | O |  |  |
| class_curr_apps | int |  | O |  | 0 |
| class_start_date | datetime |  | O |  |  |
| class_end_date | datetime |  | O |  |  |
| class_state | varchar |  | O |  |  |
| class_create_date | datetime |  | O |  |  |
| class_update_date | datetime |  | O |  |  |
| class_deleted | boolean |  | O |  | FALSE |
| user_seq | long |  | O | O |  |

### class_state
- DRAFT: 초안(신청 불가)
- OPEN: 모집 중(신청 가능)
- CLOSED: 모집 마감(신청 불가)

---

### Enrollments
| 컬럼 | 타입 | PK | NN | FK | Default |
|------|------|----|----|----|----------|
| enroll_seq | long | O | O |  | auto_increment |
| enroll_state | varchar |  | O |  |  |
| enroll_create_date | datetime |  | O |  |  |
| enroll_update_date | datetime |  | O |  |  |
| user_seq | long |  | O | O |  |
| class_seq | long |  | O | O |  |

### enroll_state
- PENDING: 신청 완료, 결제 대기
- CONFIRMED: 결제 완료, 수강 확정
- CANCELLED: 취소됨
