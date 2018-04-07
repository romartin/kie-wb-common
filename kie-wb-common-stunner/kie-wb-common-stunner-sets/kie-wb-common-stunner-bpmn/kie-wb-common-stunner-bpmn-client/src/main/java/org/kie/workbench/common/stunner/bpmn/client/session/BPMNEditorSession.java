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

package org.kie.workbench.common.stunner.bpmn.client.session;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.kie.workbench.common.stunner.bpmn.client.workitem.WorkItemDefinitionClientRegistry;
import org.kie.workbench.common.stunner.bpmn.qualifiers.BPMN;
import org.kie.workbench.common.stunner.core.client.session.impl.EditorSession;
import org.kie.workbench.common.stunner.core.client.session.impl.EditorSessionDelegate;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.uberfire.mvp.Command;

@Dependent
@BPMN
public class BPMNEditorSession
        extends EditorSessionDelegate<EditorSession> {

    private final EditorSession session;
    private final WorkItemDefinitionClientRegistry workItemDefinitionService;

    @Inject
    public BPMNEditorSession(final @Default EditorSession session,
                             final WorkItemDefinitionClientRegistry workItemDefinitionService) {
        this.session = session;
        this.workItemDefinitionService = workItemDefinitionService;
    }

    @Override
    public void load(final Metadata metadata,
                     final Command callback) {
        super.load(metadata,
                     () -> {
                         workItemDefinitionService.load(metadata,
                                                        callback);
                     });
    }

    @Override
    protected EditorSession getDelegate() {
        return session;
    }
}
