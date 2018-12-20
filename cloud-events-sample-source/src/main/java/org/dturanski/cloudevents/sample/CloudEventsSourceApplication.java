package org.dturanski.cloudevents.sample;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.dturanski.cloudevents.publisher.CloudEventPublisher;
import org.dturanski.cloudevents.publisher.DefaultCloudEventMapper;
import org.dturanski.cloudevents.publisher.WebClientProperties;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Slf4j
public class CloudEventsSourceApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(CloudEventsSourceApplication.class, args);
	}

	@Autowired
	private CloudEventPublisher cloudEventsPublisher;

	@Autowired
	private DefaultCloudEventMapper mapper;

	public Supplier<Flux<String>> sampleSource() {
		return () -> Flux.interval(Duration.ofSeconds(1)).map(l -> "Hello World");
	}

	@Autowired
	WebClientProperties webClientProperties;

	@Override
	public void run(String... args) {

		sampleSource().get().subscribe(data -> {
			CloudEvent cloudEvent = mapper.apply(data);
			log.info("Posting cloud event {} to {}", cloudEvent, webClientProperties.getTargetUri());
			cloudEventsPublisher.postCloudEvent(cloudEvent)
				.subscribe(
					response -> log.info("status {}", response.statusCode())
				);
		});
	}

	@PostMapping(value = "/events")
	//TODO: Parsing error with "time" field when parsing argument as CloudEvent
	public void postCloudEvent(@RequestBody Map<String, Object> cloudEvent) {

		long time = (long) ((Double) cloudEvent.get("time") * 1000);

		log.info("received {} {}", cloudEvent, new SimpleDateFormat("yyyy-MM-dd-hh.mm.ss").format(time));
	}

}
