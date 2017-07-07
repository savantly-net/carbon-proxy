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

import org.joda.time.DateTime;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.savantly.metrics.carbonProxy.Application;
import net.savantly.metrics.carbonProxy.ApplicationConfiguration;
import net.savantly.metrics.carbonProxy.kafka.KafkaMetricProducer;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@DirtiesContext(methodMode=MethodMode.AFTER_METHOD)
public class TcpCarbonListenerTest {

	private final static Logger log = LoggerFactory.getLogger(TcpCarbonListenerTest.class);
	private Random random = new Random();
	private int threadTimeOut = 10;

	@Autowired
	ApplicationConfiguration carbonProxy;
	@MockBean
	KafkaMetricProducer producer;
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
		// when Kafka producer attempts to connect to the server
		// mock the send, and do a countdown on the latch
		Mockito.doAnswer(voidAnswer).when(producer).send(Mockito.any());
	}


	@Test
	public void testPlainText() throws IOException, InterruptedException {
		latch = new CountDownLatch(1);
		long id = 1;
		testOneConnection(id, createMultiMetricPayload(id));
		latch.await(threadTimeOut, TimeUnit.SECONDS);
	}
	
	@Test
	public void testPayloadWithMissingField() throws IOException, InterruptedException {
		latch = new CountDownLatch(1);
		long id = 2;
		testOneConnection(id, createSingleMetricPayloadWithMissingField(id));
		latch.await(threadTimeOut, TimeUnit.SECONDS);
	}
	
	@Test
	public void testPayloadWithCrLf() throws IOException, InterruptedException {
		latch = new CountDownLatch(1);
		long id = 3;
		testOneConnection(id, createMultiMetricPayloadWithCrLf(id ));
		latch.await(threadTimeOut, TimeUnit.SECONDS);
	}
	
	@Test
	public void testLoadAndPerformance() throws InterruptedException{
		
		int threadPoolSize = 10;
		int loopSize = 10;
		
		latch = new CountDownLatch(loopSize);
		
		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		for (int i = 0; i < loopSize; i++) {
			executor.submit(() -> {
				String threadName = Thread.currentThread().getName();
			    log.debug("Starting new connection test");
			    try {
			    	long threadId = Thread.currentThread().getId();
					testOneConnection(threadId, createMultiMetricPayload(threadId));
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
		Socket clientSocket = new Socket("localhost", carbonProxy.getServerPort());
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		outToServer.write(payload.getBytes());
		
		clientSocket.shutdownOutput();
		clientSocket.shutdownInput();
		clientSocket.close();
	}
	
	private String createMultiMetricPayload(long id){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			String msg = String.format("test.relay.tcp-%s.count %s %s\n", id, random.nextInt(10), DateTime.now().getMillis()/1000-(i*30));
			sb.append(msg);
		}
		return sb.toString();
	}
	
	private String createMultiMetricPayloadWithCrLf(long id){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			String msg = String.format("test.relay.tcp-%s.count %s %s\r\n", id, random.nextInt(10), DateTime.now().getMillis()/1000-(i*30));
			sb.append(msg);
		}
		return sb.toString();
	}
	
	private String createSingleMetricPayload(long id){
		return String.format("test.relay.tcp-%s.count %s %s\n", id, random.nextInt(10), DateTime.now().getMillis()/1000);
	}
	
	private String createSingleMetricPayloadWithMissingField(long id){
		return String.format("test.relay.tcp-%s.count %s\n", id, random.nextInt(10));
	}

}
