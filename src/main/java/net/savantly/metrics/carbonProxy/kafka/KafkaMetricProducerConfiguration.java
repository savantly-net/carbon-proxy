package net.savantly.metrics.carbonProxy.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.messaging.MessageHandler;

import net.savantly.metrics.carbonProxy.filter.FilterService;
import net.savantly.metrics.carbonProxy.filter.MetricFilter;
import net.savantly.metrics.carbonProxy.filter.MetricFilterService;
import net.savantly.metrics.schema.MetricDefinition;

@Configuration
@ConfigurationProperties("kafka.producer")
public class KafkaMetricProducerConfiguration {

	@Value("${kafka.bootstrap-servers}")
	private String bootstrapServers;

	private Map<String, MetricFilter> filters = new HashMap<>();

	@Bean
	public Map<String, Object> producerConfigs() {
		Map<String, Object> props = new HashMap<>();

		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		return props;
	}

	@Bean
	public ProducerFactory<String, MetricDefinition> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	@Bean
	public KafkaTemplate<String, MetricDefinition> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

	@Bean
	public KafkaMetricProducer sender() {
		return new KafkaMetricProducer();
	}
	
	@Bean
	public MessageHandler kafkaMetricProducerMessageHandler(
			KafkaMetricProducer producer, 
			@Qualifier("kafkaProducerFilterService") FilterService filterService){
		return new KafkaMetricProducerMessageHandler(producer, filterService);
	}
	
	@Bean("kafkaProducerFilterService")
	public FilterService kafkaProducerFilterService(){
		return new MetricFilterService(this.getFilters());
	}

	public Map<String, MetricFilter> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, MetricFilter> filters) {
		this.filters = filters;
	}
}