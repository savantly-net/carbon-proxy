package net.savantly.metrics.carbonProxy;

import java.util.Map;

import javax.validation.constraints.Min;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.MetricsEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.Scheduled;

import net.savantly.metrics.carbonProxy.test.MetricFactory;

@Configuration
@ConfigurationProperties("carbon.performance")
public class PerformanceMonitoringConfiguration {
	private final Logger log = LoggerFactory.getLogger(PerformanceMonitoringConfiguration.class);
	private boolean monitoring = true;
	private String prefix = "";
	private boolean test = false;
	// TODO: allow metric exclusion 
	//private String[] exclude = new String[0];
	
	@Min(value=1000, message="the rate should be at least 1 second")
	private int frequencyMs = 60000;
	
	private QueueChannel carbonQueue;
	private MetricsEndpoint metricsEndpoint;
	private MessageChannel multiMetricInputChannel;
	
	@Autowired
	public PerformanceMonitoringConfiguration(
			@Qualifier("carbonQueue") QueueChannel carbonQueue,
			MetricsEndpoint metricsEndpoint,
			@Qualifier("multiMetricInputChannel") MessageChannel multiMetricInputChannel) {
		this.carbonQueue = carbonQueue;
		this.metricsEndpoint = metricsEndpoint;
		this.multiMetricInputChannel = multiMetricInputChannel;
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
	
	@ConditionalOnProperty("carbon.performance.test")
	@Scheduled(fixedRateString="5000")
	public void loadAndPerformanceTest() {
		String metrics = MetricFactory.mixedMetrics(200);
		this.multiMetricInputChannel.send(new GenericMessage<String>(metrics));
	}
	

	public boolean isMonitoring() {
		return monitoring;
	}

	public void setMonitoring(boolean monitoring) {
		this.monitoring = monitoring;
	}

	public boolean isTest() {
		return test;
	}

	public void setTest(boolean test) {
		this.test = test;
	}
}
