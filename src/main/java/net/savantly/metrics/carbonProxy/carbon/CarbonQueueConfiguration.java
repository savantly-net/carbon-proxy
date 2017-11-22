package net.savantly.metrics.carbonProxy.carbon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import net.savantly.metrics.carbonProxy.AppChannels;
import net.savantly.metrics.carbonProxy.filter.MetricFilter;
import net.savantly.metrics.carbonProxy.filter.StringFilterService;

@Configuration("carbonQueueConfiguration")
@ConfigurationProperties("carbon")
public class CarbonQueueConfiguration {
	private static final Logger log = LoggerFactory.getLogger(CarbonQueueConfiguration.class);

	public static final String CARBON_FILTER_SERVICE = "carbonFilterService";
	public static final String CARBON_ITEM_AGGREGATOR = "carbonItemAggregator";
	
	private List<String> aggregatorMatches = new ArrayList<String>();
	
	private Map<String, MetricFilter> filters = new HashMap<>();
	private String[] defaultChannels = {AppChannels.CARBON_UDP_RELAY_CHANNEL};
	private String[] aggregatorChannels = {AppChannels.CARBON_UDP_AGGREGATOR_CHANNEL};
	
	private MessageChannel metricInputChannel;
	
	public CarbonQueueConfiguration(
			@Qualifier(AppChannels.SINGLE_METRIC_INPUT_CHANNEL) MessageChannel metricInputChannel) {
		this.metricInputChannel = metricInputChannel;
	}

	
	@Bean(CARBON_FILTER_SERVICE)
	public StringFilterService carbonFilterService(){
		return new StringFilterService(filters);
	}
	
	@Router(inputChannel=AppChannels.CARBON_QUEUE_CHANNEL)
	public String[] carbonQueueRouter(Message<String> message) {
		String payload = (String) message.getPayload();
		for (String regex : getAggregatorMatches()) {
			if (payload.matches(regex)) {
				return this.aggregatorChannels;
			}
		}
		return this.defaultChannels;
	}
	
	@Bean
	public IntegrationFlow carbonQueueFlow(@Qualifier(CARBON_FILTER_SERVICE) StringFilterService filterService) {

		return IntegrationFlows.from(metricInputChannel)
				.filter(String.class, (g) -> {
							return filterService.isMatched(g);
						}, c -> c.id("carbonFilter"))
				.aggregate(a -> {
					a.outputProcessor(g -> {
							String agg = g.getMessages()
		                        .stream()
		                        .<String>map(m -> (String) m.getPayload())
		                        .collect(Collectors.joining("\n"));
	                        log.debug("aggregated messages: {}", agg);
	                        return agg;
                        });
					a.expireGroupsUponCompletion(true);
					a.expireGroupsUponTimeout(true);
					a.id(CARBON_ITEM_AGGREGATOR);
				})
				.channel(AppChannels.CARBON_QUEUE_CHANNEL)
				.get();
	}

	public Map<String, MetricFilter> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, MetricFilter> filters) {
		this.filters = filters;
	}

	public List<String> getAggregatorMatches() {
		return aggregatorMatches;
	}

	public void setAggregatorMatches(List<String> aggregatorMatches) {
		this.aggregatorMatches = aggregatorMatches;
	}
}
