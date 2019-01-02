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

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author David Turanski
 **/
@Configuration
@EnableConfigurationProperties({ WebClientProperties.class, DefaultCloudEventMapper.class })
public class CloudEventsPublisherAutoConfiguration {

	@Bean
	public WebClient webClient(WebClientProperties properties) {
		HttpClient httpClient = HttpClient.create()
			.tcpConfiguration(client ->
				properties.getTimeoutMs() > 0 ?
					client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getTimeoutMs()) : client);

		return WebClient.builder()
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.baseUrl(properties.getTargetUri())
			.build();

	}

	@Bean
	@ConditionalOnMissingBean
	public WebClientCloudEventPublisher cloudEventPublisher(WebClient client,
		DefaultCloudEventMapper cloudEventMapper) {
		return new WebClientCloudEventPublisher(client, cloudEventMapper);
	}

}
