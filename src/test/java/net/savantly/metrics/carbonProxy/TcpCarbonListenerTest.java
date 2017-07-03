package net.savantly.metrics.carbonProxy;

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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.savantly.metrics.carbonProxy.kafka.KafkaMetricProducerMessageHandler;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
public class TcpCarbonListenerTest {

	private final static Logger log = LoggerFactory.getLogger(TcpCarbonListenerTest.class);
	private Random random = new Random();

	@Autowired
	ApplicationConfiguration carbonProxy;
	@Autowired
	KafkaMetricProducerMessageHandler handler;


	@Test
	public void testPlainText() throws IOException, InterruptedException {
		long id = 1;
		testOneConnection(id, createMultiMetricPayload(id));
		handler.getLatch().await(10, TimeUnit.SECONDS);
	}
	
	@Test
	public void testPayloadWithMissingField() throws IOException, InterruptedException {
		long id = 2;
		testOneConnection(id, createSingleMetricPayloadWithMissingField(id));
		handler.getLatch().await(10, TimeUnit.SECONDS);
	}
	
	@Test
	public void testPayloadWithCrLf() throws IOException, InterruptedException {
		long id = 3;
		testOneConnection(id, createMultiMetricPayloadWithCrLf(id ));
		handler.getLatch().await(10, TimeUnit.SECONDS);
	}
	
	@Test
	public void testLoadAndPerformance(){
		
		int threadPoolSize = 10;
		int loopSize = 10;
		
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
			handler.getLatch().await(10, TimeUnit.SECONDS);
		    executor.shutdown();
		    executor.awaitTermination(5, TimeUnit.SECONDS);
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
		for (int i = 0; i < 10; i++) {
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
