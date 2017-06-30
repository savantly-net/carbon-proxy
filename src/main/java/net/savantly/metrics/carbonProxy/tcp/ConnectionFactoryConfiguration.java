package net.savantly.metrics.carbonProxy.tcp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;

import net.savantly.metrics.carbonProxy.ApplicationConfiguration;

@Configuration
public class ConnectionFactoryConfiguration {
	
	@Autowired
	ApplicationConfiguration config;
	@Autowired
	Serializer serializer;
	@Autowired
	Deserializer deSerializer;
	
	@Bean("serverConnectionFactory")
	public AbstractServerConnectionFactory getTcpServerConnectionFactory(){
		TcpNetServerConnectionFactory connectionFactory = new TcpNetServerConnectionFactory(config.getServerPort());
		connectionFactory.setLocalAddress(config.getServerAddress());
		connectionFactory.setSingleUse(true);
		connectionFactory.setSerializer(serializer);
		connectionFactory.setDeserializer(deSerializer);
		return connectionFactory;
	}
	
	
	@Bean("carbonTcpConnectionFactory")
	public AbstractClientConnectionFactory clientCF() {
		TcpNetClientConnectionFactory connectionFactory = new TcpNetClientConnectionFactory(config.getCarbonHost(), config.getCarbonPort());
		connectionFactory.setSingleUse(false);
		return connectionFactory;
	}
	
}
