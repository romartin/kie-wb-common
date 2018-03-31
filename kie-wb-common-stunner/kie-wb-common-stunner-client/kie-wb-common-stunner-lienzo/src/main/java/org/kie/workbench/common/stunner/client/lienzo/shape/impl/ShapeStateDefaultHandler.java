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

package org.kie.workbench.common.stunner.client.lienzo.shape.impl;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.ait.lienzo.client.core.types.Shadow;
import com.ait.lienzo.shared.core.types.ColorName;
import org.kie.workbench.common.stunner.client.lienzo.shape.view.LienzoShapeView;
import org.kie.workbench.common.stunner.core.client.shape.ShapeState;
import org.kie.workbench.common.stunner.core.client.shape.impl.ShapeStateAttributeHandler;
import org.kie.workbench.common.stunner.core.client.shape.impl.ShapeStateAttributeHandler.ShapeStateAttribute;
import org.kie.workbench.common.stunner.core.client.shape.impl.ShapeStateAttributesFactory;
import org.kie.workbench.common.stunner.core.client.shape.impl.ShapeStateHandler;
import org.kie.workbench.common.stunner.core.client.shape.view.HasShadow;
import org.kie.workbench.common.stunner.core.client.shape.view.ShapeView;

/**
 * It provides some default visualization attributes for each of the different shape states.
 * It expects two shapes (the "background" and the "border" shape), this way applies different
 * styling attributes for each one and depending on the state, if also applies shadows.
 */
public class ShapeStateDefaultHandler
        implements ShapeStateHandler {

    public enum RenderType {
        FILL(),
        STROKE();
    }

    public static ShapeStateAttributesFactory.Builder STROKE_ATTRIBUTES_BUILDER =
            new ShapeStateAttributesFactory.Builder()
                    .with(ShapeState.SELECTED,
                          attrs -> attrs
                                  .set(ShapeStateAttribute.STROKE_ALPHA, 1d))
                    .with(ShapeState.HIGHLIGHT,
                          attrs -> attrs
                                  .set(ShapeStateAttribute.STROKE_COLOR, "#FFFFFF")
                                  .set(ShapeStateAttribute.STROKE_WIDTH, 6d)
                                  .set(ShapeStateAttribute.STROKE_ALPHA, 1d))
                    .with(ShapeState.INVALID,
                          attrs -> attrs
                                  .set(ShapeStateAttribute.STROKE_COLOR, ShapeStateAttributesFactory.COLOR_INVALID)
                                  .set(ShapeStateAttribute.STROKE_ALPHA, 1d));

    public static ShapeStateAttributesFactory.ShapeStatesAttributes STROKE_ATTRIBUTES = STROKE_ATTRIBUTES_BUILDER.build();

    public static ShapeStateAttributesFactory.Builder FILL_ATTRIBUTES_BUILDER =
            new ShapeStateAttributesFactory.Builder()
                    .with(ShapeState.SELECTED,
                          attrs -> attrs
                                  .set(ShapeStateAttribute.FILL_ALPHA, 1d))
                    .with(ShapeState.HIGHLIGHT,
                          attrs -> attrs
                                  .set(ShapeStateAttribute.FILL_COLOR, "#FFFFFF")
                                  .set(ShapeStateAttribute.FILL_ALPHA, 1d))
                    .with(ShapeState.INVALID,
                          attrs -> attrs
                                  .set(ShapeStateAttribute.FILL_COLOR, ShapeStateAttributesFactory.COLOR_INVALID)
                                  .set(ShapeStateAttribute.FILL_ALPHA, 1d));

    public static ShapeStateAttributesFactory.ShapeStatesAttributes FILL_ATTRIBUTES = FILL_ATTRIBUTES_BUILDER.build();

    private static final Shadow SHADOW_HIGHLIGHT = new Shadow(ColorName.BLACK.getColor().setA(0.50), 15, 0, 0);
    private static final Shadow SHADOW_SELECTED = new Shadow(ColorName.BLACK.getColor().setA(0.40), 5, 2, 2);

    private final ShapeStateAttributeAnimationHandler<LienzoShapeView<?>> borderShapeStateHandler;
    private final ShapeStateAttributeAnimationHandler<LienzoShapeView<?>> backgroundShapeStateHandler;
    private ShapeState state;

    public ShapeStateDefaultHandler() {
        this(new ShapeStateAttributeAnimationHandler<>(),
             new ShapeStateAttributeAnimationHandler<>());
    }

    ShapeStateDefaultHandler(final ShapeStateAttributeAnimationHandler<LienzoShapeView<?>> borderShapeStateHandler,
                             final ShapeStateAttributeAnimationHandler<LienzoShapeView<?>> backgroundShapeStateHandler) {
        this.backgroundShapeStateHandler = backgroundShapeStateHandler.onComplete(this::applyShadow);
        this.borderShapeStateHandler = borderShapeStateHandler;
        this.state = ShapeState.NONE;
        setBackgroundRenderType(RenderType.STROKE);
        setBorderRenderType(RenderType.FILL);
    }

    public ShapeStateDefaultHandler setBackgroundRenderType(final RenderType renderType) {
        useBackgroundAttributes(getAttributes(renderType).attributes());
        return this;
    }

    public ShapeStateDefaultHandler setBorderRenderType(final RenderType renderType) {
        useBorderAttributes(getAttributes(renderType).attributes());
        return this;
    }

    public ShapeStateDefaultHandler useBackgroundAttributes(final Function<ShapeState, ShapeStateAttributeHandler.ShapeStateAttributes> stateAttributesProvider) {
        backgroundShapeStateHandler.getAttributesHandler().useAttributes(stateAttributesProvider);
        return this;
    }

    public ShapeStateDefaultHandler useBorderAttributes(final Function<ShapeState, ShapeStateAttributeHandler.ShapeStateAttributes> stateAttributesProvider) {
        borderShapeStateHandler.getAttributesHandler().useAttributes(stateAttributesProvider);
        return this;
    }

    private static ShapeStateAttributesFactory.ShapeStatesAttributes getAttributes(final RenderType type) {
        return RenderType.FILL.equals(type) ? FILL_ATTRIBUTES : STROKE_ATTRIBUTES;
    }

    public ShapeStateDefaultHandler setBorderShape(final Supplier<LienzoShapeView<?>> shapeSupplier) {
        borderShapeStateHandler.getAttributesHandler().setView(shapeSupplier);
        return this;
    }

    public ShapeStateDefaultHandler setBackgroundShape(final Supplier<LienzoShapeView<?>> shapeSupplier) {
        backgroundShapeStateHandler.getAttributesHandler().setView(shapeSupplier);
        return this;
    }

    @Override
    public void applyState(final ShapeState shapeState) {
        this.state = shapeState;
        switch (shapeState) {
            case NONE:
                backgroundShapeStateHandler.applyState(shapeState);
                borderShapeStateHandler.applyState(shapeState);
                break;
            case SELECTED:
                borderShapeStateHandler.applyState(ShapeState.NONE);
                backgroundShapeStateHandler.applyState(shapeState);
                break;
            case HIGHLIGHT:
                borderShapeStateHandler.applyState(ShapeState.NONE);
                backgroundShapeStateHandler.applyState(shapeState);
                break;
            case INVALID:
                backgroundShapeStateHandler.applyState(ShapeState.NONE);
                borderShapeStateHandler.applyState(shapeState);
                break;
        }
    }

    @Override
    public ShapeStateHandler shapeAttributesChanged() {
        backgroundShapeStateHandler.shapeAttributesChanged();
        borderShapeStateHandler.shapeAttributesChanged();
        return this;
    }

    @Override
    public ShapeState reset() {
        final ShapeState result = state;
        getShadowShape().ifPresent(this::removeShadow);
        backgroundShapeStateHandler.reset();
        borderShapeStateHandler.reset();
        this.state = ShapeState.NONE;
        return result;
    }

    @Override
    public ShapeState getShapeState() {
        return state;
    }

    private void applyShadow() {
        getShadowShape().ifPresent(this::updateShadow);
    }

    private void removeShadow(final HasShadow shape) {
        shape.removeShadow();
    }

    private void updateShadow(final HasShadow shape) {
        if (isStateSelected(state)) {
            shape.setShadow(SHADOW_SELECTED.getColor(),
                            SHADOW_SELECTED.getBlur(),
                            SHADOW_SELECTED.getOffset().getX(),
                            SHADOW_SELECTED.getOffset().getY());
        } else if (isStateHighlight(state)) {
            shape.setShadow(SHADOW_HIGHLIGHT.getColor(),
                            SHADOW_HIGHLIGHT.getBlur(),
                            SHADOW_HIGHLIGHT.getOffset().getX(),
                            SHADOW_HIGHLIGHT.getOffset().getY());
        } else {
            removeShadow(shape);
        }
    }

    private Optional<HasShadow> getShadowShape() {
        return getShadowShape(backgroundShapeStateHandler.getAttributesHandler().getShapeView());
    }

    private Optional<HasShadow> getShadowShape(final ShapeView<?> shape) {
        final ShapeView<?> candidate = null != getBackgroundShape() ? getBackgroundShape() : shape;
        if (candidate instanceof HasShadow) {
            return Optional.of((HasShadow) candidate);
        }
        return Optional.empty();
    }

    LienzoShapeView<?> getBackgroundShape() {
        return null != backgroundShapeStateHandler ? backgroundShapeStateHandler.getAttributesHandler().getShapeView() : null;
    }

    private static boolean isStateSelected(ShapeState state) {
        return ShapeState.SELECTED.equals(state);
    }

    private static boolean isStateHighlight(ShapeState state) {
        return ShapeState.HIGHLIGHT.equals(state);
    }
}
