package flab.project.service;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import flab.project.domain.EmailVerification;
import flab.project.domain.SmsVerification;
import flab.project.domain.User;
import flab.project.domain.UserAgreement;
import flab.project.domain.UserRole;
import flab.project.dto.UserDto;
import flab.project.dto.UserResponseDto;
import flab.project.exception.ExceptionCode;
import flab.project.exception.KakaoException;
import flab.project.repository.EmailVerificationRepository;
import flab.project.repository.SmsVerificationRepository;
import flab.project.repository.UserRepository;
import flab.project.util.EncryptionUtils;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class UserService {

    private final RequestHistoryService requestHistoryService;
    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final SmsVerificationRepository smsVerificationRepository;
    private final UserAgreementService userAgreementService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registrationUser(UserDto userDto, String emailVerification, String smsVerification) {

        LocalDateTime now = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        EmailVerification checkedEmailVerification = checkEmailVerification(emailVerification, now);
        SmsVerification checkedSmsVerification = checkSmsVerification(smsVerification, now);

        User user = UserDto.createUser(userDto, checkedEmailVerification.getEmail(),
                checkedSmsVerification.getPhoneNumber(), passwordEncoder);

        if (isRegistered(user)) {
            throw new KakaoException(ExceptionCode.USER_EXIST,
                    Map.of("Email", user.getEmail(), "Name", user.getName(), "PhoneNumber", user.getPhoneNumber()));
        }

        userRepository.save(user);
        setUserAgreement(user, userDto.isOptionalLocationTerms());

        cleanVerification(user);
    }

    private void setUserAgreement(User user, boolean optionalLocationTerm) {
        userAgreementService.setUserEssentialTerms(user);

        if (optionalLocationTerm) {
            userAgreementService.setUserLocationTerms(user);
        }
    }

    private void cleanVerification(User user) {
        emailVerificationRepository.deleteByEmail(user.getEmail());
        smsVerificationRepository.deleteByPhoneNumber(user.getPhoneNumber());
    }

    // 친구목록 찾기로 변경
    public List<UserResponseDto> findUserFriends(Long userId) {
        return userRepository.findAll().stream()
                .map(user -> UserResponseDto.of(user))
                .collect(Collectors.toList());
    }

    public String findEmail(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).map(findUser -> findUser.getEmail())
                .orElseThrow(() -> new KakaoException(ExceptionCode.EMAIL_NOT_FOUND));
    }

    private EmailVerification checkEmailVerification(String emailEncryptKey, LocalDateTime now) {
        EmailVerification findVerification = emailVerificationRepository.findByEmailEncryptKey(emailEncryptKey)
                .orElseThrow(() -> new KakaoException(ExceptionCode.EMAIL_VERIFICATION_NOT_FOUND));

        if (!findVerification.checkExpiration(now)) {
            throw new KakaoException(ExceptionCode.EMAIL_EXPIRED_VERIFICATION);
        }

        if (!findVerification.isVerified()) {
            throw new KakaoException(ExceptionCode.EMAIL_UNVERIFIED_VERIFICATION);
        }

        return findVerification;
    }

    private SmsVerification checkSmsVerification(String phoneNumber, LocalDateTime now) {
        SmsVerification findVerification = smsVerificationRepository.findBySmsEncryptKey(phoneNumber)
                .orElseThrow(() -> new KakaoException(ExceptionCode.SMS_VERIFICATION_NOT_FOUND));

        if (!findVerification.checkExpiration(now)) {
            throw new KakaoException(ExceptionCode.SMS_EXPIRED_VERIFICATION);
        }

        if (!findVerification.isVerified()) {
            throw new KakaoException(ExceptionCode.SMS_UNVERIFIED_VERIFICATION);
        }

        return findVerification;
    }

    public String availableEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new KakaoException(ExceptionCode.DUPLICATED_EMAIL);
        }

        if (emailVerificationRepository.existsByEmail(email)) {
            return updateEmailVerification(email).getEmailEncryptKey();
        }

        return createEmailVerification(email).getEmailEncryptKey();
    }

    private EmailVerification updateEmailVerification(String email) {
        EmailVerification findEmailVerification = emailVerificationRepository.findByEmail(email)
                .orElseGet(() -> EmailVerification.builder().build());

        Long milliseconds = System.currentTimeMillis();
        LocalDateTime createdAt = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime expirationTime = Instant.ofEpochMilli((milliseconds + 300000)).atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        findEmailVerification.renewEmailVerification(createdAt, expirationTime);

        return emailVerificationRepository.save(findEmailVerification);
    }

    private EmailVerification createEmailVerification(String email) {
        Long milliseconds = System.currentTimeMillis();
        LocalDateTime createdAt = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime expirationTime = Instant.ofEpochMilli((milliseconds + 300000)).atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        EmailVerification emailVerification = EmailVerification.builder()
                .email(email)
                .emailEncryptKey(EncryptionUtils.generateEncryptedKey(email))
                .isVerified(true)
                .createdAt(createdAt)
                .expirationTime(expirationTime)
                .build();

        return emailVerificationRepository.save(emailVerification);
    }

    private boolean isRegistered(User user) {
        return userRepository.existsByEmail(user.getEmail());
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new KakaoException(ExceptionCode.USER_NOT_FOUND));
    }

    public User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new KakaoException(ExceptionCode.USER_NOT_FOUND));
    }

    public List<UserResponseDto> getUserFriends(Long userId) {
        List<UserResponseDto> friends = userRepository.findAll().stream().filter(user -> user.getId() != userId)
                .map(user -> UserResponseDto.of(user)).collect(Collectors.toList());
        return friends;
    }

    public void testUserInput() {
        Faker faker = new Faker(Locale.KOREA);

        for (int i = 0; i < 1000000; i++) {
            String name = faker.name().fullName();
            String email = faker.internet().emailAddress();
            String phoneNumber = faker.phoneNumber().phoneNumber().replace("-", "");

            User user = User.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode("asdf1234!"))
                    .phoneNumber(phoneNumber)
                    .nickname(name)
                    .userRole(UserRole.USER)
                    .build();

            userRepository.save(user);
        }

    }
}
