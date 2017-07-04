package net.savantly.metrics.carbonProxy.udp;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.savantly.metrics.carbonProxy.Application;
import net.savantly.metrics.carbonProxy.kafka.KafkaMetricProducerMessageHandler;
import net.savantly.metrics.carbonProxy.test.utils.UdpClient;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
public class UdpCarbonListenerTest {

	private final static Logger log = LoggerFactory.getLogger(UdpCarbonListenerTest.class);	
	
	@Autowired
	KafkaMetricProducerMessageHandler handler;
	
	@Value("${carbonProxy.server-port}")
	int port;

	@Test
	public void testSingle() throws IOException, InterruptedException {
		testOneConnection(1);
		handler.getLatch().await(10, TimeUnit.SECONDS);
		Assert.assertEquals(handler.getLatch().getCount(), 0);
	}
	@Test
	public void testMulti(){
		
		int threadPoolSize = 10;
		int loopSize = 5;
		
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
			handler.getLatch().await(10, TimeUnit.SECONDS);
		    executor.shutdown();
		    executor.awaitTermination(10, TimeUnit.SECONDS);
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
		    Assert.assertEquals(handler.getLatch().getCount(), 0);
		}

	}
	
	private void testOneConnection(long id) throws IOException{
		UdpClient client = new UdpClient(port);
		Random r = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			String str = String.format("test.relay.udp-%s.count %s %s\n", id, r.nextInt(10), DateTime.now().getMillis()/1000-(i*30));
			sb.append(str);
		}
		String msg = sb.toString();
		client.sendMessage(msg);
		client.close();
	}

}
