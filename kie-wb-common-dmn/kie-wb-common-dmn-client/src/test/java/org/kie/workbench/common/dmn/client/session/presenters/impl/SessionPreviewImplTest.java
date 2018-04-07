/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.dmn.client.session.presenters.impl;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.kie.workbench.common.dmn.client.session.BaseCommandsTest;
import org.mockito.runners.MockitoJUnitRunner;

// TODO
@Ignore

@RunWith(MockitoJUnitRunner.class)
public class SessionPreviewImplTest extends BaseCommandsTest {
/*
    @Mock
    private DefinitionManager definitionManager;

    @Mock
    private ShapeManager shapeManager;

    @Mock
    private TextPropertyProviderFactory textPropertyProviderFactory;

    @Mock
    private CanvasCommandManager<AbstractCanvasHandler> canvasCommandManager;

    @Mock
    private DefinitionUtils definitionUtils;

    @Mock
    private GraphUtils graphUtils;

    @Mock
    private ManagedInstance<BaseCanvasHandler> canvasHandlerFactories;

    @Mock
    private ManagedInstance<CanvasCommandFactory> canvasCommandFactories;

    @Mock
    private SelectionControl<AbstractCanvasHandler, ?> selectionControl;

    @Mock
    private WidgetWrapperView view;

    @Mock
    private AbstractClientSession session;

    @Mock
    private SessionViewer.SessionViewerCallback callback;

    @Mock
    private CanvasFactory canvasFactory;

    @Mock
    private ZoomControl zoomControl;

    @Mock
    private AbstractCanvas canvas;

    @Mock
    private AbstractCanvas.View canvasView;

    private DMNSessionPreview preview;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        this.preview = new DMNSessionPreview(definitionManager,
                                             shapeManager,
                                             textPropertyProviderFactory,
                                             canvasCommandManager,
                                             definitionUtils,
                                             graphUtils,
                                             canvasHandlerFactories,
                                             canvasCommandFactories,
                                             selectionControl,
                                             view);
        final DiagramImpl diagram = new DiagramImpl("diagram",
                                                    new MetadataImpl());
        final GraphImpl graph = new GraphImpl("graph",
                                              new GraphNodeStoreImpl());
        final DefinitionSetImpl definitionSet = new DefinitionSetImpl("id");
        diagram.setGraph(graph);
        graph.setContent(definitionSet);
        definitionSet.setBounds(new BoundsImpl(new BoundImpl(0.0,
                                                             0.0),
                                               new BoundImpl(100.0,
                                                             100.0)));

        when(session.getCanvasHandler()).thenReturn(canvasHandler);
        when(session.getCanvas()).thenReturn(canvas);
        when(canvasHandler.getDiagram()).thenReturn(diagram);
        when(shapeManager.getCanvasFactory(any(Diagram.class))).thenReturn(canvasFactory);
        when(canvasFactory.newCanvas()).thenReturn(canvas);
        when(canvasFactory.newControl(eq(ZoomControl.class))).thenReturn(zoomControl);
        when(canvasHandlerFactories.select(any(Annotation.class))).thenReturn(canvasHandlerFactories);
        when(canvasHandlerFactories.get()).thenReturn(canvasHandler);
        when(canvas.getView()).thenReturn(canvasView);

        preview.open(session,
                     callback);
    }

    @Test
    public void checkExecutionCommands() {
        final AbstractCanvasGraphCommand command = new MockCommand();
        preview.handleCanvasCommandExecutedEvent(makeCommandExecutionContext(command));

        verify(canvasCommandManager).execute(any(BaseCanvasHandler.class),
                                             eq(command));
    }

    @Test
    public void checkVetoExecutionCommands() {
        final AbstractCanvasGraphCommand command = new MockVetoExecutionCommand();
        preview.handleCanvasCommandExecutedEvent(makeCommandExecutionContext(command));

        verify(canvasCommandManager,
               never()).execute(any(AbstractCanvasHandler.class),
                                any(AbstractCanvasGraphCommand.class));
    }

    @Test
    public void checkUndoCommands() {
        final AbstractCanvasGraphCommand command = new MockCommand();
        preview.handleCanvasUndoCommandExecutedEvent(makeCommandUndoContext(command));

        verify(canvasCommandManager).undo(any(BaseCanvasHandler.class),
                                          eq(command));
    }

    @Test
    public void checkVetoUndoCommands() {
        final AbstractCanvasGraphCommand command = new MockVetoUndoCommand();
        preview.handleCanvasUndoCommandExecutedEvent(makeCommandUndoContext(command));

        verify(canvasCommandManager,
               never()).undo(any(AbstractCanvasHandler.class),
                             any(AbstractCanvasGraphCommand.class));
    }*/
}
