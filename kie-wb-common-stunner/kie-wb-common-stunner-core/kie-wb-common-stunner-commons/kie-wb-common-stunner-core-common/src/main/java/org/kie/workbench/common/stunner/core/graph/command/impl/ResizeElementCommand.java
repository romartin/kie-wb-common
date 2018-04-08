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
package org.kie.workbench.common.stunner.core.graph.command.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.kie.soup.commons.validation.PortablePreconditions;
import org.kie.workbench.common.stunner.core.command.Command;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.command.impl.AbstractCompositeCommand;
import org.kie.workbench.common.stunner.core.command.util.CommandUtils;
import org.kie.workbench.common.stunner.core.definition.adapter.DefinitionAdapter;
import org.kie.workbench.common.stunner.core.definition.property.PropertyMetaTypes;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandExecutionContext;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;
import org.kie.workbench.common.stunner.core.graph.content.view.BoundsImpl;
import org.kie.workbench.common.stunner.core.graph.content.view.Point2D;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.util.GraphUtils;
import org.kie.workbench.common.stunner.core.rule.RuleViolation;

// TODO: Check bound constraints (checkBoundsExceeded)
// TODO: Update docked nodes' location

@Portable
public final class ResizeElementCommand extends AbstractGraphCompositeCommand {

    private static Logger LOGGER = Logger.getLogger(ResizeElementCommand.class.getName());

    private final String uuid;
    private final Point2D location;
    private final double width;
    private final double height;
    private transient Node<? extends View<?>, Edge> node;

    public ResizeElementCommand(final @MapsTo("uuid") String uuid,
                                final @MapsTo("location") Point2D location,
                                final @MapsTo("width") double width,
                                final @MapsTo("height") double height) {
        this.uuid = PortablePreconditions.checkNotNull("uuid",
                                                       uuid);
        this.location = location;
        this.width = width;
        this.height = height;
        this.node = null;
    }

    public ResizeElementCommand(final Node<? extends View<?>, Edge> node,
                                final Point2D location,
                                final double width,
                                final double height) {
        this(node.getUUID(),
             location,
             width,
             height);
        this.node = node;
    }

    public Point2D getLocation() {
        return location;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Node<?, Edge> getNode() {
        return node;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    protected AbstractCompositeCommand<GraphCommandExecutionContext, RuleViolation> initialize(final GraphCommandExecutionContext context) {
        super.initialize(context);
        fetchCandidate(context);
        if (null != location) {
            addCommand(new UpdateElementPositionCommand(node,
                                                        location));
        }
        getResizeCommands(context,
                          node,
                          width,
                          height)
                .forEach(this::addCommand);
        if (GraphUtils.hasDockedNodes(node)) {
            // TODO: Update Docked nodes position
        }
        return this;
    }

    @Override
    protected CommandResult<RuleViolation> executeCommands(final GraphCommandExecutionContext context) {
        final CommandResult<RuleViolation> result = super.executeCommands(context);
        if (!CommandUtils.isError(result)) {
            final Point2D current = GraphUtils.getPosition(node.getContent());
            node.getContent()
                    .setBounds(BoundsImpl.build(current.getX(),
                                                current.getY(),
                                                current.getX() + width,
                                                current.getY() + height));
        }
        return result;
    }

    /**
     * It provides the necessary canvas commands in order to update the domain model with new values that will met
     * the new bounding box size.
     * It always updates the element's position, as resize can update it, and it updates as well some of the bean's properties.
     */
    private Collection<Command<GraphCommandExecutionContext, RuleViolation>> getResizeCommands(final GraphCommandExecutionContext context,
                                                                                               final Element<? extends Definition<?>> element,
                                                                                               final double w,
                                                                                               final double h) {
        final List<Command<GraphCommandExecutionContext, RuleViolation>> result = new LinkedList<>();
        if (null != element.asNode()) {
            final Definition content = element.getContent();
            final Node<? extends Definition<?>, Edge> node = element.asNode();
            final Object def = content.getDefinition();
            final DefinitionAdapter<Object> adapter =
                    context.getDefinitionManager()
                            .adapters()
                            .registry()
                            .getDefinitionAdapter(def.getClass());
            final Object width = adapter.getMetaProperty(PropertyMetaTypes.WIDTH, def);
            if (null != width) {
                result.add(buildUpdateValueCommand(context, node, width, w));
            }
            final Object height = adapter.getMetaProperty(PropertyMetaTypes.HEIGHT, def);
            if (null != height) {
                result.add(buildUpdateValueCommand(context, node, height, h));
            }
            final Object radius = adapter.getMetaProperty(PropertyMetaTypes.RADIUS, def);
            if (null != radius) {
                final double r = w > h ? (h / 2) : (w / 2);
                result.add(buildUpdateValueCommand(context, node, radius, r));
            }
        }
        return result;
    }

    private Command<GraphCommandExecutionContext, RuleViolation> buildUpdateValueCommand(final GraphCommandExecutionContext context,
                                                                                         final Node<? extends Definition<?>, Edge> node,
                                                                                         final Object property,
                                                                                         final Object value) {
        final String id = context.getDefinitionManager().adapters().forProperty().getId(property);
        return new UpdateElementPropertyValueCommand(node,
                                                     id,
                                                     value);
    }

    @Override
    protected boolean delegateRulesContextToChildren() {
        return true;
    }

    @SuppressWarnings("unchecked")
    private void fetchCandidate(final GraphCommandExecutionContext context) {
        if (null == node) {
            node = (Node<? extends View<?>, Edge>) checkNodeNotNull(context,
                                                                    uuid);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " " +
                "[element=" + uuid +
                ", location=" + location +
                ", width=" + width +
                ", height=" + height +
                "]";
    }
}