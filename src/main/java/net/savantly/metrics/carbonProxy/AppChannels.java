package net.savantly.metrics.carbonProxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.MessageChannel;

@Configuration
public class AppChannels {

	public static final String CARBON_UDP_RELAY_CHANNEL = "carbonUdpRelayChannel";
	public static final String CARBON_UDP_AGGREGATOR_CHANNEL = "carbonUdpAggegatorChannel";
	public static final String CARBON_TCP_RELAY_CHANNEL = "carbonTcpRelayChannel";
	public static final String CARBON_QUEUE_CHANNEL = "carbonQueue";
	public static final String PRE_PROCESSOR_QUEUE_CHANNEL = "preProcessorQueueChannel";
	public static final String MULTI_METRIC_INPUT_CHANNEL = "multiMetricInputChannel";
	public static final String SINGLE_METRIC_INPUT_CHANNEL = "singleMetricInputChannel";
	public static final String KAFKA_HANDLER_INPUT_CHANNEL = "kafkaHandlerInputChannel";
	public static final String INBOUND_TCP_CHANNEL = "inboundTcpChannel";
	public static final String INBOUND_UDP_CHANNEL = "inboundUdpChannel";

	@Value("${preProcessorQueueChannel.size}")
	int preProcessorQueueChannelSize;
	
	@Bean(INBOUND_UDP_CHANNEL)
	public DirectChannel inboundUdpChannel(){
		 return MessageChannels.direct().get();
	}
	
	@Bean(INBOUND_TCP_CHANNEL)
	public DirectChannel inboundTcpChannel(){
		 return MessageChannels.direct().get();
	}
	
	@Bean(SINGLE_METRIC_INPUT_CHANNEL)
	public MessageChannel singleMetricInputChannel(){
		PublishSubscribeChannel channel = MessageChannels.publishSubscribe().get();
		return channel;
	}
	
	@Bean(MULTI_METRIC_INPUT_CHANNEL)
	public MessageChannel multiMetricInputChannel(){
		PublishSubscribeChannel channel = MessageChannels.publishSubscribe().get();
		return channel;
	}
	
	
	@Bean(PRE_PROCESSOR_QUEUE_CHANNEL)
	public MessageChannel preProcessorQueueChannel(){
		return MessageChannels.queue(preProcessorQueueChannelSize).get();
	}
	
	@Bean(CARBON_QUEUE_CHANNEL)
	public QueueChannel carbonQueue(){
		 QueueChannel channel = MessageChannels.queue().get();
		 return channel;
	}
}
