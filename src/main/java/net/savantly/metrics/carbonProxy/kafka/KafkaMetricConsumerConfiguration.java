package net.savantly.metrics.carbonProxy.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import net.savantly.metrics.carbonProxy.schema.MetricDefinition;

@Configuration
@ConditionalOnProperty("kafka.consumer.enabled")
public class KafkaMetricConsumerConfiguration {

	@Value("${kafka.bootstrap-servers}")
	private String bootstrapServers;
	@Value("${kafka.consumer.group-id")
	private String groupId;

	@Bean
	public Map<String, Object> consumerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

		return props;
	}

	@Bean
	public ConsumerFactory<String, MetricDefinition> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(),
				new JsonDeserializer<>(MetricDefinition.class));
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, MetricDefinition> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, MetricDefinition> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());

		return factory;
	}

	@Bean
	public KafkaMetricConsumer receiver() {
		return new KafkaMetricConsumer();
	}
}
