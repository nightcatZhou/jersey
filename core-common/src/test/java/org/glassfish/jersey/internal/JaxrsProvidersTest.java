/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.internal;

import java.lang.annotation.Annotation;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import javax.ws.rs.ext.RuntimeDelegate;

import org.glassfish.jersey.internal.inject.AbstractModule;
import org.glassfish.jersey.internal.inject.ContextInjectionResolver;
import org.glassfish.jersey.message.internal.MessagingModules;
import org.glassfish.jersey.process.internal.ProcessingTestModule;
import org.glassfish.jersey.process.internal.RequestScope;

import org.glassfish.hk2.HK2;
import org.glassfish.hk2.Services;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class JaxrsProvidersTest {

    private static class Module extends AbstractModule {

        @Override
        protected void configure() {
            bind(ContextResolver.class).toInstance(new ContextResolver<String>() {

                @Override
                public String getContext(Class<?> type) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
        }
    }

    public JaxrsProvidersTest() {
        RuntimeDelegate.setInstance(new TestRuntimeDelegate());
    }

    @Test
    public void testProviders() {
        final Services services = HK2.get().create(null,
                new ContextInjectionResolver.Module(),
                new ProcessingTestModule(),
                new MessagingModules.MessageBodyProviders(),
                new Module());

        ProcessingTestModule.initProviders(services);

        RequestScope scope = services.forContract(RequestScope.class).get();

        try {
            scope.enter();

            Providers instance = services.forContract(Providers.class).get();

            assertNotNull(instance);
            assertSame(JaxrsProviders.class, instance.getClass());

            assertNotNull(instance.getExceptionMapper(Throwable.class));
            assertNotNull(instance.getMessageBodyReader(String.class, String.class, new Annotation[0], MediaType.TEXT_PLAIN_TYPE));
            assertNotNull(instance.getMessageBodyWriter(String.class, String.class, new Annotation[0], MediaType.TEXT_PLAIN_TYPE));
            assertNotNull(instance.getContextResolver(String.class, MediaType.TEXT_PLAIN_TYPE));

        } finally {
            scope.exit();
        }
    }
}