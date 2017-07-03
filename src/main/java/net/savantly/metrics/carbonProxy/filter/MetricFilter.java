package net.savantly.metrics.carbonProxy.filter;

public class MetricFilter {
	private MetricAttribute attribute;
	private MetricFilterType filter;
	private String value;
	
	public MetricAttribute getAttribute() {
		return attribute;
	}
	public void setAttribute(MetricAttribute attribute) {
		this.attribute = attribute;
	}
	public MetricFilterType getFilter() {
		return filter;
	}
	public void setFilter(MetricFilterType filter) {
		this.filter = filter;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
