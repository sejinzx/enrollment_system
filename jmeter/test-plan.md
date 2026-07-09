# JMeter Test

## 목적

Kafka Consumer Group 설정에 따른 수강 신청 처리 성능 비교

## 환경

- Docker
- Kafka
- MySQL
- Spring Boot
- JMeter

## 테스트 파일 구성

| 파일 | 역할 | 사용 데이터 |
|---|---|---|
| `setting.jmx` | 테스트용 강의 생성 | `classes.csv` |
| `login.jmx` | 회원가입 및 로그인, 토큰 발급 | `users.csv` |
| `enroll.jmx` | 수강 신청 부하 테스트 | `token.csv` |

## 테스트 준비 과정

1. `setting.jmx` 실행
   - `classes.csv` 데이터를 기반으로 테스트용 강의 생성

2. DB에서 생성된 강의의 `class_state` 값을 `1`로 수동 변경
   - 수강 신청 가능한 상태로 설정

3. `login.jmx` 실행
   - `users.csv` 데이터를 기반으로 회원가입 및 로그인 수행
   - 발급된 토큰을 `token.csv`로 저장

4. `enroll.jmx` 실행
   - `token.csv`의 JWT 토큰을 사용하여 수강 신청 요청 수행

## 테스트 시나리오

- 회원가입
- 로그인
- 강의 생성
- 수강 신청
- 동시 요청 1,000건

## 비교 결과

| 설정 | 평균 응답시간 | 처리량 |
|---|---:|---:|
| Partition 1 / Concurrency 1 | 1389ms | 373.41 req/s |
| Partition 3 / Concurrency 3 | 206ms | 637.76 req/s |

## 결과 요약

Kafka Partition 수와 Consumer Concurrency를 함께 확장한 결과, 평균 응답시간은 1389ms에서 206ms로 감소했고 처리량은 373.41 req/s에서 637.76 req/s로 증가했습니다.

이를 통해 단일 Consumer 처리 구조보다 Partition과 Consumer를 병렬화한 구조가 수강 신청 요청 처리 성능 개선에 효과적임을 확인했습니다.
