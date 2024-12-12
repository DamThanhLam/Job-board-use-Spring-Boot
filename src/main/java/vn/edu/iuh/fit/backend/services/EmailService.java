package vn.edu.iuh.fit.backend.services;

import com.google.auth.oauth2.AccessToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.codec.binary.Base64;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.backend.models.Company;
import vn.edu.iuh.fit.backend.models.Job;
import vn.edu.iuh.fit.backend.models.JobSkill;

@Service
public class EmailService {
    private static OAuth2AuthorizedClientService authorizedClientService = null;

    public EmailService(OAuth2AuthorizedClientService authorizedClientService) {
        EmailService.authorizedClientService = authorizedClientService;
    }

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param toEmailAddress   email address of the receiver
     * @param fromEmailAddress email address of the sender, the mailbox account
     * @param subject          subject of the email
     * @param bodyText         body text of the email
     * @return the MimeMessage to be used to send email
     * @throws MessagingException - if a wrongly formatted address is encountered.
     */
    public static MimeMessage createEmail(String toEmailAddress,
                                          String fromEmailAddress,
                                          String subject,
                                          String bodyText)
            throws MessagingException, jakarta.mail.MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(fromEmailAddress));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(toEmailAddress));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }
    /**
     * Send an email from the user's mailbox to its recipient.
     *
     * @param fromEmailAddress - Email address to appear in the from: header
     * @param toEmailAddress   - Email address of the recipient
     * @return the sent message, {@code null} otherwise.
     * @throws MessagingException - if a wrongly formatted address is encountered.
     * @throws IOException        - if service account credentials file not found.
     */
    public static Message sendEmail(String fromEmailAddress,
                                    String toEmailAddress, String htmlContent)
            throws MessagingException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User principal = (OAuth2User) authentication.getPrincipal();
            String fromEmail = principal.getAttribute("email");
            String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
            String principalName = oauthToken.getName();
            // Lấy OAuth2AuthorizedClient từ OAuth2AuthorizedClientService
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(clientRegistrationId, principalName);
            System.out.println(authorizedClient);
            if (authorizedClient != null) {
                // Truy cập access token
                /* Load pre-authorized user credentials from the environment.
                TODO(developer) - See https://developers.google.com/identity for
                guides on implementing OAuth2 for your application.*/
//              GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
//                .createScoped(GmailScopes.GMAIL_SEND);
                // Sử dụng access token để tạo GoogleCredentials
                System.out.println("Access Token: " + authorizedClient.getAccessToken().getTokenValue());
                AccessToken accessToken = new AccessToken(authorizedClient.getAccessToken().getTokenValue(), null);

                GoogleCredentials credentials = GoogleCredentials.create(accessToken)
                        .createScoped(GmailScopes.GMAIL_SEND);
                HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

                // Create the gmail API client
                Gmail service = new Gmail.Builder(new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        requestInitializer)
                        .setApplicationName("Gmail samples")
                        .build();



                // Encode as MIME message
                Properties props = new Properties();
                Session session = Session.getDefaultInstance(props, null);
                MimeMessage email = new MimeMessage(session);
                email.setFrom(new InternetAddress(fromEmailAddress));
                email.addRecipient(javax.mail.Message.RecipientType.TO,
                        new InternetAddress(toEmailAddress));
                email.setSubject("Job Invitation");
                email.setContent(htmlContent, "text/html");

                // Encode and wrap the MIME message into a gmail message
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                email.writeTo(buffer);
                byte[] rawMessageBytes = buffer.toByteArray();
                String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
                Message message = new Message();
                message.setRaw(encodedEmail);

                try {
                    // Create send message
                    message = service.users().messages().send("me", message).execute();
                    System.out.println("Message id: " + message.getId());
                    System.out.println(message.toPrettyString());
                    return message;
                } catch (GoogleJsonResponseException e) {
                    // TODO(developer) - handle error appropriately
                    GoogleJsonError error = e.getDetails();
                    if (error.getCode() == 403) {
                        System.err.println("Unable to send message: " + e.getDetails());
                    } else {
                        throw e;
                    }
                }
            }
        }
        return null;

    }
    public static String getHtmlTemplateInviteCandidate(String filePath, Job job) {
        try {
            // Đọc tệp HTML vào chuỗi
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            // Thay thế các placeholder với giá trị thực tế từ đối tượng Job
            content = content.replace("{{companyName}}", job.getCompany().getName())
                    .replace("{{jobTitle}}", job.getJobName())
                    .replace("{{jobDescription}}", job.getJobDesc())
                    .replace("{{jobStatus}}", job.getStatus().toString());
//                    .replace("{{applyLink}}", "https://example.com/apply") // Đường link apply
//                    .replace("{{companyDomain}}", job.getCompany().getWebUrl());

            // Lấy danh sách kỹ năng từ jobSkills và tạo HTML cho chúng
            StringBuilder jobSkillsHtml = new StringBuilder();
            for (JobSkill jobSkill : job.getJobSkills()) {
                jobSkillsHtml.append("<tr>")
                        .append("<td style='padding: 10px; border: 1px solid #ddd;'>")
                        .append(jobSkill.getSkill().getSkillName()).append("</td>")
                        .append("<td style='padding: 10px; border: 1px solid #ddd;'>")
                        .append(jobSkill.getSkill().getSkillDescription()).append("</td>")
                        .append("<td style='padding: 10px; border: 1px solid #ddd;'>")
                        .append(jobSkill.getSkillLevel().toString()).append("</td>")
                        .append("<td style='padding: 10px; border: 1px solid #ddd;'>")
                        .append(jobSkill.getMoreInfos()).append("</td>")
                        .append("</tr>");
            }

            // Thay thế placeholder {{jobSkills}} với HTML động cho kỹ năng
            content = content.replace("{{jobSkills}}", jobSkillsHtml.toString());

            return content;
        } catch (IOException e) {
            throw new RuntimeException("Error reading email template file", e);
        }
    }
}