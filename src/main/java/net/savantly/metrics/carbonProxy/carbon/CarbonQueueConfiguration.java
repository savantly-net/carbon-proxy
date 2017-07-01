package net.savantly.metrics.carbonProxy.carbon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.MessageChannel;

@Configuration
public class CarbonQueueConfiguration {

	@Autowired
	private MessageChannel publisherChannel;
	
	@Bean("carbonQueue")
	public QueueChannel carbonQueue(){
		 QueueChannel channel = MessageChannels.queue().get();
		 return channel;
	}
	
	@Bean
	public IntegrationFlow carbonQueueFlow() {
		return IntegrationFlows.from(publisherChannel)
				.channel("carbonQueue")
				.channel("carbonUdpRelayChannel")
				.get();
	}
}
