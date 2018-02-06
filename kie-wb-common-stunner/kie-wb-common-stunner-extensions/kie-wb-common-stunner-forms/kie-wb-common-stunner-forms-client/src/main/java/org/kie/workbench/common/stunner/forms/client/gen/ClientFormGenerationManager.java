/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.forms.client.gen;

import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.stunner.core.client.service.ClientRuntimeError;
import org.kie.workbench.common.stunner.forms.service.FormGeneratedEvent;
import org.kie.workbench.common.stunner.forms.service.FormGenerationService;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;

// TODO: I18n.
@ApplicationScoped
public class ClientFormGenerationManager {

    private final Caller<FormGenerationService> formGenerationService;

    protected ClientFormGenerationManager() {
        this(null);
    }

    @Inject
    public ClientFormGenerationManager(final Caller<FormGenerationService> formGenerationService) {
        this.formGenerationService = formGenerationService;
    }

    public void call(final Consumer<FormGenerationService> service) {
        service.accept(formGenerationService.call(getRemoteCallback(),
                                                  getErrorCallback()));
    }

    // Listen for form generation events.
    void onFormGeneratedEvent(@Observes FormGeneratedEvent event) {
        showNotification("Forms generation completed successfully for [" + event.getName() + "]");
    }

    private static RemoteCallback<Void> getRemoteCallback() {
        return getRemoteCallback(() -> {
        });
    }

    private static RemoteCallback<Void> getRemoteCallback(final Command callback) {
        return aVoid -> {
            callback.execute();
        };
    }

    private static ErrorCallback<Message> getErrorCallback() {
        return getErrorCallback(e -> {
        });
    }

    @SuppressWarnings("unchecked")
    private static <V> ErrorCallback<Message> getErrorCallback(final ParameterizedCommand<V> callback) {
        return (message, throwable) -> {
            final Throwable t = null != throwable.getCause() ?
                    throwable.getCause() :
                    throwable;
            showError(t.getMessage());
            callback.execute((V) new ClientRuntimeError(throwable));
            return false;
        };
    }

    private static void showNotification(final String message) {
        showNotification("[INFO] Form Generation",
                         message,
                         IconType.CHECK);
    }

    private static void showError(final String message) {
        showNotification("[ERROR] Form Generation",
                         message,
                         IconType.EXCLAMATION);
    }

    private static void showNotification(final String title,
                                         final String message,
                                         final IconType icon) {
        Notify.notify(title,
                      buildHtmlEscapedText(message),
                      icon);
    }

    private static String buildHtmlEscapedText(final String message) {
        return new SafeHtmlBuilder().appendEscapedLines(message).toSafeHtml().asString();
    }
}
