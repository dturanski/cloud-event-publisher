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

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * @author David Turanski
 **/
@Slf4j
public class CloudEventsClient {

	public final static String CLOUD_EVENT_CONTENT_TYPE = "application/cloudevents+json";

	private final WebClient webclient;

	private final DefaultCloudEventMapper mapper;

	public CloudEventsClient(WebClient webclient, DefaultCloudEventMapper mapper) {
		this.webclient = webclient;
		this.mapper = mapper;
	}

	public Mono<ClientResponse> convertAndPost(Object data) {
		return postCloudEvent(mapper.apply(data));
	}

	public Mono<ClientResponse> postCloudEvent(CloudEvent cloudEvent) {
		return webclient.post()
			.contentType(MediaType.parseMediaType(CLOUD_EVENT_CONTENT_TYPE))
			.syncBody(cloudEvent)
			.exchange() .doOnNext(response -> {
				HttpStatus httpStatus = response.statusCode();
				if (httpStatus.is4xxClientError() || httpStatus.is5xxServerError()) {
					throw WebClientResponseException.create(
						httpStatus.value(),
						httpStatus.getReasonPhrase(),
						response.headers().asHttpHeaders(),
						null,
						null);
				}
			});
	}
}
