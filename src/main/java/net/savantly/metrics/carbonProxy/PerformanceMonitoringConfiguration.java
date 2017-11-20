package net.savantly.metrics.carbonProxy;

import java.util.Map;

import javax.validation.constraints.Min;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.MetricsEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@ConfigurationProperties("carbon.performance")
public class PerformanceMonitoringConfiguration {
	private final Logger log = LoggerFactory.getLogger(PerformanceMonitoringConfiguration.class);
	private boolean monitoring = true;
	private String prefix = "";
	// TODO: allow metric exclusion 
	//private String[] exclude = new String[0];
	
	@Min(value=1000, message="the rate should be at least 1 second")
	private int frequencyMs = 60000;
	
	private QueueChannel carbonQueue;
	private MetricsEndpoint metricsEndpoint;
	
	@Autowired
	public PerformanceMonitoringConfiguration(
			@Qualifier("carbonQueue") QueueChannel carbonQueue,
			MetricsEndpoint metricsEndpoint) {
		this.carbonQueue = carbonQueue;
		this.metricsEndpoint = metricsEndpoint;
	}
	
	@Scheduled(fixedRateString="${carbon.performance.frequencyMs:60000}")
	public void sendStatistics() {
		long epoch = DateTime.now().getMillis()/1000;
		Map<String, Object> response = metricsEndpoint.invoke();
		response.keySet().stream().forEach((k) -> {
			String metricString = String.format("%scarbon-proxy.%s %s %s\n", this.prefix, k, response.get(k), epoch);
			log.debug("sending performance metrics: {}", metricString);
			this.carbonQueue.send(new GenericMessage<String>(metricString));
		});
	}

	public boolean isMonitoring() {
		return monitoring;
	}

	public void setMonitoring(boolean monitoring) {
		this.monitoring = monitoring;
	}
}
