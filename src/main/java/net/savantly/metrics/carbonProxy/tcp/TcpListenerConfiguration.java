package net.savantly.metrics.carbonProxy.tcp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;

import net.savantly.metrics.carbonProxy.AppChannels;

@Configuration
public class TcpListenerConfiguration {


	@Bean
	public TcpInboundGateway tcpInGate(AbstractServerConnectionFactory connectionFactory) {
		TcpInboundGateway inGate = new TcpInboundGateway();
		inGate.setConnectionFactory(connectionFactory);
		inGate.setRequestChannelName(AppChannels.INBOUND_TCP_CHANNEL);
		return inGate;
	}
	
	
	@Bean
	public IntegrationFlow inboundTcpFlow() {
	    return IntegrationFlows.from(AppChannels.INBOUND_TCP_CHANNEL)
	            .channel(AppChannels.PRE_PROCESSOR_QUEUE_CHANNEL)
	            .get();
	}
	
}
