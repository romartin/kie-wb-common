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

package org.kie.workbench.common.stunner.core.client.command;

import java.util.Optional;

import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Typed;

import com.google.gwt.core.client.GWT;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvas;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.client.session.impl.EditorSession;
import org.kie.workbench.common.stunner.core.client.session.impl.ViewerSession;
import org.kie.workbench.common.stunner.core.command.Command;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.registry.command.CommandRegistry;

import static org.kie.workbench.common.stunner.core.command.util.CommandUtils.isError;

@Dependent
@Typed(CanvasSessionCommandManager.class)
public class CanvasSessionCommandManager
        implements SessionCommandManager<AbstractCanvasHandler> {

    private final RequestCommands commands;
    private ClientSession<AbstractCanvas, AbstractCanvasHandler> session;
    private CanvasCommandManager<AbstractCanvasHandler> commandManager;

    public CanvasSessionCommandManager() {
        this.commands =
                new RequestCommands.Builder()
                        .onComplete(command -> getRegistry().ifPresent(r -> r.register(command)))
                        .onRollback(command -> undo(getCanvasHandler(), command))
                        .build();
    }

    @SuppressWarnings("unchecked")
    public CanvasSessionCommandManager init(final ClientSession session) {
        this.session = session;
        return this;
    }

    public CommandResult<CanvasViolation> allow(final Command<AbstractCanvasHandler, CanvasViolation> command) {
        return allow(getCanvasHandler(), command);
    }

    public CommandResult<CanvasViolation> execute(final Command<AbstractCanvasHandler, CanvasViolation> command) {
        return execute(getCanvasHandler(), command);
    }

    public CommandResult<CanvasViolation> undo() {
        final Command<AbstractCanvasHandler, CanvasViolation> lastEntry =
                getRegistry()
                        .map(CommandRegistry::peek)
                        .orElse(null);
        if (null != lastEntry) {
            return undo(getCanvasHandler(), lastEntry);
        }
        return CanvasCommandResultBuilder.failed();
    }

    @Override
    public CommandResult<CanvasViolation> allow(final AbstractCanvasHandler context,
                                                final Command<AbstractCanvasHandler, CanvasViolation> command) {
        return getCommandManager().allow(context, command);
    }

    @Override
    public CommandResult<CanvasViolation> execute(final AbstractCanvasHandler context,
                                                  final Command<AbstractCanvasHandler, CanvasViolation> command) {
        final CommandResult<CanvasViolation> result = getCommandManager().execute(context, command);
        if (isError(result)) {
            rollback();
        } else if (commands.isStarted()) {
            commands.push(command);
        } else {
            getRegistry().ifPresent(r -> r.register(command));
        }
        return result;
    }

    @Override
    public CommandResult<CanvasViolation> undo(final AbstractCanvasHandler context,
                                               final Command<AbstractCanvasHandler, CanvasViolation> command) {
        final CommandResult<CanvasViolation> result = getCommandManager().undo(context, command);
        if (!isError(result)) {
            getRegistry().ifPresent(CommandRegistry::pop);
        }
        return result;
    }

    @Override
    public void start() {
        if (!commands.isStarted()) {
            GWT.log("****** Starting new request");
            commands.start();
        }
    }

    @Override
    public void rollback() {
        commands.rollback();
    }

    @Override
    public void complete() {
        GWT.log("****** Completing request");
        commands.complete();
    }

    @PreDestroy
    public void destroy() {
        commands.clear();
        session = null;
        commandManager = null;
    }

    private Optional<EditorSession> ifEditorSession() {
        if (session instanceof EditorSession) {
            return Optional.of((EditorSession) session);
        }
        return Optional.empty();
    }

    private Optional<CommandRegistry<Command<AbstractCanvasHandler, CanvasViolation>>> getRegistry() {
        return ifEditorSession().map(EditorSession::getCommandRegistry);
    }

    private CanvasCommandManager<AbstractCanvasHandler> getCommandManager() {
        if (null == commandManager) {
            if (session instanceof EditorSession) {
                commandManager = ((EditorSession) session).getCommandManager();
            } else {
                commandManager = ((ViewerSession) session).getCommandManager();
            }
        }
        return commandManager;
    }

    private AbstractCanvasHandler getCanvasHandler() {
        return session.getCanvasHandler();
    }
}
