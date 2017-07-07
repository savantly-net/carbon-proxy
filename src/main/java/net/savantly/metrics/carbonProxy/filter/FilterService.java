package net.savantly.metrics.carbonProxy.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class FilterService<T> {

	protected final Map<String, MetricFilter> filters;
	protected final Map<String, Pattern> regexPatterns = new HashMap<>();
	
	public FilterService(Map<String, MetricFilter> filters) {
		// Precompile all the regex patterns
		this.filters = filters;
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

	/**
	 * Returns true when one of the filters matches @param metric
	 * @param metric
	 * @return
	 */
	public abstract boolean isMatched(T metric);
	
}
