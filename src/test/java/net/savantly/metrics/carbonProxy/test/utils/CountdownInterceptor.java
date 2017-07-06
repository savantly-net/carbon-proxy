package net.savantly.metrics.carbonProxy.test.utils;

import java.util.concurrent.CountDownLatch;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

public class CountdownInterceptor implements ChannelInterceptor{
	
	private CountDownLatch latch;
	
	public CountdownInterceptor(CountDownLatch latch) {
		this.latch = latch;
	}

	public CountDownLatch getLatch() {
		return latch;
	}

	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		latch.countDown();
		return message;
	}

	@Override
	public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
		latch.countDown();
	}

	@Override
	public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
		latch.countDown();
	}

	@Override
	public boolean preReceive(MessageChannel channel) {
		latch.countDown();
		return true;
	}

	@Override
	public Message<?> postReceive(Message<?> message, MessageChannel channel) {
		latch.countDown();
		return message;
	}

	@Override
	public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
		latch.countDown();
	}

}
