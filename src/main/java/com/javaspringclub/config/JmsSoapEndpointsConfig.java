package com.javaspringclub.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.region.policy.RedeliveryPolicyMap;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.jms.JMSConfigFeature;
import org.apache.cxf.transport.jms.JMSConfiguration;
import org.apache.cxf.transport.jms.JMSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.server.endpoint.annotation.Endpoint;


@Configuration
public class JmsSoapEndpointsConfig {
	
	//@Autowired
	//SpringBus bus;
	
	@Value("${activemq.packages.trusted:com}")
	private String trustedPackage;
	
	@Value("${activemq.broker-url:tcp://localhost:61616}")
	private String brokerUrl;
	
	@Value("${activemq.username:admin}")
	private String avtiveMqUserName;
	
	@Value("${activemq.password:admin}")
	private String avtiveMqPassword;

	@Value("${activemq.queue.target.destination:poc}")
	private String queueTarDest;
	
	@Value("${activemq.queue.reply.destination: poc-reply}")
	private String queueRepDest;
	
	/*
	@Bean
	public Endpoint jmsEndpoint(JMSConfigFeature jmsConfigFeature, SpringBus bus, @Qualifier("CheckSoapJmsEndPt")CheckSoap checkSoap)	{
		EndpointImpl endpoint = new EndpointImpl(bus, checkSoap);
		endpoint.getFeatures().add(jmsConfigFeature);
		endpoint.publish("jms://");
		return endpoint;
	}
	*/
	
	@Bean
	public ActiveMQConnectionFactory mqConnectionFactory()		{
		
		ActiveMQConnectionFactory mqConnectionFactory = new ActiveMQConnectionFactory();
			
		mqConnectionFactory.setBrokerURL(brokerUrl);
		mqConnectionFactory.setUserName(avtiveMqUserName);
		mqConnectionFactory.setPassword(avtiveMqPassword);
		
		List<String> trustedPackages = new ArrayList<String>();
		trustedPackages.add(trustedPackage);
		
		RedeliveryPolicy redeliveryPolicyForQueue = mqConnectionFactory.getRedeliveryPolicy();
		redeliveryPolicyForQueue.setInitialRedeliveryDelay(500);
		redeliveryPolicyForQueue.setBackOffMultiplier(2);
		redeliveryPolicyForQueue.setUseExponentialBackOff(true);
		redeliveryPolicyForQueue.setMaximumRedeliveries(2);
		
		RedeliveryPolicyMap redeliveryPolicyMap = mqConnectionFactory.getRedeliveryPolicyMap();
		redeliveryPolicyMap.put(new ActiveMQTopic(">"), redeliveryPolicyForQueue);
		
		mqConnectionFactory.setRedeliveryPolicy(redeliveryPolicyForQueue);
		mqConnectionFactory.setTrustedPackages(trustedPackages);
		mqConnectionFactory.setObjectMessageSerializationDefered(true);
		mqConnectionFactory.setCopyMessageOnSend(false);
		mqConnectionFactory.setRedeliveryPolicyMap(redeliveryPolicyMap);
		
		return mqConnectionFactory;
	}
	
	@Bean
	public JMSConfigFeature jmsConfigFeature(ActiveMQConnectionFactory mqConnectionFactory){
		
		JMSConfigFeature feature = new JMSConfigFeature();
		JMSConfiguration jmsConfiguration = new JMSConfiguration();
		jmsConfiguration.setConnectionFactory(mqConnectionFactory);
		jmsConfiguration.setTargetDestination(queueTarDest);
		jmsConfiguration.setReplyDestination(queueRepDest);
		jmsConfiguration.setMessageType(JMSConstants.TEXT_MESSAGE_TYPE);
		feature.setJmsConfig(jmsConfiguration);
		return feature;
	}

}