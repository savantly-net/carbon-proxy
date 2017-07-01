package net.savantly.metrics.carbonProxy.processors;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

@Configuration
@ConditionalOnProperty(prefix="carbonProxy.kafka", name="enabled")
@ConfigurationProperties("carbonProxy.kafka")
public class KafkaProcessor {
	private static final Logger log = LoggerFactory.getLogger(KafkaProcessor.class);
	
	@Value("${carbonProxy.kafka.topic}")
	private String topic;
	@Value("${carbonProxy.kafka.brokers}")
	private String brokers;
	
	@Autowired
	private MessageChannel publisherChannel;
    @Autowired
    private KafkaTemplate<String, String> template;
    
    @Bean
    public Map<String, Object> producerConfigs() {
      Map<String, Object> props = new HashMap<>();
      // list of host:port pairs used for establishing the initial connections to the Kakfa cluster
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

      return props;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
      return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
      return new KafkaTemplate<>(producerFactory());
    }

    
	public MessageHandler kafkaHandler() {
		MessageHandler messageHandler = new MessageHandler(){
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				template.send(topic, message.getPayload().toString());
				log.debug(message.getPayload().toString());
			}
			
		};
		return messageHandler;
	}
	
	@Bean
	public IntegrationFlow kafkaIntegrationFlow() {
		return IntegrationFlows.from(publisherChannel)
				.handle(kafkaHandler())
				.get();
	}
}
