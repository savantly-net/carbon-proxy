package net.savantly.metrics.carbonProxy.kafka;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.util.concurrent.ListenableFuture;

import net.savantly.metrics.carbonProxy.filter.FilterService;
import net.savantly.metrics.schema.MetricDefinition;

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
		if (message == null || message.getPayload() == null) {
			return;
		}
		MetricDefinition metricDefinition = (MetricDefinition) message.getPayload();
		ListenableFuture<SendResult<String, MetricDefinition>> sendresult = null;
		
		// If the message isn't valid then don't send it
		if(metricDefinition == null || metricDefinition.getId() == null){
			log.debug("skipping invalid metric '{}'", metricDefinition);
		} else {
			try {
				if (filterService.isEmpty()) {
					log.debug("Sending message '{}'", metricDefinition.toString());
					sendresult = producer.send(metricDefinition);
				} else {
					boolean matched = false;
					matched = filterService.isMatched(metricDefinition);

					if (matched) {
						log.debug("Sending matched message '{}'", metricDefinition.toString());
						sendresult =producer.send(metricDefinition);
					} else {
						log.debug("metric did not match filter '{}'", metricDefinition);
					}
				}
				if(sendresult != null){
					SendResult<String, MetricDefinition> response = sendresult.get();
					log.debug("{}",response.getRecordMetadata());
				}
				
			} catch (Exception e) {
				log.error("Failed to send message", e);
			} finally {
				latch.countDown();
			}
		}
	}

}
