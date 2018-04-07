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
import org.kie.workbench.common.stunner.core.client.canvas.controls.pan.PanControl;
import org.kie.workbench.common.stunner.core.client.canvas.controls.select.SelectionControl;
import org.kie.workbench.common.stunner.core.client.canvas.controls.zoom.ZoomControl;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.uberfire.mvp.Command;

public abstract class ViewerSessionDelegate<S extends ViewerSession> extends ViewerSession {

    protected abstract S getDelegate();

    @Override
    public void load(final Metadata metadata,
                     final Command callback) {
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
    public ZoomControl<AbstractCanvas> getZoomControl() {
        return getDelegate().getZoomControl();
    }

    @Override
    public PanControl<AbstractCanvas> getPanControl() {
        return getDelegate().getPanControl();
    }

    @Override
    public SelectionControl<AbstractCanvasHandler, Element> getSelectionControl() {
        return getDelegate().getSelectionControl();
    }
}
