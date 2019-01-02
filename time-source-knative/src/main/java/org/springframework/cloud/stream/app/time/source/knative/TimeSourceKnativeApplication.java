/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.time.source.knative;

import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;
import org.dturanski.cloudevents.publisher.CloudEventPublisher;
import org.dturanski.cloudevents.publisher.WebClientCloudEventPublisher;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;

@SpringBootApplication
@Slf4j
@Import({ org.springframework.cloud.stream.app.time.source.TimeSourceConfiguration.class })
public class TimeSourceKnativeApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(TimeSourceKnativeApplication.class, args);
	}

	@Autowired
	private WebClientCloudEventPublisher cloudEventPublisher;


	@Autowired
	private Supplier<Flux<Message<?>>> source;

	@Override
	public void run(String... args) {
		source.get().subscribe(m->
			cloudEventPublisher.convertAndPost(m.getPayload())
			.subscribe(clientResponse -> { }));
	}
}
