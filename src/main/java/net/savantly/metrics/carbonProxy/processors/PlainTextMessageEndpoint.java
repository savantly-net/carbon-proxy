package net.savantly.metrics.carbonProxy.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Transformer;

@MessageEndpoint("plainTextMessageEndpoint")
public class PlainTextMessageEndpoint {
	private static final Logger log = LoggerFactory.getLogger(PlainTextMessageEndpoint.class);

	@Transformer(inputChannel = "byteArrayChannel", outputChannel = "carbonQueue")
	public String convertBytesToString(byte[] bytes) {
		String str = new String(bytes);
		log.debug(str);
		return str;
	}
	
/*	@Router(inputChannel="inboundRouterChannel")
	public String[] routePlaintextMessage(String payload, @Headers Map<String, ?> headers){
		if (headers.containsKey("ip_tcp_remotePort")){
			return Arrays.array("outboundTcpChannel", "inboundCarbonQueueChannel");
		} else return Arrays.array("inboundCarbonQueueChannel");
	}*/

}
