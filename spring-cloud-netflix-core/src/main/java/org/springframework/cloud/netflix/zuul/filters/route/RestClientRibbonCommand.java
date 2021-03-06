/*
 * Copyright 2013-2016 the original author or authors.
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
 *
 */

package org.springframework.cloud.netflix.zuul.filters.route;

import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpResponse;
import com.netflix.niws.client.http.RestClient;
import org.springframework.cloud.netflix.zuul.filters.route.support.AbstractRibbonCommand;
import org.springframework.util.MultiValueMap;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * Hystrix wrapper around Eureka Ribbon command
 *
 * see original
 * https://github.com/Netflix/zuul/blob/master/zuul-netflix/src/main/java/com/
 * netflix/zuul/dependency/ribbon/hystrix/RibbonCommand.java
 */
@SuppressWarnings("deprecation")
public class RestClientRibbonCommand extends AbstractRibbonCommand<RestClient, HttpRequest, HttpResponse> {

	@SuppressWarnings("unused")
	@Deprecated
	public RestClientRibbonCommand(String commandKey, RestClient restClient, HttpRequest.Verb verb, String uri,
								   Boolean retryable, MultiValueMap<String, String> headers,
								   MultiValueMap<String, String> params, InputStream requestEntity) {
		this(commandKey, restClient, new RibbonCommandContext(commandKey, verb.verb(), uri, retryable, headers, params, requestEntity));
	}

	public RestClientRibbonCommand(String commandKey, RestClient client, RibbonCommandContext context) {
		super(commandKey, client, context);
	}

	@Override
	protected HttpRequest createRequest() throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.verb(getVerb(this.context.getMethod()))
				.uri(this.context.uri())
				.entity(this.context.getRequestEntity());

		if(this.context.getRetryable() != null) {
			builder.setRetriable(this.context.getRetryable());
		}

		for (String name : this.context.getHeaders().keySet()) {
			List<String> values = this.context.getHeaders().get(name);
			for (String value : values) {
				builder.header(name, value);
			}
		}
		for (String name : this.context.getParams().keySet()) {
			List<String> values = this.context.getParams().get(name);
			for (String value : values) {
				builder.queryParams(name, value);
			}
		}

		customizeRequest(builder);

		return builder.build();
	}

	protected void customizeRequest(HttpRequest.Builder requestBuilder) {
		// noop
	}

	@Deprecated
	public URI getUri() {
		return this.context.uri();
	}

	@SuppressWarnings("unused")
	@Deprecated
	public HttpRequest.Verb getVerb() {
		return getVerb(this.context.getVerb());
	}

	protected static HttpRequest.Verb getVerb(String method) {
		if (method == null)
			return HttpRequest.Verb.GET;
		try {
			return HttpRequest.Verb.valueOf(method.toUpperCase());
		}
		catch (IllegalArgumentException e) {
			return HttpRequest.Verb.GET;
		}
	}

}
