package com.apkharsh.paymentLogger.email.impl;

import com.apkharsh.paymentLogger.email.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.from-email}")
    private String fromEmail;

    @Value("${app.support-email}")
    private String supportEmail;

    @Override
    public void sendOTPEmail(String toEmail, String otp) {
        sendEmailSync("OTP", toEmail, "Account Verification OTP - Payment Logger",
                buildOTPEmailTemplate(otp, toEmail), true);
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendOTPEmailAsync(String toEmail, String otp) {
        sendEmailAsync("OTP", toEmail, "Account Verification OTP - Payment Logger",
                buildOTPEmailTemplate(otp, toEmail));
    }

    @Override
    public void sendPasswordChangeConfirmation(String toEmail, String userName) {
        sendEmailSync("Password Change Confirmation", toEmail,
                "Password Changed Successfully - Payment Logger",
                buildPasswordChangeTemplate(userName, toEmail), false);
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendPasswordChangeConfirmationAsync(String toEmail, String userName) {
        sendEmailAsync("Password Change Confirmation", toEmail,
                "Password Changed Successfully - Payment Logger",
                buildPasswordChangeTemplate(userName, toEmail));
    }

    @Override
    public void sendSecurityAlert(String toEmail, String userName) {
        sendEmailSync("Security Alert", toEmail,
                "⚠️ Security Alert - Multiple Password Reset Attempts",
                buildSecurityAlertTemplate(userName, toEmail), false);
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendSecurityAlertAsync(String toEmail, String userName) {
        sendEmailAsync("Security Alert", toEmail,
                "⚠️ Security Alert - Multiple Password Reset Attempts",
                buildSecurityAlertTemplate(userName, toEmail));
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendWelcomeEmail(String toEmail, String userName) {
        sendEmailAsync("Welcome Email", toEmail,
                "Welcome to Payment Logger! 🎉",
                buildWelcomeEmailTemplate(userName, toEmail));
    }

    // ========== CORE EMAIL SENDING LOGIC (DRY) ==========

    /**
     * Send email synchronously with error handling
     *
     * @param emailType     Type of email for logging
     * @param toEmail       Recipient email
     * @param subject       Email subject
     * @param htmlContent   HTML body
     * @param throwOnError  Whether to throw exception on failure
     */
    private void sendEmailSync(String emailType, String toEmail, String subject,
                               String htmlContent, boolean throwOnError) {
        try {
            log.info("Sending {} to: {}", emailType, toEmail);

            MimeMessage message = createMimeMessage(toEmail, subject, htmlContent);
            mailSender.send(message);

            log.info("{} sent successfully to: {}", emailType, toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send {} to: {}", emailType, toEmail, e);
            if (throwOnError) {
                throw new RuntimeException("Failed to send email. Please try again later.", e);
            }
        }
    }

    /**
     * Send email asynchronously with error handling
     *
     * @param emailType   Type of email for logging
     * @param toEmail     Recipient email
     * @param subject     Email subject
     * @param htmlContent HTML body
     */
    private void sendEmailAsync(String emailType, String toEmail, String subject, String htmlContent) {
        try {
            log.info("Sending {} (async) to: {}", emailType, toEmail);

            MimeMessage message = createMimeMessage(toEmail, subject, htmlContent);
            mailSender.send(message);

            log.info("{} sent successfully (async) to: {}", emailType, toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send {} (async) to: {}", emailType, toEmail, e);
        } catch (Exception e) {
            log.error("Unexpected error sending {} to: {}", emailType, toEmail, e);
        }
    }

    /**
     * Create and configure MimeMessage
     *
     * @param toEmail     Recipient email
     * @param subject     Email subject
     * @param htmlContent HTML body
     * @return Configured MimeMessage
     * @throws MessagingException if message creation fails
     */
    private MimeMessage createMimeMessage(String toEmail, String subject, String htmlContent)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        return message;
    }

    // ========== EMAIL TEMPLATES ==========

    private String buildOTPEmailTemplate(String otp, String email) {
        return buildEmailTemplate(
                "🔐 Verify Your Account",
                "#667eea", "#764ba2",
                String.format("""
                        <p>Hello,</p>
                        <p>We received a verification request for <strong>%s</strong>.</p>
                        
                        <p>Use the following One-Time Password (OTP) to complete your verification:</p>
                        
                        <div class="otp-box">
                            <div style="font-size: 14px; color: #666; margin-bottom: 10px;">Your OTP Code</div>
                            <div class="otp-code">%s</div>
                            <div style="font-size: 14px; color: #666; margin-top: 10px;">Valid for 10 minutes</div>
                        </div>
                        
                        <div class="info-box">
                            <strong>⏰ Important:</strong> This OTP will expire in <strong>10 minutes</strong>. You have <strong>3 attempts</strong> to enter the correct code.
                        </div>
                        
                        <div class="warning-box">
                            <strong>⚠️ Security Notice:</strong><br>
                            • If you didn't request this verification, please ignore this email.<br>
                            • Never share your OTP with anyone.<br>
                            • Our team will never ask for your OTP.
                        </div>
                        
                        <p style="margin-top: 30px;">Need help? Contact us at <a href="mailto:%s">%s</a></p>
                        """, email, otp, supportEmail, supportEmail)
        );
    }

    private String buildPasswordChangeTemplate(String userName, String email) {
        return buildEmailTemplate(
                "✅ Password Changed Successfully",
                "#28a745", "#20c997",
                String.format("""
                        <p>Hello %s,</p>
                        
                        <div class="success-box">
                            <strong>✓ Your password has been changed successfully.</strong>
                        </div>
                        
                        <p>Your account password for <strong>%s</strong> was recently changed.</p>
                        
                        <p><strong>If you made this change:</strong><br>
                        No further action is needed. Your account is secure.</p>
                        
                        <p><strong>If you didn't make this change:</strong><br>
                        Please contact our support team immediately at <a href="mailto:%s">%s</a></p>
                        
                        <p style="margin-top: 30px;">Best regards,<br>
                        <strong>Payment Logger Team</strong></p>
                        """, userName, email, supportEmail, supportEmail)
        );
    }

    private String buildSecurityAlertTemplate(String userName, String email) {
        return buildEmailTemplate(
                "⚠️ Security Alert",
                "#dc3545", "#c82333",
                String.format("""
                        <p>Hello %s,</p>
                        
                        <div class="alert-box">
                            <strong>⚠️ Multiple verification attempts detected on your account.</strong>
                        </div>
                        
                        <p>We detected multiple verification requests for <strong>%s</strong> in a short period.</p>
                        
                        <p><strong>What to do:</strong></p>
                        <ul>
                            <li>If this was you, you can safely ignore this email</li>
                            <li>If this wasn't you, your account may be at risk</li>
                            <li>Consider changing your password immediately</li>
                            <li>Contact support if you need assistance</li>
                        </ul>
                        
                        <p>Contact us: <a href="mailto:%s">%s</a></p>
                        """, userName, email, supportEmail, supportEmail)
        );
    }

    private String buildWelcomeEmailTemplate(String userName, String email) {
        return buildEmailTemplate(
                "🎉 Welcome to Payment Logger!",
                "#667eea", "#764ba2",
                String.format("""
                        <p>Hello %s,</p>
                        
                        <div class="welcome-box">
                            <strong>✓ Your account has been successfully created!</strong>
                        </div>
                        
                        <p>Thank you for joining Payment Logger. We're excited to have you on board!</p>
                        
                        <p><strong>Get started by:</strong></p>
                        <ul>
                            <li>Setting up your payment preferences</li>
                            <li>Adding your first transaction</li>
                            <li>Exploring daily summaries</li>
                        </ul>
                        
                        <p>Need help? We're here for you at <a href="mailto:%s">%s</a></p>
                        
                        <p style="margin-top: 30px;">Best regards,<br>
                        <strong>Payment Logger Team</strong></p>
                        """, userName, supportEmail, supportEmail)
        );
    }

    /**
     * Build complete HTML email template with consistent styling
     *
     * @param title       Email header title
     * @param colorStart  Gradient start color
     * @param colorEnd    Gradient end color
     * @param content     Email body content
     * @return Complete HTML email
     */
    private String buildEmailTemplate(String title, String colorStart, String colorEnd, String content) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body { 
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                            line-height: 1.6; 
                            color: #333;
                            margin: 0;
                            padding: 0;
                            background-color: #f5f5f5;
                        }
                        .container { 
                            max-width: 600px; 
                            margin: 20px auto;
                            background: white;
                            border-radius: 12px;
                            overflow: hidden;
                            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                        }
                        .header { 
                            background: linear-gradient(135deg, %s 0%%, %s 100%%); 
                            color: white; 
                            padding: 40px 30px;
                            text-align: center;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                            font-weight: 600;
                        }
                        .content { padding: 40px 30px; }
                        .otp-box { 
                            background: linear-gradient(135deg, #667eea15 0%%, #764ba215 100%%);
                            border: 2px dashed #667eea;
                            border-radius: 12px; 
                            padding: 30px;
                            text-align: center;
                            margin: 30px 0;
                        }
                        .otp-code { 
                            font-size: 42px;
                            font-weight: bold;
                            color: #667eea;
                            letter-spacing: 8px;
                            font-family: 'Courier New', monospace;
                            margin: 10px 0;
                        }
                        .info-box {
                            background: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 4px;
                        }
                        .warning-box {
                            background: #f8d7da;
                            border-left: 4px solid #dc3545;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 4px;
                        }
                        .success-box { 
                            background: #d4edda; 
                            border-left: 4px solid #28a745; 
                            padding: 15px; 
                            margin: 20px 0; 
                            border-radius: 4px; 
                        }
                        .alert-box { 
                            background: #f8d7da; 
                            border-left: 4px solid #dc3545; 
                            padding: 15px; 
                            margin: 20px 0; 
                            border-radius: 4px; 
                        }
                        .welcome-box { 
                            background: #e7f3ff; 
                            border-left: 4px solid #2196F3; 
                            padding: 15px; 
                            margin: 20px 0; 
                            border-radius: 4px; 
                        }
                        .footer { 
                            text-align: center;
                            padding: 30px;
                            background: #f8f9fa;
                            color: #6c757d;
                            font-size: 14px;
                        }
                        a { color: #667eea; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s</h1>
                        </div>
                        
                        <div class="content">
                            %s
                        </div>
                        
                        <div class="footer">
                            <p><strong>Payment Logger</strong></p>
                            <p>This is an automated email. Please do not reply to this message.</p>
                            <p style="margin-top: 20px; color: #999; font-size: 12px;">
                                © 2025 Payment Logger. All rights reserved.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """, colorStart, colorEnd, title, content);
    }
}
