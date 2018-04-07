/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.core.client.session.impl;

import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvas;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.controls.clipboard.ClipboardControl;
import org.kie.workbench.common.stunner.core.client.canvas.controls.connection.ConnectionAcceptorControl;
import org.kie.workbench.common.stunner.core.client.canvas.controls.containment.ContainmentAcceptorControl;
import org.kie.workbench.common.stunner.core.client.canvas.controls.docking.DockingAcceptorControl;
import org.kie.workbench.common.stunner.core.client.canvas.controls.keyboard.KeyboardControl;
import org.kie.workbench.common.stunner.core.client.canvas.controls.pan.PanControl;
import org.kie.workbench.common.stunner.core.client.canvas.controls.select.SelectionControl;
import org.kie.workbench.common.stunner.core.client.canvas.controls.zoom.ZoomControl;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommandManager;
import org.kie.workbench.common.stunner.core.client.command.CanvasViolation;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.command.Command;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.kie.workbench.common.stunner.core.registry.command.CommandRegistry;

public abstract class EditorSessionDelegate<S extends EditorSession>
        extends EditorSession {

    protected abstract S getDelegate();

    @Override
    public void load(final Metadata metadata,
                     final org.uberfire.mvp.Command callback) {
        getDelegate().load(metadata,
                           callback);
    }

    @Override
    public void pause() {
        getDelegate().pause();
    }

    @Override
    public void open() {
        getDelegate().open();
    }

    @Override
    public void destroy() {
        getDelegate().destroy();
    }

    @Override
    public AbstractCanvas getCanvas() {
        return getDelegate().getCanvas();
    }

    @Override
    public AbstractCanvasHandler getCanvasHandler() {
        return getDelegate().getCanvasHandler();
    }

    @Override
    public CanvasCommandManager<AbstractCanvasHandler> getCommandManager() {
        return getDelegate().getCommandManager();
    }

    @Override
    public CommandRegistry<Command<AbstractCanvasHandler, CanvasViolation>> getCommandRegistry() {
        return getDelegate().getCommandRegistry();
    }

    @Override
    public ZoomControl<AbstractCanvas> getZoomControl() {
        return getDelegate().getZoomControl();
    }

    @Override
    public PanControl<AbstractCanvas> getPanControl() {
        return getDelegate().getPanControl();
    }

    @Override
    public KeyboardControl<AbstractCanvas, ClientSession> getKeyboardControl() {
        return getDelegate().getKeyboardControl();
    }

    @Override
    public ClipboardControl<Element, AbstractCanvas, ClientSession> getClipboardControl() {
        return getDelegate().getClipboardControl();
    }

    @Override
    public SelectionControl<AbstractCanvasHandler, Element> getSelectionControl() {
        return getDelegate().getSelectionControl();
    }

    @Override
    public ConnectionAcceptorControl<AbstractCanvasHandler> getConnectionAcceptorControl() {
        return getDelegate().getConnectionAcceptorControl();
    }

    @Override
    public ContainmentAcceptorControl<AbstractCanvasHandler> getContainmentAcceptorControl() {
        return getDelegate().getContainmentAcceptorControl();
    }

    @Override
    public DockingAcceptorControl<AbstractCanvasHandler> getDockingAcceptorControl() {
        return getDelegate().getDockingAcceptorControl();
    }
}
