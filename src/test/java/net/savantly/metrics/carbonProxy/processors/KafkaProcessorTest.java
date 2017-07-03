package net.savantly.metrics.carbonProxy.processors;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.savantly.metrics.carbonProxy.kafka.KafkaMetricConsumer;
import net.savantly.metrics.carbonProxy.kafka.KafkaMetricConsumerConfiguration;
import net.savantly.metrics.carbonProxy.kafka.KafkaMetricProducerConfiguration;
import net.savantly.metrics.carbonProxy.schema.MetricDefinition;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { 
		KafkaProcessorTest.TestConfiguration.class, 
		KafkaMetricProducerConfiguration.class,
		KafkaMetricConsumerConfiguration.class,
		KafkaProcessor.class })
public class KafkaProcessorTest {

	private final static Logger log = LoggerFactory.getLogger(KafkaProcessorTest.class);
	public static final String TOPIC = "metrics";

	@Autowired
	@Qualifier("publisherChannel")
	private MessageChannel publisherChannel;
	
	@Autowired
	private KafkaMetricConsumer receiver;
	
	@Autowired
	private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, TOPIC);

	@BeforeClass
	public static void beforeClass() {
		TestConfiguration.kafkaBootstrapServers = embeddedKafka.getBrokersAsString();
		log.debug("kafkaServers='{}'", TestConfiguration.kafkaBootstrapServers);
		// override the property in application.properties
		System.setProperty("kafka.bootstrap-servers", TestConfiguration.kafkaBootstrapServers);
	}

	@Before
	public void setUp() throws Exception {
		try {
		    // wait until the partitions are assigned
		    for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry
		        .getListenerContainers()) {
		      ContainerTestUtils.waitForAssignment(messageListenerContainer,
		          embeddedKafka.getPartitionsPerTopic());
		    }

		} catch (Exception e) {
			log.error("Couldn't start KafkaMessageListenerContainer", e);
		}
	}


	@Test
	public void test() throws InterruptedException {
		String payloadString = String.format("test.metric.value 123 %s", DateTime.now().getMillis() / 1000);
		MetricDefinition metric = new MetricDefinition(payloadString, MetricDefinition.Style.Metric_1_0);
		metric.setMetric(payloadString);

		Message<String> message = new Message<String>() {
			@Override
			public String getPayload() {
				return payloadString;
			}

			@Override
			public MessageHeaders getHeaders() {
				return new MessageHeaders(new HashMap<>());
			}
		};

		try {
			publisherChannel.send(message);
		} catch (Exception e) {
			log.error("Failed to send message", e);
		} finally {
			receiver.getLatch().await(5000, TimeUnit.MILLISECONDS);
		    Assert.assertEquals(receiver.getLatch().getCount(), 0);
		}
	}

	@EnableKafka
	@Configuration
	@SpringBootApplication
	public static class TestConfiguration {

		public static String kafkaBootstrapServers;
		{
			System.setProperty("kafka.consumer.enabled", "true");
		}

		@Bean("publisherChannel")
		public MessageChannel publisherChannel() {
			return MessageChannels.publishSubscribe().get();
		}
	}

}
