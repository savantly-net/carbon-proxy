package net.savantly.metrics.carbonProxy.carbon;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;

@Configuration
public class CarbonQueueConfiguration {

	@Bean("carbonQueue")
	public QueueChannel carbonQueue(){
		 return MessageChannels.queue().get();
	}
	
	@Bean
	public IntegrationFlow carbonQueueFlow() {
		return IntegrationFlows.from("carbonQueue")
				.channel("carbonUdpRelayChannel")
				.get();
	}
}
