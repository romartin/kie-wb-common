/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import elemental2.promise.Promise;
import org.appformer.client.context.EditorContextProvider;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.kie.workbench.common.stunner.client.widgets.presenters.Viewer;
import org.kie.workbench.common.stunner.client.widgets.presenters.session.SessionDiagramPresenter;
import org.kie.workbench.common.stunner.client.widgets.presenters.session.SessionPresenter;
import org.kie.workbench.common.stunner.client.widgets.presenters.session.impl.SessionEditorPresenter;
import org.kie.workbench.common.stunner.client.widgets.presenters.session.impl.SessionViewerPresenter;
import org.kie.workbench.common.stunner.core.client.annotation.DiagramEditor;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.CanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.util.CanvasFileExport;
import org.kie.workbench.common.stunner.core.client.service.ClientRuntimeError;
import org.kie.workbench.common.stunner.core.client.service.ServiceCallback;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.client.session.impl.EditorSession;
import org.kie.workbench.common.stunner.core.client.session.impl.ViewerSession;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.kogito.client.service.AbstractKogitoClientDiagramService;
import org.uberfire.client.annotations.WorkbenchClientEditor;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.promise.Promises;
import org.uberfire.client.workbench.widgets.common.ErrorPopupPresenter;
import org.uberfire.lifecycle.GetContent;
import org.uberfire.lifecycle.GetPreview;
import org.uberfire.lifecycle.IsDirty;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnFocus;
import org.uberfire.lifecycle.OnLostFocus;
import org.uberfire.lifecycle.OnMayClose;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.lifecycle.SetContent;
import org.uberfire.mvp.PlaceRequest;

@ApplicationScoped
@DiagramEditor
@WorkbenchClientEditor(identifier = BPMNDiagramEditor.EDITOR_ID)
public class BPMNDiagramEditor {

    private static final Logger LOGGER = Logger.getLogger(BPMNDiagramEditor.class.getName());

    public static final String EDITOR_ID = "BPMNDiagramEditor";

    private final Promises promises;
    private final FlowPanel view;
    private final EditorContextProvider editorContextProvider;
    private final ManagedInstance<SessionEditorPresenter<EditorSession>> editorSessionPresenterInstances;
    private final ManagedInstance<SessionViewerPresenter<ViewerSession>> viewerSessionPresenterInstances;
    protected final AbstractKogitoClientDiagramService diagramServices;
    private final CanvasFileExport canvasFileExport;
    private final ErrorPopupPresenter errorPopupPresenter;

    private SessionDiagramPresenter diagramPresenter;

    @Inject
    public BPMNDiagramEditor(Promises promises,
                             EditorContextProvider editorContextProvider,
                             ManagedInstance<SessionEditorPresenter<EditorSession>> editorSessionPresenterInstances,
                             ManagedInstance<SessionViewerPresenter<ViewerSession>> viewerSessionPresenterInstances,
                             AbstractKogitoClientDiagramService diagramServices,
                             CanvasFileExport canvasFileExport,
                             ErrorPopupPresenter errorPopupPresenter) {
        this.promises = promises;
        this.editorContextProvider = editorContextProvider;
        this.editorSessionPresenterInstances = editorSessionPresenterInstances;
        this.viewerSessionPresenterInstances = viewerSessionPresenterInstances;
        this.diagramServices = diagramServices;
        this.canvasFileExport = canvasFileExport;
        this.errorPopupPresenter = errorPopupPresenter;
        this.view = new FlowPanel();
    }

    @OnStartup
    public void onStartup(final PlaceRequest place) {
        log("onStartup");

        boolean isReadOnly = place.getParameter("readOnly", null) != null;
        isReadOnly |= editorContextProvider.isReadOnly();
        // isReadOnly = true;
        log("readonly = " + isReadOnly);

        // TODO: Init docks.
        // diagramPropertiesDock.init(AuthoringPerspective.PERSPECTIVE_ID);
        // diagramPreviewAndExplorerDock.init(AuthoringPerspective.PERSPECTIVE_ID);

        // TODO: Init Menus?

        // Instantiate editor instance.
        if (!isReadOnly) {
            diagramPresenter = editorSessionPresenterInstances.get();
        } else {
            diagramPresenter = viewerSessionPresenterInstances.get();
        }
        view.add(diagramPresenter.getView());
    }

    @OnOpen
    public void onOpen() {
        log("onOpen");
    }

    @OnFocus
    public void onFocus() {
        log("onFocus");
        diagramPresenter.focus();
    }

    @OnLostFocus
    public void onLostFocus() {
        log("onLostFocus");
        diagramPresenter.lostFocus();
    }

    @IsDirty
    public boolean isDirty() {
        log("isDirty");
        // TODO
        return false;
    }

    @OnMayClose
    public boolean onMayClose() {
        log("onMayClose");
        return false;
    }

    @OnClose
    public void onClose() {
        log("onClose");
        // TODO: menuSessionItems.destroy();
        // TODO: presenter.destroySession();
        // TODO: viewer/editorSessionPresenterInstances.destroyAll(); ?
    }

    @WorkbenchPartTitle
    public String getTitleText() {
        // TODO
        return "Rogeeeer Title";
    }

    /*@WorkbenchPartTitleDecoration
    public IsWidget getTitle() {

    }*/

    /*@WorkbenchMenu
    public void getMenus(final Consumer<Menus> menusConsumer) {

    }*/

    @GetContent
    public Promise getContent() {
        log("getContent");
        flush();
        //  TODO: return diagramServices.transform(getEditor().getEditorProxy().getContentSupplier().get());
        return promises.resolve("rogeeeeeerContent");
    }

    private ClientSession getSession() {
        return (ClientSession) diagramPresenter.getInstance();
    }

    private CanvasHandler getCanvasHandler() {
        return (CanvasHandler) diagramPresenter.getHandler();
    }

    @GetPreview
    public Promise getPreview() {
        log("getPreview");
        //return promises.resolve("rogeeeeeerPreview");
        CanvasHandler canvasHandler = getCanvasHandler();
        if (canvasHandler != null) {
            return Promise.resolve(canvasFileExport.exportToSvg((AbstractCanvasHandler) canvasHandler));
        } else {
            return Promise.resolve("");
        }
    }

    void flush() {
        // TODO: formsFlushManager.flush(getSession(), formElementUUID);
    }

    @SetContent
    public Promise setContent(final String path, final String value) {
        log("setContent ==> " + value);
        //return promises.resolve();
        Promise<Void> promise =
                promises.create((success, failure) -> {
                    onClose();
                    diagramServices.transform(path,
                                              value,
                                              new ServiceCallback<Diagram>() {

                                                  @Override
                                                  public void onSuccess(final Diagram diagram) {
                                                      open(diagram, new Viewer.Callback() {
                                                          @Override
                                                          public void onSuccess() {
                                                              success.onInvoke((Void) null);
                                                          }

                                                          @Override
                                                          public void onError(ClientRuntimeError error) {
                                                              onEditorError(error);
                                                              failure.onInvoke(error);
                                                          }
                                                      });
                                                  }

                                                  @Override
                                                  public void onError(final ClientRuntimeError error) {
                                                      onEditorError(error);
                                                      failure.onInvoke(error);
                                                  }
                                              });
                });
        return promise;
    }

    public void open(final Diagram diagram,
                     final Viewer.Callback callback) {
        // TODO: this.layoutHelper.applyLayout(diagram, openDiagramLayoutExecutor);

        // TODO: baseEditorView.showLoading();

        diagramPresenter.open(diagram, new SessionPresenter.SessionPresenterCallback() {

            @Override
            public void onSuccess() {
                // TODO: initialiseKieEditorForSession(diagram);
                // TODO: menuSessionItems.ifPresent(menuItems -> menuItems.bind(getSession()));
                callback.onSuccess();
            }

            @Override
            public void onError(ClientRuntimeError error) {
                // TODO: onLoadError(error);
                onEditorError(error);
                callback.onError(error);
            }
        });
    }

    private void onEditorError(ClientRuntimeError error) {
        log(error.getMessage());
        errorPopupPresenter.showMessage(error.getMessage());
    }

    @WorkbenchPartView
    public IsWidget asWidget() {
        return view;
    }

    private static void log(String message) {
        LOGGER.severe(message);
    }
}
