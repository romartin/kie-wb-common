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

package org.kie.workbench.common.stunner.core.client.components.proxies;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.Canvas;
import org.kie.workbench.common.stunner.core.client.canvas.event.selection.CanvasSelectionEvent;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommand;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommandFactory;
import org.kie.workbench.common.stunner.core.client.command.SessionCommandManager;
import org.kie.workbench.common.stunner.core.client.event.keyboard.KeyDownEvent;
import org.kie.workbench.common.stunner.core.client.event.keyboard.KeyboardEvent;
import org.kie.workbench.common.stunner.core.client.shape.EdgeShape;
import org.kie.workbench.common.stunner.core.client.shape.NodeShape;
import org.kie.workbench.common.stunner.core.command.Command;
import org.kie.workbench.common.stunner.core.command.impl.DeferredCommand;
import org.kie.workbench.common.stunner.core.command.impl.DeferredCompositeCommand;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.Bounds;
import org.kie.workbench.common.stunner.core.graph.content.view.MagnetConnection;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewImpl;
import org.kie.workbench.common.stunner.core.graph.impl.EdgeImpl;
import org.kie.workbench.common.stunner.core.graph.impl.NodeImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.mocks.EventSourceMock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NodeProxyTest {

    private static final String SHAPE_SET_ID = "ss1";
    private static final String TARGET_NODE_ID = "target1";
    private static final String EDGE_ID = "edge1";

    @Mock
    private SessionCommandManager<AbstractCanvasHandler> commandManager;

    @Mock
    private EventSourceMock<CanvasSelectionEvent> selectionEvent;

    @Mock
    private CanvasCommandFactory<AbstractCanvasHandler> commandFactory;

    @Mock
    private AbstractCanvasHandler canvasHandler;

    @Mock
    private Canvas canvas;

    @Mock
    private Diagram diagram;

    @Mock
    private Metadata metadata;

    @Mock
    private EdgeShape connector;

    @Mock
    private NodeShape targetShape;

    private NodeProxy tested;
    private ElementProxy proxy;
    private ElementProxyTest.ElementProxyViewMock<NodeShape> view;
    private Edge<ViewConnector<?>, Node> edge;
    private Node<View<?>, Edge> sourceNode;
    private Node<View<?>, Edge> targetNode;

    @Before
    public void setUp() {
        sourceNode = new NodeImpl<>("sourceNode");
        sourceNode.setContent(new ViewImpl<>(mock(Object.class),
                                             Bounds.create()));
        targetNode = new NodeImpl<>(TARGET_NODE_ID);
        targetNode.setContent(new ViewImpl<>(mock(Object.class),
                                             Bounds.create()));
        edge = new EdgeImpl<>(EDGE_ID);
        proxy = spy(new ElementProxy(commandManager, selectionEvent));
        view = spy(new ElementProxyTest.ElementProxyViewMock<>());
        when(canvasHandler.getCanvas()).thenReturn(canvas);
        when(canvasHandler.getDiagram()).thenReturn(diagram);
        when(diagram.getMetadata()).thenReturn(metadata);
        when(metadata.getShapeSetId()).thenReturn(SHAPE_SET_ID);
        when(canvas.getShape(eq(EDGE_ID))).thenReturn(connector);
        when(canvas.getShape(eq(TARGET_NODE_ID))).thenReturn(targetShape);
        tested = new NodeProxy(proxy, view, commandFactory)
                .setCanvasHandler(canvasHandler)
                .setSourceNode(sourceNode)
                .setEdge(edge)
                .setTargetNode(targetNode);
    }

    @Test
    public void testInit() {
        tested.init();
        verify(proxy, times(1)).setView(eq(view));
        verify(proxy, times(1)).setProxyBuilder(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testStart() {
        CanvasCommand<AbstractCanvasHandler> addConnector = mock(CanvasCommand.class);
        CanvasCommand<AbstractCanvasHandler> addNode = mock(CanvasCommand.class);
        CanvasCommand<AbstractCanvasHandler> setTargetNode = mock(CanvasCommand.class);
        doReturn(addConnector).when(commandFactory).addConnector(eq(sourceNode),
                                                                 eq(edge),
                                                                 any(MagnetConnection.class),
                                                                 eq(SHAPE_SET_ID));
        doReturn(addNode).when(commandFactory).addNode(eq(targetNode),
                                                       eq(SHAPE_SET_ID));
        doReturn(setTargetNode).when(commandFactory).setTargetNode(eq(targetNode),
                                                                   eq(edge),
                                                                   any());
        tested.init();
        tested.start(1d, 2d);
        verify(proxy, times(1)).start(eq(1d), eq(2d));
        NodeShape targetNodeShape = view.getShapeBuilder().get();
        assertEquals(targetShape, targetNodeShape);
        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(proxy, times(1)).execute(commandCaptor.capture());
        DeferredCompositeCommand command = (DeferredCompositeCommand) commandCaptor.getValue();
        List commands = command.getCommands();
        assertEquals(3, command.size());
        DeferredCommand c0 = (DeferredCommand) commands.get(0);
        assertEquals(addNode, c0.getCommand());
        DeferredCommand c1 = (DeferredCommand) commands.get(1);
        assertEquals(addConnector, c1.getCommand());
        DeferredCommand c2 = (DeferredCommand) commands.get(2);
        assertEquals(setTargetNode, c2.getCommand());
    }

    @Test
    public void testDestroy() {
        tested.init();
        tested.destroy();
        verify(proxy, times(1)).destroy();
    }

    @Test
    public void testCancelKey() {
        KeyDownEvent event = new KeyDownEvent(KeyboardEvent.Key.ESC);
        tested.init();
        tested.onKeyDownEvent(event);
        tested.onKeyDownEvent(new KeyDownEvent(KeyboardEvent.Key.CONTROL));
        tested.onKeyDownEvent(new KeyDownEvent(KeyboardEvent.Key.ALT));
        tested.onKeyDownEvent(new KeyDownEvent(KeyboardEvent.Key.DELETE));
        verify(proxy, times(1)).destroy();
    }
}
