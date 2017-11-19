package net.savantly.metrics.carbonProxy.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import net.savantly.metrics.schema.MetricDefinition;

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

	@Transformer(inputChannel="singleMetricInputChannel", outputChannel="kafkaHandlerInput")
	public MetricDefinition singleMetricStringToMetricDefinition(String str){
		try {
			return new MetricDefinition(str, MetricDefinition.Style.Metric_1_0);
		} catch (Exception e) {
			log.error("Failed to parse MetricDefinition: {}", str);
			return null;
		}
	}
	
	@ServiceActivator(inputChannel="kafkaHandlerInput")
	public void handleKafkaMessage(Message<MetricDefinition> message){
		handler.handleMessage(message);
	}
	
/*	@Bean
	public IntegrationFlow kafkaIntegrationFlow() {
		return IntegrationFlows.from(singleMetricInputChannel)
				.transform(MessageChannels)
				.handle(handler)
				.get();
	}*/

}
