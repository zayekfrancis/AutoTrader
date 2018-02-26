import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Email {

	String host, port, emailid, username, password;
	Properties props = System.getProperties();
	Session l_session = null;

	public boolean sendRealMail(String emailTo, String subject, String body) {
		host = "smtp.mail.yahoo.com";
		port = "587";
		emailid = "trader_auto@yahoo.com";
		username = "trader_auto@yahoo.com";
		password = "moneyMoneymoneyMoney";

		try {
			java.util.Properties props = null;
			props = System.getProperties();
			props.put("mail.smtp.user", username);
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.debug", "true");

			if (!"".equals(port)) {
				props.put("mail.smtp.starttls.enable", "true");
				props.put("mail.smtp.port", port);
				props.put("mail.smtp.socketFactory.port", port);
			}

			Session session = Session.getDefaultInstance(props, null);
			session.setDebug(true);

			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username));
			msg.setSubject(subject);
			msg.setText(body, "ISO-8859-1");
			msg.setSentDate(new Date());
			msg.setHeader("content-Type", "text/html;charset=\"ISO-8859-1\"");
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
			msg.saveChanges();

			Transport transport = session.getTransport("smtp");
			transport.connect(host, username, password);
			transport.sendMessage(msg, msg.getAllRecipients());
			transport.close();

			return true;
		} catch (Exception mex) {
			mex.printStackTrace();
			return false;
		}
	}

	public void sendMail() {
		host = "smtp.mail.yahoo.com";
		port = "587";
		emailid = "trader_auto@yahoo.com";
		username = "trader_auto@yahoo.com";
		password = "moneyMoneymoneyMoney";

		emailSettings();
		createSession();
		sendMessage("trader_auto@yahoo.com", "mycarispimperthanyours@gmail.com", "Test", "test Mail");
	}

	public void emailSettings() {
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.auth", "true");
		props.put("mail.debug", "false");
		props.put("mail.smtp.port", port);
		// props.put("mail.smtp.socketFactory.port", port);
		// props.put("mail.smtp.socketFactory.class",
		// "javax.net.ssl.SSLSocketFactory");
		// props.put("mail.smtp.socketFactory.fallback", "false");

	}

	public void createSession() {

		l_session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		l_session.setDebug(true); // Enable the debug mode

	}

	public boolean sendMessage(String emailFromUser, String toEmail, String subject, String msg) {
		// System.out.println("Inside sendMessage 2 :: >> ");
		try {
			// System.out.println("Sending Message
			// *********************************** ");
			MimeMessage message = new MimeMessage(l_session);
			emailid = emailFromUser;
			// System.out.println("mail id in property =============
			// >>>>>>>>>>>>>> " + emailid);
			// message.setFrom(new InternetAddress(emailid));
			message.setFrom(new InternetAddress(this.emailid));

			message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
			message.setSubject(subject);
			message.setContent(msg, "text/html");

			// message.setText(msg);
			Transport.send(message);
			System.out.println("Message Sent");
		} catch (MessagingException mex) {
			mex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} // end catch block
		return true;
	}

}