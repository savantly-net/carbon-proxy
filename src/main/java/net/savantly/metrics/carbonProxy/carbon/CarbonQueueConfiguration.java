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
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import net.savantly.metrics.carbonProxy.KeyValuePair;
import net.savantly.metrics.carbonProxy.filter.MetricFilter;
import net.savantly.metrics.carbonProxy.filter.StringFilterService;
import net.savantly.metrics.carbonProxy.rewriter.RewriterService;

@Configuration("carbonQueueConfiguration")
@ConfigurationProperties("carbon")
public class CarbonQueueConfiguration {
	private static final Logger log = LoggerFactory.getLogger(CarbonQueueConfiguration.class);

	public static final String CARBON_QUEUE_CHANNEL = "carbonQueue";
	
	private List<String> aggregatorMatches = new ArrayList<String>();
	
	private Map<String, MetricFilter> filters = new HashMap<>();
	private Map<String, KeyValuePair> replacements = new HashMap<>();
	private String[] defaultChannels = {CarbonRelayConfiguration.CARBON_UDP_RELAY_CHANNEL};
	private String[] aggregatorChannels = {CarbonRelayConfiguration.CARBON_UDP_AGGREGATOR_CHANNEL};
	
	private MessageChannel metricInputChannel;
	
	public CarbonQueueConfiguration(
			@Qualifier("singleMetricInputChannel") MessageChannel metricInputChannel) {
		this.metricInputChannel = metricInputChannel;
	}
	
	@Bean(CARBON_QUEUE_CHANNEL)
	public QueueChannel carbonQueue(){
		 QueueChannel channel = MessageChannels.queue().get();
		 return channel;
	}
	
	@Bean("carbonFilterService")
	public StringFilterService carbonFilterService(){
		return new StringFilterService(filters);
	}
	
	@Bean("rewriterService")
	public RewriterService rewriterService(){
		RewriterService service = new RewriterService();
		service.setPatterns(replacements);
		return service;
	}
	
	@Router(inputChannel=CARBON_QUEUE_CHANNEL)
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
	public IntegrationFlow carbonQueueFlow(@Qualifier("carbonFilterService") StringFilterService filterService) {

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
					a.id("carbonItemAggregator");
				})
				.channel(CARBON_QUEUE_CHANNEL)
				.get();
	}

	public Map<String, MetricFilter> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, MetricFilter> filters) {
		this.filters = filters;
	}

	public Map<String, KeyValuePair> getReplacements() {
		return replacements;
	}

	public void setReplacements(Map<String, KeyValuePair> replacements) {
		this.replacements = replacements;
	}

	public List<String> getAggregatorMatches() {
		return aggregatorMatches;
	}

	public void setAggregatorMatches(List<String> aggregatorMatches) {
		this.aggregatorMatches = aggregatorMatches;
	}
}
