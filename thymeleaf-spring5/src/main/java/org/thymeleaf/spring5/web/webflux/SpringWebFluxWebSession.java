/*
 * =============================================================================
 *
 *   Copyright (c) 2011-2021, The THYMELEAF team (http://www.thymeleaf.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */

package org.thymeleaf.spring5.web.webflux;

import java.util.Collections;
import java.util.Map;

import org.springframework.web.server.WebSession;
import org.thymeleaf.util.Validate;

/**
 *
 * @author Daniel Fern&aacute;ndez
 *
 * @since 3.1.0
 *
 */
final class SpringWebFluxWebSession implements ISpringWebFluxWebSession {

    private final WebSession session;


    SpringWebFluxWebSession(final WebSession session) {
        super();
        Validate.notNull(session, "Session cannot be null");
        this.session = session;
    }

    @Override
    public boolean exists() {
        return this.session.isStarted();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(this.session.getAttributes());
    }

    @Override
    public Object getAttributeValue(final String name) {
        Validate.notNull(name, "Name cannot be null");
        return this.session.getAttribute(name);
    }

    @Override
    public void setAttributeValue(final String name, final Object value) {
        Validate.notNull(name, "Name cannot be null");
        if (value == null) {
            this.session.getAttributes().remove(name);
        } else {
            this.session.getAttributes().put(name, value);
        }
    }

    @Override
    public void removeAttribute(final String name) {
        Validate.notNull(name, "Name cannot be null");
        this.session.getAttributes().remove(name);
    }

    @Override
    public Object getNativeSessionObject() {
        return this.session;
    }

}
