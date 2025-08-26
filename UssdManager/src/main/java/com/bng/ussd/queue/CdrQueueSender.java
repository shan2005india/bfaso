package com.bng.ussd.queue;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.bng.ussd.exception.coreException;
import com.bng.ussd.util.LogValues;
import com.bng.ussd.util.Logger;


public class CdrQueueSender {
	
	private String qName;
	
	public void sendToQ(String text)  {
		String brokerURL = "tcp://localhost:61616";
		try{
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				brokerURL);
		Connection connection = connectionFactory.createConnection();
		connection.start();
		Session session = connection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);
		Queue queue = session.createQueue(qName);
		MessageProducer messageProducer = session.createProducer(queue);
		TextMessage textMessage = session.createTextMessage();
		textMessage.setText(text);
        
		messageProducer.send(textMessage);
		connection.close();}
		catch(Exception ex){
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(ex));
		}
	}

	public String getqName() {
		return qName;
	}

	public void setqName(String qName) {
		this.qName = qName;
	}

}
