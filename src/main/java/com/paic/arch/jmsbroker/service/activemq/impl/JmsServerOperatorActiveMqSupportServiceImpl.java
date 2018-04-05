package com.paic.arch.jmsbroker.service.activemq.impl;

import static org.slf4j.LoggerFactory.getLogger;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.DestinationStatistics;
import org.slf4j.Logger;

import com.paic.arch.jmsbroker.JmsMessageBrokerSupport;
import com.paic.arch.jmsbroker.service.JmsServerSupportService;

public class JmsServerOperatorActiveMqSupportServiceImpl implements JmsServerSupportService {
	private static final Logger LOG = getLogger(JmsServerOperatorActiveMqSupportServiceImpl.class);

	private String brokerUrl;
	private BrokerService brokerService;

	private JmsMessageBrokerSupport jmsMessageBrokerSupport;

	@Override
	public void createEmbeddedBroker() throws Exception {
		brokerService = new BrokerService();
		brokerService.setPersistent(false);
		brokerService.addConnector(brokerUrl);
	}
	@Override
	public void startEmbeddedBroker() throws Exception {
		brokerService.start();
	}
	@Override
	public void stopTheRunningBroker() throws Exception {
		if (brokerService == null) {
			throw new IllegalStateException("Cannot stop the broker from this API: "
					+ "perhaps it was started independently from this utility");
		}
		brokerService.stop();
		brokerService.waitUntilStopped();
	}
	

	public JmsServerOperatorActiveMqSupportServiceImpl(JmsMessageBrokerSupport jmsMessageBrokerSupport) throws Exception {
		this.jmsMessageBrokerSupport = jmsMessageBrokerSupport;
		this.brokerUrl = jmsMessageBrokerSupport.getBrokerUrl();
	}
	
	@Override
	public long getEnqueuedMessageCountAt(String aDestinationName) throws Exception {
		return getDestinationStatisticsFor(aDestinationName).getMessages().getCount();
	}
	@Override
	public boolean isEmptyQueueAt(String aDestinationName) throws Exception {
		return getEnqueuedMessageCountAt(aDestinationName) == 0;
	}

	private DestinationStatistics getDestinationStatisticsFor(String aDestinationName) throws Exception {
		Broker regionBroker = brokerService.getRegionBroker();
		for (org.apache.activemq.broker.region.Destination destination : regionBroker.getDestinationMap().values()) {
			if (destination.getName().equals(aDestinationName)) {
				return destination.getDestinationStatistics();
			}
		}
		throw new IllegalStateException(
				String.format("Destination %s does not exist on broker at %s", aDestinationName, brokerUrl));
	}

}
