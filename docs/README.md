
# 🎓 수강 신청 시스템 (Enrollment System)

동시 요청이 집중되는 수강 신청 환경에서 **정원 초과와 중복 신청을 방지하고 데이터 정합성을 유지하는 웹 서비스**입니다.

Spring Boot와 MySQL을 기반으로 개발했으며, Kafka를 활용해 수강 신청 요청과 실제 처리 로직을 비동기로 분리했습니다.

비관적 락 기반 처리 과정에서 발생한 락 경쟁 및 데드락 문제를 해결하기 위해 조건부 UPDATE를 적용했습니다.

## 🛠️ 기술 스택

| 구분 | 기술 |
|---|---|
| Backend | Java 17, Spring Boot, Spring Security, JPA |
| Database | MySQL, Redis |
| Message Queue | Apache Kafka |
| Infrastructure | Docker, AWS EC2 |
| CI/CD | GitHub Actions |
| Test | JUnit 5, JMeter |

## ✨ 주요 기능

- Spring Security + JWT 기반 인증/인가
- 강의 등록, 수정, 삭제 및 조회
- Kafka 기반 비동기 수강 신청
- 조건부 UPDATE를 이용한 정원 초과 방지
- 복합 UNIQUE 제약조건을 이용한 중복 신청 방지
- 수강 신청 취소 및 재신청
- GitHub Actions 기반 AWS 자동 배포

---

## 1. 시스템 아키텍처

### 수강 신청 처리 흐름

```text
Client
  │
  ▼
Spring Boot API
  │
  ▼
Kafka Producer
  │
  ▼
Kafka Topic
  │
  ▼
Kafka Consumer
  │
  ▼
EnrollService
  │
  ├── 기존 신청 검증
  │
  ├── 조건부 UPDATE
  │
  └── 신청 데이터 저장
        │
        ▼
      MySQL
```

수강 신청 요청은 Kafka Producer를 통해 메시지로 발행하고, Consumer에서 실제 신청 로직을 처리합니다.

- Topic Partition: 3개
- Consumer Concurrency: 3개
- Message Key: 강의 ID (`classSeq`)

수강 신청 API는 요청을 접수한 후 HTTP 202 Accepted를 반환하며, 실제 신청 처리는 비동기로 진행됩니다.

---

## 2. 동시성 문제 해결

### 문제 상황

기존에는 비관적 락을 이용해 강의를 조회하고 정원을 확인한 뒤 신청 인원을 증가시켰습니다.

그러나 동시 요청이 증가하면서 동일한 강의 데이터에 대한 락 경쟁과 데드락이 발생했습니다.

### 개선 방법

강의 조회와 신청 인원 증가를 분리하는 대신, 정원 확인과 신청 인원 증가를 하나의 조건부 UPDATE로 처리하도록 변경했습니다.

```sql
UPDATE classes
SET class_curr_apps = class_curr_apps + 1
WHERE class_seq = ?
  AND class_deleted = false
  AND class_state = 'OPEN'
  AND class_curr_apps < class_max_cap;
```

UPDATE 결과가 1이면 정원 확보에 성공한 것으로 판단하고, 0이면 신청 실패로 처리합니다.

신청 인원 증가와 수강 신청 데이터 저장은 동일한 트랜잭션에서 처리하여 저장 실패 시 함께 롤백되도록 구성했습니다.

### 중복 신청 방지

동일한 사용자가 같은 강의에 중복 신청하는 것을 방지하기 위해 복합 UNIQUE 제약조건을 적용했습니다.

```sql
UNIQUE (user_seq, class_seq)
```

애플리케이션의 기존 신청 조회와 DB 제약조건을 함께 사용하여 중복 신청을 방지합니다.

취소된 신청은 새로운 데이터를 생성하지 않고 기존 신청의 상태를 변경하여 재신청 처리합니다.

---

## 3. 동시성 및 데이터 정합성 테스트

JUnit 기반으로 실제 MySQL 환경에서 동시성 테스트를 구현했습니다.

### 정원 초과 방지

정원 100명인 강의에 서로 다른 사용자 200명이 동시에 신청하는 상황을 테스트합니다.

| 검증 항목 | 기대 결과 |
|---|---:|
| 신청 성공 | 100건 |
| 정원 초과 거절 | 100건 |
| 실제 신청 데이터 | 100건 |
| 현재 신청 인원 | 100명 |

### 중복 신청 방지

동일한 사용자가 같은 강의에 20번 동시에 신청하는 상황을 테스트합니다.

| 검증 항목 | 기대 결과 |
|---|---:|
| 신청 성공 | 1건 |
| 중복 신청 거절 | 19건 |
| 실제 신청 데이터 | 1건 |

---

## 4. JMeter 성능 테스트

JMeter를 이용해 Kafka Partition 및 Consumer Concurrency 구성에 따른 HTTP API 성능을 비교했습니다.

| 구성 | 평균 응답시간 | 처리량 |
|---|---:|---:|
| Partition 1 / Concurrency 1 | 1,389ms | 373.41 req/s |
| Partition 3 / Concurrency 3 | 206ms | 637.76 req/s |

Partition과 Consumer Concurrency를 함께 확장한 구성에서 평균 HTTP 응답시간 감소와 처리량 증가를 확인했습니다.

※ 비동기 API의 HTTP 응답 기준이며, 실제 수강 신청 완료시간과는 구분됩니다.

---

## 5. CI/CD 및 AWS 배포

GitHub Actions와 Docker를 이용해 빌드 및 배포 과정을 자동화했습니다.

```text
GitHub Push
    │
    ▼
Gradle Build
    │
    ▼
Docker Image Build
    │
    ▼
Docker Hub
    │
    ▼
AWS EC2
    │
    ▼
Docker Compose
    │
    ▼
서비스 실행
```

- Pull Request 시 Gradle 테스트 실행
- main 브랜치 Push 시 Docker 이미지 빌드 및 업로드
- SSH를 이용한 EC2 자동 배포
- Docker Compose 기반 Spring Boot, MySQL, Kafka, Redis 실행 환경 구성

---

## 📄 프로젝트 문서

- [API 명세서](./API-DESC.md)
- [DB 스키마](./DB-SCHEMA.md)
- [JMeter 테스트](../jmeter/test-plan.md)
