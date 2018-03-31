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

package org.kie.workbench.common.stunner.shapes.client;

import org.kie.workbench.common.stunner.client.lienzo.shape.impl.ShapeStateDefaultHandler;
import org.kie.workbench.common.stunner.client.lienzo.shape.view.wires.ext.WiresConnectorViewExt;
import org.kie.workbench.common.stunner.core.client.shape.ShapeState;
import org.kie.workbench.common.stunner.core.client.shape.impl.ConnectorShape;
import org.kie.workbench.common.stunner.core.client.shape.impl.ShapeStateAttributeHandler;
import org.kie.workbench.common.stunner.core.client.shape.impl.ShapeStateAttributesFactory;
import org.kie.workbench.common.stunner.core.client.shape.view.HasControlPoints;
import org.kie.workbench.common.stunner.core.definition.shape.ShapeViewDef;

public class BasicConnectorShape<W, D extends ShapeViewDef<W, V>, V extends WiresConnectorViewExt>
        extends ConnectorShape<W, D, V> {

    public static ShapeStateAttributesFactory.Builder STROKE_ATTRIBUTES_BUILDER =
            new ShapeStateAttributesFactory.Builder()
                    .with(ShapeState.SELECTED,
                          attrs -> attrs
                                  .set(ShapeStateAttributeHandler.ShapeStateAttribute.STROKE_ALPHA, 1d))
                    .with(ShapeState.HIGHLIGHT,
                          attrs -> attrs
                                  .set(ShapeStateAttributeHandler.ShapeStateAttribute.STROKE_COLOR, "#FFFFFF")
                                  // .set(ShapeStateAttributeHandler.ShapeStateAttribute.STROKE_WIDTH, 1d)
                                  .set(ShapeStateAttributeHandler.ShapeStateAttribute.STROKE_ALPHA, 1d))
                    .with(ShapeState.INVALID,
                          attrs -> attrs
                                  .set(ShapeStateAttributeHandler.ShapeStateAttribute.STROKE_COLOR, ShapeStateAttributesFactory.COLOR_INVALID)
                                  .set(ShapeStateAttributeHandler.ShapeStateAttribute.STROKE_ALPHA, 1d));

    public static ShapeStateAttributesFactory.ShapeStatesAttributes STROKE_ATTRIBUTES = STROKE_ATTRIBUTES_BUILDER.build();

    @SuppressWarnings("unchecked")
    public BasicConnectorShape(D shapeDef,
                               V view) {
        super(shapeDef,
              view,
              new ShapeStateDefaultHandler()
                      .useBorderAttributes(STROKE_ATTRIBUTES.attributes())
                      .setBorderShape(() -> view)
                      .setBackgroundShape(() -> view));
    }

    @Override
    public void applyState(final ShapeState shapeState) {
        super.applyState(shapeState);
        if (isSelected()) {
            getShapeView().showControlPoints(HasControlPoints.ControlPointType.POINTS);
        } else {
            getShapeView().hideControlPoints();
        }
    }

    private boolean isSelected() {
        return ShapeState.SELECTED.equals(getShape().getShapeStateHandler().getShapeState());
    }
}
