package net.savantly.metrics.carbonProxy.rewriter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.savantly.metrics.carbonProxy.KeyValuePair;

public class RewriterService {
	
	private final static Logger log = LoggerFactory.getLogger(RewriterService.class);

	private Map<String, KeyValuePair> patterns = new HashMap<>();
	private final Map<Pattern, String> compiledPatterns = new HashMap<>();

	public Map<String, KeyValuePair> getPatterns() {
		return patterns;
	}

	public void setPatterns(Map<String, KeyValuePair> patterns) {
		this.patterns = patterns;
		compiledPatterns.clear();
		patterns.values().stream().forEach(p -> {
			log.info("compiling rewriter pattern: {} -> {}", p.getKey(), p.getValue());
			compiledPatterns.put(Pattern.compile(p.getKey()), p.getValue());
		});
	}
	
	public String[] rewrite(String[] metricString) {
		return Arrays.stream(metricString).map(s -> {
			return processPatterns(s);
		}).toArray(String[]::new);
	}
	
	public String rewrite(String metricString) {
		return processPatterns(metricString);
	}

	private String processPatterns(String s) {
		for (Pattern pattern : compiledPatterns.keySet()) {
			// If a pattern matches, then apply it and return
			if (pattern.matcher(s).matches()) {
				log.trace("replacing matched pattern: {}", pattern);
				String changed = pattern.matcher(s).replaceFirst(compiledPatterns.get(pattern));
				log.trace("new value: {}", changed);
				return changed;
			}
		}
		return s;
	}
}
