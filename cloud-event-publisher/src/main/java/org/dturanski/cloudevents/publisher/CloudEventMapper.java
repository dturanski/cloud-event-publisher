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
import java.util.UUID;
import java.util.function.Function;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventBuilder;
import io.cloudevents.SpecVersion;
import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;

/**
 * @author David Turanski
 **/
@Data
@ConfigurationProperties(prefix = "io.cloudevents.event")
public class CloudEventMapper<T> implements Function<T, CloudEvent<T>> {

	private MediaType contentType = MediaType.TEXT_PLAIN;

	private String type = "unknown";

	private URI source = URI.create("");

	private URI schemaUrl;

	private String specVersion = SpecVersion.DEFAULT.toString();

	@Override
	public CloudEvent apply(T data) {
		return cloudEventBuilder()
			.data(data)
			.build();
	}

	public CloudEventBuilder<T> cloudEventBuilder() {
		return new CloudEventBuilder<T>()
			.contentType(this.contentType.toString())
			.type(this.type)
			.source(this.source)
			.id(UUID.randomUUID().toString())

			//TODO: Iteroperability issues with GSon
			// .time(ZonedDateTime.now())
			.schemaURL(this.schemaUrl)
			.specVersion(this.specVersion);
	}
}
