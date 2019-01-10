/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dturanski.source.supplier.cloudevents;

import java.util.function.Consumer;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;
import org.dturanski.cloudevents.publisher.CloudEventPublisher;
import reactor.core.publisher.Flux;

import org.springframework.boot.CommandLineRunner;
import org.springframework.messaging.Message;

/**
 * @author David Turanski
 **/
@Slf4j
public class CloudEventsPublisherCommandLineRunner implements CommandLineRunner {

	private final CloudEventPublisher cloudEventPublisher;

	private final Supplier<Flux<Message<?>>> source;

	private final Consumer consumer;

	public CloudEventsPublisherCommandLineRunner(CloudEventPublisher cloudEventPublisher,
		Supplier<Flux<Message<?>>> source, Consumer consumer) {

		this.cloudEventPublisher = cloudEventPublisher;
		this.source = source;
		this.consumer = consumer;
	}

	public CloudEventsPublisherCommandLineRunner(CloudEventPublisher cloudEventPublisher,
		Supplier<Flux<Message<?>>> source) {

		this.cloudEventPublisher = cloudEventPublisher;
		this.source = source;
		this.consumer = o -> {};
	}

	@Override
	public void run(String... args) {

		source.get().subscribe(m -> {
			log.trace("publishing {}", m.getPayload());
			cloudEventPublisher.convertAndPost(m.getPayload()).subscribe(
				consumer::accept);
		});
	}

}
