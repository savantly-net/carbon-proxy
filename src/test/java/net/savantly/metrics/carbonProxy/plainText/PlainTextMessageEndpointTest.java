package net.savantly.metrics.carbonProxy.plainText;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import net.savantly.metrics.carbonProxy.rewriter.RewriterService;
import net.savantly.metrics.carbonProxy.test.MetricFactory;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PlainTextMessageEndpointTest.TestConfiguration.class})
public class PlainTextMessageEndpointTest {

	private final static Logger log = LoggerFactory.getLogger(PlainTextMessageEndpointTest.class);	
	
	@Autowired
	PlainTextMessageEndpoint endpoint;
	
	@Test
	public void testWithAllGoodMetrics() throws InterruptedException, IOException {
		int count = 100;
		String[] result = endpoint.splitMultiMetricMessage(MetricFactory.goodMetrics(count));
		assertEquals("should be this many lines:", count, result.length);
	}
	
	@Test
	public void testWithAllBadValues() throws InterruptedException, IOException {
		int count = 100;
		String[] result = endpoint.splitMultiMetricMessage(MetricFactory.badMetrics(count));
		assertEquals("should be this many lines:", 0, result.length);
	}
	
	@Test
	public void testWithMixedValues() throws InterruptedException, IOException {
		int count = 99;
		String[] result = endpoint.splitMultiMetricMessage(MetricFactory.mixedMetrics(count));
		assertEquals("should be this many lines:", count/3, result.length);
	}

	
	@Configuration
	protected static class TestConfiguration {
		@Bean
		public PlainTextMessageEndpoint plainTextMessageEndpoint(RewriterService rewriter) throws JAXBException{
			return new PlainTextMessageEndpoint(rewriter);
		}
		@Bean
		public RewriterService rewriterService() {
			return new RewriterService();
		}
	}
}
