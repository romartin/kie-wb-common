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

import java.util.function.BiFunction;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.Canvas;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommandFactory;
import org.kie.workbench.common.stunner.core.client.command.CanvasViolation;
import org.kie.workbench.common.stunner.core.client.event.keyboard.KeyDownEvent;
import org.kie.workbench.common.stunner.core.client.shape.NodeShape;
import org.kie.workbench.common.stunner.core.client.shape.Shape;
import org.kie.workbench.common.stunner.core.client.shape.ShapeState;
import org.kie.workbench.common.stunner.core.client.shape.view.event.AbstractMouseEvent;
import org.kie.workbench.common.stunner.core.command.impl.DeferredCompositeCommand;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.MagnetConnection;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;

@Dependent
public class NodeProxy implements ShapeProxy {

    private final ElementProxy proxy;
    private final ShapeProxyView<NodeShape> view;

    private BiFunction<Node, Node, MagnetConnection> magnetConnectionBuilder;
    private Node<View<?>, Edge> targetNode;
    private Edge<ViewConnector<?>, Node> edge;
    private Node<View<?>, Edge> sourceNode;

    @Inject
    public NodeProxy(final ElementProxy proxy,
                     final ShapeProxyView<NodeShape> view) {
        this.proxy = proxy;
        this.view = view;
        this.magnetConnectionBuilder = MagnetConnection.Builder::forTarget;
    }

    @PostConstruct
    public void init() {
        proxy
                .setView(view)
                .setProxyBuilder(this::onCreateProxy);
    }

    public NodeProxy setMagnetConnectionBuilder(final BiFunction<Node, Node, MagnetConnection> magnetConnectionBuilder) {
        this.magnetConnectionBuilder = magnetConnectionBuilder;
        return this;
    }

    public NodeProxy setCanvasHandler(final AbstractCanvasHandler canvasHandler) {
        proxy.setCanvasHandler(canvasHandler);
        return this;
    }

    public NodeProxy setTargetNode(Node<View<?>, Edge> targetNode) {
        this.targetNode = targetNode;
        return this;
    }

    public NodeProxy setEdge(Edge<ViewConnector<?>, Node> edge) {
        this.edge = edge;
        return this;
    }

    public NodeProxy setSourceNode(Node<View<?>, Edge> sourceNode) {
        this.sourceNode = sourceNode;
        return this;
    }

    public NodeProxy start(final AbstractMouseEvent event) {
        start(event.getX(), event.getY());
        return this;
    }

    @Override
    public void start(final double x,
                      final double y) {
        proxy.start(x, y);
    }

    @Override
    public void destroy() {
        proxy.destroy();
        targetNode = null;
        edge = null;
        sourceNode = null;
        magnetConnectionBuilder = null;
    }

    void onKeyDownEvent(final @Observes KeyDownEvent event) {
        proxy.handleCancelKey(event.getKey());
    }

    private NodeShape onCreateProxy() {
        final AbstractCanvasHandler canvasHandler = proxy.getCanvasHandler();
        final CanvasCommandFactory<AbstractCanvasHandler> commandFactory = proxy.lookupCanvasFactory();
        final String rootUUID = canvasHandler.getDiagram().getMetadata().getCanvasRootUUID();

        proxy.execute(new DeferredCompositeCommand.Builder<AbstractCanvasHandler, CanvasViolation>()
                              .deferCommand(() -> null != rootUUID ?
                                      commandFactory.addChildNode(canvasHandler.getGraphIndex().getNode(rootUUID),
                                                                  targetNode,
                                                                  getShapeSetId()) :
                                      commandFactory.addNode(targetNode,
                                                             getShapeSetId()))
                              .deferCommand(() -> commandFactory.addConnector(sourceNode,
                                                                              edge,
                                                                              magnetConnectionBuilder.apply(sourceNode, targetNode),
                                                                              getShapeSetId()))
                              // TODO: BPMN should provide an specific different magnet approach...
                              .deferCommand(() -> commandFactory.setTargetNode(targetNode,
                                                                               edge,
                                                                               magnetConnectionBuilder.apply(targetNode, sourceNode)))
                              .build());

        final Canvas canvas = proxy.getCanvas();
        final NodeShape targetShape = (NodeShape) canvas.getShape(targetNode.getUUID());
        final Shape<?> edgeShape = canvas.getShape(edge.getUUID());
        edgeShape.applyState(ShapeState.SELECTED);
        return targetShape;
    }

    private Metadata getMetadata() {
        return proxy.getCanvasHandler().getDiagram().getMetadata();
    }

    private String getShapeSetId() {
        return getMetadata().getShapeSetId();
    }
}
