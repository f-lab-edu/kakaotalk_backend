package flab.project.scheduler;

import flab.project.repository.EmailVerificationRepository;
import flab.project.repository.SmsVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/*
    TODO: 스케줄러는 batch 서비스로 바꿔야 한다.
 */

@Component
@RequiredArgsConstructor
public class VerificationScheduler {

    private final EmailVerificationRepository emailVerificationRepository;
    private final SmsVerificationRepository smsVerificationRepository;

    @Scheduled(cron = "0 0 0 1 * ?")
    public void cleanExpired() {
        LocalDateTime now = LocalDateTime.now();
        emailVerificationRepository.deleteByExpirationTimeBefore(now);
        smsVerificationRepository.deleteByExpirationTimeBefore(now);
    }
}
