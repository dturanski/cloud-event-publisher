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

package org.dturanski.cloudevents.publisher;

import java.net.URI;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import reactor.test.StepVerifier;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class CloudEventPublisherTests {

	private CloudEventPublisher cloudEventPublisher;

	private WebClient webClient;

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

	@Before
	public void setUp() {

		CloudEventMapper cloudEventMapper = new CloudEventMapper();
		webClient =
			WebClient.builder().baseUrl(String.format("http://localhost:%d/events", wireMockRule.port()))
				.build();
		cloudEventMapper.setContentType(MediaType.APPLICATION_JSON);
		cloudEventMapper.setSource(URI.create("/test"));
		cloudEventMapper.setType("test.event.type");
		cloudEventPublisher = new CloudEventPublisher(webClient, cloudEventMapper);

	}

	@Test
	public void postCloudEvent() {

		stubFor(post(urlEqualTo("/events"))
			.withHeader(HttpHeaders.CONTENT_TYPE, equalTo(CloudEventPublisher.CLOUD_EVENT_CONTENT_TYPE))
			.willReturn(aResponse()
				.withStatus(200)));

		cloudEventPublisher.convertAndPost(new MyCustomEvent("bar")).subscribe(
			response -> {

				assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);

				verify(postRequestedFor(urlEqualTo("/events"))
					.withHeader(HttpHeaders.CONTENT_TYPE, equalTo(CloudEventPublisher.CLOUD_EVENT_CONTENT_TYPE))
					.withRequestBody(matchingJsonPath("$.type", equalTo("test.event.type")))
					.withRequestBody(matchingJsonPath("$.source", equalTo("/test")))
					.withRequestBody(matchingJsonPath("$.contentType", equalTo("application/json")))
					.withRequestBody(matchingJsonPath("$.data.foo", equalTo("bar")))
				);
			}
		);
	}

	@Test
	public void handlesServerError() {
		stubFor(post(urlEqualTo("/events"))
			.withHeader(HttpHeaders.CONTENT_TYPE, equalTo(CloudEventPublisher.CLOUD_EVENT_CONTENT_TYPE))
			.willReturn(aResponse()
				.withStatus(500)));

		StepVerifier
			.create(cloudEventPublisher.convertAndPost(new MyCustomEvent("bar")))
			.verifyError(WebClientResponseException.InternalServerError.class);
	}

	@Test
	public void handlesNotFoundError() {
		stubFor(post(urlEqualTo("/events"))
			.withHeader(HttpHeaders.CONTENT_TYPE, equalTo(CloudEventPublisher.CLOUD_EVENT_CONTENT_TYPE))
			.willReturn(aResponse()
				.withStatus(404)));

		StepVerifier
			.create(cloudEventPublisher.convertAndPost(new MyCustomEvent("bar")))
			.verifyError(WebClientResponseException.NotFound.class);
	}

	@Data
	@AllArgsConstructor
	static class MyCustomEvent {

		private String foo;
	}

}
