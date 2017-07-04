package net.savantly.metrics.carbonProxy.carbon;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.MessageChannel;

import net.savantly.metrics.carbonProxy.filter.MetricFilter;
import net.savantly.metrics.carbonProxy.filter.StringFilterService;

@Configuration
@ConfigurationProperties("carbon")
public class CarbonQueueConfiguration {

	private Map<String, MetricFilter> filters = new HashMap<>();
	
	@Autowired
	@Qualifier("singleMetricInputChannel")
	private MessageChannel metricInputChannel;

	
	@Bean("carbonQueue")
	public QueueChannel carbonQueue(){
		 QueueChannel channel = MessageChannels.queue().get();
		 return channel;
	}
	
	@Bean("carbonFilterService")
	public StringFilterService carbonFilterService(){
		return new StringFilterService(filters);
	}
	
	@Bean
	public IntegrationFlow carbonQueueFlow(@Qualifier("carbonFilterService") StringFilterService filterService) {

		return IntegrationFlows.from(metricInputChannel)
				.filter(String.class, (g) -> {
							return filterService.isMatched(g);
						}, c -> c.id("carbonFilter"))
				.channel("carbonQueue")
				.channel("carbonUdpRelayChannel")
				.get();
	}

	public Map<String, MetricFilter> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, MetricFilter> filters) {
		this.filters = filters;
	}
}
