package flab.project.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    USER_EXIST(HttpStatus.CONFLICT, "가입된 유저입니다.", "가입된 유저입니다.", 10000),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다.", "존재하지 않는 유저입니다.", 10001),

    DUPLICATED_EMAIL(HttpStatus.BAD_REQUEST, "이메일이 중복되었습니다.", "사용할 수 없는 이메일입니다.", 10002),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "이메일을 찾을 수 없습니다.", "존재하지 않는 이메일입니다.", 10003),

    EMAIL_VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "이메일 인증 기록이 없습니다.", "기록이 없습니다.", 10004),
    SMS_VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SMS 인증 기록이 없습니다.", "기록이 없습니다.", 10005),
    EMAIL_EXPIRED_VERIFICATION(HttpStatus.UNAUTHORIZED, "이메일 인증 유효기간이 지났습니다.", "사용할 수 없는 인증입니다.", 10006),
    EMAIL_UNVERIFIED_VERIFICATION(HttpStatus.FORBIDDEN, "이메일 인증이 필요합니다.", "인증이 필요합니다.", 10007),
    SMS_EXPIRED_VERIFICATION(HttpStatus.UNAUTHORIZED, "SMS 인증 유효기간이 지났습니다.", "사용할 수 없는 인증입니다.", 10008),
    SMS_UNVERIFIED_VERIFICATION(HttpStatus.FORBIDDEN, "SMS 인증이 필요합니다.", "인증이 필요합니다.", 10009),
    SMS_CODE_NOT_MATCH(HttpStatus.UNAUTHORIZED, "인증번호가 올바르지 않습니다.", "인증번호가 올바르지 않습니다.", 10010),

    ESSENTIAL_TERM_NOT_AGREEMENT(HttpStatus.BAD_REQUEST, "필수 약관에 동의하지 않았습니다.", "약관을 체크해야 합니다.", 10011),
    TERM_NOT_FOUND(HttpStatus.NOT_FOUND, "찾을 수 없는 약관 ID 입니다.", "존재하지 않는 약관입니다.", 10012),

    EMAIL_VALIDATION(HttpStatus.BAD_REQUEST, "이메일 형식이 맞지 않습니다.", "잘못된 형식입니다.", 20000),
    PASSWORD_VALIDATION(HttpStatus.BAD_REQUEST, "비밀번호 형식이 맞지 않습니다.", "잘못된 형식입니다.", 20001),
    NAME_VALIDATION(HttpStatus.BAD_REQUEST, "이름 형식이 맞지 않습니다.", "잘못된 형식입니다.", 20003),
    PHONE_NUMBER_VALIDATION(HttpStatus.BAD_REQUEST, "휴대전화 형식이 맞지 않습니다.", "잘못된 형식입니다.", 20004),
    PASSWORD_CONFIRM(HttpStatus.BAD_REQUEST, "비밀번호와 확인 비밀번호가 일치하지 않습니다.", "잘못된 형식입니다.", 20005),

    ALREADY_FRIEND(HttpStatus.BAD_REQUEST, "이미 친구입니다.", "이미 친구입니다.", 30000),
    NO_FRIEND_RELATIONSHIP(HttpStatus.BAD_REQUEST, "없는 친구 관계입니다.", "잘못된 입력입니다.", 30001),
    NOT_FRIEND(HttpStatus.BAD_REQUEST, "유저의 친구가 아닙니다.", "잘못된 입력입니다.", 30002),
    ALREADY_DELETED_FRIEND(HttpStatus.BAD_REQUEST, "이미 삭제된 유저입니다.", "이미 삭제된 유저입니다.", 30003),


    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 채팅방입니다.", "잘못된 입력입니다.", 50000),
    CHATROOM_FORBIDDEN(HttpStatus.FORBIDDEN, "접근할 수 없는 채팅방입니다.", "접근 권한이 없습니다.", 50001),
    CHATROOM_NOT_CREATED(HttpStatus.BAD_REQUEST, "채팅방을 생성할 수 없습니다.", "잘못된 입력입니다.", 50002),
    CHATROOM_NOT_INVITE(HttpStatus.BAD_REQUEST, "개인 채팅에 초대할 수 업습니다.", "잘못된 입력입니다.", 50003),

    REQUEST_TOO_FAST(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 빠릅니다.", "요청할 수 없습니다.", 60000),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "요청이 잘못되었습니다.", "잘못된 요청입니다.", 60001),
    BAD_FILE_TYPE(HttpStatus.BAD_REQUEST, "잘못된 파일 형식입니다.", "잘못된 파일 형식입니다.", 60002),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "파일이 너무 큽니다.", "파일이 너무 큽니다.", 60003),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다.", "파일을 찾을 수 없습니다.", 60004),


    API_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "외부 API를 처리하지 못했습니다.", "응답할 수 없습니다.", 70000),

    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다.", "서버에서 요청을 처리하지 못했습니다.", 80000);

    private final HttpStatus status;

    private final String internalMessage;

    private final String externalMessage;

    private final int code;
}
