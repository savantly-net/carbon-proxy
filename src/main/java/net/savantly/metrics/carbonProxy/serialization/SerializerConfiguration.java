package net.savantly.metrics.carbonProxy.serialization;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.ip.tcp.serializer.ByteArrayRawSerializer;

@Configuration
public class SerializerConfiguration {

	@Bean({"defaultSerializer", "defaultDeSerializer"})
	public ByteArrayRawSerializer getDefaultSerializer(){
		return new ByteArrayRawSerializer();
	}
}
