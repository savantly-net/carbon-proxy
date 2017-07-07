package net.savantly.metrics.carbonProxy.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import net.savantly.metrics.schema.MetricDefinition;

public class KafkaMetricProducer {
	  private static final Logger log = LoggerFactory.getLogger(KafkaMetricProducer.class);

	  @Value("${kafka.producer.topic}")
	  private String jsonTopic;

	  @Autowired
	  private KafkaTemplate<String, MetricDefinition> kafkaTemplate;

	  public ListenableFuture<SendResult<String, MetricDefinition>> send(MetricDefinition metricDefinition) {
	    log.debug("sending MetricDefinition='{}'", metricDefinition.toString());
	    ListenableFuture<SendResult<String, MetricDefinition>> sendResult = kafkaTemplate.send(jsonTopic, metricDefinition.getId(), metricDefinition);
		return sendResult;
	  }
}
