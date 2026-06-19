# 🎓 수강 신청 시스템 (Enrollment System)

## 📌 프로젝트 개요

수강 신청 시스템은 사용자가 강의를 조회하고 신청 및 관리할 수 있는 웹 서비스입니다.

동시 요청이 집중되는 수강 신청 환경을 가정하여 인증/인가, 동시성 제어, 비동기 메시지 처리 구조를 직접 설계하고 구현했습니다.

Kafka를 활용해 수강 신청 요청을 비동기 처리하고, Docker 기반 환경에서 부하 테스트를 수행하며 데이터 정합성과 처리 안정성을 검증했습니다.

---

## 🎯 주요 기능

* 회원가입 및 로그인
* JWT 기반 인증/인가 처리
* 강의 목록 및 상세 조회
* 수강 신청 및 신청 취소
* 신청 인원 제한 검증
* 중복 신청 방지
* Kafka 기반 비동기 수강 신청 처리

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
Kafka Consumer
    ↓
수강 신청 처리
    ↓
DB 저장
```

수강 신청 요청을 Kafka Topic에 적재한 뒤 Consumer가 처리하도록 구성하여 요청 처리와 비즈니스 로직을 분리했습니다.

---

## 🚀 동시성 제어

초기에는 비관적 락 기반으로 수강신청을 처리했으나, 요청 증가 시 데이터베이스 락 경합이 발생할 수 있음을 확인했습니다.

이를 개선하기 위해 Kafka 기반 비동기 처리 구조를 적용하여 수강 신청 요청을 순차적으로 처리하고 데이터 정합성을 확보했습니다.

### 적용 효과

* 데이터베이스 락 의존도 감소
* 요청 처리와 비즈니스 로직 분리
* 동시 요청 환경에서 데이터 정합성 확보
* 확장할 수 있 메시지 기반 처리 구조 적용

---

## 🔐 인증 및 데이터 관리

* Spring Security + JWT 기반 인증/인가
* JWT Filter 기반 인증 처리
* Stateless 인증 구조 적용
* Redis TTL 기반 인증 데이터 관리
* EntityGraph 적용을 통한 N+1 문제 사전 방지

---

## 📈 부하 테스트

Docker 환경에서 Kafka, MySQL, 애플리케이션을 구성한 뒤 JMeter를 활용해 부하 테스트를 진행했습니다.

### 테스트 시나리오

* 회원가입
* 로그인 및 JWT 발급
* 수강 신청 요청
* 최대 1,000건 요청 생성

### 검증 항목

* Kafka Producer 정상 적재
* Consumer 정상 처리
* 정원 초과 방지
* 중복 신청 방지
* 데이터 정합성 유지

### 결과

* 메시지 유실 없이 정상 처리
* 정원 초과 신청 발생 없음
* 중복 신청 방지 검증 완료
* Kafka 기반 비동기 처리 구조 정상 동작 확인

---

## 🛠️ 기술 스택

### Backend

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* JWT

### Database

* MySQL
* Redis

### Message Queue

* Apache Kafka

### Infra & Test

* Docker
* JMeter

---

## 📄 문서

* API 명세서: [API-DESC.md](./docs/API-DESC.md)
* DB 설계: [DB-SCHEMA.md](./docs/DB-SCHEMA.md)
