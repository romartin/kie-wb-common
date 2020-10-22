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

// TODO

// @RunWith(GwtMockitoTestRunner.class)
public class BPMNDiagramEditorTest {

    /*private static final String ELEMENTUUID = "ElementUUID";

    private BPMNDiagramEditor editor;

    @Mock
    private DiagramEditorCore.View view;

    @Mock
    private FileMenuBuilder fileMenuBuilder;

    @Mock
    private PlaceManager placeManager;

    @Mock
    private MultiPageEditorContainerView multiPageEditorContainerView;

    @Mock
    private EventSourceMock<ChangeTitleWidgetEvent> changeTitleNotificationEvent;

    @Mock
    private EventSourceMock<NotificationEvent> notificationEvent;

    @Mock
    private EventSourceMock<OnDiagramFocusEvent> onDiagramFocusEvent;

    @Mock
    private TextEditorView xmlEditorView;

    @Mock
    private ManagedInstance<SessionEditorPresenter<EditorSession>> editorSessionPresenterInstances;

    @Mock
    private ManagedInstance<SessionViewerPresenter<ViewerSession>> viewerSessionPresenterInstances;

    @Mock
    private BPMNStandaloneEditorMenuSessionItems menuSessionItems;

    @Mock
    private ErrorPopupPresenter errorPopupPresenter;

    @Mock
    private DiagramClientErrorHandler diagramClientErrorHandler;

    @Mock
    private ClientTranslationService translationService;

    @Mock
    private DocumentationView documentationView;

    @Mock
    private DiagramEditorPreviewAndExplorerDock diagramPreviewAndExplorerDock;

    @Mock
    private DiagramEditorPropertiesDock diagramPropertiesDock;

    @Mock
    private LayoutHelper layoutHelper;

    @Mock
    private OpenDiagramLayoutExecutor openDiagramLayoutExecutor;

    @Mock
    private AbstractKogitoClientDiagramService diagramServices;

    @Mock
    private CanvasFileExport canvasFileExport;

    @Mock
    private FormsFlushManager formsFlushManager;

    @Mock
    private SessionPresenter sessionPresenter;

    @Mock
    private ClientSession clientSession;

    private Promises promises = new SyncPromises();

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        editor = spy(new BPMNDiagramEditor(view,
                                           fileMenuBuilder,
                                           placeManager,
                                           multiPageEditorContainerView,
                                           changeTitleNotificationEvent,
                                           notificationEvent,
                                           onDiagramFocusEvent,
                                           xmlEditorView,
                                           editorSessionPresenterInstances,
                                           viewerSessionPresenterInstances,
                                           menuSessionItems,
                                           errorPopupPresenter,
                                           diagramClientErrorHandler,
                                           translationService,
                                           documentationView,
                                           diagramPreviewAndExplorerDock,
                                           diagramPropertiesDock,
                                           layoutHelper,
                                           openDiagramLayoutExecutor,
                                           diagramServices,
                                           formsFlushManager,
                                           canvasFileExport,
                                           promises));

        when(editor.getSessionPresenter()).thenReturn(sessionPresenter);
        when(sessionPresenter.getInstance()).thenReturn(clientSession);
    }

    @Test
    public void testMenuInitialized() {
        editor.menuBarInitialized = false;
        editor.makeMenuBar();
        assertEquals(editor.menuBarInitialized, true);

        editor.menuBarInitialized = true;
        editor.makeMenuBar();
        assertEquals(editor.menuBarInitialized, true);
    }

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
    }*/
}