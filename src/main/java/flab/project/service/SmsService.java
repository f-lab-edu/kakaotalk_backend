package flab.project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import flab.project.domain.SmsVerification;
import flab.project.dto.SmsRequestDto;
import flab.project.dto.SmsResponseDto;
import flab.project.exception.APIException;
import flab.project.exception.ExceptionCode;
import flab.project.exception.VerificationException;
import flab.project.feign.SmsFeignClient;
import flab.project.repository.SmsVerificationRepository;
import flab.project.util.ObjectMapperUtils;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@PropertySource("classpath:application.properties")
@RequiredArgsConstructor
@Service
public class SmsService {

    @Value("${naver-cloud-sms.accessKey}")
    private String accessKey;

    @Value("${naver-cloud-sms.secretKey}")
    private String secretKey;

    @Value("${naver-cloud-sms.serviceId}")
    private String serviceId;

    @Value("${naver-cloud-sms.senderPhone}")
    private String from;

    private final SmsVerificationRepository smsVerificationRepository;
    private final SmsFeignClient smsFeignClient;

    /**
     * TODO: 재요청 시 update 해야 한다.
     */

    public void sendAuthenticationSms(String phoneNumber) {
        long milliseconds = System.currentTimeMillis();

        //sms 확인을 위한 엔티티 생성 및 db에 저장
        LocalDateTime createdAt = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime expirationTime = Instant.ofEpochMilli((milliseconds + 300000)).atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        String verificationCode = makeRandomMessage();

        SmsVerification smsVerification = SmsVerification.builder()
                .phoneNumber(phoneNumber)
                .verificationCode(verificationCode)
                .expirationTime(expirationTime)
                .isVerified(false)
                .createdAt(createdAt)
                .build();

        SmsRequestDto sms = makeSms(phoneNumber, verificationCode);
        sendSmsApi(Long.toString(milliseconds), sms);

        smsVerificationRepository.save(smsVerification);
    }

    private SmsRequestDto makeSms(String phoneNumber, String verificationCode) {
        List<SmsRequestDto.MessageDto> messages = new ArrayList<>();
        messages.add(SmsRequestDto.MessageDto.builder().to(deleteDashPhoneNumber(phoneNumber)).build());

        return SmsRequestDto.builder()
                .type("SMS")
                .contentType("COMM")
                .countryCode("82")
                .from(from)
                .content("sms 본인인증 번호는 [" + verificationCode + "] 입니다.")
                .messages(messages)
                .build();
    }

    private String deleteDashPhoneNumber(String phoneNumber) {
        return phoneNumber.replace("-", "");
    }

    private void sendSmsApi(String time, SmsRequestDto smsRequestDto) {

        /* sendSms의 ErrorResponse 처리하는 법 */
        ResponseEntity<SmsResponseDto> response = smsFeignClient.sendSms(serviceId, time, accessKey, makeSignature(time), smsRequestDto);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new APIException(ExceptionCode.API_FAIL);
        }

    }

    public String makeSignature(String time) {
        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = "/sms/v2/services/" + this.serviceId + "/messages";
        String timestamp = time;
        String accessKey = this.accessKey;
        String secretKey = this.secretKey;

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(timestamp)
                .append(newLine)
                .append(accessKey)
                .toString();

        SecretKeySpec signingKey = null;
        Mac mac = null;
        try {
            signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
            mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new APIException(ExceptionCode.API_FAIL);
        }

        byte[] rawHmac = new byte[0];
        try {
            rawHmac = mac.doFinal(message.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new APIException(ExceptionCode.API_FAIL);
        }

        String encodeBase64String = Base64.encodeBase64String(rawHmac);

        return encodeBase64String;
    }

    public String makeRandomMessage() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }

    @Transactional
    public void checkSmsCode(String phoneNumber, String code) {

        smsVerificationRepository.findByPhoneNumber(phoneNumber)
                .filter(findVerification -> {
                    if (!LocalDateTime.now().isBefore(findVerification.getExpirationTime())) {
                        throw new VerificationException(ExceptionCode.SMS_EXPIRED_VERIFICATION);
                    }

                    if (!findVerification.getVerificationCode().equals(code)) {
                        throw new VerificationException(ExceptionCode.SMS_CODE_NOT_MATCH);
                    }

                    findVerification.successVerification();
                    smsVerificationRepository.save(findVerification);

                    return true;
                }).orElseThrow(() -> new VerificationException(ExceptionCode.SMS_VERIFICATION_NOT_FOUND));
    }
}
