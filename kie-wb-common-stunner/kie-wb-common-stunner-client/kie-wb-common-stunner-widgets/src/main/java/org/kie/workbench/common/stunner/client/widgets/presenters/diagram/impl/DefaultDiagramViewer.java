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

package org.kie.workbench.common.stunner.client.widgets.presenters.diagram.impl;

import java.lang.annotation.Annotation;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.kie.workbench.common.stunner.client.widgets.canvas.wires.WiresCanvasPresenter;
import org.kie.workbench.common.stunner.client.widgets.views.WidgetWrapperView;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvas;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.controls.select.SelectionControl;
import org.kie.workbench.common.stunner.core.client.canvas.controls.select.SingleSelection;
import org.kie.workbench.common.stunner.core.client.canvas.controls.zoom.ZoomControl;
import org.kie.workbench.common.stunner.core.client.session.impl.InstanceUtils;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.kie.workbench.common.stunner.core.util.DefinitionUtils;

/**
 * A generic DiagramViewer implementation.
 * It opens a diagram instance in a new canvas and handler instances for read-only purposes.,
 * It provides a zoom and selection control that third parties can interacting with, but it does not provide
 * any controls that allow the diagram's authoring.
 */
@Dependent
public class DefaultDiagramViewer
        extends AbstractDiagramViewer<Diagram, AbstractCanvasHandler> {

    private final DefinitionUtils definitionUtils;
    private final ManagedInstance<AbstractCanvas> canvasInstances;
    private final ManagedInstance<AbstractCanvasHandler> canvasHandlerInstances;
    private final ManagedInstance<ZoomControl<AbstractCanvas>> zoomControlInstances;
    private final ManagedInstance<SelectionControl<AbstractCanvasHandler, Element>> selectionControlInstances;

    private AbstractCanvas canvas;
    private AbstractCanvasHandler canvasHandler;
    private ZoomControl<AbstractCanvas> zoomControl;
    private SelectionControl<AbstractCanvasHandler, Element> selectionControl;

    @Inject
    public DefaultDiagramViewer(final DefinitionUtils definitionUtils,
                                final @Any ManagedInstance<AbstractCanvas> canvasInstances,
                                final @Any ManagedInstance<AbstractCanvasHandler> canvasHandlerInstances,
                                final @Any ManagedInstance<ZoomControl<AbstractCanvas>> zoomControlInstances,
                                final @Any @SingleSelection ManagedInstance<SelectionControl<AbstractCanvasHandler, Element>> selectionControlInstances,
                                final WidgetWrapperView view) {
        super(view);
        this.definitionUtils = definitionUtils;
        this.canvasInstances = canvasInstances;
        this.canvasHandlerInstances = canvasHandlerInstances;
        this.zoomControlInstances = zoomControlInstances;
        this.selectionControlInstances = selectionControlInstances;
    }

    @Override
    public void open(final Diagram item,
                     final int width,
                     final int height,
                     final DiagramViewerCallback<Diagram> callback) {
        this.open(item,
                  width,
                  height,
                  false,
                  callback);
    }

    @Override
    protected void onOpen(final Diagram diagram) {
        final Annotation qualifier =
                definitionUtils.getQualifier(diagram.getMetadata().getDefinitionSetId());
        canvas = InstanceUtils.lookup(canvasInstances, qualifier);
        canvasHandler = InstanceUtils.lookup(canvasHandlerInstances, qualifier);
        zoomControl = InstanceUtils.lookup(zoomControlInstances, qualifier);
        selectionControl = InstanceUtils.lookup(selectionControlInstances, qualifier);
    }

    @Override
    protected void enableControls() {
        zoomControl.enable(getCanvas());
        selectionControl.enable(getHandler());
    }

    @Override
    protected void disableControls() {
        zoomControl.disable();
        selectionControl.disable();
    }

    @Override
    protected void destroyControls() {
        zoomControl.destroy();
        selectionControl.destroy();
        zoomControlInstances.destroy(zoomControl);
        zoomControlInstances.destroyAll();
        selectionControlInstances.destroy(selectionControl);
        selectionControlInstances.destroyAll();
        zoomControl = null;
        selectionControl = null;
    }

    @Override
    protected void destroyInstances() {
        super.destroyInstances();
        canvasInstances.destroy(canvas);
        canvasInstances.destroyAll();
        canvasHandlerInstances.destroy(canvasHandler);
        canvasHandlerInstances.destroyAll();
        canvas = null;
        canvasHandler = null;
    }

    @Override
    public AbstractCanvasHandler getHandler() {
        return canvasHandler;
    }

    @Override
    protected void scalePanel(final int width,
                              final int height) {
        getWiresCanvasPresenter().getLienzoPanel().setPixelSize(width,
                                                                height);
    }

    private WiresCanvasPresenter getWiresCanvasPresenter() {
        return (WiresCanvasPresenter) getCanvas();
    }

    public AbstractCanvas getCanvas() {
        return canvas;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ZoomControl<AbstractCanvas> getZoomControl() {
        return zoomControl;
    }

    @Override
    public SelectionControl<AbstractCanvasHandler, Element> getSelectionControl() {
        return selectionControl;
    }
}
