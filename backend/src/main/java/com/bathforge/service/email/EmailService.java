package com.bathforge.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;

import java.util.Base64;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${bathforge.industry.email}")
    private String industryEmail;

    @Value("${bathforge.sender.email}")
    private String fromEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendQuoteRequest(QuoteRequestEmailData emailData) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(industryEmail);
        helper.setSubject("New Quote Request - " + emailData.getUserFullName());

        String htmlContent = buildQuoteRequestHtml(emailData);
        helper.setText(htmlContent, true);

        // Attach scene snapshot if provided
        if (emailData.getSceneSnapshot() != null && !emailData.getSceneSnapshot().isEmpty()) {
            try {
                // Remove data URL prefix if present
                String base64Image = emailData.getSceneSnapshot();
                if (base64Image.contains(",")) {
                    base64Image = base64Image.split(",")[1];
                }

                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                ByteArrayDataSource dataSource = new ByteArrayDataSource(imageBytes, "image/png");
                helper.addAttachment("bathroom-scene.png", dataSource);
            } catch (Exception e) {
                logger.error("Failed to attach scene snapshot: " + e.getMessage());
            }
        }

        mailSender.send(message);
        logger.info("Quote request email sent to {} for user {}", industryEmail, emailData.getUserEmail());
    }

    private String buildQuoteRequestHtml(QuoteRequestEmailData emailData) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html><head><style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append("h1 { color: #0066cc; }");
        html.append("h2 { color: #0099cc; margin-top: 20px; }");
        html.append("table { border-collapse: collapse; width: 100%; margin: 10px 0; }");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append("th { background-color: #f2f2f2; }");
        html.append(".section { margin: 20px 0; }");
        html.append("</style></head><body>");

        html.append("<h1>New Quote Request from BathForge</h1>");

        // Customer Information
        html.append("<div class='section'>");
        html.append("<h2>Customer Information</h2>");
        html.append("<table>");
        html.append("<tr><th>Name</th><td>").append(emailData.getUserFullName()).append("</td></tr>");
        html.append("<tr><th>Email</th><td>").append(emailData.getUserEmail()).append("</td></tr>");
        if (emailData.getUserPhone() != null && !emailData.getUserPhone().isEmpty()) {
            html.append("<tr><th>Phone</th><td>").append(emailData.getUserPhone()).append("</td></tr>");
        }
        if (emailData.getUserCompany() != null && !emailData.getUserCompany().isEmpty()) {
            html.append("<tr><th>Company</th><td>").append(emailData.getUserCompany()).append("</td></tr>");
        }
        html.append("</table>");
        html.append("</div>");

        // Room Dimensions
        if (emailData.getRoomDimensions() != null) {
            html.append("<div class='section'>");
            html.append("<h2>Room Dimensions</h2>");
            html.append("<p>").append(emailData.getRoomDimensions()).append("</p>");
            html.append("</div>");
        }

        // Products
        if (emailData.getProducts() != null && !emailData.getProducts().isEmpty()) {
            html.append("<div class='section'>");
            html.append("<h2>Selected Products</h2>");
            html.append("<table>");
            html.append("<tr><th>Product</th><th>Category</th><th>Color</th><th>Position</th></tr>");
            for (QuoteRequestEmailData.ProductDetail product : emailData.getProducts()) {
                html.append("<tr>");
                html.append("<td>").append(product.getName()).append("</td>");
                html.append("<td>").append(product.getCategory()).append("</td>");
                html.append("<td>").append(product.getColor() != null ? product.getColor() : "N/A").append("</td>");
                html.append("<td>").append(product.getPosition() != null ? product.getPosition() : "N/A")
                        .append("</td>");
                html.append("</tr>");
            }
            html.append("</table>");
            html.append("</div>");
        }

        // Coverings
        if (emailData.getCoverings() != null && !emailData.getCoverings().isEmpty()) {
            html.append("<div class='section'>");
            html.append("<h2>Wall and Floor Coverings</h2>");
            html.append("<table>");
            html.append("<tr><th>Type</th><th>Product</th></tr>");
            for (QuoteRequestEmailData.CoveringDetail covering : emailData.getCoverings()) {
                html.append("<tr>");
                html.append("<td>").append(covering.getType()).append("</td>");
                html.append("<td>").append(covering.getName()).append("</td>");
                html.append("</tr>");
            }
            html.append("</table>");
            html.append("</div>");
        }

        // Additional Notes
        if (emailData.getAdditionalNotes() != null && !emailData.getAdditionalNotes().isEmpty()) {
            html.append("<div class='section'>");
            html.append("<h2>Additional Notes</h2>");
            html.append("<p>").append(emailData.getAdditionalNotes().replace("\n", "<br>")).append("</p>");
            html.append("</div>");
        }

        html.append("<hr>");
        html.append(
                "<p><em>Note: An account has been automatically created for this customer in the BathForge system.</em></p>");

        html.append("</body></html>");

        return html.toString();
    }

    public void sendUserConfirmation(QuoteRequestEmailData emailData) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(emailData.getUserEmail());
        helper.setSubject("Quote Request Confirmation - BathForge");

        String htmlContent = buildUserConfirmationHtml(emailData);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        logger.info("Confirmation email sent to user {}", emailData.getUserEmail());
    }

    private String buildUserConfirmationHtml(QuoteRequestEmailData emailData) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html><head><style>");
        html.append(
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; padding: 20px; }");
        html.append(
                ".container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        html.append("h1 { color: #0066cc; margin-bottom: 20px; }");
        html.append("h2 { color: #0099cc; margin-top: 25px; margin-bottom: 15px; font-size: 18px; }");
        html.append(
                ".info-box { background: #f0f8ff; border-left: 4px solid #0066cc; padding: 15px; margin: 20px 0; }");
        html.append(
                ".credentials { background: #fff9e6; border: 1px solid #ffcc00; padding: 15px; margin: 20px 0; border-radius: 5px; }");
        html.append("table { border-collapse: collapse; width: 100%; margin: 15px 0; }");
        html.append("th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }");
        html.append("th { background-color: #f8f8f8; font-weight: 600; }");
        html.append(
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 2px solid #eee; color: #666; font-size: 14px; }");
        html.append(".highlight { color: #0066cc; font-weight: 600; }");
        html.append("</style></head><body>");

        html.append("<div class='container'>");
        html.append("<h1>Thank You for Your Quote Request!</h1>");

        html.append("<p>Dear ").append(emailData.getUserFullName()).append(",</p>");
        html.append(
                "<p>We have successfully received your quote request for your bathroom design. Our team will review your requirements and get back to you shortly.</p>");

        // Account Information
        html.append("<div class='credentials'>");
        html.append("<h2>🔐 Your Account Information</h2>");
        html.append("<p>An account has been created for you to track your quote request:</p>");
        html.append("<table>");
        html.append("<tr><th>Email (Username)</th><td>").append(emailData.getUserEmail()).append("</td></tr>");
        html.append("<tr><th>Password</th><td>The password you provided during registration</td></tr>");
        html.append("</table>");
        html.append(
                "<p><strong>⚠️ Important:</strong> Please save your password securely. You will need it to log in and track your quote request.</p>");
        html.append("</div>");

        // Request Summary
        html.append("<div class='info-box'>");
        html.append("<h2>📋 Your Request Summary</h2>");

        if (emailData.getRoomDimensions() != null) {
            html.append("<p><strong>Room:</strong> ").append(emailData.getRoomDimensions()).append("</p>");
        }

        if (emailData.getProducts() != null && !emailData.getProducts().isEmpty()) {
            html.append("<p><strong>Products Selected:</strong> ").append(emailData.getProducts().size())
                    .append(" item(s)</p>");
            html.append("<ul>");
            for (QuoteRequestEmailData.ProductDetail product : emailData.getProducts()) {
                html.append("<li>").append(product.getName());
                if (product.getCategory() != null) {
                    html.append(" <em>(").append(product.getCategory()).append(")</em>");
                }
                html.append("</li>");
            }
            html.append("</ul>");
        }

        if (emailData.getCoverings() != null && !emailData.getCoverings().isEmpty()) {
            html.append("<p><strong>Coverings:</strong> ").append(emailData.getCoverings().size())
                    .append(" surface(s)</p>");
        }

        html.append("</div>");

        // Additional Notes
        if (emailData.getAdditionalNotes() != null && !emailData.getAdditionalNotes().isEmpty()) {
            html.append("<div class='info-box'>");
            html.append("<h2>💬 Your Notes</h2>");
            html.append("<p>").append(emailData.getAdditionalNotes().replace("\n", "<br>")).append("</p>");
            html.append("</div>");
        }

        // Next Steps
        html.append("<h2>📌 What Happens Next?</h2>");
        html.append("<ol>");
        html.append("<li>Our team will review your design and requirements</li>");
        html.append("<li>We will prepare a detailed quote for your project</li>");
        html.append("<li>You will receive a personalized quote via email within 2-3 business days</li>");
        html.append("<li>You can log in to your account to track the status of your request</li>");
        html.append("</ol>");

        // Footer
        html.append("<div class='footer'>");
        html.append(
                "<p>If you have any questions or need to make changes to your request, please don't hesitate to contact us.</p>");
        html.append("<p><strong>BathForge Team</strong><br>");
        html.append("Email: ").append(fromEmail).append("</p>");
        html.append(
                "<p><em>This is an automated confirmation email. Please do not reply directly to this message.</em></p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body></html>");

        return html.toString();
    }
}
