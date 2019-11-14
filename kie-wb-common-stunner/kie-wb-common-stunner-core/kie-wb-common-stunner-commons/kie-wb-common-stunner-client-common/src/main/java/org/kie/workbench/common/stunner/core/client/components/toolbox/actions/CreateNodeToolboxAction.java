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

package org.kie.workbench.common.stunner.core.client.components.toolbox.actions;

import java.lang.annotation.Annotation;

import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.kie.workbench.common.stunner.core.client.api.ClientFactoryManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.util.CanvasLayoutUtils;
import org.kie.workbench.common.stunner.core.client.components.proxy.NodeProxy;
import org.kie.workbench.common.stunner.core.client.i18n.ClientTranslationService;
import org.kie.workbench.common.stunner.core.client.shape.view.event.MouseClickEvent;
import org.kie.workbench.common.stunner.core.client.shape.view.event.MouseMoveEvent;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;
import org.kie.workbench.common.stunner.core.util.DefinitionUtils;
import org.kie.workbench.common.stunner.core.util.HashUtil;
import org.kie.workbench.common.stunner.core.util.UUID;

import static org.kie.workbench.common.stunner.core.client.session.impl.InstanceUtils.lookup;

/**
 * A toolbox action/operation for creating a new node, connector and connections from
 * the source toolbox' element.
 */
@Dependent
@FlowActionsToolbox
public class CreateNodeToolboxAction
        extends AbstractToolboxAction
        implements IsToolboxActionDraggable<AbstractCanvasHandler> {

    static final String KEY_TITLE = "org.kie.workbench.common.stunner.core.client.toolbox.createNewNode";

    private final ManagedInstance<GeneralCreateNodeAction> actions;
    private final NodeProxy nodeProxy;
    private final ClientFactoryManager clientFactoryManager;

    private String nodeId;
    private String edgeId;

    @Inject
    public CreateNodeToolboxAction(final DefinitionUtils definitionUtils,
                                   final ClientTranslationService translationService,
                                   final @Any ManagedInstance<GeneralCreateNodeAction> actions,
                                   final NodeProxy nodeProxy,
                                   final ClientFactoryManager clientFactoryManager) {
        super(definitionUtils,
              translationService);
        this.actions = actions;
        this.nodeProxy = nodeProxy;
        this.clientFactoryManager = clientFactoryManager;
    }

    public String getNodeId() {
        return nodeId;
    }

    public CreateNodeToolboxAction setNodeId(final String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public CreateNodeToolboxAction setEdgeId(final String edgeId) {
        this.edgeId = edgeId;
        return this;
    }

    @Override
    protected String getTitleKey(final AbstractCanvasHandler canvasHandler,
                                 final String uuid) {
        return KEY_TITLE;
    }

    @Override
    protected String getTitleDefinitionId(final AbstractCanvasHandler canvasHandler,
                                          final String uuid) {
        return nodeId;
    }

    @Override
    protected String getGlyphId(final AbstractCanvasHandler canvasHandler,
                                final String uuid) {
        return nodeId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ToolboxAction<AbstractCanvasHandler> onMouseClick(final AbstractCanvasHandler canvasHandler,
                                                             final String uuid,
                                                             final MouseClickEvent event) {
        final Metadata metadata = canvasHandler.getDiagram().getMetadata();
        final Annotation qualifier = getDefinitionUtils().getQualifier(metadata.getDefinitionSetId());
        final GeneralCreateNodeAction action = lookup(actions, qualifier);
        action.executeAction(canvasHandler,
                             uuid,
                             nodeId,
                             edgeId);

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ToolboxAction<AbstractCanvasHandler> onMoveStart(final AbstractCanvasHandler canvasHandler,
                                                            final String uuid,
                                                            final MouseMoveEvent event) {
        final Element<View<?>> sourceElement = (Element<View<?>>) CanvasLayoutUtils.getElement(canvasHandler,
                                                                                               uuid);
        final Node<View<?>, Edge> sourceNode = sourceElement.asNode();

        final Edge<ViewConnector<?>, Node> connector =
                (Edge<ViewConnector<?>, Node>) clientFactoryManager
                        .newElement(UUID.uuid(),
                                    edgeId)
                        .asEdge();
        final Node<View<?>, Edge> targetNode =
                (Node<View<?>, Edge>) clientFactoryManager
                        .newElement(UUID.uuid(),
                                    nodeId)
                        .asNode();

        nodeProxy.setup(NodeProxy.Arguments.create(canvasHandler,
                                                   targetNode,
                                                   connector,
                                                   sourceNode));

        nodeProxy.start(event);

        return this;
    }

    @PreDestroy
    public void destroy() {
        nodeProxy.destroy();
        nodeId = null;
        edgeId = null;
    }

    @Override
    public int hashCode() {
        return HashUtil.combineHashCodes(edgeId.hashCode(),
                                         nodeId.hashCode());
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof CreateNodeToolboxAction) {
            CreateNodeToolboxAction other = (CreateNodeToolboxAction) o;
            return other.edgeId.equals(edgeId) &&
                    other.nodeId.equals(nodeId);
        }
        return false;
    }
}
