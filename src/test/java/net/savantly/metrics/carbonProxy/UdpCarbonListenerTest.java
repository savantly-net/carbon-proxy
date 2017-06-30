package net.savantly.metrics.carbonProxy;

import static org.junit.Assert.fail;

import java.io.IOException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class UdpCarbonListenerTest {

	private final static Logger log = LoggerFactory.getLogger(UdpCarbonListenerTest.class);
	
	@Value("${carbonProxy.server-port}")
	int port;

	@Test
	public void testSingle() throws IOException, InterruptedException {
		testOneConnection(1);
	}
	@Test
	public void testMulti(){
		
		int threadPoolSize = 10;
		int loopSize = 10;
		
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
