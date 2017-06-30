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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class TcpCarbonListenerTest {

	private final static Logger log = LoggerFactory.getLogger(TcpCarbonListenerTest.class);

	@Autowired
	ApplicationConfiguration carbonProxy;


	@Test
	public void testPlainText() throws IOException, InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		testOneConnection(1);
		//latch.await(3, TimeUnit.SECONDS);

/*		while (!clientSocket.isInputShutdown() && !clientSocket.isClosed()) {

			if (inFromServer.available() > 0) {
				// available stream to be read
				int length = inFromServer.available();

				// create buffer
				byte[] buf = new byte[length];

				// read the full data into the buffer
				inFromServer.readFully(buf);
				clientSocket.shutdownInput();
				clientSocket.close();

				String response = new String(buf);
				log.info(response);
			}
		}*/
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
	

	private void testOneConnection(long l) throws IOException, InterruptedException {
		Random r = new Random();
		Socket clientSocket = new Socket("localhost", carbonProxy.getServerPort());
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		//DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			String msg = String.format("test.relay.tcp-%s.count %s %s\n",l, r.nextInt(10), DateTime.now().getMillis()/1000-(i*30));
			sb.append(msg);
		}

		log.info("Writing msg to tcp socket: " + sb.toString());
		outToServer.write(sb.toString().getBytes());
		
		clientSocket.shutdownOutput();
		clientSocket.shutdownInput();
		clientSocket.close();
	}

}
