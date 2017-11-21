package net.savantly.metrics.carbonProxy.test;

import java.util.Random;

import org.joda.time.DateTime;

public class MetricFactory {
	
	/**
	 * creates a metric with the given id and value - with a epoch of 15sec before now.
	 * @param id
	 * @param value
	 * @return
	 */
	public static String formatGoodMetric(int id, float value) {
		return String.format("test.relay.udp-%s.count %s %s\n", id, value, (DateTime.now().getMillis()/1000)-15);
	}
	
	public static String formatMetricWithMissingValue(int id) {
		return String.format("test.relay.udp-%s.count %s\n", id, (DateTime.now().getMillis()/1000)-15);
	}
	
	public static String formatMetricWithMissingEpoch(int id, float value) {
		return String.format("test.relay.udp-%s.count %s\n", id, value);
	}
	
	public static String goodMetric() {
		Random r = new Random();
		return formatGoodMetric(r.nextInt(10), r.nextFloat());
	}
	
	public static String metricWithoutValue() {
		Random r = new Random();
		return formatMetricWithMissingValue(r.nextInt(10));
	}
	
	public static String goodMetrics(int count) {
		Random r = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			sb.append(formatGoodMetric(r.nextInt(10), r.nextFloat()));
		}
		String msg = sb.toString();
		return msg;
	}
	
	public static String badMetrics(int count) {
		Random r = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			// alternate different bad types
			if(i%2 == 0) {
				sb.append(formatMetricWithMissingEpoch(r.nextInt(10), r.nextFloat()));
			} else {
				sb.append(formatMetricWithMissingValue(r.nextInt(10)));
			}
		}
		String msg = sb.toString();
		return msg;
	}
	
	/**
	 * 33% are bad with no value
	 * 33% are bad with no epoch
	 * 33% are good
	 * @param count
	 * @return
	 */
	public static String mixedMetrics(int count) {
		Random r = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			// alternate different metric types
			if(i%3 == 0) {
				sb.append(formatGoodMetric(r.nextInt(10), r.nextFloat()));
			} else if(i%2 == 0) {
				sb.append(formatMetricWithMissingEpoch(r.nextInt(10), r.nextFloat()));
			} else {
				sb.append(formatMetricWithMissingValue(r.nextInt(10)));
			}
		}
		String msg = sb.toString();
		return msg;
	}

}
