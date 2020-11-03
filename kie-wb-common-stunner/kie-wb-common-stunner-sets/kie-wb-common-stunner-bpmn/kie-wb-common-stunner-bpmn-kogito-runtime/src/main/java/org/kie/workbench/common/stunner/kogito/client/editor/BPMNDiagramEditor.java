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

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.IsWidget;
import elemental2.promise.Promise;
import org.appformer.client.context.EditorContextProvider;
import org.kie.workbench.common.stunner.client.widgets.presenters.Viewer;
import org.kie.workbench.common.stunner.core.client.annotation.DiagramEditor;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.CanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.util.CanvasFileExport;
import org.kie.workbench.common.stunner.core.client.service.ClientRuntimeError;
import org.kie.workbench.common.stunner.core.client.service.ServiceCallback;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.forms.client.widgets.FormsFlushManager;
import org.kie.workbench.common.stunner.kogito.api.editor.impl.KogitoDiagramResourceImpl;
import org.kie.workbench.common.stunner.kogito.client.docks.DiagramEditorPreviewAndExplorerDock;
import org.kie.workbench.common.stunner.kogito.client.docks.DiagramEditorPropertiesDock;
import org.kie.workbench.common.stunner.kogito.client.perspectives.AuthoringPerspective;
import org.kie.workbench.common.stunner.kogito.client.service.AbstractKogitoClientDiagramService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.client.annotations.WorkbenchClientEditor;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.promise.Promises;
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
    private final EditorContextProvider editorContextProvider;
    private final StunnerKogitoEditor kogitoEditor;
    private final AbstractKogitoClientDiagramService diagramServices;
    private final CanvasFileExport canvasFileExport;
    private final DiagramEditorPreviewAndExplorerDock diagramPreviewAndExplorerDock;
    private final DiagramEditorPropertiesDock diagramPropertiesDock;
    private final FormsFlushManager formsFlushManager;

    @Inject
    public BPMNDiagramEditor(Promises promises,
                             EditorContextProvider editorContextProvider,
                             StunnerKogitoEditor kogitoEditor,
                             AbstractKogitoClientDiagramService diagramServices,
                             CanvasFileExport canvasFileExport,
                             DiagramEditorPreviewAndExplorerDock diagramPreviewAndExplorerDock,
                             DiagramEditorPropertiesDock diagramPropertiesDock,
                             FormsFlushManager formsFlushManager) {
        this.promises = promises;
        this.editorContextProvider = editorContextProvider;
        this.kogitoEditor = kogitoEditor;
        this.diagramServices = diagramServices;
        this.canvasFileExport = canvasFileExport;
        this.diagramPreviewAndExplorerDock = diagramPreviewAndExplorerDock;
        this.diagramPropertiesDock = diagramPropertiesDock;
        this.formsFlushManager = formsFlushManager;
    }

    @OnStartup
    public void onStartup(final PlaceRequest place) {
        log("onStartup");

        GWT.setUncaughtExceptionHandler(e -> log("BPMN Editor exception happened!", e));

        boolean isReadOnly = place.getParameter("readOnly", null) != null;
        isReadOnly |= editorContextProvider.isReadOnly();
        // isReadOnly = true;
        log("readonly = " + isReadOnly);
        kogitoEditor.setReadOnly(isReadOnly);

        // Docks.
        diagramPropertiesDock.init(AuthoringPerspective.PERSPECTIVE_ID);
        diagramPreviewAndExplorerDock.init(AuthoringPerspective.PERSPECTIVE_ID);

        // Menus.
        Menus.init();
    }

    @OnOpen
    public void onOpen() {
        log("onOpen");
    }

    @OnFocus
    public void onFocus() {
        log("onFocus");
        kogitoEditor.focus();
    }

    @OnLostFocus
    public void onLostFocus() {
        log("onLostFocus");
        kogitoEditor.lostFocus();
    }

    @IsDirty
    public boolean isDirty() {
        log("isDirty");
        return DiagramState.isDirty();
    }

    @OnMayClose
    public boolean onMayClose() {
        log("onMayClose");
        return !isDirty() || showConfirmClose();
    }

    private boolean showConfirmClose() {
        // TODO: return baseEditorView.confirmClose();
        return true;
    }

    @OnClose
    public void onClose() {
        log("onClose");
        close();
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
        KogitoDiagramResourceImpl resource = new KogitoDiagramResourceImpl(kogitoEditor.getCanvasHandler().getDiagram());
        return diagramServices.transform(resource);
    }

    @GetPreview
    public Promise getPreview() {
        log("getPreview");
        CanvasHandler canvasHandler = kogitoEditor.getCanvasHandler();
        if (canvasHandler != null) {
            return Promise.resolve(canvasFileExport.exportToSvg((AbstractCanvasHandler) canvasHandler));
        } else {
            return Promise.resolve("");
        }
    }

    private void flush() {
        formsFlushManager.flush(kogitoEditor.getSession());
    }

    @SetContent
    public Promise setContent(final String path, final String value) {
        log("setContent ==> " + value);
        Promise<Void> promise =
                promises.create((success, failure) -> {
                    close();
                    diagramServices.transform(path,
                                              value,
                                              new ServiceCallback<Diagram>() {

                                                  @Override
                                                  public void onSuccess(final Diagram diagram) {
                                                      kogitoEditor.open(diagram, new Viewer.Callback() {
                                                          @Override
                                                          public void onSuccess() {
                                                              onDiagramOpenSuccess();
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

    private void onDiagramOpenSuccess() {

        CanvasHandler canvasHandler = kogitoEditor.getCanvasHandler();
        DiagramState.init(() -> canvasHandler.getDiagram());

        // TODO: updateTitle(diagram.getMetadata().getTitle());

        // Configure path's.
        Metadata metadata = canvasHandler.getDiagram().getMetadata();
        String title = metadata.getTitle();
        final String uri = metadata.getRoot().toURI();
        Path path = PathFactory.newPath(title, uri + "/" + title + ".bpmn");
        metadata.setPath(path);

        DiagramState.reset();

        Menus.bind(kogitoEditor.getSession());

        openDocks();
    }

    private void close() {
        Menus.destroy();
        closeDocks();
        kogitoEditor.close();
    }

    private void openDocks() {
        diagramPropertiesDock.open();
        diagramPreviewAndExplorerDock.open();
    }

    private void closeDocks() {
        diagramPropertiesDock.close();
        diagramPreviewAndExplorerDock.close();
    }

    private void onEditorError(ClientRuntimeError error) {
        final String message = error.getThrowable() != null ?
                error.getThrowable().getMessage() : error.getMessage();
        log(message, error.getThrowable());
        kogitoEditor.handleError(error);
    }

    @WorkbenchPartView
    public IsWidget asWidget() {
        return kogitoEditor.getView();
    }

    private static void log(String message, Throwable throwable) {
        if (null != throwable) {
            LOGGER.log(Level.SEVERE, message, throwable);
        } else {
            log(message);
        }
    }

    private static void log(String message) {
        LOGGER.severe(message);
    }

    // TODO
    private static class Menus {

        static void init() {

        }

        static void bind(Object session) {
            // TODO: menuSessionItems.ifPresent(menuItems -> menuItems.bind(getSession()));
        }

        static void destroy() {

        }
    }

    // TODO
    private static class DiagramState {

        private static int currentHash;
        static Supplier<Diagram> diagram;

        static void init(Supplier<Diagram> diagram) {
            DiagramState.diagram = diagram;
        }

        static int getContentHash() {
            if (null == diagram) {
                return 0;
            }
            Diagram diagram = DiagramState.diagram.get();
            if (null == diagram) {
                return 0;
            }
            int hash = diagram.hashCode();
            // TODO: Original impl is also looking for shapes stuff?
            return hash;
        }

        // TODO: Who calls this? somewhere from kogito ( before was BaseKogitoEditor#resetContentHash() )?
        static void reset() {
            currentHash = getContentHash();
        }

        static boolean isDirty() {
            return null != diagram && getContentHash() != currentHash;
        }
    }
}
