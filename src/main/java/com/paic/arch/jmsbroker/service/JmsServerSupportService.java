package com.paic.arch.jmsbroker.service;

import com.paic.arch.jmsbroker.JmsMessageBrokerSupport;

public interface JmsServerSupportService {
	public void stopTheRunningBroker() throws Exception;

	public void startEmbeddedBroker() throws Exception;

	public void createEmbeddedBroker() throws Exception;

	long getEnqueuedMessageCountAt(String aDestinationName) throws Exception;

	boolean isEmptyQueueAt(String aDestinationName) throws Exception;

}
