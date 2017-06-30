package net.savantly.metrics.carbonProxy.udp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.ip.udp.UnicastReceivingChannelAdapter;

import net.savantly.metrics.carbonProxy.ApplicationConfiguration;

@Configuration
public class UdpListenerConfiguration {
	
	@Autowired
	private ApplicationConfiguration config;

	@Bean
	public UnicastReceivingChannelAdapter unicastReceivingChannelAdapter() {
		UnicastReceivingChannelAdapter udpAdapter = new UnicastReceivingChannelAdapter(config.getServerPort());
		udpAdapter.setLocalAddress(config.getServerAddress());
		udpAdapter.setOutputChannelName("inboundUdpChannel");
		return udpAdapter;
	}
	
	
	@Bean("inboundUdpChannel")
	public DirectChannel inboundUdpChannel(){
		 return MessageChannels.direct().get();
	}
	
	@Bean
	public IntegrationFlow inboundUdpFlow() {
	    return IntegrationFlows.from("inboundUdpChannel")
	            .channel("byteArrayChannel")
	            .get();
	}
}
