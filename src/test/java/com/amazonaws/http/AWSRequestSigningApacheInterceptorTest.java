/*
 * ****************************************************************************************************
 * This class has been copied from https://github.com/amazon-archives/aws-request-signing-apache-interceptor
 * It is an old, archived, project from AWS, and as such is not published on maven central, so we cannot
 * include it as a gradle dependency.
 *
 * It has been slightly modified by converting it to junit 5 and assertJ (so that we don't have old test
 * dependencies), but other than that it has not been modified in any way, including not attempting to
 * convert it to kotlin, hence it is a java class in the java source tree.
 * ****************************************************************************************************
 *
 * Copyright 2012-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.amazonaws.http;

import com.amazonaws.SignableRequest;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.Signer;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;

public class AWSRequestSigningApacheInterceptorTest {

    private static AWSRequestSigningApacheInterceptor createInterceptor() {
        AWSCredentialsProvider anonymousCredentialsProvider =
                new AWSStaticCredentialsProvider(new AnonymousAWSCredentials());
        return new AWSRequestSigningApacheInterceptor("servicename",
                new AddHeaderSigner("Signature", "wuzzle"),
                anonymousCredentialsProvider);

    }

    @Test
    public void testSimpleSigner() throws Exception {
        HttpEntityEnclosingRequest request =
                new BasicHttpEntityEnclosingRequest("GET", "/query?a=b");
        request.setEntity(new StringEntity("I'm an entity"));
        request.addHeader("foo", "bar");
        request.addHeader("content-length", "0");

        HttpCoreContext context = new HttpCoreContext();
        context.setTargetHost(HttpHost.create("localhost"));

        createInterceptor().process(request, context);

        assertThat(request.getFirstHeader("foo").getValue()).isEqualTo("bar");
        assertThat(request.getFirstHeader("Signature").getValue()).isEqualTo("wuzzle");
        assertThat(request.getFirstHeader("content-length")).isNull();
    }

    @Test
    public void testBadRequest() {

        HttpRequest badRequest = new BasicHttpRequest("GET", "?#!@*%");

        IOException ex = catchThrowableOfType(() -> createInterceptor().process(badRequest, new BasicHttpContext()), IOException.class);

        assertThat(ex).isNotNull();
    }

    @Test
    public void testEncodedUriSigner() throws Exception {
        HttpEntityEnclosingRequest request =
                new BasicHttpEntityEnclosingRequest("GET", "/foo-2017-02-25%2Cfoo-2017-02-26/_search?a=b");
        request.setEntity(new StringEntity("I'm an entity"));
        request.addHeader("foo", "bar");
        request.addHeader("content-length", "0");

        HttpCoreContext context = new HttpCoreContext();
        context.setTargetHost(HttpHost.create("localhost"));

        createInterceptor().process(request, context);

        assertThat(request.getFirstHeader("foo").getValue()).isEqualTo("bar");
        assertThat(request.getFirstHeader("Signature").getValue()).isEqualTo("wuzzle");
        assertThat(request.getFirstHeader("content-length")).isNull();
        assertThat(request.getFirstHeader("resourcePath").getValue()).isEqualTo("/foo-2017-02-25%2Cfoo-2017-02-26/_search");
    }

    private static class AddHeaderSigner implements Signer {
        private final String name;
        private final String value;

        private AddHeaderSigner(String name, String value) {
            this.name = name;
            this.value = value;
        }


        @Override
        public void sign(SignableRequest<?> request, AWSCredentials credentials) {
            request.addHeader(name, value);
            request.addHeader("resourcePath", request.getResourcePath());
        }
    }

}
