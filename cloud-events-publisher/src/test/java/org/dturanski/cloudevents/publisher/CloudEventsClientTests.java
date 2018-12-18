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

import java.net.URI;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.cloudevents.CloudEventBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.reactive.function.client.ClientResponse;
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

public class CloudEventsClientTests {

	private WebClientCloudEventPublisher cloudEventPublisher;

	private CloudEventsClient cloudEventsClient;

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() {

		DefaultCloudEventMapper cloudEventMapper = new DefaultCloudEventMapper();
		cloudEventsClient = new CloudEventsClient(
			WebClient.builder().baseUrl(String.format("http://localhost:%d/events", wireMockRule.port()))
			.build(),
			cloudEventMapper);
		cloudEventMapper.setContentType(MediaType.APPLICATION_JSON);
		cloudEventMapper.setSource(URI.create("/test"));
		cloudEventMapper.setType("test.event.type");
		cloudEventPublisher = new WebClientCloudEventPublisher(cloudEventsClient, cloudEventMapper);

	}

	@Test
	public void postCloudEvent() {

		stubFor(post(urlEqualTo("/events"))
			.withHeader(HttpHeaders.CONTENT_TYPE, equalTo(CloudEventsClient.CLOUD_EVENT_CONTENT_TYPE))
			.willReturn(aResponse()
				.withStatus(200)));

		 ClientResponse response = cloudEventsClient.convertAndPost(new MyCustomEvent("bar")).block();

		 assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);

		verify(postRequestedFor(urlEqualTo("/events"))
			.withHeader(HttpHeaders.CONTENT_TYPE, equalTo(CloudEventsClient.CLOUD_EVENT_CONTENT_TYPE))
			.withRequestBody(matchingJsonPath("$.type", equalTo("test.event.type")))
			.withRequestBody(matchingJsonPath("$.source", equalTo("/test")))
			.withRequestBody(matchingJsonPath("$.contentType", equalTo("application/json")))
			.withRequestBody(matchingJsonPath("$.data.foo", equalTo("bar")))
		);
	}

	@Test
	public void handlesServerError() {
		stubFor(post(urlEqualTo("/events"))
			.withHeader(HttpHeaders.CONTENT_TYPE, equalTo(CloudEventsClient.CLOUD_EVENT_CONTENT_TYPE))
			.willReturn(aResponse()
				.withStatus(500)));

		expectedException.expect(WebClientResponseException.InternalServerError.class);

		cloudEventPublisher.publish(new MyCustomEvent("bar"));

	}

	@Test
	public void handlesNotFoundError() {
		stubFor(post(urlEqualTo("/events"))
			.withHeader(HttpHeaders.CONTENT_TYPE, equalTo(CloudEventsClient.CLOUD_EVENT_CONTENT_TYPE))
			.willReturn(aResponse()
				.withStatus(404)));

		expectedException.expect(WebClientResponseException.NotFound.class);
		cloudEventPublisher.publish(new MyCustomEvent("bar"));
	}

	@Data
	@AllArgsConstructor
	static class MyCustomEvent {
		private String foo;
	}

}
