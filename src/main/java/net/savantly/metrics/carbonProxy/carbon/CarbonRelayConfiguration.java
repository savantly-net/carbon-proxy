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
import net.savantly.metrics.carbonProxy.AppChannels;

@Configuration
public class CarbonRelayConfiguration {
	
	public static final String UNICAST_SENDER = "unicastSender";
	public static final String UNICAST_AGGREGATOR_SENDER = "unicastAggregatorSender";
	
	@Autowired
	ApplicationConfiguration config;

	
	@MessagingGateway(defaultRequestChannel = AppChannels.CARBON_TCP_RELAY_CHANNEL, name="carbonTcpRelayGateway")
	public interface Gateway {
		String viaTcp(String in);
	}
	
	@Bean
	@ServiceActivator(inputChannel = AppChannels.CARBON_TCP_RELAY_CHANNEL)
	public MessageHandler tcpOutGate(AbstractClientConnectionFactory connectionFactory) {
		TcpOutboundGateway gate = new TcpOutboundGateway();
		gate.setConnectionFactory(connectionFactory);
		return gate;
	}



	@Bean(UNICAST_SENDER)
	@ServiceActivator(inputChannel = AppChannels.CARBON_UDP_RELAY_CHANNEL)
	public UnicastSendingMessageHandler unicastSender() {
		UnicastSendingMessageHandler udpAdapter = new UnicastSendingMessageHandler(config.getCarbonHost(), config.getCarbonPort());
		udpAdapter.setLoggingEnabled(true);
		return udpAdapter;
	}
	
	@Bean(UNICAST_AGGREGATOR_SENDER)
	@ServiceActivator(inputChannel = AppChannels.CARBON_UDP_AGGREGATOR_CHANNEL)
	public UnicastSendingMessageHandler unicastAggregatorSender() {
		UnicastSendingMessageHandler udpAdapter = new UnicastSendingMessageHandler(config.getCarbonHost(), config.getCarbonAggregatorPort());
		udpAdapter.setLoggingEnabled(true);
		return udpAdapter;
	}
}
