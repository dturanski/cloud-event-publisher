/*
 * Copyright 2018 the original author or authors.
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

package org.dturanski.cloudevents.publisher;

import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.web.reactive.function.client.ClientResponse;

/**
 * @author David Turanski
 **/
@Slf4j
public class WebClientCloudEventPublisher implements CloudEventPublisher {

	private final DefaultCloudEventMapper cloudEventMapper;

	private final CloudEventsClient client;

	public WebClientCloudEventPublisher(CloudEventsClient client, DefaultCloudEventMapper mapper) {
		this.client = client;
		this.cloudEventMapper = mapper;
	}

	@Override
	public CloudEvent publish(Object data) {
		CloudEvent cloudEvent = cloudEventMapper.apply(data);
		client.postCloudEvent(cloudEvent).block();
		return cloudEvent;
	}

	public Mono<ClientResponse> convertAndPost(Object data) {
		CloudEvent cloudEvent = cloudEventMapper.apply(data);
		return client.postCloudEvent(cloudEvent);
	}
}
