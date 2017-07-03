package net.savantly.metrics.carbonProxy;

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
	private int carbonPort;
	private String carbonHost;
	private int pollingFrequency;
	

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

}
