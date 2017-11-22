package net.savantly.metrics.carbonProxy.udp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.ip.udp.UnicastReceivingChannelAdapter;
import org.springframework.messaging.MessageChannel;

import net.savantly.metrics.carbonProxy.AppChannels;
import net.savantly.metrics.carbonProxy.ApplicationConfiguration;

@Configuration
public class UdpListenerConfiguration {
	
	@Autowired
	private ApplicationConfiguration config;	
	
	@Bean
	public UnicastReceivingChannelAdapter unicastReceivingChannelAdapter(
			@Qualifier(AppChannels.INBOUND_UDP_CHANNEL) MessageChannel inboundUdpChannel) {
		UnicastReceivingChannelAdapter udpAdapter = new UnicastReceivingChannelAdapter(config.getServerPort());
		udpAdapter.setLocalAddress(config.getServerAddress());
		udpAdapter.setOutputChannel(inboundUdpChannel);
		return udpAdapter;
	}

	@Bean
	public IntegrationFlow inboundUdpFlow(
			@Qualifier(AppChannels.PRE_PROCESSOR_QUEUE_CHANNEL) MessageChannel preProcessorQueueChannel,
			@Qualifier(AppChannels.INBOUND_UDP_CHANNEL) MessageChannel inboundUdpChannel) {
	    return IntegrationFlows.from(inboundUdpChannel)
	            .channel(preProcessorQueueChannel)
	            .get();
	}
}
