package net.savantly.metrics.carbonProxy.carbon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import net.savantly.metrics.carbonProxy.ApplicationConfiguration;

public class CarbonMessageRouter extends AbstractMessageRouter {
	private static final Logger log = LoggerFactory.getLogger(CarbonMessageRouter.class);
	private List<String> aggregatorMatches;
	private List<MessageChannel> defaultChannels = new ArrayList<>();
	private List<MessageChannel> aggregatorChannels = new ArrayList<>();
	
	public CarbonMessageRouter(ApplicationConfiguration config,
			MessageChannel defaultChannel,
			MessageChannel aggregatorChannel) {
		this.aggregatorMatches = config.getAggregatorMatches();
		this.defaultChannels.add(defaultChannel);
		this.aggregatorChannels.add(aggregatorChannel);
	}

	@Override
	protected Collection<MessageChannel> determineTargetChannels(Message<?> message) {
		if (message.getPayload().getClass() != String.class) {
			log.error("invalid message type: {}; expected String", message.getPayload().getClass());
			return null;
		} else {
			String payload = (String) message.getPayload();
			for (String regex : aggregatorMatches) {
				if (payload.matches(regex)) {
					return this.aggregatorChannels;
				}
			}
			return this.defaultChannels;
		}
	}

}
