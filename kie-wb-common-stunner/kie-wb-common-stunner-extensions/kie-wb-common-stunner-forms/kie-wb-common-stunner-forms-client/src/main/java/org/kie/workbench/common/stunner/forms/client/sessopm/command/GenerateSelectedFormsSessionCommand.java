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

package org.kie.workbench.common.stunner.forms.client.sessopm.command;

import java.util.Collection;
import java.util.function.Predicate;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.session.ClientFullSession;
import org.kie.workbench.common.stunner.core.client.session.command.AbstractClientSessionCommand;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.kie.workbench.common.stunner.core.graph.processing.index.Index;
import org.kie.workbench.common.stunner.forms.client.gen.ClientFormGenerationManager;
import org.kie.workbench.common.stunner.forms.service.FormGenerationService;

@Dependent
public class GenerateSelectedFormsSessionCommand extends AbstractClientSessionCommand<ClientFullSession> {

    private final ClientFormGenerationManager formGenerationManager;
    private Predicate<Element> acceptor;

    protected GenerateSelectedFormsSessionCommand() {
        this(null);
    }

    @Inject
    public GenerateSelectedFormsSessionCommand(final ClientFormGenerationManager formGenerationManager) {
        super(true);
        this.formGenerationManager = formGenerationManager;
        acceptor = e -> true;
    }

    public GenerateSelectedFormsSessionCommand setElementAcceptor(final Predicate<Element> acceptor) {
        this.acceptor = acceptor;
        return this;
    }

    @Override
    public <V> void execute(Callback<V> callback) {
        formGenerationManager.call(this::call);
        callback.onSuccess();
    }

    private void call(final FormGenerationService service) {
        final AbstractCanvasHandler canvasHandler = getCanvasHandler();
        final Index index = canvasHandler.getGraphIndex();
        final String[] selectedItems =
                getSelectedItems()
                        .stream()
                        .map(index::get)
                        .filter(acceptor)
                        .map(Element::getUUID)
                        .toArray(String[]::new);
        if (selectedItems.length > 0) {
            service.generate(getCanvasHandler().getDiagram(),
                             selectedItems);
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<String> getSelectedItems() {
        return getSession().getSelectionControl().getSelectedItems();
    }
}
