package com.sejinzx.enrollmentSystem.error;

public enum ErrorCode {

    INVALID_INPUT(400, "잘못된 요청입니다"),
    INVALID_PASSWORD(401, "비밀번호가 틀렸습니다"),
    FORBIDDEN(403, "권한이 없습니다"),
    CLASS_NOT_OPEN(403, "신청 가능한 상태가 아닙니다"),
    CLASS_MODIFICATION_NOT_ALLOWED(403, "해당 상태에서는 수정할 수 없습니다"),
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다"),
    CLASS_NOT_FOUND(404, "강의를 찾을 수 없습니다"),
    ENROLL_NOT_FOUND(404, "수강 신청 내역을 찾을 수 없습니다"),
    USERS_CLASS_NOT_FOUND(404, "사용자의 강의를 찾을 수 없습니다"),
    DUPLICATE_USER_ID(409, "이미 존재하는 아이디입니다"),
    DUPLICATE_ENROLL(409, "이미 수강신청을 했습니다"),
    CLASS_CAPACITY_FULL(409, "정원이 초과되었습니다"),
    CANCEL_PERIOD_EXPIRED(409, "결제 후 3일이 지나 취소할 수 없습니다");

    private final int status;
    private final String error;

    ErrorCode(int status, String error) {
        this.status = status;
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}
