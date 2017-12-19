package com.afh.mail;

/**
 * Created by chandan on 9/7/2015.
 */
import com.afh.constants.Constants;
import com.afh.controller.AskForHelpObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class SendMail
{
    static final Logger logger = LoggerFactory.getLogger(SendMail.class);
    private Properties props;
    private Session session;
    public SendMail() {
        this.props = new Properties();
        this.props.put("mail.smtp.host", "smtp.gmail.com");
        this.props.put("mail.smtp.socketFactory.port", "465");
        this.props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        this.props.put("mail.smtp.auth", "true");
        this.props.put("mail.smtp.port", "465");

        try {
            this.session = Session.getDefaultInstance(this.props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(Constants.DEFAULT_EMAIL_ID, "Hanks123#");
                        }
                    });
        } catch(Exception e) {
            this.session = null;
        }
    }

    //TODO: can make the message one constant blob and just pass inputs then
    public void sendEmail(String toAddress, String username, AskForHelpObject askForHelpObject) {
        try {
            Message message = new MimeMessage(this.session);
            message.setFrom(new InternetAddress("noreply@askForHelp.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(toAddress));
            message.setSubject("Help " + username + " for finding " + askForHelpObject.getType()
                    + ":"
                    + StringUtils.join(askForHelpObject.getSkills()));

            String targetUser = toAddress.split("@")[0];
            message.setText("Dear " + targetUser + "," +
                            "\n\n Hope you are doing fine and enjoying life, Can you please Help your friend: " + username +
                            "\n\n in finding "+askForHelpObject.getType() +
                            "\n\n with skills "+ StringUtils.join(askForHelpObject.getSkills())+
                            "\n\n in City "+askForHelpObject.getCity() +
                            "\n\n in State "+askForHelpObject.getState() +
                            "\n\n in Country "+askForHelpObject.getCountry()  +
                            "\n\n " +
                            "\n\n " +
                            "\n\n Please click on the link to to help your friend : " + Constants.DEFAULT_EMAIL_LINK_SEND +

                            "\n\n Thanks for taking the time to read this email" +
                            "\n\n Yours Sincerely" +
                            "\n\n Ask for Help Team" );



            Transport.send(message);

            logger.info("Sucessfully send email to:"+toAddress + ", help:"+askForHelpObject);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {

        SendMail sendMail = new SendMail();
        AskForHelpObject askForHelpObject = new AskForHelpObject.AskForHelpObjectBuilder()
                .type("Doctor")
                .city("palo alto")
                .country("US")
                .state("CA")
                .skills(Arrays.asList("Pediatrician", "optometry"))
                .build();

        sendMail.sendEmail("test@gmail.com", "test",askForHelpObject );

    }
}