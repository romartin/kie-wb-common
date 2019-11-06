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

public interface ConnectorProxy
        extends ShapeProxy<AbstractCanvasHandler, ConnectorProxy.Arguments> {

    class Arguments extends ShapeProxy.ShapeProxyArguments<AbstractCanvasHandler> {

        private final AbstractCanvasHandler canvasHandler;
        private final Edge<? extends ViewConnector<?>, Node> edge;
        private final Node<? extends View<?>, Edge> sourceNode;

        public static Arguments create(final AbstractCanvasHandler canvasHandler,
                                       final Edge<? extends ViewConnector<?>, Node> edge,
                                       final Node<? extends View<?>, Edge> sourceNode) {
            return new Arguments(canvasHandler, edge, sourceNode);
        }

        private Arguments(final AbstractCanvasHandler canvasHandler,
                          final Edge<? extends ViewConnector<?>, Node> edge,
                          final Node<? extends View<?>, Edge> sourceNode) {
            this.canvasHandler = canvasHandler;
            this.edge = edge;
            this.sourceNode = sourceNode;
        }

        public Edge<? extends ViewConnector<?>, Node> getEdge() {
            return edge;
        }

        public Node<? extends View<?>, Edge> getSourceNode() {
            return sourceNode;
        }

        @Override
        public AbstractCanvasHandler getCanvasHandler() {
            return canvasHandler;
        }
    }
}
