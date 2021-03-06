package net.savantly.metrics.carbonProxy.udp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.ip.udp.UnicastSendingMessageHandler;
import org.springframework.test.context.junit4.SpringRunner;

import net.savantly.metrics.carbonProxy.Application;
import net.savantly.metrics.carbonProxy.test.utils.CountdownInterceptor;
import net.savantly.metrics.carbonProxy.test.utils.UdpClient;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, UdpCarbonListenerTest.TestConfiguration.class})
public class UdpCarbonListenerTest {

	private final static Logger log = LoggerFactory.getLogger(UdpCarbonListenerTest.class);	
	private int threadTimeOut = 10;
	
	@Value("${carbonProxy.server-port}")
	int port;
	
	@MockBean(name="unicastSender")
	UnicastSendingMessageHandler handler;
	@MockBean(name="unicastAggregatorSender")
	UnicastSendingMessageHandler aggregatorHandler;
	private CountDownLatch latch;
	private Answer<Void> voidAnswer = new Answer<Void>(){
		@Override
		public Void answer(InvocationOnMock invocation) throws Throwable {
			latch.countDown();
			return null;
		}
	};
	
	
	@Before
	public void before(){
		// when the udp senders attempts to connect to the server
		// mock the send, and do a countdown on the latch
		Mockito.doAnswer(voidAnswer).when(handler).handleMessage(Mockito.any());
		Mockito.doAnswer(voidAnswer).when(aggregatorHandler).handleMessage(Mockito.any());
	}
	
	@Test
	public void testSingle() throws InterruptedException, IOException {
		latch = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.submit(()->{
			try {
				testOneConnection(1);
			} catch (Exception e) {
				log.error("",e);
			}
		});
		try {
		    log.debug("attempt to shutdown executor");
		    executor.shutdown();
		    executor.awaitTermination(threadTimeOut, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			log.debug("tasks interrupted");
			fail("took too long");
		}
		finally {
		    if (!executor.isTerminated()) {
		    	log.debug("cancel non-finished tasks");
		    }
		    executor.shutdownNow();
		    log.debug("shutdown finished");
		}
		latch.await(threadTimeOut, TimeUnit.SECONDS);
		//assertEquals("Latch count should be 0", 0, latch.getCount());
	}
	
	@Test
	public void testMultiLineMessage_heavyload() throws InterruptedException{
		
		int threadPoolSize = 10;
		int loopSize = 30;
		
		latch = new CountDownLatch(loopSize);
		
		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		for (int i = 0; i < loopSize; i++) {
			executor.submit(() -> {
				String threadName = Thread.currentThread().getName();
			    log.debug("Starting new connection test");
			    try {
					testOneConnection(Thread.currentThread().getId());
				} catch (Exception e) {
					log.error(threadName, e);
				}
			});
		}
		
		try {
		    log.debug("attempt to shutdown executor");
		    executor.shutdown();
		    executor.awaitTermination(threadTimeOut, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			log.error("tasks interrupted");
			fail("took too long");
		}
		finally {
		    if (!executor.isTerminated()) {
		    	log.debug("cancel non-finished tasks");
		    }
		    executor.shutdownNow();
		    log.debug("shutdown finished");
		    latch.await(threadTimeOut, TimeUnit.SECONDS);
		    assertEquals("Latch count should be 0", 0, latch.getCount());
		}

	}
	
	@Test
	public void testSingleXmlMetric() throws InterruptedException, IOException {
		latch = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.submit(()->{
			try {
				testOneConnectionWithXmlData(1);
			} catch (Exception e) {
				log.error("",e);
			}
		});
		try {
		    log.debug("attempt to shutdown executor");
		    executor.shutdown();
		    executor.awaitTermination(threadTimeOut, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			log.debug("tasks interrupted");
			fail("took too long");
		}
		finally {
		    if (!executor.isTerminated()) {
		    	log.debug("cancel non-finished tasks");
		    }
		    executor.shutdownNow();
		    log.debug("shutdown finished");
		}
		latch.await(threadTimeOut, TimeUnit.SECONDS);
		assertEquals("Latch count should be 0", 0, latch.getCount());
	}
	
	private void testOneConnectionWithXmlData(long id) throws IOException{
		UdpClient client = new UdpClient(port);
		Random r = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			String str = String.format("<metric type=\"IntCounter\" name=\"ABC|Server|%s|HOST:Timeout\" value=\"%s\" />\n", id, r.nextInt(10));
			sb.append(str);
		}
		String msg = sb.toString();
		client.sendMessage(msg);
		client.close();
	}
	
	private void testOneConnection(long id) throws IOException{
		UdpClient client = new UdpClient(port);
		Random r = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			String str = String.format("test.relay.udp-%s.count %s %s\n", id, r.nextInt(10), DateTime.now().getMillis()/1000-(i*30));
			sb.append(str);
		}
		String msg = sb.toString();
		client.sendMessage(msg);
		client.close();
	}
	
	@Configuration
	protected static class TestConfiguration {
		@Bean
		public CountdownInterceptor countDownInterceptor(){
			return new CountdownInterceptor(new CountDownLatch(1));
		}
	}

}
