package net.savantly.metrics.carbonProxy.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import net.savantly.metrics.carbonProxy.filter.MetricFilter;
import net.savantly.metrics.carbonProxy.filter.MetricFilterType;
import net.savantly.metrics.carbonProxy.schema.MetricDefinition;

public class KafkaMetricProducerMessageHandler implements MessageHandler {
	private static final Logger log = LoggerFactory.getLogger(KafkaMetricProducerMessageHandler.class);

	private KafkaMetricProducer producer;
	private KafkaMetricProducerConfiguration producerConfiguration;

	private Map<String, MetricFilter> filters;
	private Map<String, Pattern> regexPatterns = new HashMap<>();

	public KafkaMetricProducerMessageHandler(KafkaMetricProducer producer, KafkaMetricProducerConfiguration producerConfiguration) {
		this.producer = producer;
		this.producerConfiguration = producerConfiguration;
		
		// Precompile all the regex patterns
		this.filters = this.producerConfiguration.getFilters();
		for (String key : filters.keySet()) {
			MetricFilter f = filters.get(key);
			if(f.getFilter().equals(MetricFilterType.regex)){
				regexPatterns.put(key, Pattern.compile(f.getValue()));
			}
		}
	}

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		MetricDefinition metricDefinition = (MetricDefinition) message.getPayload();
		log.debug("Sending message '{}'", metricDefinition.toString());
		try {
			if (filters.isEmpty()) {
				producer.send(metricDefinition);
			} else {
				boolean matched = false;
				for (String filter : filters.keySet()) {
					matched = checkForFilter(filters.get(filter), metricDefinition);
				}
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

	private boolean checkForFilter(MetricFilter metricFilter, MetricDefinition metricDefinition) {
		switch (metricFilter.getFilter()) {
		case regex:
			if(applyRegexFilter(metricFilter, metricDefinition))
				return true;
		case substring:
			if(applySubstringFilter(metricFilter, metricDefinition))
				return true;
		default:
			break;
		}
		return false;
	}

	private boolean applySubstringFilter(MetricFilter metricFilter, MetricDefinition metricDefinition) {
		switch (metricFilter.getAttribute()) {
		case metric:
			if (metricDefinition.getMetric().contains(metricFilter.getValue())) {
				return true;
			}
		case name:
			if (metricDefinition.getName().contains(metricFilter.getValue())) {
				return true;
			}
		default:
			break;
		}
		return false;
	}

	private boolean applyRegexFilter(MetricFilter metricFilter, MetricDefinition metricDefinition) {
		switch (metricFilter.getAttribute()) {
		case metric:
			for (String key : filters.keySet()) {
				if(regexPatterns.get(key).matcher(metricDefinition.getMetric()).matches())
					return true;
			}
		case name:
			for (String key : filters.keySet()) {
				if(regexPatterns.get(key).matcher(metricDefinition.getName()).matches())
					return true;
			}
		default:
			break;
		}
		return false;
	}

}
