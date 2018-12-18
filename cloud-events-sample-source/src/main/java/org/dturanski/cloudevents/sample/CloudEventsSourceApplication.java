package org.dturanski.cloudevents.sample;

import java.time.Duration;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;
import org.dturanski.cloudevents.publisher.CloudEventsClient;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class CloudEventsSourceApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(CloudEventsSourceApplication.class, args);
	}

	@Autowired
	CloudEventsClient cloudEventsClient;

	public Supplier<Flux<String>> sampleSource() {
		return () -> Flux.interval(Duration.ofSeconds(1)).map(l -> "Hello World");
	}

	@Override
	public void run(String... args) throws Exception {
		sampleSource().get().subscribe(data -> {
			log.info("Posting data {}", data);
			cloudEventsClient.convertAndPost(data)
			.subscribe(
				response -> log.info("status {}", response)
			);
		});
	}
}
