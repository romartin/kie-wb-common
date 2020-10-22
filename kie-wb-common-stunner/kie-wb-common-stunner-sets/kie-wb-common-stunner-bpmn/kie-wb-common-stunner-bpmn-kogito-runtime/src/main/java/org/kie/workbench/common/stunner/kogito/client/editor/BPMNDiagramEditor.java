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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import elemental2.promise.Promise;
import org.appformer.client.context.EditorContextProvider;
import org.kie.workbench.common.stunner.client.widgets.editor.StunnerEditor;
import org.kie.workbench.common.stunner.client.widgets.presenters.Viewer;
import org.kie.workbench.common.stunner.core.client.annotation.DiagramEditor;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.CanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.util.CanvasFileExport;
import org.kie.workbench.common.stunner.core.client.service.ClientRuntimeError;
import org.kie.workbench.common.stunner.core.client.service.ServiceCallback;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.forms.client.widgets.FormsFlushManager;
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

    public static final String EDITOR_ID = "BPMNDiagramEditor";

    private final Promises promises;
    private final EditorContextProvider editorContextProvider;
    private final StunnerEditor stunnerEditor;
    private final AbstractKogitoClientDiagramService diagramServices;
    private final CanvasFileExport canvasFileExport;
    private final DiagramEditorPreviewAndExplorerDock diagramPreviewAndExplorerDock;
    private final DiagramEditorPropertiesDock diagramPropertiesDock;
    private final FormsFlushManager formsFlushManager;

    @Inject
    public BPMNDiagramEditor(Promises promises,
                             EditorContextProvider editorContextProvider,
                             StunnerEditor stunnerEditor,
                             AbstractKogitoClientDiagramService diagramServices,
                             CanvasFileExport canvasFileExport,
                             DiagramEditorPreviewAndExplorerDock diagramPreviewAndExplorerDock,
                             DiagramEditorPropertiesDock diagramPropertiesDock,
                             FormsFlushManager formsFlushManager) {
        this.promises = promises;
        this.editorContextProvider = editorContextProvider;
        this.stunnerEditor = stunnerEditor;
        this.diagramServices = diagramServices;
        this.canvasFileExport = canvasFileExport;
        this.diagramPreviewAndExplorerDock = diagramPreviewAndExplorerDock;
        this.diagramPropertiesDock = diagramPropertiesDock;
        this.formsFlushManager = formsFlushManager;
    }

    @OnStartup
    public void onStartup(final PlaceRequest place) {
        boolean isReadOnly = place.getParameter("readOnly", null) != null;
        isReadOnly |= editorContextProvider.isReadOnly();
        // isReadOnly = true;
        stunnerEditor.setReadOnly(isReadOnly);

        // Docks.
        diagramPropertiesDock.init(AuthoringPerspective.PERSPECTIVE_ID);
        diagramPreviewAndExplorerDock.init(AuthoringPerspective.PERSPECTIVE_ID);

        // Editor's commands.
        commandsInit();
    }

    @OnOpen
    public void onOpen() {
    }

    @OnFocus
    public void onFocus() {
        stunnerEditor.focus();
    }

    @OnLostFocus
    public void onLostFocus() {
        stunnerEditor.lostFocus();
    }

    @IsDirty
    public boolean isDirty() {
        return stunnerEditor.isDirty();
    }

    @OnMayClose
    public boolean onMayClose() {
        return !isDirty() || showConfirmClose();
    }

    private boolean showConfirmClose() {
        // TODO: return baseEditorView.confirmClose();
        return true;
    }

    @OnClose
    public void onClose() {
        commandsDestroy();
        diagramPropertiesDock.close();
        diagramPreviewAndExplorerDock.close();
        stunnerEditor.close();
    }

    @WorkbenchPartTitle
    public String getTitleText() {
        return "";
    }

    @WorkbenchPartView
    public IsWidget asWidget() {
        return stunnerEditor.getView();
    }

    @GetContent
    public Promise getContent() {
        if (stunnerEditor.isXmlEditorEnabled()) {
            String value = stunnerEditor.getXmlEditorView().getContent();
            return promises.resolve(value);
        }
        flush();
        return diagramServices.transform(stunnerEditor.getDiagram());
    }

    @GetPreview
    public Promise getPreview() {
        CanvasHandler canvasHandler = stunnerEditor.getCanvasHandler();
        if (canvasHandler != null) {
            return Promise.resolve(canvasFileExport.exportToSvg((AbstractCanvasHandler) canvasHandler));
        } else {
            return Promise.resolve("");
        }
    }

    private void flush() {
        formsFlushManager.flush(stunnerEditor.getSession());
    }

    @SetContent
    public Promise setContent(final String path, final String value) {
        // Close current editor, if any.
        stunnerEditor.close();
        Promise<Void> promise =
                promises.create((success, failure) -> {
                    diagramServices.transform(path,
                                              value,
                                              new ServiceCallback<Diagram>() {

                                                  @Override
                                                  public void onSuccess(final Diagram diagram) {
                                                      stunnerEditor.open(diagram, new Viewer.Callback() {
                                                          @Override
                                                          public void onSuccess() {
                                                              onDiagramOpenSuccess();
                                                              success.onInvoke((Void) null);
                                                          }

                                                          @Override
                                                          public void onError(ClientRuntimeError error) {
                                                              failure.onInvoke(error);
                                                          }
                                                      });
                                                  }

                                                  @Override
                                                  public void onError(final ClientRuntimeError error) {
                                                      stunnerEditor.handleError(error);
                                                      failure.onInvoke(error);
                                                  }
                                              });
                });
        return promise;
    }

    private void onDiagramOpenSuccess() {
        CanvasHandler canvasHandler = stunnerEditor.getCanvasHandler();

        // Configure path's.
        Metadata metadata = canvasHandler.getDiagram().getMetadata();
        String title = metadata.getTitle();
        final String uri = metadata.getRoot().toURI();
        Path path = PathFactory.newPath(title, uri + "/" + title + ".bpmn");
        metadata.setPath(path);

        commandsBind(stunnerEditor.getSession());

        diagramPropertiesDock.open();
        diagramPreviewAndExplorerDock.open();
    }

    private void commandsInit() {
        // TODO
    }

    private void commandsBind(ClientSession session) {
        // TODO
    }

    private void commandsDestroy() {
        // TODO
    }
}
