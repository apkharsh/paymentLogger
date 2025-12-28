package com.apkharsh.paymentLogger.email.impl;

import com.apkharsh.paymentLogger.email.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    /**
     * Send OTP email for password reset
     */
    public void sendOTPEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset OTP - Payment Logger");

            String htmlContent = buildOTPEmailTemplate(otp, toEmail);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }

    /**
     * Send password change confirmation email
     */
    public void sendPasswordChangeConfirmation(String toEmail, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Changed Successfully - Payment Logger");

            String htmlContent = buildPasswordChangeTemplate(userName, toEmail);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password change confirmation sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password change confirmation to: {}", toEmail, e);
            // Don't throw exception - password already changed
        }
    }

    /**
     * Send security alert for suspicious activity
     */
    public void sendSecurityAlert(String toEmail, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(" Security Alert - Multiple Password Reset Attempts");

            String htmlContent = buildSecurityAlertTemplate(userName, toEmail);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Security alert sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send security alert to: {}", toEmail, e);
        }
    }

    /**
     * OTP Email Template
     */
    private String buildOTPEmailTemplate(String otp, String email) {
        return """
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
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                        color: white; 
                        padding: 40px 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: 600;
                    }
                    .content { 
                        padding: 40px 30px;
                    }
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
                    .footer { 
                        text-align: center;
                        padding: 30px;
                        background: #f8f9fa;
                        color: #6c757d;
                        font-size: 14px;
                    }
                    .btn {
                        display: inline-block;
                        padding: 12px 30px;
                        background: #667eea;
                        color: white;
                        text-decoration: none;
                        border-radius: 6px;
                        margin: 20px 0;
                        font-weight: 600;
                    }
                    a { color: #667eea; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Password Reset Request</h1>
                    </div>
                    
                    <div class="content">
                        <p>Hello,</p>
                        <p>We received a request to reset your password for <strong>%s</strong>.</p>
                        
                        <p>Use the following One-Time Password (OTP) to complete your password reset:</p>
                        
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
                            • If you didn't request this password reset, please ignore this email.<br>
                            • Never share your OTP with anyone.<br>
                            • Our team will never ask for your OTP.
                        </div>
                        
                        <p style="margin-top: 30px;">Need help? Contact us at <a href="mailto:%s">%s</a></p>
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
            """.formatted(email, otp, supportEmail, supportEmail);
    }

    /**
     * Password Change Confirmation Template
     */
    private String buildPasswordChangeTemplate(String userName, String email) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 20px auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%); color: white; padding: 40px 30px; text-align: center; }
                    .content { padding: 40px 30px; }
                    .success-box { background: #d4edda; border-left: 4px solid #28a745; padding: 15px; margin: 20px 0; border-radius: 4px; }
                    .footer { text-align: center; padding: 30px; background: #f8f9fa; color: #6c757d; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Password Changed Successfully</h1>
                    </div>
                    
                    <div class="content">
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
                    </div>
                    
                    <div class="footer">
                        <p>© 2025 Payment Logger. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, email, supportEmail, supportEmail);
    }

    /**
     * Security Alert Template
     */
    private String buildSecurityAlertTemplate(String userName, String email) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 20px auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #dc3545 0%%, #c82333 100%%); color: white; padding: 40px 30px; text-align: center; }
                    .content { padding: 40px 30px; }
                    .alert-box { background: #f8d7da; border-left: 4px solid #dc3545; padding: 15px; margin: 20px 0; border-radius: 4px; }
                    .footer { text-align: center; padding: 30px; background: #f8f9fa; color: #6c757d; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚠️ Security Alert</h1>
                    </div>
                    
                    <div class="content">
                        <p>Hello %s,</p>
                        
                        <div class="alert-box">
                            <strong>⚠️ Multiple password reset attempts detected on your account.</strong>
                        </div>
                        
                        <p>We detected multiple password reset requests for <strong>%s</strong> in a short period.</p>
                        
                        <p><strong>What to do:</strong></p>
                        <ul>
                            <li>If this was you, you can safely ignore this email</li>
                            <li>If this wasn't you, your account may be at risk</li>
                            <li>Consider changing your password immediately</li>
                            <li>Contact support if you need assistance</li>
                        </ul>
                        
                        <p>Contact us: <a href="mailto:%s">%s</a></p>
                    </div>
                    
                    <div class="footer">
                        <p>© 2025 Payment Logger Security Team</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, email, supportEmail, supportEmail);
    }
}
