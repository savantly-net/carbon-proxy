package net.savantly.metrics.carbonProxy.schema;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricDefinition {
	private final static Logger log = LoggerFactory.getLogger(MetricDefinition.class);
	
	private String id;
	private String name;
	private String metric;
	private int interval;
	private String unit;
	private String mtype;
	private String[] tags;
	private long lastUpdate;
	private int partition;
	private double value;
	
	public enum Style {
		Metric_1_0,
		Metric_2_0
	}
	
	
	// Constructors
	public MetricDefinition() {}
	public MetricDefinition(String str, Style style) {
		switch (style) {
		case Metric_1_0:
			MetricDefinition.fromMetric_1_0(str, this);
			break;

		default:
			throw new RuntimeException(String.format("Style not implemented yet: %s", style));
		}
	}
	
	// Factory
	public static MetricDefinition Factory(String str, Style style){
		return new MetricDefinition(str, style);
	}
	
	// Private methods
	private static MetricDefinition fromMetric_1_0(String str, MetricDefinition metricDefinition){
		int fieldCount = 3;
		String[] parts = str.split("\\s");
		if(parts.length < fieldCount){
			if(log.isDebugEnabled()){
				throw new RuntimeException(String.format("Metric string is missing %s fields: %s", fieldCount - parts.length, str));
			} else {
				return null;
			}
		}

		metricDefinition.setValue(Double.parseDouble(parts[1]));
		metricDefinition.setLastUpdate(Long.parseLong(parts[2]));
		metricDefinition.setName(parts[0]);
		metricDefinition.setMetric(str);
		
		try {
			metricDefinition.setId(metricDefinition.createHash());
		} catch (NoSuchAlgorithmException e) {
			log.error("Failed to create hash for id", e);
		}
		
		return metricDefinition;
	}
	
	private String createHash() throws NoSuchAlgorithmException{
		MessageDigest md = MessageDigest.getInstance("MD5");
	    md.update(this.name.getBytes());
	    md.update(this.metric.getBytes());
	    md.update(String.format("%s", this.lastUpdate).getBytes());
	    byte[] digest = md.digest();
	    String myHash = DatatypeConverter
	      .printHexBinary(digest).toUpperCase();
	    return myHash;
	}
	
	// Getters/Setters

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getMtype() {
		return mtype;
	}

	public void setMtype(String mtype) {
		this.mtype = mtype;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public int getPartition() {
		return partition;
	}

	public void setPartition(int partition) {
		this.partition = partition;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "MetricDefinition [id=" + id + ", name=" + name + ", metric=" + metric + ", interval=" + interval
				+ ", unit=" + unit + ", mtype=" + mtype + ", tags=" + Arrays.toString(tags) + ", lastUpdate="
				+ lastUpdate + ", partition=" + partition + ", value=" + value + "]";
	}

	
}
