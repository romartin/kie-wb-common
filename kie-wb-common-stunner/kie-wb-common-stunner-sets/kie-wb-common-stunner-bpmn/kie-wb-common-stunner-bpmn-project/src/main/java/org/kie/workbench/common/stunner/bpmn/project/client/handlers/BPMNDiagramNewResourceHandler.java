/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.bpmn.project.client.handlers;

import com.google.gwt.user.client.ui.IsWidget;
import org.kie.workbench.common.stunner.bpmn.BPMNDefinitionSet;
import org.kie.workbench.common.stunner.bpmn.project.client.editor.BPMNDiagramEditor;
import org.kie.workbench.common.stunner.bpmn.project.client.resource.BPMNDiagramResourceType;
import org.kie.workbench.common.stunner.bpmn.resource.BPMNDefinitionSetResourceType;
import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.project.client.handlers.AbstractProjectDiagramNewResourceHandler;
import org.kie.workbench.common.stunner.project.client.service.ClientProjectDiagramServices;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class BPMNDiagramNewResourceHandler extends AbstractProjectDiagramNewResourceHandler<BPMNDiagramResourceType> {

    protected BPMNDiagramNewResourceHandler() {
        this( null, null, null );
    }

    @Inject
    public BPMNDiagramNewResourceHandler( final DefinitionManager definitionManager,
                                          final ClientProjectDiagramServices projectDiagramServices,
                                          final BPMNDiagramResourceType projectDiagramResourceType ) {
        super( definitionManager, projectDiagramServices, projectDiagramResourceType );
    }

    @Override
    protected Class<?> getDefinitionSetType() {
        return BPMNDefinitionSet.class;
    }

    @Override
    protected String getEditorIdentifier() {
        return BPMNDiagramEditor.EDITOR_ID;
    }

    @Override
    public String getDescription() {
        return BPMNDefinitionSetResourceType.NAME;
    }

    @Override
    public IsWidget getIcon() {
        return null;
    }

}
