package net.savantly.metrics.carbonProxy.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.savantly.metrics.carbonProxy.kafka.KafkaMetricProducerConfiguration;
import net.savantly.metrics.carbonProxy.schema.MetricDefinition;

@Service
public class FilterService {

	private final KafkaMetricProducerConfiguration producerConfiguration;
	private final Map<String, MetricFilter> filters;
	private final Map<String, Pattern> regexPatterns = new HashMap<>();
	
	@Autowired
	public FilterService(KafkaMetricProducerConfiguration producerConfiguration) {
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

	public boolean isEmpty() {
		return filters.isEmpty();
	}
	
	public boolean isMatched(MetricDefinition metricDefinition) {
		boolean matched = false;
		for (String filter : filters.keySet()) {
			matched = checkForFilter(filters.get(filter), metricDefinition);
		}
		return matched;
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
