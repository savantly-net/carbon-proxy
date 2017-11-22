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
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.annotation.Transformer;

import net.savantly.metrics.carbonProxy.AppChannels;
import net.savantly.metrics.carbonProxy.ca.CaMetric;
import net.savantly.metrics.carbonProxy.rewriter.RewriterService;

@MessageEndpoint("plainTextMessageEndpoint")
public class PlainTextMessageEndpoint {
	
	private static final Logger log = LoggerFactory.getLogger(PlainTextMessageEndpoint.class);
	private JAXBContext jaxbContext;
	private Unmarshaller unmarshaller;
	private RewriterService rewriter;
	
	private CountDownLatch latch = new CountDownLatch(1);
	public CountDownLatch getLatch(){
		return latch;
	}
	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}
	
	@Autowired
	public PlainTextMessageEndpoint(RewriterService rewriter) throws JAXBException {
		this.jaxbContext = JAXBContext.newInstance(CaMetric.class);
		this.unmarshaller = jaxbContext.createUnmarshaller();
		this.rewriter = rewriter;
	}

	@Transformer(inputChannel = AppChannels.PRE_PROCESSOR_QUEUE_CHANNEL, outputChannel = AppChannels.MULTI_METRIC_INPUT_CHANNEL)
	public String convertBytesToString(byte[] bytes) {
		String str = new String(bytes);
		log.debug(str);
		latch.countDown();
		return str;
	}
	

	
	@Splitter(inputChannel=AppChannels.MULTI_METRIC_INPUT_CHANNEL, outputChannel=AppChannels.SINGLE_METRIC_INPUT_CHANNEL)
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
		if (metricString != null) {
			String[] parts = metricString.split(" ");
			if (parts.length == 3) {
				try {
					Float.parseFloat(parts[1]);
					return metricString;
				} catch (Exception ex) {
					log.debug("dropping invalid metric value: {}", metricString);
					return null;
				}
			}
		}
		return null;
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
		try (StringReader sr = new StringReader(s)){
			CaMetric response = (CaMetric) unmarshaller.unmarshal(sr);
			sr.close();
			return response.toString();
		} catch (Exception e) {
			return null;
		}
		
	}
}
