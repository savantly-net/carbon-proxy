package net.savantly.metrics.carbonProxy.tcp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;

@Configuration
public class TcpListenerConfiguration {
	
	@Bean("inboundTcpChannel")
	public DirectChannel inboundTcpChannel(){
		 return MessageChannels.direct().get();
	}

	@Bean
	public TcpInboundGateway tcpInGate(AbstractServerConnectionFactory connectionFactory) {
		TcpInboundGateway inGate = new TcpInboundGateway();
		inGate.setConnectionFactory(connectionFactory);
		inGate.setRequestChannelName("inboundTcpChannel");
		return inGate;
	}
	
	
	@Bean
	public IntegrationFlow inboundTcpFlow() {
	    return IntegrationFlows.from("inboundTcpChannel")
	            .channel("preProcessorQueueChannel")
	            .get();
	}
	
}
