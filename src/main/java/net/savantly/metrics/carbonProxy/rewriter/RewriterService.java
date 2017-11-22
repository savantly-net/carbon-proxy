package net.savantly.metrics.carbonProxy.rewriter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import net.savantly.metrics.carbonProxy.KeyValuePair;

@ConfigurationProperties("rewriter")
@Service
public class RewriterService {
	
	private final static Logger log = LoggerFactory.getLogger(RewriterService.class);
	private final Map<String, PatternPair> compiledPatterns = new HashMap<>();
	
	private Map<String, KeyValuePair> replacements = new HashMap<>();

	public Map<String, KeyValuePair> getReplacements() {
		return replacements;
	}
	public void setReplacements(Map<String, KeyValuePair> replacements) {
		this.replacements = replacements;
		compiledPatterns.clear();
		replacements.keySet().stream().forEach(p -> {
			log.info("compiling rewriter pattern: {} -> {}", replacements.get(p).getKey(), replacements.get(p).getValue());
			PatternPair pp = new PatternPair(Pattern.compile(replacements.get(p).getKey()), replacements.get(p).getValue());
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
