package net.savantly.metrics.carbonProxy.filter;

import java.util.Map;

import net.savantly.metrics.carbonProxy.schema.Metric;

public class MetricFilterService extends FilterService<Metric>{

	public MetricFilterService(Map<String, MetricFilter> filters) {
		super(filters);
	}
	
	public boolean isMatched(Metric metric) {
		if(this.isEmpty()) return true;
		boolean matched = false;
		for (String filter : filters.keySet()) {
			matched = checkForFilter(filters.get(filter), metric);
		}
		return matched;
	}
	
	boolean checkForFilter(MetricFilter metricFilter, Metric metric) {
		switch (metricFilter.getFilter()) {
		case regex:
			if(applyRegexFilter(metricFilter, metric))
				return true;
		case substring:
			if(applySubstringFilter(metricFilter, metric))
				return true;
		default:
			break;
		}
		return false;
	}

	private boolean applySubstringFilter(MetricFilter metricFilter, Metric metric) {
		switch (metricFilter.getAttribute()) {
		case metric:
			if (metric.getMetric().contains(metricFilter.getValue())) {
				return true;
			}
		case name:
			if (metric.getName().contains(metricFilter.getValue())) {
				return true;
			}
		default:
			break;
		}
		return false;
	}

	private boolean applyRegexFilter(MetricFilter metricFilter, Metric metric) {
		switch (metricFilter.getAttribute()) {
		case metric:
			for (String key : filters.keySet()) {
				if(regexPatterns.get(key).matcher(metric.getMetric()).matches())
					return true;
			}
		case name:
			for (String key : filters.keySet()) {
				if(regexPatterns.get(key).matcher(metric.getName()).matches())
					return true;
			}
		default:
			break;
		}
		return false;
	}

}
