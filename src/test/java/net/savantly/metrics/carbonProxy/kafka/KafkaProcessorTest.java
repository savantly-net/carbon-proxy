package net.savantly.metrics.carbonProxy.kafka;

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
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.savantly.metrics.carbonProxy.ApplicationConfiguration;
import net.savantly.metrics.schema.MetricDefinition;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { 
		KafkaProcessorTest.TestConfiguration.class, 
		ApplicationConfiguration.class,
		KafkaMetricProducerConfiguration.class,
		KafkaMetricConsumerConfiguration.class,
		KafkaProcessor.class })
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
public class KafkaProcessorTest {

	private final static Logger log = LoggerFactory.getLogger(KafkaProcessorTest.class);
	public static final String TOPIC = "metrics";

	@Autowired
	@Qualifier("singleMetricInputChannel")
	private MessageChannel singleMetricInputChannel;
	
	@Autowired
	private KafkaMetricConsumer receiver;
	@Autowired
	KafkaProcessor processor;
	
	@Autowired
	private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, TOPIC);

	
	@BeforeClass
	public static void beforeClass() {
		System.setProperty("kafka.producer.enabled", "true");
		System.setProperty("kafka.consumer.enabled", "true");
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

		GenericMessage<String> message = new GenericMessage<String>(payloadString);

		try {
			singleMetricInputChannel.send(message);
		} catch (Exception e) {
			log.error("Failed to send message", e);
		} finally {
			receiver.getLatch().await(60, TimeUnit.SECONDS);
		    Assert.assertEquals(0, receiver.getLatch().getCount());
		}
	}
	
	@Test
	public void testInvalidMetric() throws InterruptedException {
		String payloadString = String.format("test.metric.value nan %s", DateTime.now().getMillis() / 1000);

		try {
			processor.singleMetricStringToMetricDefinition(payloadString);
		} catch (Exception e) {
			Assert.assertNotNull(e);
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
	}

}
