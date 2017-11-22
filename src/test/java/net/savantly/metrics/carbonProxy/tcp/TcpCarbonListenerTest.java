package net.savantly.metrics.carbonProxy.tcp;

import static org.junit.Assert.fail;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.integration.ip.udp.UnicastSendingMessageHandler;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.savantly.metrics.carbonProxy.Application;
import net.savantly.metrics.carbonProxy.ApplicationConfiguration;
import net.savantly.metrics.carbonProxy.test.MetricFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@DirtiesContext(methodMode=MethodMode.AFTER_METHOD)
public class TcpCarbonListenerTest {

	private final static Logger log = LoggerFactory.getLogger(TcpCarbonListenerTest.class);
	private Random random = new Random();
	private int threadTimeOut = 10;

	@Autowired
	ApplicationConfiguration carbonProxy;
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
	public void testPlainText() throws IOException, InterruptedException {
		latch = new CountDownLatch(1);
		long id = 1;
		testOneConnection(id, MetricFactory.goodMetric());
		latch.await(threadTimeOut, TimeUnit.SECONDS);
	}
	
	@Test
	public void testPayloadWithMissingField() throws IOException, InterruptedException {
		latch = new CountDownLatch(1);
		long id = 2;
		testOneConnection(id, MetricFactory.badMetrics(1));
		latch.await(threadTimeOut, TimeUnit.SECONDS);
	}
	
	@Test
	public void testMultiMetricLinesPayload() throws IOException, InterruptedException {
		latch = new CountDownLatch(1);
		long id = 3;
		testOneConnection(id, MetricFactory.goodMetrics(3));
		latch.await(threadTimeOut, TimeUnit.SECONDS);
	}
	
	@Test
	public void testLoadAndPerformance() throws InterruptedException{
		
		int threadPoolSize = 10;
		int loopSize = 10;
		int metricsPerMessage = 9;
		
		latch = new CountDownLatch(loopSize);
		
		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		for (int i = 0; i < loopSize; i++) {
			executor.submit(() -> {
				String threadName = Thread.currentThread().getName();
			    log.debug("Starting new connection test");
			    try {
			    	long threadId = Thread.currentThread().getId();
					testOneConnection(threadId, MetricFactory.mixedMetrics(metricsPerMessage));
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
	}
	

	private void testOneConnection(long l, String payload) throws IOException, InterruptedException {
		try(Socket clientSocket = new Socket("localhost", carbonProxy.getServerPort())) {
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			outToServer.write(payload.getBytes());
			
			clientSocket.shutdownOutput();
			clientSocket.shutdownInput();
			clientSocket.close();
		} catch (Exception e) {
			log.error("Failed to connect to server: {}", e);
		}
	}

}
