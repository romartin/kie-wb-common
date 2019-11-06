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

import java.util.ArrayList;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvas;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.event.mouse.CanvasMouseDownEvent;
import org.kie.workbench.common.stunner.core.client.canvas.event.mouse.CanvasMouseUpEvent;
import org.kie.workbench.common.stunner.core.client.event.keyboard.KeyDownEvent;
import org.kie.workbench.common.stunner.core.client.event.keyboard.KeyboardEvent;
import org.kie.workbench.common.stunner.core.client.service.ClientRuntimeError;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.client.session.event.SessionDestroyedEvent;
import org.kie.workbench.common.stunner.core.client.session.event.SessionOpenedEvent;
import org.kie.workbench.common.stunner.core.client.session.impl.EditorSession;
import org.kie.workbench.common.stunner.core.client.session.impl.ViewerSession;
import org.kie.workbench.common.stunner.core.command.Command;
import org.kie.workbench.common.stunner.core.command.CommandListener;
import org.kie.workbench.common.stunner.core.command.CommandManager;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.command.HasCommandListener;
import org.kie.workbench.common.stunner.core.command.exception.CommandException;
import org.kie.workbench.common.stunner.core.command.impl.CommandRegistryListener;
import org.kie.workbench.common.stunner.core.command.impl.CompositeCommand;
import org.kie.workbench.common.stunner.core.command.util.CommandUtils;
import org.kie.workbench.common.stunner.core.registry.command.CommandRegistry;

// TODO: Is it really App scoped? test it while having multiple editors open...

@ApplicationScoped
@Default
@Typed(SessionCommandManager.class)
public class DefaultSessionCommandManager
        implements SessionCommandManager<AbstractCanvasHandler> {

    private static Logger LOGGER = Logger.getLogger(DefaultSessionCommandManager.class.getName());

    private final SessionManager sessionManager;
    private CommandListener<AbstractCanvasHandler, CanvasViolation> listener;
    private Stack<Command<AbstractCanvasHandler, CanvasViolation>> commands;
    private boolean rollback;

    @Inject
    public DefaultSessionCommandManager(final SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.rollback = false;
    }

    @Override
    public CommandResult<CanvasViolation> allow(final AbstractCanvasHandler context,
                                                final Command<AbstractCanvasHandler, CanvasViolation> command) {
        return runSafeOperation(() -> getDelegate().allow(context, command),
                                result -> {
                                    if (null != this.listener) {
                                        listener.onAllow(context, command, result);
                                    }
                                });
    }

    @Override
    public CommandResult<CanvasViolation> execute(final AbstractCanvasHandler context,
                                                  final Command<AbstractCanvasHandler, CanvasViolation> command) {
        return runSafeOperation(() -> getDelegate().execute(context, command),
                                result -> {
                                    if (null != this.listener) {
                                        listener.onExecute(context, command, result);
                                    }
                                });
    }

    @Override
    public CommandResult<CanvasViolation> undo(final AbstractCanvasHandler context,
                                               final Command<AbstractCanvasHandler, CanvasViolation> command) {
        return runSafeOperation(() -> getDelegate().undo(context, command),
                                result -> {
                                    if (null != this.listener) {
                                        listener.onUndo(context, command, result);
                                    }
                                });
    }

    @Override
    public CommandResult<CanvasViolation> undo(final AbstractCanvasHandler context) {
        final Command<AbstractCanvasHandler, CanvasViolation> lastEntry = getRegistry().peek();
        if (null != lastEntry) {
            return runSafeOperation(() -> getDelegate().undo(context, lastEntry),
                                    result -> {
                                    });
        }
        return CanvasCommandResultBuilder.FAILED;
    }

    @Override
    public CommandRegistry<Command<AbstractCanvasHandler, CanvasViolation>> getRegistry() {
        final EditorSession session = getFullSession();
        if (null != session) {
            return session.getCommandRegistry();
        }
        return null;
    }

    private CommandResult<CanvasViolation> runSafeOperation(final Supplier<CommandResult<CanvasViolation>> operation,
                                                            final Consumer<CommandResult<CanvasViolation>> postOperation) {
        try {
            CommandResult<CanvasViolation> result = operation.get();
            if (!CommandUtils.isError(result)) {
                postOperation.accept(result);
            }
            return result;
        } catch (final CommandException ce) {
            sessionManager.handleCommandError(ce);
        } catch (final RuntimeException e) {
            sessionManager.handleClientError(new ClientRuntimeError(e));
        }
        return CanvasCommandResultBuilder.FAILED;
    }

    @Override
    public void setCommandListener(final CommandListener<AbstractCanvasHandler, CanvasViolation> listener) {
        this.listener = listener;
    }

    private CommandManager<AbstractCanvasHandler, CanvasViolation> getDelegate() {
        return setDelegateListener(registryListener);
    }

    @SuppressWarnings("unchecked")
    private CommandManager<AbstractCanvasHandler, CanvasViolation> setDelegateListener(final CommandListener<AbstractCanvasHandler, CanvasViolation> listener) {
        CanvasCommandManager<AbstractCanvasHandler> commandManager = getDelegateCommandManager();
        if (commandManager != null) {
            final HasCommandListener<CommandListener<AbstractCanvasHandler, CanvasViolation>> hasCommandListener =
                    (HasCommandListener<CommandListener<AbstractCanvasHandler, CanvasViolation>>) commandManager;
            hasCommandListener.setCommandListener(listener);
        }
        return commandManager;
    }

    private CanvasCommandManager<AbstractCanvasHandler> getDelegateCommandManager() {
        final ClientSession<AbstractCanvas, AbstractCanvasHandler> session = getCurrentSession();
        CanvasCommandManager<AbstractCanvasHandler> commandManager = null;
        if (session instanceof EditorSession) {
            commandManager = ((EditorSession) session).getCommandManager();
        } else if (session instanceof ViewerSession) {
            commandManager = ((ViewerSession) session).getCommandManager();
        }
        return commandManager;
    }

    private ClientSession<AbstractCanvas, AbstractCanvasHandler> getCurrentSession() {
        return sessionManager.getCurrentSession();
    }

    private EditorSession getFullSession() {
        return (EditorSession) getCurrentSession();
    }

    private final CommandListener<AbstractCanvasHandler, CanvasViolation> registryListener =
            new CommandRegistryListener<AbstractCanvasHandler, CanvasViolation>() {

                @Override
                public void onAllow(final AbstractCanvasHandler context,
                                    final Command<AbstractCanvasHandler, CanvasViolation> command,
                                    final CommandResult<CanvasViolation> result) {
                }

                @Override
                public void onExecute(final AbstractCanvasHandler context,
                                      final Command<AbstractCanvasHandler, CanvasViolation> command,
                                      final CommandResult<CanvasViolation> result) {
                    if (CommandUtils.isError(result)) {
                        rollback();
                    } else if (commands != null) {
                        commands.push(command);
                    } else {
                        super.onExecute(context, command, result);
                    }
                }

                @Override
                public void onUndo(final AbstractCanvasHandler context,
                                   final Command<AbstractCanvasHandler, CanvasViolation> command,
                                   final CommandResult<CanvasViolation> result) {
                    super.onUndo(context, command, result);
                }

                @Override
                protected CommandRegistry<Command<AbstractCanvasHandler, CanvasViolation>> getRegistry() {
                    return DefaultSessionCommandManager.this.getRegistry();
                }
            };

    /**
     * Listens to canvas mouse down event. It produces a new client request to start.
     */
    void onCanvasMouseDownEvent(final @Observes CanvasMouseDownEvent mouseDownEvent) {
        start();
    }

    /**
     * Listens to canvas up down event. It produces the current client request to complete.
     */
    void onCanvasMouseUpEvent(final @Observes CanvasMouseUpEvent mouseUpEvent) {
        complete();
    }

    /**
     * Checks that once opening a new client session, no pending requests are present.
     */
    void onCanvasSessionOpened(final @Observes SessionOpenedEvent sessionOpenedEvent) {
        if (isRequestStarted()) {
            LOGGER.log(Level.WARNING,
                       "New session opened but the request has not been completed. Unexpected behaviors can occur.");
            clear();
        }
    }

    /**
     * Checks that once disposing a client session, no pending requests are present.
     */
    void onCanvasSessionDestroyed(final @Observes SessionDestroyedEvent sessionDestroyedEvent) {
        if (isRequestStarted()) {
            LOGGER.log(Level.WARNING,
                       "Current client request has not been completed yet.");
        }
    }

    /**
     * Checks if need to cancel current request operation, by listening to [ESC] key pressed event.
     */
    void onKeyDownEvent(final @Observes KeyDownEvent event) {
        if (KeyboardEvent.Key.ESC == event.getKey()) {
            rollback();
        }
    }

    /**
     * Starts a new client request.
     */
    private void start() {
        if (isRequestStarted()) {
            LOGGER.log(Level.WARNING,
                       "Current client request has not been completed yet." +
                               "A new client request cannot be started!");
            clear();
        }
        commands = new Stack<>();
        rollback = false;
    }

    /**
     * Completes the current client request. It registers the composite command into the
     * session's registry.
     */
    private void complete() {
        if (isRequestStarted()) {
            final boolean hasCommands = !commands.isEmpty();
            if (hasCommands && rollback) {
                final AbstractCanvasHandler canvasHandler = getCurrentSession().getCanvasHandler();
                commands.forEach(c -> c.undo(canvasHandler));
            } else if (hasCommands) {
                // If any commands have been aggregated, let's composite them and add into the registry.
                // Notice the composite command is set to the default "reverse" undo order. So it means
                // that any component that relies on RequestCommandManager must consider that commands will
                // be undo in the reverse order. Otherwise, each component should composite the commands to execute
                // and set the desired undo order in it's state, and finally perform the execution via this command manager.
                getRegistry().register(new CompositeCommand.Builder<AbstractCanvasHandler, CanvasViolation>()
                                               .forward() // PLEASE DO NOT CHANGE THIS, see comment above.
                                               .addCommands(new ArrayList<>(commands))
                                               .build());
            }
        } else {
            LOGGER.log(Level.WARNING,
                       "Current client request has not been started.");
        }
        clear();
    }

    private void rollback() {
        rollback = true;
    }

    private boolean isRequestStarted() {
        return null != commands;
    }

    private void clear() {
        setDelegateListener(null);
        if (commands != null) {
            commands.clear();
            commands = null;
        }
        rollback = false;
    }
}
