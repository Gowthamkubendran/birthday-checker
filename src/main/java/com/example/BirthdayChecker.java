package com.example;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.time.ZoneId;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class BirthdayChecker {

    public static void main(String args[]) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String databaseName = System.getenv("DB_NAME");
            String host = System.getenv("DB_HOST");
            String port = System.getenv("DB_PORT");
            String dbUser = System.getenv("DB_USER");
            String dbPass = System.getenv("DB_PASS");

            if (databaseName == null || host == null || port == null || dbUser == null || dbPass == null) {
                System.err.println("Database environment variables are not all set!");
                return;
            }

            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?ssl-mode=REQUIRED";

            Connection con = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);

           LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));

            LocalDate tomorrow = today.plusDays(1);

            notifyEmail(today, con);
            notifyEmail(tomorrow, con);

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void notifyEmail(LocalDate date, Connection con) throws SQLException, UnsupportedEncodingException, IOException {
        HashMap<String, List<String>> map = new HashMap<>();

        int d = date.getDayOfMonth();
        String monthName = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String dateString = d + "-" + monthName;

        String qry = "select * from birthdays where date= '" + dateString + "'";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(qry);

        while (rs.next()) {
            String id = rs.getString(2);
            if (map.containsKey(id)) {
                map.get(id).add(rs.getString(4));
            } else {
                List<String> li = new ArrayList<>();
                li.add(rs.getString(4));
                map.put(id, li);
            }
        }

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));

        String prefix = today.equals(date) ? "Today " : "Tomorrow ";

        for (String email : map.keySet()) {
            List<String> birthdayFriends = map.get(email);

            String htmlContent = buildBirthdayHtmlMessage(prefix, birthdayFriends);

            System.out.println("Sending email to: " + email);
            sendEmail(email, "Birthday Reminder!", htmlContent);
        }
    }

    private static String buildBirthdayHtmlMessage(String prefix, List<String> friends) {
        String friendsList = String.join(", ", friends);
        String plural = friends.size() > 1 ? "s" : "";

        return "<html>" +
               "<body style='font-family: Arial, sans-serif; color: #333;'>" +
                   "<div style='border: 1px solid #ddd; padding: 20px; border-radius: 8px; background-color: #f9f9f9; max-width: 600px;'>" +
                       "<h2 style='color: #4CAF50;'>🎉 " + prefix + "</h2>" +
                       "<p style='font-size: 16px;'>" +
                           "It's the birthday of your <strong>friend" + plural + "</strong>: " +
                           "<span style='color: #2196F3; font-weight: bold;'>" + friendsList + "</span>." +
                       "</p>" +
                       "<p style='font-size: 15px;'>Make their day <strong style='color: #E91E63;'>extra special</strong> with a warm message or a call! 🎂</p>" +
                   "</div>" +
               "</body>" +
               "</html>";
    }

    public static void sendEmail(String to, String subject, String content) throws UnsupportedEncodingException {
        final String from = "kubendranrani50@gmail.com";
        final String fromName = "Birthday Reminder App";
        final String password = System.getenv("EMAIL_PASS");

        if (password == null) {
            System.err.println("Email password (EMAIL_PASS) environment variable not set!");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, fromName));
            message.setReplyTo(InternetAddress.parse(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✅ Email sent to " + to);
        } catch (MessagingException e) {
            System.out.println("Email sending failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
