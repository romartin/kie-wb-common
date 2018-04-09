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
import org.kie.workbench.common.stunner.core.client.command.CanvasViolation;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.Bounds;
import org.kie.workbench.common.stunner.core.graph.content.definition.DefinitionSet;
import org.kie.workbench.common.stunner.core.graph.content.view.Point2D;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

// TODO: Implement allow based on the current element size, in order to not display the button from the toolbox

public class ExpandElementCommand extends AbstractCanvasCommand {

    // Reduce padding when fixing floating palette on top issue.
    private static final double PADDING = 50;

    public enum Direction {
        HORIZONTAL,
        VERTICAL,
        BOTH;
    }

    private final Node<View<?>, Edge> element;
    private final Direction direction;
    private ResizeElementCommand resizeElementCommand;

    public ExpandElementCommand(final Node<View<?>, Edge> element,
                                final Direction direction) {
        this.element = element;
        this.direction = direction;
    }

    @Override
    public CommandResult<CanvasViolation> execute(final AbstractCanvasHandler context) {
        return loadResizeCommand(context,
                                 element)
                .execute(context);
    }

    @Override
    public CommandResult<CanvasViolation> undo(final AbstractCanvasHandler context) {
        return resizeElementCommand.undo(context);
    }

    private ResizeElementCommand loadResizeCommand(final AbstractCanvasHandler canvasHandler,
                                                   final Node<View<?>, Edge> node) {
        resizeElementCommand = buildResizeCommand(canvasHandler,
                                                  node);
        return resizeElementCommand;
    }

    @SuppressWarnings("unchecked")
    private ResizeElementCommand buildResizeCommand(final AbstractCanvasHandler canvasHandler,
                                                    final Node<View<?>, Edge> node) {
        final Bounds nodeBounds = node.getContent().getBounds();
        final Graph<DefinitionSet, ? extends Node> graph = canvasHandler.getDiagram().getGraph();
        final Bounds bounds = graph.getContent().getBounds();
        final Double gminX = bounds.getUpperLeft().getX();
        final Double gmaxX = bounds.getLowerRight().getX();
        final Double gminY = bounds.getUpperLeft().getY();
        final Double gmaxY = bounds.getLowerRight().getY();
        final Double nminX = nodeBounds.getUpperLeft().getX();
        final Double nmaxX = nodeBounds.getLowerRight().getX();
        final Double nminY = nodeBounds.getUpperLeft().getY();
        final Double nmaxY = nodeBounds.getLowerRight().getY();
        Point2D location = null;
        double width = 0;
        double height = 0;
        switch (direction) {
            case HORIZONTAL:
                location = new Point2D(gminX + PADDING, nminY);
                width = gmaxX - gminX - (2 * PADDING);
                height = nmaxY - nminY;
                break;
            case VERTICAL:
                location = new Point2D(nminX, gminY + PADDING);
                width = nmaxX - nminX;
                height = gmaxY - gminY - (2 * PADDING);
                break;
            case BOTH:
                location = new Point2D(gminX + PADDING, gminY + PADDING);
                width = gmaxX - gminX - (2 * PADDING);
                height = gmaxY - gminY - (2 * PADDING);
                break;
        }
        return new ResizeElementCommand(node,
                                        location,
                                        width,
                                        height);
    }

    public Node<View<?>, Edge> getElement() {
        return element;
    }

    public Direction getDirection() {
        return direction;
    }
}
