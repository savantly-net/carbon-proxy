package net.savantly.metrics.carbonProxy.ca;

import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTime;

@XmlRootElement(name="metric")
public class CaMetric {

	private String name;
	private float value;
	private long epoch = DateTime.now().getMillis()/1000;
	

	private final Pattern dotPattern = Pattern.compile("\\||\\:");

	@Override
	public String toString() {
		return String.format("%s %s %s", cleanName(name), value, epoch);
	}
	
	private String cleanName(String metricName) {
		String[] agentNameParts = metricName.split("\\|");
		String cleaned = dotPattern.matcher(metricName).replaceAll(".").replaceAll(" |\\(|\\)", "");
		//return String.format("%s.EAI.%s.%s", agentNameParts[0], cleaned);
		return cleaned;
	}

	@XmlAttribute(required=true, name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute(required=true, name="value")
	public float getValue() {
		return value;
	}
	public void setValue(float value) {
		this.value = value;
	}
	
	public long getEpoch() {
		return epoch;
	}
	public void setEpoch(long epoch) {
		this.epoch = epoch;
	}
}
