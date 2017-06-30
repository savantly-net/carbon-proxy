package net.savantly.metrics.carbonProxy.carbon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.ip.tcp.TcpOutboundGateway;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.udp.UnicastSendingMessageHandler;
import org.springframework.messaging.MessageHandler;

import net.savantly.metrics.carbonProxy.ApplicationConfiguration;

@Configuration
public class CarbonRelayConfiguration {
	
	@Autowired
	ApplicationConfiguration config;

	
	@MessagingGateway(defaultRequestChannel = "carbonTcpRelayChannel", name="carbonTcpRelayGateway")
	public interface Gateway {
		String viaTcp(String in);
	}
	
	@Bean
	@ServiceActivator(inputChannel = "carbonTcpRelayChannel")
	public MessageHandler tcpOutGate(AbstractClientConnectionFactory connectionFactory) {
		TcpOutboundGateway gate = new TcpOutboundGateway();
		gate.setConnectionFactory(connectionFactory);
		//gate.setOutputChannelName("carbonTcpRelayChannel");
		return gate;
	}



	@Bean
	@ServiceActivator(inputChannel = "carbonUdpRelayChannel")
	public UnicastSendingMessageHandler unicastSender() {
		UnicastSendingMessageHandler udpAdapter = new UnicastSendingMessageHandler(config.getCarbonHost(), config.getCarbonPort());
		return udpAdapter;
	}
}
