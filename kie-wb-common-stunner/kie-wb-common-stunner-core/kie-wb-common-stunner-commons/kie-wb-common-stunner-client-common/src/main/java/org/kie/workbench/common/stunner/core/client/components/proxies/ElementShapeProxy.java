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

import java.util.function.Supplier;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.Canvas;
import org.kie.workbench.common.stunner.core.client.canvas.event.selection.CanvasSelectionEvent;
import org.kie.workbench.common.stunner.core.client.command.CanvasViolation;
import org.kie.workbench.common.stunner.core.client.command.SessionCommandManager;
import org.kie.workbench.common.stunner.core.client.event.keyboard.KeyboardEvent;
import org.kie.workbench.common.stunner.core.client.shape.ElementShape;
import org.kie.workbench.common.stunner.core.client.shape.ShapeState;
import org.kie.workbench.common.stunner.core.command.Command;
import org.kie.workbench.common.stunner.core.command.CommandResult;

@Dependent
public class ElementShapeProxy implements ShapeProxy {

    private final SessionCommandManager<AbstractCanvasHandler> commandManager;
    private final Event<CanvasSelectionEvent> selectionEvent;

    private AbstractCanvasHandler canvasHandler;
    private ShapeProxyView<ElementShape> view;
    private Supplier<ElementShape> proxyBuilder;

    @Inject
    public ElementShapeProxy(final SessionCommandManager<AbstractCanvasHandler> commandManager,
                             final Event<CanvasSelectionEvent> selectionEvent) {
        this.commandManager = commandManager;
        this.selectionEvent = selectionEvent;
    }

    @SuppressWarnings("unchecked")
    public ElementShapeProxy setView(final ShapeProxyView<? extends ElementShape> view) {
        this.view = (ShapeProxyView<ElementShape>) view;
        return this;
    }

    public ElementShapeProxy setProxyBuilder(final Supplier<ElementShape> proxyBuilder) {
        this.proxyBuilder = proxyBuilder;
        return this;
    }

    public ElementShapeProxy setCanvasHandler(final AbstractCanvasHandler canvasHandler) {
        this.canvasHandler = canvasHandler;
        return this;
    }

    @Override
    public void start(final double x,
                      final double y) {
        view
                .onCreate(this::createProxy)
                .onAccept(this::acceptProxy)
                .onDestroy(this::destroyProxy)
                .setCanvas(getCanvas())
                .start(x, y);
    }

    @Override
    public void destroy() {
        if (null != view) {
            view.destroy();
        }
        canvasHandler = null;
        view = null;
        proxyBuilder = null;
    }

    public CommandResult<CanvasViolation> execute(final Command<AbstractCanvasHandler, CanvasViolation> command) {
        return commandManager.execute(canvasHandler, command);
    }

    void handleCancelKey(KeyboardEvent.Key key) {
        if (KeyboardEvent.Key.ESC == key) {
            destroy();
        }
    }

    public Canvas getCanvas() {
        return canvasHandler.getCanvas();
    }

    public AbstractCanvasHandler getCanvasHandler() {
        return canvasHandler;
    }

    private ElementShape createProxy() {
        commandManager.start();
        ElementShape instance = proxyBuilder.get();
        instance.applyState(ShapeState.SELECTED);
        return instance;
    }

    private void acceptProxy(final ElementShape shape) {
        commandManager.complete();
        selectionEvent.fire(new CanvasSelectionEvent(canvasHandler, shape.getUUID()));
    }

    private void destroyProxy(final ElementShape shape) {
        commandManager.rollback();
        commandManager.complete();
    }
}
