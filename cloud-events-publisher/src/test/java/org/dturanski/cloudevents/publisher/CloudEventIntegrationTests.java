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

import io.cloudevents.CloudEvent;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author David Turanski
 **/

@SpringBootTest(properties = {
	"io.cloudevents.event.type=test.event.type",
	"io.cloudevents.event.source=/test/source",
	"io.cloudevents.event.content-type=text/plain"
})
@RunWith(SpringRunner.class)
public class CloudEventIntegrationTests {

	@Autowired CloudEventsClient cloudEventsClient;

	@Autowired
	DefaultCloudEventMapper<String> cloudEventMapper;

	@Test
	public void cloudEventMapper() {
		CloudEvent<String> cloudEvent = cloudEventMapper.apply("hello, world");
		assertThat(cloudEvent.getData().get()).isEqualTo("hello, world");
		assertThat(cloudEvent.getSource()).isEqualTo(URI.create("/test/source"));
		assertThat(cloudEvent.getContentType().get()).isEqualTo("text/plain");
		assertThat(cloudEvent.getType()).isEqualTo("test.event.type");
	}


	@SpringBootApplication
	static class TestApp {
	}

}
