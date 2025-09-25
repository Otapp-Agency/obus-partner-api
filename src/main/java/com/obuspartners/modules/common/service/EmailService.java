package com.obuspartners.modules.common.service;

public interface EmailService {
    void sendEmail(String recipientEmail, String subject, String body);
}
