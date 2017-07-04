package net.savantly.metrics.carbonProxy.filter;

import java.util.Map;

public class StringFilterService extends FilterService<String>{

	public StringFilterService(Map<String, MetricFilter> filters) {
		super(filters);
	}

	@Override
	public boolean isMatched(String metric) {
		if(this.isEmpty()) return true;
		boolean matched = false;
		for (String filter : filters.keySet()) {
			matched = checkForFilter(filters.get(filter), metric);
		}
		return matched;
	}
	

	boolean checkForFilter(MetricFilter metricFilter, String metric) {
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

	private boolean applySubstringFilter(MetricFilter metricFilter, String metric) {
		if (metric.contains(metricFilter.getValue())) {
			return true;
		}
		return false;
	}

	private boolean applyRegexFilter(MetricFilter metricFilter, String metric) {
		for (String key : filters.keySet()) {
			if(regexPatterns.get(key).matcher(metric).matches())
				return true;
		}
		return false;
	}

}
