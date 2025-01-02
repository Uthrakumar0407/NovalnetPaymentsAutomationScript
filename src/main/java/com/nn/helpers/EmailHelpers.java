package com.nn.helpers;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

import com.nn.utilities.Log;

import static com.nn.constants.Constants.*;

public class EmailHelpers {

	public static void sendEmail(String mailBody, String[] attachments) {
		
		final String fromEmail = "test@novalnetsolutions.com";
		final String password = "m6fbO%sarfuiXoEsjk";
		
		//Set SMTP server properties
		Properties properties = new Properties(); 
		properties.put("mail.smtp.host", "mail1.novalnetsolutions.com");
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.user", fromEmail);
		properties.put("mail.password", password);
		
		//creating session with authentication
		Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail,password);
		    }
		};
		Session session = Session.getInstance(properties,auth);
		
		  try {
			  	//create new email message
				Message message = new MimeMessage(session);

					message.setFrom(new InternetAddress(fromEmail));
					message.setRecipient(RecipientType.TO, new InternetAddress(REPORT_SENT_EMAIL));
					message.setSubject(REPORT_TITLE);
					message.setSentDate(new Date());

				//create message part
				MimeBodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setContent(mailBody,"text/html");
				
				//create multipart
				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);
				
				//Adding attachments
				if(attachments != null && attachments.length > 0) {
					for(String file :attachments) {
						MimeBodyPart attach = new MimeBodyPart();
						
						try {
							attach.attachFile(file);
						} catch (IOException e) {
							e.printStackTrace();
							Log.info(e.getMessage());
							Log.info("Email attachment file : "+attach);
						}
						
						multipart.addBodyPart(attach);
					}
				}
				
				//set the mutipart into message body
				message.setContent(multipart);
				
				//send email
				Transport.send(message);
				
	        }catch(AddressException e) {
	        	e.printStackTrace();
	        	Log.info(e.getMessage());
	        }catch(MessagingException e) {
	        	e.printStackTrace();
	        	Log.info(e.getMessage());
	        }
		
	}
	
	public static void sendEmailWithTestCounts(int numberOfTestCasesRun, int numberOfTestCasesPassed, int numberOfTestCasesFailed, int numberOfTestCasesSkipped) {
		sendEmail(emailBody(numberOfTestCasesRun, numberOfTestCasesPassed, numberOfTestCasesFailed, numberOfTestCasesSkipped),new String[] {EXTENT_HTML_REPORT_PATH});		
	}

	 public static String emailBody(int numberOfTestCasesRun, int numberOfTestCasesPassed, int numberOfTestCasesFailed, int numberOfTestCasesSkipped) {
			String body = "<html>" 
						+"<body>  "
						+ "<table class=\"container\" align=\"left\" style=\"padding-top:20px\"> "
						+"<tr><td >Dear User, </td></tr>" 
						+"<tr><td >This email contains the test report of the Selenium Autmation Testing in the attachment below. <br><br></td></tr>"+"\r\n" 
						+"<tr align=\"left\"><td><h4> "+REPORT_TITLE +"</h4></td></tr>"
						+"<tr><td align=\"left\" style=\"font-size:18px;color:blue\">Total Test Cases RUN : "+ numberOfTestCasesRun+" </td></tr>" 
						+"<tr><td align=\"left\" style=\"font-size:18px;color:green\">Total Test Cases PASSED : "+ numberOfTestCasesPassed+" </td></tr>"
						+"<tr><td align=\"left\" style=\"font-size:18px;color:red\">Total Test Cases FAILED : "+ numberOfTestCasesFailed+" </td></tr>"
						+"<tr><td align=\"left\" style=\"font-size:18px;color:#8B8000\">Total Test Cases SKIPPED : "+ numberOfTestCasesSkipped+" <br><br></td></tr>" +"\r\n"
						+"<tr><td >Regards, </td></tr>" 
						+"<tr><td >Software Testing Team </td></tr>"+"\r\n"
						+"</table>\r\n" +"\r\n" + "\r\n"
						+"</body>\r\n"+"</html>\r\n";
			return body;
		}
	
}
