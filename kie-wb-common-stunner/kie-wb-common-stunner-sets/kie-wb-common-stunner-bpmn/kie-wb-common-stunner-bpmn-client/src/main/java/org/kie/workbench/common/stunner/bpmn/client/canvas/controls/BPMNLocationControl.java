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

package org.kie.workbench.common.stunner.bpmn.client.canvas.controls;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.kie.workbench.common.stunner.bpmn.client.forms.util.ContextUtils;
import org.kie.workbench.common.stunner.bpmn.definition.Lane;
import org.kie.workbench.common.stunner.bpmn.qualifiers.BPMN;
import org.kie.workbench.common.stunner.client.lienzo.canvas.controls.LocationControlImpl;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.controls.CanvasRegistationControl;
import org.kie.workbench.common.stunner.core.client.canvas.controls.drag.LocationControl;
import org.kie.workbench.common.stunner.core.client.command.CanvasViolation;
import org.kie.workbench.common.stunner.core.client.session.ClientFullSession;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.kie.workbench.common.stunner.core.graph.content.view.Point2D;

@Dependent
@BPMN
public class BPMNLocationControl
        implements LocationControl<AbstractCanvasHandler, Element>,
                   CanvasRegistationControl<AbstractCanvasHandler, Element> {

    private final LocationControlImpl locationControl;

    @Inject
    public BPMNLocationControl(final LocationControlImpl locationControl) {
        this.locationControl = locationControl;
    }

    @PostConstruct
    public void init() {
        locationControl.setSiblingAllowed(BPMNLocationControl::areLanes);
    }

    @Override
    public void setCommandManagerProvider(final CommandManagerProvider<AbstractCanvasHandler> provider) {
        locationControl.setCommandManagerProvider(provider);
    }

    @Override
    public void enable(final AbstractCanvasHandler context) {
        locationControl.enable(context);
    }

    @Override
    public void disable() {
        locationControl.disable();
    }

    @Override
    public void register(final Element element) {
        locationControl.register(element);
    }

    @Override
    public void deregister(final Element element) {
        locationControl.deregister(element);
    }

    @Override
    public CommandResult<CanvasViolation> move(final Element[] element,
                                               final Point2D[] location) {
        return locationControl.move(element,
                                    location);
    }

    @Override
    public void bind(final ClientFullSession session) {
        locationControl.bind(session);
    }

    @Override
    public void unbind() {
        locationControl.unbind();
    }

    private static boolean areLanes(final Element element1,
                                    final Element element2) {
        return isLane(element1) && isLane(element2);
    }

    private static boolean isLane(final Element element) {
        return ContextUtils.isElementNodeBeanType(element,
                                                  Lane.class);
    }
}
