package net.savantly.metrics.carbonProxy;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;

@Configuration
@ConfigurationProperties("carbonProxy")
public class ApplicationConfiguration {

	
	private int serverPort;
	private String serverAddress;
	private int carbonPort = 2003;
	private String carbonHost = "localhost";
	private int pollingFrequency;
	private int carbonAggregatorPort = 2023;
	private List<String> aggregatorMatches = new ArrayList<String>();
	

	@Bean(name = PollerMetadata.DEFAULT_POLLER)
	public PollerMetadata poller() {
		return Pollers.fixedRate(pollingFrequency).get();
	}
	
	
	@Bean("singleMetricInputChannel")
	public MessageChannel singleMetricInputChannel(){
		PublishSubscribeChannel channel = MessageChannels.publishSubscribe().get();
		return channel;
	}
	
	@Bean("multiMetricInputChannel")
	public MessageChannel multiMetricInputChannel(){
		PublishSubscribeChannel channel = MessageChannels.publishSubscribe().get();
		return channel;
	}
	
	public int getServerPort() {
		return serverPort;
	}
	public void setServerPort(int port) {
		this.serverPort = port;
	}
	public String getServerAddress() {
		return serverAddress;
	}
	public void setServerAddress(String address) {
		this.serverAddress = address;
	}
	public int getCarbonPort() {
		return carbonPort;
	}
	public void setCarbonPort(int carbonPort) {
		this.carbonPort = carbonPort;
	}
	public String getCarbonHost() {
		return carbonHost;
	}
	public void setCarbonHost(String carbonHost) {
		this.carbonHost = carbonHost;
	}
	
	public int getPollingFrequency() {
		return pollingFrequency;
	}
	public void setPollingFrequency(int pollingFrequency) {
		this.pollingFrequency = pollingFrequency;
	}


	public int getCarbonAggregatorPort() {
		return carbonAggregatorPort;
	}


	public void setCarbonAggregatorPort(int carbonAggregatorPort) {
		this.carbonAggregatorPort = carbonAggregatorPort;
	}


	public List<String> getAggregatorMatches() {
		return aggregatorMatches;
	}


	public void setAggregatorMatches(List<String> aggregatorMatches) {
		this.aggregatorMatches = aggregatorMatches;
	}

}
