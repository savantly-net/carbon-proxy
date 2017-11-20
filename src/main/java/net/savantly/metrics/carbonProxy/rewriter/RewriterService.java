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
	private final Map<String, PatternPair> compiledPatterns = new HashMap<>();

	private Map<String, KeyValuePair> patterns = new HashMap<>();
	public Map<String, KeyValuePair> getPatterns() {
		return patterns;
	}
	public void setPatterns(Map<String, KeyValuePair> patterns) {
		this.patterns = patterns;
		compiledPatterns.clear();
		patterns.keySet().stream().forEach(p -> {
			log.info("compiling rewriter pattern: {} -> {}", patterns.get(p).getKey(), patterns.get(p).getValue());
			PatternPair pp = new PatternPair(Pattern.compile(patterns.get(p).getKey()), patterns.get(p).getValue());
			compiledPatterns.put(p, pp);
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
		for (String patternKey : compiledPatterns.keySet()) {
			// If a pattern matches, then apply it and return
			Pattern pattern = compiledPatterns.get(patternKey).pattern;
			String replacement = compiledPatterns.get(patternKey).replacement;
			if (pattern.matcher(s).matches()) {
				log.trace("replacing matched pattern [{}]: {}", patternKey, pattern);
				String changed = pattern.matcher(s).replaceFirst(replacement);
				log.trace("new value: {}", changed);
				return changed;
			}
		}
		return s;
	}
	
	private class PatternPair {
		Pattern pattern;
		String replacement;
		public PatternPair(Pattern pattern, String replacement) {
			this.pattern = pattern;
			this.replacement = replacement;
		}
	}
}
