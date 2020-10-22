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

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import elemental2.promise.Promise;
import org.appformer.client.context.EditorContextProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.client.widgets.editor.StunnerEditor;
import org.kie.workbench.common.stunner.core.client.canvas.CanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.util.CanvasFileExport;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.diagram.DiagramImpl;
import org.kie.workbench.common.stunner.core.diagram.MetadataImpl;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.forms.client.widgets.FormsFlushManager;
import org.kie.workbench.common.stunner.kogito.client.docks.DiagramEditorPreviewAndExplorerDock;
import org.kie.workbench.common.stunner.kogito.client.docks.DiagramEditorPropertiesDock;
import org.kie.workbench.common.stunner.kogito.client.perspectives.AuthoringPerspective;
import org.kie.workbench.common.stunner.kogito.client.service.AbstractKogitoClientDiagramService;
import org.mockito.Mock;
import org.uberfire.client.promise.Promises;
import org.uberfire.ext.widgets.core.client.editors.texteditor.TextEditorView;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.promise.SyncPromises;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class BPMNDiagramEditorTest {

    private Promises promises;
    @Mock
    private EditorContextProvider editorContextProvider;
    @Mock
    private StunnerEditor stunnerEditor;
    @Mock
    private AbstractKogitoClientDiagramService diagramServices;
    @Mock
    private CanvasFileExport canvasFileExport;
    @Mock
    private DiagramEditorPreviewAndExplorerDock diagramPreviewAndExplorerDock;
    @Mock
    private DiagramEditorPropertiesDock diagramPropertiesDock;
    @Mock
    private FormsFlushManager formsFlushManager;
    @Mock
    private ClientSession session;
    @Mock
    private CanvasHandler canvasHandler;
    private DiagramImpl diagram;

    private BPMNDiagramEditor tested;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        promises = new SyncPromises();
        diagram = new DiagramImpl("testDiagram",
                                  mock(Graph.class),
                                  new MetadataImpl.MetadataImplBuilder("testSet").build());
        when(session.getCanvasHandler()).thenReturn(canvasHandler);
        when(canvasHandler.getDiagram()).thenReturn(diagram);
        when(stunnerEditor.getSession()).thenReturn(session);
        when(stunnerEditor.getCanvasHandler()).thenReturn(canvasHandler);
        when(stunnerEditor.getDiagram()).thenReturn(diagram);
        tested = new BPMNDiagramEditor(promises,
                                       editorContextProvider,
                                       stunnerEditor,
                                       diagramServices,
                                       canvasFileExport,
                                       diagramPreviewAndExplorerDock,
                                       diagramPropertiesDock,
                                       formsFlushManager);
    }

    @Test
    public void testStartup() {
        when(editorContextProvider.isReadOnly()).thenReturn(false);
        tested.onStartup(new DefaultPlaceRequest());
        verify(stunnerEditor, times(1)).setReadOnly(eq(false));
        verifyDocksAreInit();
    }

    @Test
    public void testStartupReadOnly() {
        when(editorContextProvider.isReadOnly()).thenReturn(true);
        tested.onStartup(new DefaultPlaceRequest());
        verify(stunnerEditor, times(1)).setReadOnly(eq(true));
        verifyDocksAreInit();
    }

    private void verifyDocksAreInit() {
        verify(diagramPropertiesDock, times(1)).init(eq(AuthoringPerspective.PERSPECTIVE_ID));
        verify(diagramPreviewAndExplorerDock, times(1)).init(eq(AuthoringPerspective.PERSPECTIVE_ID));
    }

    @Test
    public void testOnFocus() {
        tested.onFocus();
        verify(stunnerEditor, times(1)).focus();
        verify(stunnerEditor, never()).lostFocus();
    }

    @Test
    public void testOnLostFocus() {
        tested.onLostFocus();
        verify(stunnerEditor, times(1)).lostFocus();
        verify(stunnerEditor, never()).focus();
    }

    @Test
    public void testIsDirty() {
        when(stunnerEditor.isDirty()).thenReturn(true);
        assertTrue(tested.isDirty());
    }

    @Test
    public void testOnClose() {
        tested.onClose();
        verify(diagramPropertiesDock, times(1)).close();
        verify(diagramPreviewAndExplorerDock, times(1)).close();
        verify(stunnerEditor, times(1)).close();
    }

    @Test
    public void testAsWidget() {
        IsWidget w = mock(IsWidget.class);
        when(stunnerEditor.getView()).thenReturn(w);
        assertEquals(w, tested.asWidget());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetContentFromDiagram() {
        Promise rawValue = mock(Promise.class);
        when(diagramServices.transform(eq(diagram))).thenReturn(rawValue);
        when(stunnerEditor.isXmlEditorEnabled()).thenReturn(false);
        assertEquals(rawValue, tested.getContent());
    }

    @Test
    public void testGetContentFromXmlEditor() {
        when(stunnerEditor.isXmlEditorEnabled()).thenReturn(true);
        TextEditorView textEditorView = mock(TextEditorView.class);
        when(stunnerEditor.getXmlEditorView()).thenReturn(textEditorView);
        when(textEditorView.getContent()).thenReturn("xmlTestContent");

        Promise content = tested.getContent();

        // TODO
    }

    @Test
    public void testGetPreview() {

        // TODO

    }

    @Test
    public void testSetContent() {

        // TODO

    }

    // TODO: Keep old tests here?
    /*

    @Test
    public void testSuperOnCloseOnSetContent() {
        //First setContent call context
        editor.setContent("", "");
        verify(menuSessionItems, times(1)).destroy();

        //Second setContent call context
        final String path = "/project/src/main/resources/diagrams/process.bpmn";
        editor.setContent(path, "");
        verify(menuSessionItems, times(2)).destroy();
    }

    @Test
    public void testDocksAndOrdering() {
        editor.initDocks();
        InOrder initOrder = inOrder(diagramPropertiesDock, diagramPreviewAndExplorerDock);
        initOrder.verify(diagramPropertiesDock).init(eq(AuthoringPerspective.PERSPECTIVE_ID));
        initOrder.verify(diagramPreviewAndExplorerDock).init(eq(AuthoringPerspective.PERSPECTIVE_ID));
        editor.openDocks();
        initOrder.verify(diagramPropertiesDock).open();
        initOrder.verify(diagramPreviewAndExplorerDock).open();
        editor.onClose();
        initOrder.verify(diagramPropertiesDock).close();
        initOrder.verify(diagramPreviewAndExplorerDock).close();
    }

    @Test
    public void testOnFormsOpenedEvent() {
        editor.onFormsOpenedEvent(new FormPropertiesOpened(clientSession, ELEMENTUUID, ""));
        assertEquals(ELEMENTUUID, editor.formElementUUID);
    }

    @Mock
    AbstractDiagramEditorCore<Metadata, Diagram, KogitoDiagramResourceImpl, DiagramEditorProxy<KogitoDiagramResourceImpl>> theEditor;

    @Mock
    private SessionPresenter theSessionPresenter;

    @Test
    public void testGetContent() {
        editor.formElementUUID = ELEMENTUUID;
        editor.getContent();
        verify(formsFlushManager, times(1)).flush(clientSession, ELEMENTUUID);
    }

    @Test
    public void testFlush() {
        editor.formElementUUID = ELEMENTUUID;
        editor.flush();
        verify(formsFlushManager, times(1)).flush(clientSession, ELEMENTUUID);
    }

     */
}