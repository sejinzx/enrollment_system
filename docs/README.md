# 🎓 수강 신청 시스템 (Enrollment System)

## 📌 프로젝트 개요

수강 신청 시스템은 사용자가 강의를 조회하고 신청 및 관리할 수 있는 웹 서비스입니다.

동시 요청이 집중되는 수강 신청 환경을 가정하여 Spring Security와 JWT 기반 인증/인가, Kafka 기반 비동기 처리, 데이터베이스 수준의 동시성 제어 구조를 구현했습니다.

Kafka Partition과 Consumer를 활용해 수강 신청 요청을 병렬 처리하고, 복합 UNIQUE 제약조건으로 중복 신청을 방지했습니다. 이후 동시 요청 과정에서 발생한 데드락을 해결하기 위해 기존 조회 후 수정 방식을 조건부 UPDATE 방식으로 개선했습니다.

JMeter로 1,000건의 동시 요청을 검증한 결과 오류율 0%를 기록했으며, 정원 초과와 중복 신청 없이 데이터 정합성을 유지했습니다.

---

## ✨ 주요 성과

- Kafka 기반 비동기 수강 신청 처리 구조 구현
- Partition과 Consumer를 활용한 병렬 처리 구조 구성
- 복합 UNIQUE 제약조건을 적용해 중복 신청 방지
- 조건부 UPDATE를 적용해 락 경쟁 및 데드락 해결
- 1,000건 동시 요청에서 오류율 0% 기록
- 정원 초과 및 중복 신청 없이 데이터 정합성 유지

---

## 🎯 주요 기능

- 회원가입 및 로그인
- JWT 기반 인증/인가
- 강의 목록 및 상세 조회
- 수강 신청 및 취소
- 신청 인원 제한 검증
- 중복 신청 방지
- Kafka 기반 비동기 수강 신청 처리

---

## 🏗️ 시스템 구조

### 수강 신청 처리 흐름

```text
사용자 요청
    ↓
Kafka Producer
    ↓
Topic (enrollment-request)
    ↓
Kafka Consumer Group
    ↓
조건부 UPDATE를 통한 정원 확인 및 인원 증가
    ↓
수강 신청 정보 저장
```

Kafka Producer가 수강 신청 요청을 Topic에 발행하고, Consumer Group이 요청을 비동기 방식으로 처리하도록 구성했습니다.

Topic의 Partition과 Consumer Concurrency를 각각 3개로 구성하여 여러 수강 신청 요청을 병렬로 처리했습니다.

---

## 🗄️ 데이터베이스 설계

![수강 신청 시스템 ERD](./images/erd.png)

### 테이블 구성

- `Users`: 사용자 계정 및 권한 정보 관리
- `Classes`: 강의 정보, 정원 및 현재 신청 인원 관리
- `Enrollment`: 사용자와 강의 간 수강 신청 정보 관리

### 주요 관계

- 한 명의 강사는 여러 강의를 생성할 수 있습니다.
- 한 명의 사용자는 여러 강의를 신청할 수 있습니다.
- 하나의 강의에는 여러 사용자가 신청할 수 있습니다.
- `Enrollment` 테이블을 통해 사용자와 강의의 다대다 관계를 관리합니다.
- `(user_seq, class_seq)` 복합 UNIQUE 제약조건으로 동일 사용자의 중복 신청을 방지합니다.

---

## 🚀 동시성 제어 개선

### 1. Kafka Partition을 활용한 병렬 처리

수강 신청 요청을 Kafka Topic에 발행하고 Consumer Group이 비동기 방식으로 처리하도록 구성했습니다.

Topic의 Partition과 Consumer Concurrency를 각각 3개로 구성하여 요청을 여러 Consumer가 병렬로 처리할 수 있도록 개선했습니다.

### 적용 내용

- Kafka Producer와 Consumer 분리
- Consumer Group 기반 비동기 처리
- Topic Partition 3개 구성
- Consumer Concurrency 3개 구성
- 요청 수신과 수강 신청 로직 분리

---

### 2. 복합 UNIQUE 제약조건을 통한 중복 신청 방지

여러 Consumer가 요청을 병렬로 처리하는 과정에서 동일 사용자가 같은 강의를 중복 신청할 가능성이 있었습니다.

애플리케이션 수준의 중복 확인만으로는 동시 요청을 완전히 방지하기 어렵다고 판단하여 `Enrollment` 테이블에 복합 UNIQUE 제약조건을 적용했습니다.

```sql
UNIQUE (user_seq, class_seq)
```

이를 통해 동일한 사용자와 강의 조합이 데이터베이스에 중복 저장되지 않도록 구성했습니다.

---

### 3. 조건부 UPDATE를 통한 데드락 해결

#### 문제

기존에는 다음 순서로 수강 신청을 처리했습니다.

```text
강의 조회
    ↓
현재 신청 인원과 정원 비교
    ↓
엔티티의 신청 인원 수정
    ↓
수강 신청 정보 저장
```

동시 요청이 증가하면서 여러 트랜잭션이 동일한 강의 데이터를 조회하고 수정했습니다.

이 과정에서 강의 데이터와 수강 신청 데이터에 대한 락 획득 순서가 충돌하여 데드락이 발생했고, 일부 요청이 정상적으로 처리되지 않았습니다.

#### 개선

강의를 조회한 후 엔티티를 수정하는 구조를 제거하고, 정원 확인과 현재 신청 인원 증가를 하나의 조건부 UPDATE 쿼리로 처리했습니다.

```sql
UPDATE classes
SET class_curr_apps = class_curr_apps + 1
WHERE class_seq = ?
  AND class_curr_apps < class_max_cap;
```

쿼리 실행 결과가 `1`이면 수강 신청을 진행하고, `0`이면 정원 초과로 처리했습니다.

수강 신청 정보 저장에 실패하면 트랜잭션을 롤백하여 강의 신청 인원과 수강 신청 데이터의 정합성을 유지하도록 구성했습니다.

#### 결과

- 조회 후 엔티티 수정 구조 제거
- 데이터베이스 락 점유 범위 감소
- 동시 요청 과정에서 발생한 데드락 해결
- 정원 확인과 신청 인원 증가를 원자적으로 처리
- 정원 초과 신청 방지
- 수강 신청 데이터 정합성 유지

---

## 🔐 인증 및 데이터 관리

- Spring Security + JWT 기반 인증/인가
- JWT Filter 기반 인증 처리
- Stateless 인증 구조
- EntityGraph를 활용한 연관 데이터 조회

---

## 📈 동시성 제어 검증

Docker 환경에서 Kafka, MySQL, 애플리케이션을 구성하고 JMeter를 활용해 1,000건의 동시 수강 신청 요청을 테스트했습니다.

### 테스트 시나리오

- 회원가입
- 로그인 및 JWT 발급
- 수강 신청 요청
- 동일 강의에 대한 1,000건 동시 요청

### 테스트 결과

| 항목 | 결과 |
|---|---:|
| 동시 요청 | 1,000건 |
| 평균 응답 시간 | 777ms |
| 처리량 | 492.9 req/s |
| 오류율 | 0% |
| 정원 초과 신청 | 0건 |
| 중복 신청 | 0건 |
| 데이터 정합성 | 유지 |

### 검증 내용

- 최대 1,000건 동시 요청에서 오류율 0% 기록
- 강의 정원을 초과하지 않고 제한된 인원만 신청 처리
- 동일 사용자의 중복 신청 방지
- 강의의 현재 신청 인원과 수강 신청 데이터 일치
- 조건부 UPDATE 적용 후 데드락 미발생

---

## 🛠️ 기술 스택

### Backend

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- JWT

### Database

- MySQL

### Message Queue

- Apache Kafka

### Infrastructure

- Docker
- Docker Compose
- AWS EC2
- GitHub Actions

### Test

- JMeter

---

## 📄 문서

- API 명세서: [API-DESC.md](./docs/API-DESC.md)
- DB 설계: [DB-SCHEMA.md](./docs/DB-SCHEMA.md)
