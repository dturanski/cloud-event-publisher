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

package org.dturanski.source.twitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dturanski.cloudevents.publisher.CloudEventPublisher;
import reactor.core.publisher.Flux;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.app.twitter.search.source.TwitterSearchSourceConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;

/**
 * @author David Turanski
 **/
@SpringBootApplication
@Import(TwitterSearchSourceConfiguration.class)
@Slf4j
public class TwitterSearchSourceApplication {

	public static void main(String... args) {
		SpringApplication.run(TwitterSearchSourceApplication.class, args);
	}

	@Bean
	CommandLineRunner cloudEventsPublisherCommandLineRunner(CloudEventPublisher publisher,
		Supplier<Flux<Message<?>>> source, ObjectMapper objectMapper) {
		return args -> {
			source.get().map(message -> {
				String data = null;
				try {
					List<Map> tweet = objectMapper.readValue((byte[]) message.getPayload(), List.class);
					data = (String) tweet.get(0).get("text");
				}
				catch (IOException e) {
				}
				return data;
			}).subscribe(data -> publisher.publish(data, o -> {}));
		};
	}

}
