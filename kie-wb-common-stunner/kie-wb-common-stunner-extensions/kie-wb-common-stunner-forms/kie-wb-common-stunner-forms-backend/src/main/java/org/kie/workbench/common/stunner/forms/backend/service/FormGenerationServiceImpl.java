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

package org.kie.workbench.common.stunner.forms.backend.service;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.forms.backend.gen.FormGenerationModelProviders;
import org.kie.workbench.common.stunner.forms.service.FormGeneratedEvent;
import org.kie.workbench.common.stunner.forms.service.FormGenerationService;
import org.uberfire.backend.vfs.Path;

@ApplicationScoped
@Service
public class FormGenerationServiceImpl implements FormGenerationService {

    private static Logger LOGGER = Logger.getLogger(FormGenerationServiceImpl.class.getName());

    private final FormGenerationModelProviders providers;
    private final Event<FormGeneratedEvent> formGeneratedEvent;

    // CDI proxy.
    protected FormGenerationServiceImpl() {
        this(null,
             null);
    }

    @Inject
    public FormGenerationServiceImpl(final FormGenerationModelProviders providers,
                                     final Event<FormGeneratedEvent> formGeneratedEvent) {
        this.providers = providers;
        this.formGeneratedEvent = formGeneratedEvent;
    }

    @Override
    public void generate(final Diagram diagram) {
        this.generate(diagram, new String[0]);
    }

    @Override
    public void generate(final Diagram diagram,
                         final String[] ids) {
        this.doGenerate(diagram, Arrays.stream(ids));
    }

    private void doGenerate(final Diagram diagram,
                            final Stream<String> ids) {
        LOGGER.finest("Starting form generation...");
        try {
            final Metadata metadata = diagram.getMetadata();
            final String definitionSetId = metadata.getDefinitionSetId();
            final String graphUUID = diagram.getGraph().getUUID();
            final Path path = metadata.getPath();
            final String fileName = null != path ?
                    path.getFileName() :
                    "<no-file>";
            final String idsRaw = null != ids ?
                    ids.collect(Collectors.joining(","))
                    : "<empty>";
            LOGGER.finest("FormGeneration test " +
                                  "[definitionSetId=" + definitionSetId +
                                  "[graphUUID=" + graphUUID +
                                  "[path=" + path +
                                  "[fileName=" + fileName +
                                  "[ids=" + idsRaw);

            // TODO: Form Generation
            final Object generated = providers.getModelProvider(diagram).generate(diagram);
            LOGGER.finest(generated.toString());

            // TODO: Serialize generated forms into VFS etc?

            // Fire the form generated event.
            formGeneratedEvent.fire(new FormGeneratedEvent(graphUUID, graphUUID));

            LOGGER.finest("Form generation completed successfully!");
        } catch (Throwable e) {
            // TODO: Replace try/catch by some CDI interceptor?
            LOGGER.severe("Error while calling Form Generation service.");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
