## API 명세

| Index      | 기능            | Method | Endpoint                                  | Token |
|------------|---------------|--------|-------------------------------------------|-------|
| User       | 회원가입          | POST   | /api/users/signup                         | X     |
|            | 로그인           | POST   | /api/users/login                          | X     |
|            | 아이디 중복 확인     | GET    | /api/users/exists                         | X     |
| Class      | 강의 등록         | POST   | /api/classes                              | O     |
|            | 강의 수정         | PUT    | /api/classes/{classSeq}                   | O     |
|            | 강의 목록 조회      | GET    | /api/classes                              | O     |
|            | 강의 상세 조회      | GET    | /api/classes/{classSeq}                   | O     |
|            | 강의 삭제         | DELETE | /api/classes/{classSeq}                   | O     |
| Enrollment | 수강 신청         | POST   | /api/enrollments/classes/{classSeq}       | O     |
|            | 수강 신청 취소      | DELETE | /api/enrollments/{classSeq}               | O     |
|            | 내 수강 신청 목록 조회 | GET    | /api/enrollments/my                       | O     |
|            | 강의별 수강생 목록 조회 | GET    | /api/enrollments/classes/{classSeq}/users | O     |

---

## 회원가입
<details>
<summary>요청/응답</summary>
<div markdown="1">

### Request Body

- userId: 사용자 아이디
- userPw: 사용자 비밀번호
- userType: 사용자 유형(CREATOR, CLASSMATE)

```json
{
  "userId": "user1",
  "userPw": "1234",
  "userType": "CREATOR"
}
```

### Response

```json
{
  "message: ": "signup success"
}
```

### Error

- 존재하는 아이디로 회원가입 하려는 경우

```json
{
  "status": 409,
  "error": "이미 존재하는 아이디입니다"
}
```

</div>
</details>

## 로그인
<details>
<summary>요청/응답</summary>
<div markdown="1">

### Request Body

- userId: 사용자 아이디
- userPw: 사용자 비밀번호

```json
{
  "userId": "user1",
  "userPw": "1234"
}
```

### Response

- accessToken: jwt 토큰

```json
{
  "accessToken": "jwt token",
  "message": "login success"
}
```

### Error

- 아이디가 일치하지 않는 경우

```json
{
  "status": 404,
  "error": "사용자를 찾을 수 없습니다"
}
```

- 비밀번호가 일치하지 않는 경우

```json
{
  "status": 401,
  "error": "비밀번호가 틀렸습니다."
}
```

</div>
</details>

## 아이디 중복 확인
<details>
<summary>요청/응답</summary>
<div markdown="1">

### Request Param

- userId: 사용자 아이디

```json
user2
```

### Response

```json
{
  "message: ": "available ID"
}
```

### Error

아이디가 이미 존재하는 경우

```json
{
  "status": 409,
  "error": "이미 존재하는 아이디입니다"
}
```

</div>
</details>

## 강의 등록
<details>
<summary>요청/응답</summary>
<div markdown="1">

### Header

Authorization: Bearer {JWT_TOKEN}

### Request Body

- classTitle: 강의 제목
- classContent: 강의 내용
- classPrice: 강의 가격
- classMaxCap: 강의 정원
- classStartDate: 강의 시작일
- classEndDate: 강의 종료일

```json
{
  "classTitle": "Spring Boot",
  "classContent": "Backend",
  "classPrice": 10000,
  "classMaxCap": 20,
  "classStartDate": "2026-06-12",
  "classEndDate": "2026-07-24"
}
```

### Response

- classSeq: 강의 번호

```json
{
  "classSeq": 1,
  "message": "강의 등록 완료"
}
```

### Error

- 헤더에 jwt 토큰 없는 경우

```json
{
  "status": 401,
  "error": "JWT 토큰이 필요합니다"
}
```

- CLASSMATE가 강의를 등록하려는 경우

```json
{
  "status": 403,
  "error": "권한이 없습니다"
}
```

</div>
</details>

## 강의 수정
<details>
<summary>요청/응답</summary>
<div markdown="1">

### Header

Authorization: Bearer {JWT_TOKEN}

### PathVariable

- classSeq: 강의 번호

```json
1
```

### Request Body

- userId: 사용자 아이디
- userPw: 사용자 비밀번호

```json
{
  "userId": "user1",
  "userPw": "1234"
}
```

### Response

- classSeq: 강의 번호

```json
{
	"classSeq": 1,
  "message": "강의 수정 완료"
}
```

### Error

- 본인 강의를 찾을 수 없는 경우

```json
{
  "status": 404,
  "error": "사용자의 강의를 찾을 수 없습니다"
}
```

- classState가 OPEN, CLOSED인 경우

```json
{
  "status": 403,
  "error": "해당 상태에서는 수정할 수 없습니다"
}
```

</div>
</details>

## 강의 목록 조회
<details>
<summary>요청/응답</summary>
<div markdown="1">

### Header

Authorization: Bearer {JWT_TOKEN}

### Request Param

- page: 보여줄 페이지 수

```json
0
```

- size: 한 페이지에 보여줄 데이터 개수

```json
10
```

- state: 강의 상태(null, DRAFT, OPEN, CLOSED)

```json
--
```

### Response

- classSeq: 강의 번호
- classTitle: 강의 제목
- classPrice: 강의 가격
- classMaxCap: 최대 정원
- classState: 강의 상태(DRAFT, OPEN, CLOSED)

```json
{
  "content": [
    {
      "classSeq": 1,
      "classTitle": "HTML",
      "classPrice": 15000,
      "classMaxCap": 30,
      "classState": "OPEN"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "offset": 0,
    "unpaged": false,
    "paged": true
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 1,
  "first": true,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": true,
    "sorted": false,
    "unsorted": true
  },
  "numberOfElements": 1,
  "empty": false
}
```

</div>
</details>

## 강의 상세 조회
<details>
<summary>요청/응답</summary>
<div markdown="1">

### Header

Authorization: Bearer {JWT_TOKEN}

### PathVariable

- classSeq: 강의 번호

```json
1
```

### Response

- classSeq: 강의 번호
- classTitle: 강의 제목
- classContent: 강의 내용
- classPrice: 강의 가격
- classMaxCap: 최대 정원
- classCurrCap: 현재 신청 인원
- classStartDate: 강의 시작일
- classEndDate: 강의 종료일
- classState: 강의 상태(DRAFT, OPEN, CLOSED)

```json
{
  "classSeq": 1,
  "classTitle": "HTML",
  "classContent": "Frontend",
  "classPrice": 15000,
  "classMaxCap": 30,
  "classCurrApps": 0,
  "classStartDate": "2026-05-24",
  "classEndDate": "2026-09-24",
  "classState": "OPEN"
}
```

</div>
</details>

## 강의 삭제
<details>
<summary>요청/응답</summary>
<div markdown="1">

### Header

Authorization: Bearer {JWT_TOKEN}

### PathVariable

- classSeq: 강의 번호

```json
1
```

### Response

```json
{
  "message": "강의 삭제 완료",
  "classSeq": 1
}
```

</div>
</details>

## 수강 신청
<details>
<summary>요청/응답</summary>
<div markdown="1">

### Header

Authorization: Bearer {JWT_TOKEN}

### PathVariable

- classSeq: 강의 번호

```json
1
```

### Response

- enrollSeq: 수강 신청 번호

```json
{
  "enrollSeq": 1,
  "message": "success registration"
}
```

### Error

- 강사가 수강 신청을 한 경우

```json
{
  "status": 403,
  "error": "권한이 없습니다"
}
```

- 이미 수강 신청을 한 경우

```json
{
  "status": 409,
  "error": "이미 수강신청을 했습니다"
}
```

- 정원이 초과된 경우

```json
{
  "status": 409,
  "error": "정원이 초과되었습니다"
}
```

- classState가 OPEN이 아닌 경우

```json
{
  "status": 403,
  "error": "신청 가능한 상태가 아닙니다"
}
```

</div>
</details>

## 수강 신청 취소
<details>
<summary>요청/응답</summary>
<div markdown="1">

### Header

Authorization: Bearer {JWT_TOKEN}

### PathVariable

- enrollSeq: 수강 신청 번호

```json
1
```

### Response

- enrollSeq: 수강 신청 번호

```json

  "message": "success cancel",
  "enrollSeq": 1
}
```

## Error

- 결제 후 3일 지남

```json
{
  "status": 409,
  "error": "결제 후 3일이 지나 취소할 수 없습니다"
}
```

</div>
</details>

## 내 수강 신청 목록 조회
<details>
<summary>요청/응답</summary>
<div markdown="1">

### Header

Authorization: Bearer {JWT_TOKEN}

### Request Param

- page: 보여줄 페이지 수

```json
0
```

- size: 한 페이지에 보여줄 데이터 개수

```json
10
```

### Response

- enrollSeq: 수강 신청 번호
- enrollState: 신청 상태(PENDING, CONFIRMED, CANCELLED)
- classTitle: 강의 제목
- classStartDate: 강의 시작일
- classEndDate: 강의 종료일

```json
{
  "content": [
    {
      "enrollSeq": 1,
      "enrollState": "CONFIRMED",
      "classInfo": {
        "classTitle": "HTML",
        "classStartDate": "2026-05-24",
        "classEndDate": "2026-09-24"
      }
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "last": true,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": true,
    "sorted": false,
    "unsorted": true
  },
  "numberOfElements": 1,
  "empty": false
}
```

</div>
</details>

## 강의별 수강생 목록 조회
<details>
<summary>요청/응답</summary>
<div markdown="1">

### Header

Authorization: Bearer {JWT_TOKEN}

### PathVariable

- classSeq: 강의 번호

```json
1
```

### Request Param

- page: 보여줄 페이지 수

```json
0
```

- size: 한 페이지에 보여줄 데이터 개수

```json
10
```

### Response

- enrollSeq: 수강 신청 번호
- userId: 사용자 아이디

```json
{
  "content": [
    {
      "enrollSeq": 1,
      "userId": "user2"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": true,
    "sorted": false,
    "unsorted": true
  },
  "numberOfElements": 1,
  "empty": false
}
```


</div>
</details>
