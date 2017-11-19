package net.savantly.metrics.carbonProxy.plainText;

import java.io.StringReader;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.MessageChannel;

import net.savantly.metrics.carbonProxy.ca.CaMetric;
import net.savantly.metrics.carbonProxy.rewriter.RewriterService;

@MessageEndpoint("plainTextMessageEndpoint")
public class PlainTextMessageEndpoint {
	private static final Logger log = LoggerFactory.getLogger(PlainTextMessageEndpoint.class);
	JAXBContext jaxbContext;
	Unmarshaller unmarshaller;
	
	@Autowired
	RewriterService rewriter;
	
	private CountDownLatch latch = new CountDownLatch(1);
	public CountDownLatch getLatch(){
		return latch;
	}
	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}
	
	@Value("${preProcessorQueueChannel.size}")
	int preProcessorQueueChannelSize;
	
	public PlainTextMessageEndpoint() throws JAXBException {
		jaxbContext = JAXBContext.newInstance(CaMetric.class);
		unmarshaller = jaxbContext.createUnmarshaller();
	}
	
	@Bean("preProcessorQueueChannel")
	public MessageChannel preProcessorQueueChannel(){
		return MessageChannels.queue(preProcessorQueueChannelSize).get();
	}


	@Transformer(inputChannel = "preProcessorQueueChannel", outputChannel = "multiMetricInputChannel")
	public String convertBytesToString(byte[] bytes) {
		String str = new String(bytes);
		log.debug(str);
		latch.countDown();
		return str;
	}
	

	
	@Splitter(inputChannel="multiMetricInputChannel", outputChannel="singleMetricInputChannel")
	public String[] splitMultiMetricMessage(String multiMetricString){
		String[] messagePayloads = multiMetricString.split("\\n");
		if(messagePayloads.length > 1 && log.isDebugEnabled()){
			log.debug("split multimetric message '{}'", Arrays.toString(messagePayloads));
		}
		String[] payload = Arrays.stream(messagePayloads).map((s) -> {
			return dropInvalidValues(doReplacement(ensureFormat(s)));
		})
		// only forward the non-null items
		.filter(s -> s != null)
		.toArray(String[]::new);
		
		latch.countDown();
		return payload;
	}
	private String dropInvalidValues(String metricString) {
		if (metricString != null && metricString.split(" ").length == 3) {
			return metricString;
		} else {
			return null;
		}
	}
	private String doReplacement(String s) {
		return rewriter.rewrite(s);
	}
	private String ensureFormat(String s) {
		boolean isXml = s.startsWith("<");
		if(isXml) {
			try {
				return convertXmlMetricToCarbonStyle(s);
			} catch (JAXBException e) {
				log.error("failed to convert xml metric: {}", e);
				return null;
			}
		} else {
			return s;
		}
	}
	private String convertXmlMetricToCarbonStyle(String s) throws JAXBException {
		StringReader sr = new StringReader(s);
		CaMetric response = (CaMetric) unmarshaller.unmarshal(sr);
		return response.toString();
	}

	
/*	@Bean
	public IntegrationFlow plainTextIntegrationFlow(
			@Qualifier("multiMetricOutputChannel") MessageChannel multiMetricOutputChannel,
			@Qualifier("publisherChannel") MessageChannel publisherChannel) {
		return IntegrationFlows.from(multiMetricOutputChannel)
				.channel("publisherChannel")
				.get();
	}*/


	
/*	@Router(inputChannel="inboundRouterChannel")
	public String[] routePlaintextMessage(String payload, @Headers Map<String, ?> headers){
		if (headers.containsKey("ip_tcp_remotePort")){
			return Arrays.array("outboundTcpChannel", "inboundCarbonQueueChannel");
		} else return Arrays.array("inboundCarbonQueueChannel");
	}*/

}
