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

package org.kie.workbench.common.stunner.core.client.components.proxy;

import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;

public interface NodeProxy
        extends ShapeProxy<AbstractCanvasHandler, NodeProxy.Arguments> {

    class Arguments extends ShapeProxy.ShapeProxyArguments<AbstractCanvasHandler> {

        private final AbstractCanvasHandler canvasHandler;
        private final Node<View<?>, Edge> targetNode;
        private final Edge<ViewConnector<?>, Node> edge;
        private final Node<View<?>, Edge> sourceNode;

        public static NodeProxy.Arguments create(final AbstractCanvasHandler canvasHandler,
                                                 final Node<View<?>, Edge> targetNode,
                                                 final Edge<ViewConnector<?>, Node> edge,
                                                 final Node<View<?>, Edge> sourceNode) {
            return new NodeProxy.Arguments(canvasHandler, targetNode, edge, sourceNode);
        }

        public Arguments(final AbstractCanvasHandler canvasHandler,
                         final Node<View<?>, Edge> targetNode,
                         final Edge<ViewConnector<?>, Node> edge,
                         final Node<View<?>, Edge> sourceNode) {
            this.canvasHandler = canvasHandler;
            this.targetNode = targetNode;
            this.edge = edge;
            this.sourceNode = sourceNode;
        }

        @Override
        public AbstractCanvasHandler getCanvasHandler() {
            return canvasHandler;
        }

        public Node<View<?>, Edge> getTargetNode() {
            return targetNode;
        }

        public Edge<ViewConnector<?>, Node> getEdge() {
            return edge;
        }

        public Node<View<?>, Edge> getSourceNode() {
            return sourceNode;
        }
    }
}
