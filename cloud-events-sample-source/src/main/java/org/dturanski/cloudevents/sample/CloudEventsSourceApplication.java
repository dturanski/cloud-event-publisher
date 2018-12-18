package org.dturanski.cloudevents.sample;

import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Supplier;

import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.dturanski.cloudevents.publisher.CloudEventsClient;
import org.dturanski.cloudevents.publisher.DefaultCloudEventMapper;
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
	private CloudEventsClient cloudEventsClient;

	@Autowired
	private DefaultCloudEventMapper mapper;

	public Supplier<Flux<String>> sampleSource() {
		return () -> Flux.interval(Duration.ofSeconds(1)).map(l -> "Hello World");
	}

	@Override
	public void run(String... args) throws Exception {
		sampleSource().get().subscribe(data -> {
			log.info("Posting data {}", data);
			CloudEvent cloudEvent = mapper.apply(data);
			log.info("time {}", ZonedDateTime.now());
			log.info("Posting data {}", cloudEvent);
			cloudEventsClient.postCloudEvent(cloudEvent)
				.subscribe(
					response -> log.info("status {}", response.statusCode())
				);
		});
	}

	@PostMapping(value = "/events")
	//TODO: Parsing error with "time" field when parsing argument as CloudEvent
	public void postCloudEvent(@RequestBody Map<String, Object> cloudEvent) {

		long time = (long)((Double)cloudEvent.get("time") * 1000);

		log.info("received {} {}", cloudEvent,new SimpleDateFormat("yyyy-MM-dd-hh.mm.ss").format(time));
	}

}
