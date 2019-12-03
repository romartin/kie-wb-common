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

package org.kie.workbench.common.stunner.client.lienzo.components.proxy;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.ait.lienzo.client.core.shape.wires.WiresShape;
import com.ait.lienzo.client.core.shape.wires.proxy.WiresDragProxy;
import com.ait.lienzo.client.core.shape.wires.proxy.WiresShapeProxy;
import org.kie.workbench.common.stunner.client.lienzo.canvas.wires.WiresCanvas;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.event.selection.CanvasSelectionEvent;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommandFactory;
import org.kie.workbench.common.stunner.core.client.command.CanvasViolation;
import org.kie.workbench.common.stunner.core.client.command.SessionCommandManager;
import org.kie.workbench.common.stunner.core.client.components.proxy.NodeProxy;
import org.kie.workbench.common.stunner.core.client.event.keyboard.KeyDownEvent;
import org.kie.workbench.common.stunner.core.client.event.keyboard.KeyboardEvent;
import org.kie.workbench.common.stunner.core.client.shape.Shape;
import org.kie.workbench.common.stunner.core.client.shape.ShapeState;
import org.kie.workbench.common.stunner.core.client.shape.view.event.AbstractMouseEvent;
import org.kie.workbench.common.stunner.core.command.Command;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.command.impl.DeferredCompositeCommand;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.MagnetConnection.Builder;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;

@Dependent
public class LienzoNodeProxy implements NodeProxy {

    private final SessionCommandManager<AbstractCanvasHandler> commandManager;
    private final CanvasCommandFactory<AbstractCanvasHandler> commandFactory;
    private final Event<CanvasSelectionEvent> selectionEvent;
    private NodeProxy.Arguments arguments;
    private WiresDragProxy dragProxy;

    @Inject
    public LienzoNodeProxy(final SessionCommandManager<AbstractCanvasHandler> commandManager,
                           final CanvasCommandFactory<AbstractCanvasHandler> commandFactory,
                           final Event<CanvasSelectionEvent> selectionEvent) {
        this.commandManager = commandManager;
        this.commandFactory = commandFactory;
        this.selectionEvent = selectionEvent;
    }

    @Override
    public void setup(final Arguments arguments) {
        this.arguments = arguments;
        final WiresShapeProxy proxy = new WiresShapeProxy(getCanvas().getWiresManager(),
                                                          this::createProxy,
                                                          this::acceptProxy,
                                                          this::destroyProxy);
        this.dragProxy = new WiresDragProxy(() -> proxy);
    }

    @Override
    public void enable(final AbstractMouseEvent event) {
        dragProxy.enable(event.getX(), event.getY());
    }

    @Override
    public void destroy() {
        if (null != dragProxy) {
            commandManager.rollback();
            dragProxy.destroy();
            dragProxy = null;
        }
        arguments = null;
    }

    void onKeyDownEvent(final @Observes KeyDownEvent event) {
        if (KeyboardEvent.Key.ESC == event.getKey()) {
            destroy();
        }
    }

    private void acceptProxy(final WiresShape shape) {
        commandManager.complete();
        selectionEvent.fire(new CanvasSelectionEvent(getCanvasHandler(), getTargetNode().getUUID()));
    }

    private void destroyProxy(final WiresShape shape) {
        commandManager.rollback();
        commandManager.complete();
    }

    private WiresShape createProxy() {
        final String rootUUID = getCanvasHandler().getDiagram().getMetadata().getCanvasRootUUID();
        commandManager.start();
        execute(new DeferredCompositeCommand.Builder<AbstractCanvasHandler, CanvasViolation>()
                        .deferCommand(() -> null != rootUUID ?
                                commandFactory.addChildNode(getCanvasHandler().getGraphIndex().getNode(rootUUID),
                                                            getTargetNode(),
                                                            getShapeSetId()) :
                                commandFactory.addNode(getTargetNode(),
                                                       getShapeSetId()))
                        .deferCommand(() -> commandFactory.addConnector(getSourceNode(),
                                                                        getEdge(),
                                                                        Builder.atCenter(getSourceNode()),
                                                                        getShapeSetId()))
                        // TODO: BPMN should provide an specific different magnet approach...
                        .deferCommand(() -> commandFactory.setTargetNode(getTargetNode(),
                                                                         getEdge(),
                                                                         Builder.forTarget(getSourceNode(),
                                                                                           getTargetNode())))
                        .build());

        final Shape<?> targetShape = getTargetShape();
        final Shape<?> edgeShape = getEdgeShape();
        edgeShape.applyState(ShapeState.SELECTED);
        targetShape.applyState(ShapeState.SELECTED);
        return getTargetShapeView(targetShape);
    }

    private Shape<?> getEdgeShape() {
        return getCanvas().getShape(getEdge().getUUID());
    }

    private Shape<?> getTargetShape() {
        return getCanvas().getShape(getTargetNode().getUUID());
    }

    private WiresShape getTargetShapeView(final Shape<?> connectorShape) {
        return (WiresShape) connectorShape.getShapeView();
    }

    private CommandResult<CanvasViolation> execute(final Command<AbstractCanvasHandler, CanvasViolation> command) {
        return commandManager.execute(getCanvasHandler(), command);
    }

    private WiresCanvas getCanvas() {
        return (WiresCanvas) getCanvasHandler().getCanvas();
    }

    private AbstractCanvasHandler getCanvasHandler() {
        return arguments.getCanvasHandler();
    }

    private Metadata getMetadata() {
        return getCanvasHandler().getDiagram().getMetadata();
    }

    private String getShapeSetId() {
        return getMetadata().getShapeSetId();
    }

    private Edge<ViewConnector<?>, Node> getEdge() {
        return arguments.getEdge();
    }

    private Node<? extends View<?>, Edge> getSourceNode() {
        return arguments.getSourceNode();
    }

    private Node<? extends View<?>, Edge> getTargetNode() {
        return arguments.getTargetNode();
    }
}
