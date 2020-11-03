/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.kogito.client.editor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import elemental2.dom.DomGlobal;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.kie.workbench.common.stunner.client.widgets.presenters.Viewer;
import org.kie.workbench.common.stunner.client.widgets.presenters.session.SessionDiagramPresenter;
import org.kie.workbench.common.stunner.client.widgets.presenters.session.SessionPresenter;
import org.kie.workbench.common.stunner.client.widgets.presenters.session.impl.SessionEditorPresenter;
import org.kie.workbench.common.stunner.client.widgets.presenters.session.impl.SessionViewerPresenter;
import org.kie.workbench.common.stunner.core.client.canvas.CanvasHandler;
import org.kie.workbench.common.stunner.core.client.service.ClientRuntimeError;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.client.session.impl.EditorSession;
import org.kie.workbench.common.stunner.core.client.session.impl.ViewerSession;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.DiagramParsingException;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.uberfire.client.workbench.widgets.common.ErrorPopupPresenter;
import org.uberfire.ext.widgets.common.client.ace.AceEditorMode;
import org.uberfire.ext.widgets.core.client.editors.texteditor.TextEditorView;

@ApplicationScoped
public class StunnerKogitoEditor {

    private final ManagedInstance<SessionEditorPresenter<EditorSession>> editorSessionPresenterInstances;
    private final ManagedInstance<SessionViewerPresenter<ViewerSession>> viewerSessionPresenterInstances;
    private final TextEditorView xmlEditorView;
    private final ErrorPopupPresenter errorPopupPresenter;
    private final FlowPanel view;

    private SessionDiagramPresenter diagramPresenter;
    private boolean isReadOnly;

    @Inject
    public StunnerKogitoEditor(ManagedInstance<SessionEditorPresenter<EditorSession>> editorSessionPresenterInstances,
                               ManagedInstance<SessionViewerPresenter<ViewerSession>> viewerSessionPresenterInstances,
                               TextEditorView xmlEditorView,
                               ErrorPopupPresenter errorPopupPresenter) {
        this.editorSessionPresenterInstances = editorSessionPresenterInstances;
        this.viewerSessionPresenterInstances = viewerSessionPresenterInstances;
        this.xmlEditorView = xmlEditorView;
        this.errorPopupPresenter = errorPopupPresenter;
        this.isReadOnly = false;
        this.view = new FlowPanel();
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }

    public void open(final Diagram diagram,
                     final Viewer.Callback callback) {
        showLoading();
        init();
        diagramPresenter.open(diagram, new SessionPresenter.SessionPresenterCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
                hideLoading();
            }

            @Override
            public void onError(ClientRuntimeError error) {
                hideLoading();
                callback.onError(error);
            }
        });
    }

    public void handleError(final ClientRuntimeError error) {
        final Throwable e = error.getThrowable();
        if (e instanceof DiagramParsingException) {
            final DiagramParsingException dpe = (DiagramParsingException) e;
            final Metadata metadata = dpe.getMetadata();
            final String xml = dpe.getXml();

            // TODO: setOriginalContentHash(xml.hashCode());
            // TODO: updateTitle(metadata.getTitle());
            // TODO: menuSessionItems.setEnabled(false);

            close();

            xmlEditorView.setReadOnly(isReadOnly());
            xmlEditorView.setContent(xml, AceEditorMode.XML);
            view.add(xmlEditorView.asWidget());
            // TODO getNotificationEvent().fire(new NotificationEvent(translationService.getValue(KogitoClientConstants.DIAGRAM_PARSING_ERROR, Objects.toString(e.getMessage(), "")), NotificationEvent.NotificationType.ERROR));

            Scheduler.get().scheduleDeferred(xmlEditorView::onResize);
        } else {
            final String message = error.getThrowable() != null ?
                    error.getThrowable().getMessage() : error.getMessage();
            DomGlobal.console.error("[StunnerKogitoEditor] ERROR: " + message);
            errorPopupPresenter.showMessage(message);
            //close editor in case of error when opening the editor
            // TODO getPlaceManager().forceClosePlace(getPlaceRequest());
        }
    }

    private void init() {
        close();
        if (!isReadOnly) {
            diagramPresenter = editorSessionPresenterInstances.get();
        } else {
            diagramPresenter = viewerSessionPresenterInstances.get();
        }
        diagramPresenter.displayNotifications(type -> true);
        view.add(diagramPresenter.getView());
    }

    public void focus() {
        if (!isClosed()) {
            diagramPresenter.focus();
        }
    }

    public void lostFocus() {
        if (!isClosed()) {
            diagramPresenter.lostFocus();
        }
    }

    public void close() {
        if (!isClosed()) {
            diagramPresenter.destroy();
            diagramPresenter = null;
            editorSessionPresenterInstances.destroyAll();
            viewerSessionPresenterInstances.destroyAll();
            view.clear();
        }
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    private boolean isClosed() {
        return null == diagramPresenter;
    }

    public ClientSession getSession() {
        return (ClientSession) diagramPresenter.getInstance();
    }

    public CanvasHandler getCanvasHandler() {
        return (CanvasHandler) diagramPresenter.getHandler();
    }

    public SessionDiagramPresenter getPresenter() {
        return diagramPresenter;
    }

    private void showLoading() {
        // TODO
    }

    private void hideLoading() {
        // TODO
    }

    public IsWidget getView() {
        return view;
    }
}
