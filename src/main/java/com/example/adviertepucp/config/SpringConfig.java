package com.example.adviertepucp.config;

import com.example.adviertepucp.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SpringConfig {

    @Autowired
    MailService mailService;

    @Scheduled(cron = "0 59 23 * * * ",zone = "America/Lima")
    public void scheduleTaskUsingCronExpression() {
        mailService.eliminaToken();
    }

}
