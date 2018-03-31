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

package org.kie.workbench.common.stunner.svg.gen.codegen.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.kie.workbench.common.stunner.client.lienzo.shape.impl.ShapeStateDefaultHandler;
import org.kie.workbench.common.stunner.svg.client.shape.view.SVGShapeView;
import org.kie.workbench.common.stunner.svg.gen.codegen.PrimitiveDefinitionGenerator;
import org.kie.workbench.common.stunner.svg.gen.codegen.ViewDefinitionGenerator;
import org.kie.workbench.common.stunner.svg.gen.exception.GeneratorException;
import org.kie.workbench.common.stunner.svg.gen.model.LayoutDefinition;
import org.kie.workbench.common.stunner.svg.gen.model.PrimitiveDefinition;
import org.kie.workbench.common.stunner.svg.gen.model.ShapeDefinition;
import org.kie.workbench.common.stunner.svg.gen.model.ShapeStateDefinition;
import org.kie.workbench.common.stunner.svg.gen.model.StyleSheetDefinition;
import org.kie.workbench.common.stunner.svg.gen.model.ViewDefinition;
import org.kie.workbench.common.stunner.svg.gen.model.ViewFactory;
import org.kie.workbench.common.stunner.svg.gen.model.ViewRefDefinition;
import org.kie.workbench.common.stunner.svg.gen.model.impl.SVGModelUtils;
import org.kie.workbench.common.stunner.svg.gen.model.impl.ViewDefinitionImpl;
import org.uberfire.annotations.processors.exceptions.GenerationException;

public class SVGViewDefinitionGenerator
        extends AbstractGenerator
        implements ViewDefinitionGenerator<ViewDefinition<SVGShapeView>> {

    public static final String PRIM_CHILD_TEMPLATE = "view.addChild(%1s);";
    private static final String SVG_CHILD_TEMPLATE = "view.addSVGChild(%1s, %1s.this.%1sBasicView());";

    @Override
    @SuppressWarnings("unchecked")
    public StringBuffer generate(final ViewFactory viewFactory,
                                 final ViewDefinition<SVGShapeView> viewDefinition) throws GeneratorException {
        StringBuffer result = null;
        final String factoryName = viewFactory.getSimpleName();
        final String viewId = viewDefinition.getId();
        final String methodName = viewDefinition.getFactoryMethodName();
        final ShapeDefinition main = viewDefinition.getMain();
        if (null != main) {
            final Map<String, Object> root = new HashMap<>();

            // Generate the children primitives.
            final List<String> childrenRaw = new LinkedList<>();
            final List<PrimitiveDefinition> children = viewDefinition.getChildren();
            for (final PrimitiveDefinition child : children) {
                final String childId = SVGGeneratorFormatUtils.getValidInstanceId(child);
                String childRaw =
                        SVGPrimitiveGeneratorUtils
                                .generateSvgPrimitive(childId,
                                                      SVGViewDefinitionGenerator::getGenerator,
                                                      child);
                if (null != childRaw) {
                    childrenRaw.add(childRaw);
                    childrenRaw.add(AbstractGenerator.formatString(PRIM_CHILD_TEMPLATE,
                                                                   childId));
                }
            }

            // SVG View children.
            final List<String> svgChildrenRaw = new LinkedList<>();
            final List<ViewRefDefinition> svgViewRefs = viewDefinition.getSVGViewRefs();
            svgViewRefs.forEach(viewRef -> {
                final String parent = viewRef.getParent();
                final String svgName = viewRef.getFilePath();
                final String viewRefId = viewRef.getViewRefId();
                final boolean existReferencedView = viewFactory
                        .getViewDefinitions().stream()
                        .anyMatch(def -> viewRefId.equals(def.getId()));
                if (existReferencedView) {
                    final String childRaw = formatString(SVG_CHILD_TEMPLATE,
                                                         parent,
                                                         factoryName,
                                                         viewRefId);
                    svgChildrenRaw.add(childRaw);
                } else {
                    throw new RuntimeException("The view [" + viewRefId + "] references " +
                                                       "another the view [" + svgName + "], but no factory method " +
                                                       "for it exists in [" + viewFactory.getImplementedType() + "]");
                }
            });
            // Generate the main shape.
            final PrimitiveDefinitionGenerator<PrimitiveDefinition<?>> mainGenerator = getGenerator(main);
            final StringBuffer mainBuffer = mainGenerator.generate(main);
            final LayoutDefinition mainLayoutDefinition = main.getLayoutDefinition();
            final String mainLayoutRaw = SVGPrimitiveGeneratorUtils.formatLayout(mainLayoutDefinition);
            // Generate the view's text styling stuff.
            final StyleSheetDefinition globalStyleSheetDefinition =
                    ((ViewDefinitionImpl) viewDefinition).getGlobalStyleSheetDefinition();
            final String viewTextRaw = null != globalStyleSheetDefinition ?
                    SVGShapeTextCodeBuilder.generate("view",
                                                     viewId,
                                                     globalStyleSheetDefinition) : "";
            // Look for the state shape parameters in the view definition and execute the code generation.
            final ShapeStateTemplateParameters[] stateTemplateParameters = getShapeStateTemplateParameters(viewDefinition);

            // Populate the context and generate using the template.
            root.put("viewId",
                     viewId);
            root.put("name",
                     methodName);
            root.put("mainShape",
                     mainBuffer.toString());
            root.put("layout",
                     mainLayoutRaw);
            root.put("width",
                     formatDouble(viewDefinition.getWidth()));
            root.put("height",
                     formatDouble(viewDefinition.getHeight()));
            root.put("text",
                     viewTextRaw);
            root.put("stateBgViewIds",
                     stateTemplateParameters[0].getIdentifiers());
            root.put("stateBorderViewIds",
                     stateTemplateParameters[1].getIdentifiers());
            root.put("children",
                     childrenRaw);
            root.put("stateBorderRenderType",
                     stateTemplateParameters[0].getRenderType());
            root.put("svgChildren",
                     svgChildrenRaw);
            try {
                result = writeTemplate(root);
            } catch (final GenerationException e) {
                throw new GeneratorException(e);
            }
        }

        return result;
    }

    private static ShapeStateTemplateParameters[] getShapeStateTemplateParameters(final ViewDefinition<SVGShapeView> viewDefinition) {
        final ShapeStateTemplateParameters stateBgParameters =
                new ShapeStateTemplateParameters(ShapeStateDefinition.Target.BACKGROUND);
        final ShapeStateTemplateParameters stateBorderParameters =
                new ShapeStateTemplateParameters(ShapeStateDefinition.Target.BACKGROUND);
        SVGModelUtils.visit(viewDefinition,
                            SVGViewDefinitionGenerator::canGenerateShapeCode,
                            primitive -> consumeShapeStateParameters((ShapeDefinition) primitive,
                                                                     stateBgParameters,
                                                                     stateBorderParameters));
        return new ShapeStateTemplateParameters[] {stateBgParameters, stateBorderParameters};
    }

    private static void consumeShapeStateParameters(final ShapeDefinition shapeDefinition,
                                                     final ShapeStateTemplateParameters stateBgParameters,
                                                     final ShapeStateTemplateParameters stateBorderParameters) {
        Optional<ShapeStateDefinition> stateDefinition = shapeDefinition.getStateDefinition();
        stateDefinition.ifPresent(def -> consumeShapeStateParameters(shapeDefinition.getId(),
                                                                     def,
                                                                     stateBgParameters,
                                                                     stateBorderParameters));
    }

    private static void consumeShapeStateParameters(final String id,
                                                    final ShapeStateDefinition stateDefinition,
                                                    final ShapeStateTemplateParameters stateBgParameters,
                                                    final ShapeStateTemplateParameters stateBorderParameters) {
        final ShapeStateTemplateParameters target =
                ShapeStateDefinition.Target.BACKGROUND.equals(stateDefinition.getTarget()) ?
                    stateBgParameters :
                    stateBorderParameters;
        target.add(id);
        if (!target.hasRenderTypeSet()) {
            target.setRenderType(stateDefinition.getRenderType());
        }
    }

    private static boolean canGenerateShapeCode(final PrimitiveDefinition primitive) {
        return ShapeDefinition.class.isInstance(primitive) &&
                SVGPrimitiveGeneratorUtils.CAN_GENERATE_PRIMITIVE_CODE.test(primitive);
    }

    private static class ShapeStateTemplateParameters {

        private static final String DEFAULT_VIEW = "view";
        private static final ShapeStateDefinition.RenderType DEFAULT_RENDER_TYPE = ShapeStateDefinition.RenderType.STROKE;

        private final ShapeStateDefinition.Target target;
        private final Collection<String> ids;
        private ShapeStateDefinition.RenderType renderType;

        private ShapeStateTemplateParameters(final ShapeStateDefinition.Target target) {
            this.target = target;
            this.ids = new LinkedHashSet<>();
        }

        ShapeStateTemplateParameters setRenderType(final ShapeStateDefinition.RenderType renderType) {
            this.renderType = renderType;
            return this;
        }

        boolean hasRenderTypeSet() {
            return null != renderType;
        }

        ShapeStateTemplateParameters add(final String id) {
            this.ids.add(id);
            return this;
        }

        ShapeStateDefinition.Target getTarget() {
            return target;
        }

        String getIdentifiers() {
            return ids.isEmpty() ?
                    DEFAULT_VIEW :
                    ids.stream()
                    .collect(Collectors.joining(","));
        }

        String getRenderType() {
            return generateRenderTypeArgument(null != renderType ? renderType : DEFAULT_RENDER_TYPE);
        }
    }

    private static String generateRenderTypeArgument(final ShapeStateDefinition.RenderType renderType) {
        return ShapeStateDefaultHandler.RenderType.class.getName().replace("$", ".") +
                "." + renderType.name().toUpperCase();
    }

    @SuppressWarnings("unchecked")
    private static PrimitiveDefinitionGenerator<PrimitiveDefinition<?>> getGenerator(final PrimitiveDefinition main) {
        final PrimitiveDefinitionGenerator<?>[] array = ViewGenerators.newPrimitiveDefinitionGenerators();
        final List<PrimitiveDefinitionGenerator<?>> list = new LinkedList<>();
        Collections.addAll(list,
                           array);
        return (PrimitiveDefinitionGenerator<PrimitiveDefinition<?>>) list.stream()
                .filter(generator -> generator.getDefinitionType().equals(main.getClass()))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected String getTemplatePath() {
        return "SVGShapeView";
    }
}
