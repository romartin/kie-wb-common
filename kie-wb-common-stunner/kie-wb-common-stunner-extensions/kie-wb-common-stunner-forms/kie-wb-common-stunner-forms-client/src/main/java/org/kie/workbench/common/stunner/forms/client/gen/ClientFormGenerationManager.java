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
import org.kie.workbench.common.stunner.core.client.i18n.ClientTranslationService;
import org.kie.workbench.common.stunner.core.client.service.ClientRuntimeError;
import org.kie.workbench.common.stunner.forms.client.resources.i18n.FormsClientConstants;
import org.kie.workbench.common.stunner.forms.service.FormGeneratedEvent;
import org.kie.workbench.common.stunner.forms.service.FormGenerationFailureEvent;
import org.kie.workbench.common.stunner.forms.service.FormGenerationService;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;

@ApplicationScoped
public class ClientFormGenerationManager {

    private final ClientTranslationService translationService;
    private final Caller<FormGenerationService> formGenerationService;
    private final Consumer<String> messageNotifier;
    private final Consumer<String> errorNotifier;

    protected ClientFormGenerationManager() {
        this(null, null);
    }

    @Inject
    public ClientFormGenerationManager(final ClientTranslationService translationService,
                                       final Caller<FormGenerationService> formGenerationService) {
        this(translationService,
             formGenerationService,
             ClientFormGenerationManager::showNotification,
             ClientFormGenerationManager::showError);
    }

    ClientFormGenerationManager(final ClientTranslationService translationService,
                                final Caller<FormGenerationService> formGenerationService,
                                final Consumer<String> messageNotifier,
                                final Consumer<String> errorNotifier) {
        this.translationService = translationService;
        this.formGenerationService = formGenerationService;
        this.messageNotifier = messageNotifier;
        this.errorNotifier = errorNotifier;
    }

    public void call(final Consumer<FormGenerationService> service) {
        service.accept(formGenerationService.call(getRemoteCallback(),
                                                  getErrorCallback()));
    }

    // Listen for form generation events.
    void onFormGeneratedEvent(@Observes FormGeneratedEvent event) {
        messageNotifier.accept(translationService.getKeyValue(FormsClientConstants.FormsGenerationSuccess,
                                                              event.getName()));
    }

    void onFormGenerationFailureEvent(@Observes FormGenerationFailureEvent event) {
        errorNotifier.accept(translationService.getKeyValue(FormsClientConstants.FormsGenerationFailure,
                                                            event.getName()));
    }

    private static RemoteCallback<Void> getRemoteCallback() {
        return getRemoteCallback(() -> {
        });
    }

    private static RemoteCallback<Void> getRemoteCallback(final Command callback) {
        return aVoid -> callback.execute();
    }

    private ErrorCallback<Message> getErrorCallback() {
        return getErrorCallback(e -> {
        });
    }

    @SuppressWarnings("unchecked")
    private <V> ErrorCallback<Message> getErrorCallback(final ParameterizedCommand<V> callback) {
        return (message, throwable) -> {
            final Throwable t = null != throwable.getCause() ?
                    throwable.getCause() :
                    throwable;
            errorNotifier.accept(t.getMessage());
            callback.execute((V) new ClientRuntimeError(throwable));
            return false;
        };
    }

    private static void showNotification(final String message) {
        showNotification(message,
                         IconType.CHECK);
    }

    private static void showError(final String message) {
        showNotification(message,
                         IconType.EXCLAMATION);
    }

    private static void showNotification(final String message,
                                         final IconType icon) {
        Notify.notify("Form Generation",
                      buildHtmlEscapedText(message),
                      icon);
    }

    private static String buildHtmlEscapedText(final String message) {
        return new SafeHtmlBuilder().appendEscapedLines(message).toSafeHtml().asString();
    }
}
