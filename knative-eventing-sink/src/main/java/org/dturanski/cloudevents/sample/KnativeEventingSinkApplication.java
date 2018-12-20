package org.dturanski.cloudevents.sample;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventBuilder;
import lombok.extern.slf4j.Slf4j;
import org.dturanski.cloudevents.publisher.CloudEventPublisher;
import org.dturanski.cloudevents.publisher.DefaultCloudEventMapper;
import org.dturanski.cloudevents.publisher.WebClientProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

@SpringBootApplication
@Slf4j
@EnableBinding(Sink.class)
public class KnativeEventingSinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(KnativeEventingSinkApplication.class, args);
	}

	@Autowired
	private CloudEventPublisher cloudEventsPublisher;

	@Autowired
	private DefaultCloudEventMapper mapper;

	@Autowired
	private WebClientProperties properties;

	@StreamListener(Sink.INPUT)
	public void publishCloudEvent(String data) {

		log.info("received {} ", data);
		CloudEvent cloudEvent = mapper.apply(data);

		log.info("Posting cloud event {} to {}", cloudEvent, properties.getTargetUri());

		cloudEventsPublisher.postCloudEvent(cloudEvent).subscribe(clientResponse ->
			log.info("status {}", clientResponse.statusCode())
		);
	}
}
