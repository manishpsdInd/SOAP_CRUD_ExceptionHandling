package com.javaspringclub.exception;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.namespace.QName;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ScheduledMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;

import com.javaspringclub.gs_ws.ServiceStatus;

public class DetailSoapFaultDefinitionExceptionResolver extends SoapFaultMappingExceptionResolver {

	private static final QName CODE = new QName("statusCode");
	private static final QName MESSAGE = new QName("message");

	@Value("${activemq.queue.target.destination:poc}")
	private String queueTarDest;
	
	@Override
	protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {
		logger.warn("Exception processed ", ex);
		
		if (ex instanceof ServiceFaultException) {
			ServiceStatus status = ((ServiceFaultException) ex).getServiceStatus();
			SoapFaultDetail detail = fault.addFaultDetail();
			detail.addFaultDetailElement(CODE).addText(status.getStatusCode());
			detail.addFaultDetailElement(MESSAGE).addText(status.getMessage());
			
		} else {
			SoapFaultDetail detail = fault.addFaultDetail();
			detail.addFaultDetailElement(CODE).addText("Failure");
			detail.addFaultDetailElement(MESSAGE).addText(ex.getMessage());
		}

		try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(queueTarDest);
            
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            String text = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v46=\"http://schemas.merchantwarehouse.com/merchantware/v46/\">\n"
            		+ "   <soap:Header/>\n"
            		+ "   <soap:Body>\n"
            		+ "      <v46:UnboardAccount>\n"
            		+ "         <v46:Credentials>\n"
            		+ "            <v46:MerchantName>?</v46:MerchantName>\n"
            		+ "            <v46:MerchantSiteId>?</v46:MerchantSiteId>\n"
            		+ "            <v46:MerchantKey>?</v46:MerchantKey>\n"
            		+ "         </v46:Credentials>\n"
            		+ "         <v46:Request>\n"
            		+ "            <v46:VaultToken>4343</v46:VaultToken>\n"
            		+ "         </v46:Request>\n"
            		+ "      </v46:UnboardAccount>\n"
            		+ "   </soap:Body>\n"
            		+ "</soap:Envelope>";

            TextMessage message = session.createTextMessage(text);
            message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 5000 * 60 * 1000);
            message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, 5000 * 60 * 1000);
            //message.setLongProperty("_AMQ_SCHED_DELIVERY", 60 * 1000);
            message.setLongProperty("_AMQ_SCHED_DELIVERY", System.currentTimeMillis() + 5000);
            //message.setIntProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 3);
            //message.setStringProperty(ScheduledMessage.AMQ_SCHEDULED_CRON, "0 2 12 * *");
            //message.setBooleanProperty("schedulerSupport", true);
            
            logger.info("Sent message: "+ message.hashCode() + " : " + Thread.currentThread().getName());
            producer.send(message);

            session.close();
            connection.close();
        }
        catch (Exception e) {
        	logger.warn("Caught: " + e);
        }
	}
}
