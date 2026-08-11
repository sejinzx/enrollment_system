package com.sejinzx.enrollmentSystem.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 틀렸습니다."),

    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    CLASS_NOT_FOUND(HttpStatus.NOT_FOUND, "강의를 찾을 수 없습니다."),
    ENROLL_NOT_FOUND(HttpStatus.NOT_FOUND, "수강 신청 내역을 찾을 수 없습니다."),
    USERS_CLASS_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자의 강의를 찾을 수 없습니다."),


    CLASS_DELETE_NOT_ALLOWED(HttpStatus.CONFLICT, "해당 상태에서는 삭제할 수 없습니다."),
    CLASS_MODIFICATION_NOT_ALLOWED(HttpStatus.CONFLICT, "해당 상태에서는 수정할 수 없습니다."),
    CLASS_NOT_OPEN(HttpStatus.CONFLICT, "신청 가능한 상태가 아닙니다."),
    DUPLICATE_USER_ID(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다."),
    DUPLICATE_ENROLL(HttpStatus.CONFLICT, "이미 수강신청을 했습니다."),
    CLASS_CAPACITY_FULL(HttpStatus.CONFLICT, "정원이 초과되었습니다."),
    CLASS_CURRENT_APPS_INVALID(HttpStatus.CONFLICT, "현재 신청 인원을 감소시킬 수 없습니다."),
    CANCEL_PERIOD_EXPIRED(HttpStatus.CONFLICT, "결제 후 3일이 지나 취소할 수 없습니다.");

    private final HttpStatus status;
    private final String error;

    ErrorCode(HttpStatus status, String error) {
        this.status = status;
        this.error = error;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}