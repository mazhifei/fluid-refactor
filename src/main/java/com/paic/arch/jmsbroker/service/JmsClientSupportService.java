package com.paic.arch.jmsbroker.service;

import com.paic.arch.jmsbroker.JmsMessageBrokerSupport;

public interface JmsClientSupportService {
	public JmsMessageBrokerSupport sendATextMessageToDestinationAt(String aDestinationName, final String aMessageToSend);
	
	public String retrieveASingleMessageFromTheDestination(String aDestinationName, int i);
	
	public String retrieveASingleMessageFromTheDestination(String testQueue);


}
