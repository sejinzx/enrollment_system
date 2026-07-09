# JMeter Test

## 목적
Kafka Consumer Group 설정에 따른 성능 비교

## 환경
- Docker
- Kafka
- MySQL
- Spring Boot

## 시나리오
- 회원가입
- 로그인
- 수강 신청
- 동시 요청 1,000건

## 비교

| 설정 | 평균 응답시간 | 처리량 |
|------|-------------:|-------:|
| Partition 1 / Concurrency 1 | 1389ms | 373.41 req/s |
| Partition 3 / Concurrency 3 | 206ms | 637.76 req/s |
