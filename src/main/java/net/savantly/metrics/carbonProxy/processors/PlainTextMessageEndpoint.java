package net.savantly.metrics.carbonProxy.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.MessageChannel;

@MessageEndpoint("plainTextMessageEndpoint")
public class PlainTextMessageEndpoint {
	private static final Logger log = LoggerFactory.getLogger(PlainTextMessageEndpoint.class);

	@Transformer(inputChannel = "byteArrayChannel", outputChannel = "publisherChannel")
	public String convertBytesToString(byte[] bytes) {
		String str = new String(bytes);
		log.debug(str);
		return str;
	}
	
	@Bean("publisherChannel")
	public MessageChannel publisherChannel(){
		return MessageChannels.publishSubscribe().get();
	}
	
/*	@Router(inputChannel="inboundRouterChannel")
	public String[] routePlaintextMessage(String payload, @Headers Map<String, ?> headers){
		if (headers.containsKey("ip_tcp_remotePort")){
			return Arrays.array("outboundTcpChannel", "inboundCarbonQueueChannel");
		} else return Arrays.array("inboundCarbonQueueChannel");
	}*/

}
