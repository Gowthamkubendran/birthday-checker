/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example;
/**
 *
 * @author Admin
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;

public class BirthdayChecker {
    
        public static void main(String args[]) {
        
            try{
         Class.forName("com.mysql.cj.jdbc.Driver");
              String databaseName="defaultdb";
            String host="mysql-24b6d43d-kubendranrani50-9114.g.aivencloud.com";
            String port="13951";
            String dbUser = "avnadmin";
            String dbPass = "AVNS_SI4yzcRDkCi4RP7QXfK";
              Connection con = DriverManager.getConnection( "jdbc:mysql://" + host + ":" + port + "/" + databaseName +"?ssl-mode=REQUIRED",
    dbUser, dbPass
);
            LocalDate date=LocalDate.now();
                LocalDate postdate=date.plusDays(1);
               notifyEmail(date,con);
               notifyEmail(postdate,con);
               
                 con.close();
        } catch (Exception e) {
            e.printStackTrace();}
        }
        
        
        
    public static void notifyEmail(LocalDate date,Connection con) throws SQLException, UnsupportedEncodingException, IOException{
         HashMap<String,List<String>> map=new HashMap<>();
          
        int d= date.getDayOfMonth();
     String monthName = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                 monthName=d+"-"+monthName;
                String qry="select * from birthdays where date= '"+monthName+"'";
                Statement st=con.createStatement();
                ResultSet rs=st.executeQuery(qry);
                while(rs.next()){
                String id=rs.getString(2);
                if(map.containsKey(id)){
                    map.get(id).add(rs.getString(4));
                }else{
                 List<String> li=new ArrayList<>();
                 li.add(rs.getString(4));
                      map.put(id,li);
                }
                }
               
                LocalDate today=LocalDate.now();
                String cont="";
                String content="";
              if(today.equals(date)){
                  cont+="Today ";
              }else{
                    cont+="Tomorrow ";
                }
                   for (String entry : map.keySet()) {
                     String key = entry;
        List<String> birthdayFriends = map.get(entry);

        // GenerGenerate HTML content
                String htmlContent = buildBirthdayHtmlMessage(cont, birthdayFriends);
                       
                        
              
                       System.out.println("Sending email to: " + key);
System.out.println("Content: " + content);

                sendEmail(key,"Birthday Remainder !",htmlContent);
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
        final String fromName = "Birthday Reminder App";// Replace with your email
        final String password = "spkp ytir lozz kacb";        // Use App Password (not your email password)

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
           
            message.setFrom(new InternetAddress(from, fromName));

            // Set reply-to address
            message.setReplyTo(InternetAddress.parse(from));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );
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
