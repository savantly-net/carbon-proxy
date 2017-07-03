package net.savantly.metrics.carbonProxy.plainText;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.MessageChannel;

@MessageEndpoint("plainTextMessageEndpoint")
public class PlainTextMessageEndpoint {
	private static final Logger log = LoggerFactory.getLogger(PlainTextMessageEndpoint.class);
	private final CountDownLatch latch = new CountDownLatch(2);
	public CountDownLatch getLatch(){
		return latch;
	}
	
	@Bean("publisherChannel")
	public MessageChannel publisherChannel(){
		return MessageChannels.publishSubscribe().get();
	}
	
	@Bean("multiMetricOutputChannel")
	public MessageChannel multiMetricOutputChannel(){
		DirectChannel channel = MessageChannels.direct().get();
		return channel;
	}
	

	@Transformer(inputChannel = "byteArrayChannel", outputChannel = "multiMetricInputChannel")
	public String convertBytesToString(byte[] bytes) {
		String str = new String(bytes);
		log.debug(str);
		latch.countDown();
		return str;
	}
	

	
	@Splitter(inputChannel="multiMetricInputChannel", outputChannel="multiMetricOutputChannel")
	public String[] splitMultiMetricMessage(String multiMetricString){
		String[] messagePayloads = multiMetricString.split("\\n");
		if(messagePayloads.length > 1 && log.isDebugEnabled()){
			log.debug("split multimetric message '{}'", Arrays.toString(messagePayloads));
		}
		latch.countDown();
		return messagePayloads;
	}
	
	@Bean
	public IntegrationFlow plainTextIntegrationFlow(
			@Qualifier("multiMetricOutputChannel") MessageChannel multiMetricOutputChannel,
			@Qualifier("publisherChannel") MessageChannel publisherChannel) {
		return IntegrationFlows.from(multiMetricOutputChannel)
				.channel("publisherChannel")
				.get();
	}


	
/*	@Router(inputChannel="inboundRouterChannel")
	public String[] routePlaintextMessage(String payload, @Headers Map<String, ?> headers){
		if (headers.containsKey("ip_tcp_remotePort")){
			return Arrays.array("outboundTcpChannel", "inboundCarbonQueueChannel");
		} else return Arrays.array("inboundCarbonQueueChannel");
	}*/

}
