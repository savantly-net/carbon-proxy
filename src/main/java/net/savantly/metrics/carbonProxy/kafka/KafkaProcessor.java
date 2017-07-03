package net.savantly.metrics.carbonProxy.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import net.savantly.metrics.carbonProxy.schema.MetricDefinition;

@Configuration
@ConditionalOnProperty(prefix="kafka", name="producer.enabled")
public class KafkaProcessor {
	private static final Logger log = LoggerFactory.getLogger(KafkaProcessor.class);
	
	@Autowired
	@Qualifier("singleMetricInputChannel")
	private MessageChannel singleMetricInputChannel;
	@Autowired
	@Qualifier("kafkaMetricProducerMessageHandler")
	private MessageHandler handler;

	
	@Bean
	public IntegrationFlow kafkaIntegrationFlow() {
		return IntegrationFlows.from(singleMetricInputChannel)
				.<String, MetricDefinition>transform(S -> new MetricDefinition(S, MetricDefinition.Style.Metric_1_0))
				.handle(handler)
				.get();
	}

}
