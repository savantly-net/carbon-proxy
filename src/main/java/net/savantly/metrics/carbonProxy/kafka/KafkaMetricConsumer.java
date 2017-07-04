package net.savantly.metrics.carbonProxy.kafka;

import java.util.concurrent.CountDownLatch;

import javax.sound.midi.Receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import net.savantly.metrics.carbonProxy.schema.Metric;

public class KafkaMetricConsumer {

	  private static final Logger log = LoggerFactory.getLogger(Receiver.class);

	  private CountDownLatch latch = new CountDownLatch(1);

	  public CountDownLatch getLatch() {
	    return latch;
	  }

	  @KafkaListener(topics = "${kafka.consumer.topic}")
	  public void receive(Metric metricDefinition) {
	    log.debug("received metricDefinition='{}'", metricDefinition.toString());
	    latch.countDown();
	  }
}
