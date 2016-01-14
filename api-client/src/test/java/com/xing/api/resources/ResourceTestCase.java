/*
 * Copyright (С) 2016 XING AG (http://xing.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xing.api.resources;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.xing.api.CallSpec;
import com.xing.api.HttpError;
import com.xing.api.Resource;
import com.xing.api.Response;
import com.xing.api.XingApi;

import org.junit.Before;
import org.junit.Rule;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/** Reduces resource testing boilerplate. */
public class ResourceTestCase<T extends Resource> {
    @Rule
    public final MockWebServer server = new MockWebServer();

    private final Class<T> resourceClass;
    protected XingApi mockApi;
    protected T resource;

    public ResourceTestCase(Class<T> resourceClass) {
        this.resourceClass = resourceClass;
    }

    @Before
    public void setUp() throws Exception {
        validateResource();
        mockApi = new XingApi.Builder()
              .apiEndpoint(server.url("/"))
              .loggedOut()
              .build();
        resource = mockApi.resource(resourceClass);
    }

    /**
     * Validate the resource before starting the setup.
     * <li>1. Resource must be {@code final}.</li>
     * <li>2. Each {@code public} method declared in the {@linkplain Resource} must return a {@linkplain CallSpec}.</li>
     */
    private void validateResource() throws Exception {
        // Resource must be final.
        assertTrue("Resource should be final.", Modifier.isFinal(resourceClass.getModifiers()));

        // All public methods must return a call spec.
        Method[] methods = resourceClass.getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                Class<?> returnType = method.getReturnType();
                assertThat(returnType).isEqualTo(CallSpec.class);
            }
        }
    }

    /** Asserts a spec that expects 204 as success response. */
    protected void testVoidSpec(CallSpec<Void, HttpError> spec) throws Exception {
        server.enqueue(new MockResponse().setResponseCode(204));
        Response<Void, HttpError> response = spec.execute();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.body()).isNull();
    }
}
