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

import com.ait.lienzo.client.core.shape.wires.WiresConnector;
import com.ait.lienzo.client.core.shape.wires.proxy.WiresConnectorProxy;
import com.ait.lienzo.client.core.shape.wires.proxy.WiresDragProxy;
import org.kie.workbench.common.stunner.client.lienzo.canvas.wires.WiresCanvas;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.event.selection.CanvasSelectionEvent;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommand;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommandFactory;
import org.kie.workbench.common.stunner.core.client.command.CanvasViolation;
import org.kie.workbench.common.stunner.core.client.command.SessionCommandManager;
import org.kie.workbench.common.stunner.core.client.components.proxy.ConnectorProxy;
import org.kie.workbench.common.stunner.core.client.event.keyboard.KeyDownEvent;
import org.kie.workbench.common.stunner.core.client.event.keyboard.KeyboardEvent;
import org.kie.workbench.common.stunner.core.client.shape.Shape;
import org.kie.workbench.common.stunner.core.client.shape.ShapeState;
import org.kie.workbench.common.stunner.core.client.shape.view.event.AbstractMouseEvent;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.MagnetConnection;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;

@Dependent
public class LienzoConnectorProxy implements ConnectorProxy {

    private final SessionCommandManager<AbstractCanvasHandler> commandManager;
    private final CanvasCommandFactory<AbstractCanvasHandler> commandFactory;
    private final Event<CanvasSelectionEvent> selectionEvent;
    private Arguments arguments;
    private WiresDragProxy dragProxy;

    @Inject
    public LienzoConnectorProxy(final SessionCommandManager<AbstractCanvasHandler> commandManager,
                                final CanvasCommandFactory<AbstractCanvasHandler> commandFactory,
                                final Event<CanvasSelectionEvent> selectionEvent) {
        this.commandManager = commandManager;
        this.commandFactory = commandFactory;
        this.selectionEvent = selectionEvent;
    }

    @Override
    public void setup(final Arguments arguments) {
        this.arguments = arguments;
        final WiresConnectorProxy proxy =
                new WiresConnectorProxy(getCanvas().getWiresManager(),
                                        this::createProxy,
                                        this::acceptProxy,
                                        this::destroyProxy);
        this.dragProxy = new WiresDragProxy(() -> proxy);
    }

    @Override
    public void enable(final AbstractMouseEvent event) {
        this.dragProxy.enable(event.getX(), event.getY());
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

    private WiresConnector createProxy() {
        commandManager.start();
        final Node<? extends View<?>, Edge> sourceNode = getSourceNode();
        execute(commandFactory.addConnector(sourceNode,
                                            getEdge(),
                                            MagnetConnection.Builder.atCenter(sourceNode),
                                            getMetadata().getShapeSetId()));

        final Shape<?> connector = getConnector();
        connector.applyState(ShapeState.SELECTED);
        return getConnectorView(connector);
    }

    private void acceptProxy(WiresConnector connector) {
        commandManager.complete();
        selectionEvent.fire(new CanvasSelectionEvent(getCanvasHandler(), getEdge().getUUID()));
    }

    private void destroyProxy(WiresConnector connector) {
        commandManager.rollback();
        commandManager.complete();
    }

    private Shape<?> getConnector() {
        return getCanvas().getShape(getEdge().getUUID());
    }

    private WiresConnector getConnectorView(final Shape<?> connectorShape) {
        return (WiresConnector) connectorShape.getShapeView();
    }

    private CommandResult<CanvasViolation> execute(final CanvasCommand<AbstractCanvasHandler> command) {
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

    private Edge<? extends ViewConnector<?>, Node> getEdge() {
        return arguments.getEdge();
    }

    private Node<? extends View<?>, Edge> getSourceNode() {
        return arguments.getSourceNode();
    }
}
