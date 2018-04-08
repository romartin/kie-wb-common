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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.kie.workbench.common.stunner.bpmn.client.forms.util.ContextUtils;
import org.kie.workbench.common.stunner.bpmn.qualifiers.BPMN;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.components.toolbox.actions.AbstractActionsToolboxFactory;
import org.kie.workbench.common.stunner.core.client.components.toolbox.actions.ActionsToolboxFactory;
import org.kie.workbench.common.stunner.core.client.components.toolbox.actions.ActionsToolboxView;
import org.kie.workbench.common.stunner.core.client.components.toolbox.actions.CommonActionsToolbox;
import org.kie.workbench.common.stunner.core.client.components.toolbox.actions.ExpandHorizontalNodeAction;
import org.kie.workbench.common.stunner.core.client.components.toolbox.actions.ExpandVerticalNodeAction;
import org.kie.workbench.common.stunner.core.client.components.toolbox.actions.ToolboxAction;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.kie.workbench.common.stunner.forms.client.components.toolbox.FormGenerationToolboxAction;

@ApplicationScoped
@BPMN
public class BPMNCommonActionsToolboxFactory extends AbstractActionsToolboxFactory {

    private final ActionsToolboxFactory commonActionToolbox;
    private final Supplier<FormGenerationToolboxAction> generateFormsActions;
    private final Supplier<ExpandHorizontalNodeAction> expandHorizontalNodeActions;
    private final Supplier<ExpandVerticalNodeAction> expandVerticalNodeActions;
    private final Supplier<ActionsToolboxView> views;

    // CDI proxy.
    protected BPMNCommonActionsToolboxFactory() {
        this.commonActionToolbox = null;
        this.generateFormsActions = null;
        this.expandHorizontalNodeActions = null;
        this.expandVerticalNodeActions = null;
        this.views = null;
    }

    @Inject
    public BPMNCommonActionsToolboxFactory(final @CommonActionsToolbox ActionsToolboxFactory commonActionToolbox,
                                           final @Any ManagedInstance<FormGenerationToolboxAction> generateFormsActions,
                                           final @Any ManagedInstance<ExpandHorizontalNodeAction> expandHorizontalNodeActions,
                                           final @Any ManagedInstance<ExpandVerticalNodeAction> expandVerticalNodeActions,
                                           final @CommonActionsToolbox ManagedInstance<ActionsToolboxView> views) {
        this.commonActionToolbox = commonActionToolbox;
        this.generateFormsActions = generateFormsActions::get;
        this.expandHorizontalNodeActions = expandHorizontalNodeActions::get;
        this.expandVerticalNodeActions = expandVerticalNodeActions::get;
        this.views = views::get;
    }

    @Override
    protected ActionsToolboxView<?> newViewInstance() {
        return views.get();
    }

    @Override
    @SuppressWarnings("all")
    public Collection<ToolboxAction<AbstractCanvasHandler>> getActions(final AbstractCanvasHandler canvasHandler,
                                                                       final Element<?> e) {
        final List<ToolboxAction<AbstractCanvasHandler>> actions = new LinkedList<>();
        actions.addAll(commonActionToolbox.getActions(canvasHandler,
                                                      e));
        if (ContextUtils.isExpandSupported(e)) {
            actions.add(expandHorizontalNodeActions.get());
            actions.add(expandVerticalNodeActions.get());
        }
        if (ContextUtils.isFormGenerationSupported(e)) {
            actions.add(generateFormsActions.get());
        }
        return actions;
    }
}
