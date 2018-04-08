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
package org.kie.workbench.common.stunner.core.client.canvas.command;

import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.command.Command;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandExecutionContext;
import org.kie.workbench.common.stunner.core.graph.content.view.Point2D;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.rule.RuleViolation;

public class ResizeElementCommand extends AbstractCanvasGraphCommand {

    protected final Node<View<?>, Edge> element;
    protected final Point2D location;
    protected final double width;
    protected final double height;

    public ResizeElementCommand(final Node<View<?>, Edge> element,
                                final Point2D location,
                                final double width,
                                final double height) {
        this.element = element;
        this.location = location;
        this.width = width;
        this.height = height;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Command<GraphCommandExecutionContext, RuleViolation> newGraphCommand(final AbstractCanvasHandler context) {
        return new org.kie.workbench.common.stunner.core.graph.command.impl.ResizeElementCommand(element,
                                                                                                 location,
                                                                                                 width,
                                                                                                 height);
    }

    @Override
    protected AbstractCanvasCommand newCanvasCommand(final AbstractCanvasHandler context) {
        return new ResizeCanvasElementCommand(element);
    }

    public Node<View<?>, Edge> getElement() {
        return element;
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
}
