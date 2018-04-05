package com.paic.arch.jmsbroker.service.activemq.impl;

import static org.slf4j.LoggerFactory.getLogger;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;

import com.paic.arch.jmsbroker.JmsMessageBrokerSupport;
import com.paic.arch.jmsbroker.service.JmsClientSupportService;

public class JmsClientOperatorActiveMqSupportServiceImpl implements JmsClientSupportService {
	private static final Logger LOG = getLogger(JmsClientOperatorActiveMqSupportServiceImpl.class);
	private static final int ONE_SECOND = 1000;
	private static final int DEFAULT_RECEIVE_TIMEOUT = 10 * ONE_SECOND;

	private String brokerUrl;

	private JmsMessageBrokerSupport jmsMessageBrokerSupport;


	public JmsClientOperatorActiveMqSupportServiceImpl() {
		super();
	}

	public void setJmsMessageBrokerSupport(JmsMessageBrokerSupport jmsMessageBrokerSupport) {
		this.jmsMessageBrokerSupport = jmsMessageBrokerSupport;
		this.brokerUrl = jmsMessageBrokerSupport.getBrokerUrl();
	}

	public JmsClientOperatorActiveMqSupportServiceImpl(JmsMessageBrokerSupport jmsMessageBrokerSupport) throws Exception {
		this.jmsMessageBrokerSupport = jmsMessageBrokerSupport;
		this.brokerUrl = jmsMessageBrokerSupport.getBrokerUrl();
	}

	@Override
	public JmsMessageBrokerSupport sendATextMessageToDestinationAt(String aDestinationName,
			final String aMessageToSend) {
		executeCallbackAgainstRemoteBroker(brokerUrl, aDestinationName, (aSession, aDestination) -> {
			MessageProducer producer = aSession.createProducer(aDestination);
			producer.send(aSession.createTextMessage(aMessageToSend));
			producer.close();
			return "";
		});
		return jmsMessageBrokerSupport;
	}
	@Override
	public String retrieveASingleMessageFromTheDestination(String aDestinationName) {
		return retrieveASingleMessageFromTheDestination(aDestinationName, DEFAULT_RECEIVE_TIMEOUT);
	}
	@Override
	public String retrieveASingleMessageFromTheDestination(String aDestinationName, final int aTimeout) {
		return executeCallbackAgainstRemoteBroker(brokerUrl, aDestinationName, (aSession, aDestination) -> {
			MessageConsumer consumer = aSession.createConsumer(aDestination);
			Message message = consumer.receive(aTimeout);
			if (message == null) {
				throw jmsMessageBrokerSupport.new NoMessageReceivedException(
						String.format("No messages received from the broker within the %d timeout", aTimeout));
			}
			consumer.close();
			return ((TextMessage) message).getText();
		});
	}

	private String executeCallbackAgainstRemoteBroker(String aBrokerUrl, String aDestinationName,
			JmsCallback aCallback) {
		Connection connection = null;
		String returnValue = "";
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(aBrokerUrl);
			connection = connectionFactory.createConnection();
			connection.start();
			returnValue = executeCallbackAgainstConnection(connection, aDestinationName, aCallback);
		} catch (JMSException jmse) {
			LOG.error("failed to create connection to {}", aBrokerUrl);
			throw new IllegalStateException(jmse);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException jmse) {
					LOG.warn("Failed to close connection to broker at []", aBrokerUrl);
					throw new IllegalStateException(jmse);
				}
			}
		}
		return returnValue;
	}

	interface JmsCallback {
		String performJmsFunction(Session aSession, Destination aDestination) throws JMSException;
	}

	private String executeCallbackAgainstConnection(Connection aConnection, String aDestinationName,
			JmsCallback aCallback) {
		Session session = null;
		try {
			session = aConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue(aDestinationName);
			return aCallback.performJmsFunction(session, queue);
		} catch (JMSException jmse) {
			LOG.error("Failed to create session on connection {}", aConnection);
			throw new IllegalStateException(jmse);
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (JMSException jmse) {
					LOG.warn("Failed to close session {}", session);
					throw new IllegalStateException(jmse);
				}
			}
		}
	}


	
}
