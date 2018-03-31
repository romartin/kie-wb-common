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

package org.kie.workbench.common.stunner.svg.gen.translator.impl;

import java.util.Optional;

import org.kie.workbench.common.stunner.svg.gen.exception.TranslatorException;
import org.kie.workbench.common.stunner.svg.gen.model.ShapeDefinition;
import org.kie.workbench.common.stunner.svg.gen.model.ShapeStateDefinition;
import org.kie.workbench.common.stunner.svg.gen.model.StyleDefinition;
import org.kie.workbench.common.stunner.svg.gen.model.impl.AbstractShapeDefinition;
import org.kie.workbench.common.stunner.svg.gen.model.impl.ShapeStateDefinitionImpl;
import org.kie.workbench.common.stunner.svg.gen.translator.SVGDocumentTranslator;
import org.kie.workbench.common.stunner.svg.gen.translator.SVGTranslatorContext;
import org.w3c.dom.Element;

public abstract class AbstractSVGShapeTranslator<E extends Element, O extends AbstractShapeDefinition<?>>
        extends AbstractSVGPrimitiveTranslator<E, O> {

    @Override
    protected void translatePrimitiveDefinition(E element,
                                                O def,
                                                SVGTranslatorContext context) throws TranslatorException {
        super.translatePrimitiveDefinition(element, def, context);
        final Optional<ShapeStateDefinition> shapeStateDefinition = translateShapeStateDefinition(element);
        def.setStateDefinition(shapeStateDefinition);
    }

    protected StyleDefinition translateStyles(final E element,
                                              final O def,
                                              final SVGTranslatorContext context) throws TranslatorException {
        final StyleDefinition styleDefinition = super.translateStyles(element,
                                                                      def,
                                                                      context);
        if (null != styleDefinition) {
            def.setStyleDefinition(styleDefinition);
        }
        return styleDefinition;
    }

    private Optional<ShapeStateDefinition> translateShapeStateDefinition(E element) {
        final String shapeStateBgRaw = getShapeStateBgAttributeValue(element);
        final String shapeStateBorderRaw = getShapeStateBorderAttributeValue(element);
        final boolean isBgStateActive = !isEmpty(shapeStateBgRaw);
        final boolean isBorderStateActive = !isEmpty(shapeStateBorderRaw);
        if (isBgStateActive || isBorderStateActive) {
            final ShapeStateDefinition.Target target = isBgStateActive ?
                    ShapeStateDefinition.Target.BACKGROUND :
                    ShapeStateDefinition.Target.BORDER;
            final ShapeStateDefinition.RenderType renderType =
                    ShapeStateDefinition.RenderType.valueOf(isBgStateActive ? shapeStateBgRaw : shapeStateBorderRaw);
            return Optional.of(new ShapeStateDefinitionImpl(target,
                                                            renderType));
        }
        return Optional.empty();
    }

    private String getShapeStateBgAttributeValue(final E element) {
        return element.getAttributeNS(SVGDocumentTranslator.STUNNER_URI,
                                      SVGDocumentTranslator.STUNNER_ATTR_NS_STATE_BACKGROUND);
    }

    private String getShapeStateBorderAttributeValue(final E element) {
        return element.getAttributeNS(SVGDocumentTranslator.STUNNER_URI,
                                      SVGDocumentTranslator.STUNNER_ATTR_NS_STATE_BORDER);
    }

}
