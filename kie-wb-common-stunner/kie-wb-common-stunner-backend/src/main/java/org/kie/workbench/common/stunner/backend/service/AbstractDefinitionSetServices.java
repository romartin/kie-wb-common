/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.backend.service;

import org.kie.workbench.common.stunner.core.definition.adapter.binding.BindableAdapterUtils;
import org.kie.workbench.common.stunner.core.definition.service.DefinitionSetServices;
import org.kie.workbench.common.stunner.core.definition.service.DiagramMarshaller;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Graph;

public abstract class AbstractDefinitionSetServices implements DefinitionSetServices {

    private final DiagramMarshaller<Graph, Metadata, Diagram<Graph, Metadata>> diagramMarshaller;

    protected AbstractDefinitionSetServices() {
        this( null );
    }

    public AbstractDefinitionSetServices( DiagramMarshaller<Graph, Metadata, Diagram<Graph, Metadata>> diagramMarshaller ) {
        this.diagramMarshaller = diagramMarshaller;
    }

    @Override
    public boolean accepts( String defSetId ) {
        final String id = BindableAdapterUtils.getDefinitionSetId( getResourceType().getDefinitionSetType() );
        return defSetId != null && defSetId.equals( id );
    }

    @Override
    public DiagramMarshaller<Graph, Metadata, Diagram<Graph, Metadata>> getDiagramMarshaller() {
        return diagramMarshaller;
    }

}
