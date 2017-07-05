package net.savantly.metrics.carbonProxy.kafka;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import net.savantly.metrics.carbonProxy.filter.FilterService;
import net.savantly.metrics.carbonProxy.schema.MetricDefinition;

@SuppressWarnings({"rawtypes", "unchecked"})
public class KafkaMetricProducerMessageHandler implements MessageHandler {
	private static final Logger log = LoggerFactory.getLogger(KafkaMetricProducerMessageHandler.class);
	private final CountDownLatch latch = new CountDownLatch(2);
	public CountDownLatch getLatch(){
		return latch;
	}

	private KafkaMetricProducer producer;
	private FilterService filterService;

	public KafkaMetricProducerMessageHandler(KafkaMetricProducer producer, FilterService filterService) {
		this.producer = producer;
		this.filterService = filterService;
		latch.countDown();
	}


	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		MetricDefinition metricDefinition = (MetricDefinition) message.getPayload();
		
		// If the message isn't valid then don't send it
		if(metricDefinition == null || metricDefinition.getId() == null){
			log.debug("skipping invalid metric '{}'", metricDefinition);
		} else {
			try {
				if (filterService.isEmpty()) {
					log.debug("Sending message '{}'", metricDefinition.toString());
					producer.send(metricDefinition);
				} else {
					boolean matched = false;
					matched = filterService.isMatched(metricDefinition);

					if (matched) {
						producer.send(metricDefinition);
					} else {
						log.debug("metric did not match filter '{}'", metricDefinition);
					}
				}
			} catch (Exception e) {
				log.error("Failed to send message", e);
			} 
		}
		latch.countDown();

	}

}
