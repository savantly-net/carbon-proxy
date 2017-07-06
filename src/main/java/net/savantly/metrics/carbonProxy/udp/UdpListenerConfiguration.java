package net.savantly.metrics.carbonProxy.udp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.ip.udp.UnicastReceivingChannelAdapter;
import org.springframework.messaging.MessageChannel;

import net.savantly.metrics.carbonProxy.ApplicationConfiguration;

@Configuration
public class UdpListenerConfiguration {
	
	@Autowired
	private ApplicationConfiguration config;	
	
	@Bean("inboundUdpChannel")
	public DirectChannel inboundUdpChannel(){
		 return MessageChannels.direct().get();
	}
	
	@Bean
	public UnicastReceivingChannelAdapter unicastReceivingChannelAdapter(@Qualifier("inboundUdpChannel") MessageChannel inboundUdpChannel) {
		UnicastReceivingChannelAdapter udpAdapter = new UnicastReceivingChannelAdapter(config.getServerPort());
		udpAdapter.setLocalAddress(config.getServerAddress());
		udpAdapter.setOutputChannel(inboundUdpChannel);
		return udpAdapter;
	}

	@Bean
	public IntegrationFlow inboundUdpFlow(
			@Qualifier("preProcessorQueueChannel") MessageChannel preProcessorQueueChannel,
			@Qualifier("inboundUdpChannel") MessageChannel inboundUdpChannel) {
	    return IntegrationFlows.from(inboundUdpChannel)
	            .channel(preProcessorQueueChannel)
	            .get();
	}
}
