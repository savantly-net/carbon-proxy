package net.savantly.metrics.carbonProxy;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("carbonProxy")
public class ApplicationConfiguration {

	
	private int serverPort;
	private String serverAddress;
	private int carbonPort;
	private String carbonHost;
	
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

}
