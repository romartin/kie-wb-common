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

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
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

@ApplicationScoped
public class StunnerKogitoEditor {

    private final ManagedInstance<SessionEditorPresenter<EditorSession>> editorSessionPresenterInstances;
    private final ManagedInstance<SessionViewerPresenter<ViewerSession>> viewerSessionPresenterInstances;
    private final FlowPanel view;

    private SessionDiagramPresenter diagramPresenter;
    private boolean isReadOnly;

    @Inject
    public StunnerKogitoEditor(ManagedInstance<SessionEditorPresenter<EditorSession>> editorSessionPresenterInstances,
                               ManagedInstance<SessionViewerPresenter<ViewerSession>> viewerSessionPresenterInstances) {
        this.editorSessionPresenterInstances = editorSessionPresenterInstances;
        this.viewerSessionPresenterInstances = viewerSessionPresenterInstances;
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
