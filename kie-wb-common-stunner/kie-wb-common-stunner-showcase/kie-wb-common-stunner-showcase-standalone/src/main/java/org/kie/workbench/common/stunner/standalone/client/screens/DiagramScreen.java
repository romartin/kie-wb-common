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

package org.kie.workbench.common.stunner.standalone.client.screens;

import com.google.gwt.logging.client.LogConfiguration;
import com.google.gwt.user.client.ui.IsWidget;
import org.kie.workbench.common.stunner.client.widgets.session.presenter.impl.AbstractClientSessionPresenter;
import org.kie.workbench.common.stunner.client.widgets.toolbar.Toolbar;
import org.kie.workbench.common.stunner.client.widgets.toolbar.ToolbarCommandCallback;
import org.kie.workbench.common.stunner.client.widgets.toolbar.impl.ToolbarFactory;
import org.kie.workbench.common.stunner.core.client.canvas.CanvasHandler;
import org.kie.workbench.common.stunner.core.client.service.ClientDiagramServices;
import org.kie.workbench.common.stunner.core.client.service.ClientFactoryServices;
import org.kie.workbench.common.stunner.core.client.service.ClientRuntimeError;
import org.kie.workbench.common.stunner.core.client.service.ServiceCallback;
import org.kie.workbench.common.stunner.core.client.session.impl.AbstractClientFullSession;
import org.kie.workbench.common.stunner.core.client.session.impl.AbstractClientSessionManager;
import org.kie.workbench.common.stunner.core.client.util.ClientSessionUtils;
import org.kie.workbench.common.stunner.core.client.util.StunnerClientLogger;
import org.kie.workbench.common.stunner.core.client.validation.canvas.CanvasValidationViolation;
import org.kie.workbench.common.stunner.core.client.validation.canvas.CanvasValidatorCallback;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.lookup.LookupManager;
import org.kie.workbench.common.stunner.core.lookup.diagram.DiagramLookupRequest;
import org.kie.workbench.common.stunner.core.lookup.diagram.DiagramLookupRequestImpl;
import org.kie.workbench.common.stunner.core.lookup.diagram.DiagramRepresentation;
import org.kie.workbench.common.stunner.core.util.StunnerLogger;
import org.kie.workbench.common.stunner.core.util.UUID;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.annotations.*;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.events.ChangeTitleWidgetEvent;
import org.uberfire.lifecycle.*;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
@WorkbenchScreen( identifier = DiagramScreen.SCREEN_ID )
public class DiagramScreen {

    private static Logger LOGGER = Logger.getLogger( DiagramScreen.class.getName() );

    public static final String SCREEN_ID = "DiagramScreen";

    private final ClientFactoryServices clientFactoryServices;
    private final ClientDiagramServices clientDiagramServices;
    private final AbstractClientSessionManager canvasSessionManager;
    private final AbstractClientSessionPresenter clientSessionPresenter;
    private final PlaceManager placeManager;
    private final Event<ChangeTitleWidgetEvent> changeTitleNotificationEvent;
    private final ToolbarFactory toolbars;
    private final ClientSessionUtils sessionUtils;

    private PlaceRequest placeRequest;
    private String title = "Diagram Screen";
    private AbstractClientFullSession session;
    private Toolbar<AbstractClientFullSession> toolbar;
    private Menus menu = null;

    @Inject
    public DiagramScreen( final ClientFactoryServices clientFactoryServices,
                          final ClientDiagramServices clientDiagramServices,
                          final AbstractClientSessionManager canvasSessionManager,
                          final AbstractClientSessionPresenter clientSessionPresenter,
                          final PlaceManager placeManager,
                          final Event<ChangeTitleWidgetEvent> changeTitleNotificationEvent,
                          final ToolbarFactory toolbars,
                          final ClientSessionUtils sessionUtils ) {
        this.clientFactoryServices = clientFactoryServices;
        this.clientDiagramServices = clientDiagramServices;
        this.canvasSessionManager = canvasSessionManager;
        this.clientSessionPresenter = clientSessionPresenter;
        this.placeManager = placeManager;
        this.changeTitleNotificationEvent = changeTitleNotificationEvent;
        this.toolbars = toolbars;
        this.sessionUtils = sessionUtils;
    }

    @PostConstruct
    public void init() {
        // Create a new full control session.
        session = ( AbstractClientFullSession ) canvasSessionManager.newFullSession();
        // Initialize the session presenter.
        clientSessionPresenter.initialize( session, 1400, 650 );
        // Configure toolbar.
        this.toolbar = buildToolbar();
        clientSessionPresenter.getView().setToolbar( toolbar.getView() );
        toolbar.initialize( session, new ToolbarCommandCallback<Object>() {
            @Override
            public void onCommandExecuted( final Object result ) {
            }

            @Override
            public void onError( final ClientRuntimeError error ) {
                showError( error.toString() );
            }
        } );
    }

    private Toolbar<AbstractClientFullSession> buildToolbar() {
        return toolbars
                .withVisitGraphCommand()
                .withClearCommand()
                .withClearSelectionCommand()
                .withDeleteSelectedElementsCommand()
                .withSwitchGridCommand()
                .withUndoCommand()
                .withValidateCommand()
                .build();
    }

    @OnStartup
    public void onStartup( final PlaceRequest placeRequest ) {
        this.placeRequest = placeRequest;
        this.menu = makeMenuBar();
        final String name = placeRequest.getParameter( "name", "" );
        final boolean isCreate = name == null || name.trim().length() == 0;
        final Command callback = () -> {
            final Diagram diagram = clientSessionPresenter.getCanvasHandler().getDiagram();
            if ( null != diagram ) {
                // Update screen title.
                updateTitle( diagram.getMetadata().getTitle() );

            }

        };
        if ( isCreate ) {
            final String defSetId = placeRequest.getParameter( "defSetId", "" );
            final String shapeSetd = placeRequest.getParameter( "shapeSetId", "" );
            final String title = placeRequest.getParameter( "title", "" );
            // Create a new diagram.
            newDiagram( UUID.uuid(), title, defSetId, shapeSetd, callback );

        } else {
            // Load an existing diagram.
            load( name, callback );

        }

    }

    private Menus makeMenuBar() {
        return MenuFactory
                .newTopLevelMenu( "Save" )
                .respondsWith( getSaveCommand() )
                .endMenu()
                // **** For dev. purposes. **********
                .newTopLevelMenu( "Switch log level" )
                .respondsWith( getSwitchLogLevelCommand() )
                .endMenu()
                .newTopLevelMenu( "Log session" )
                .respondsWith( getLogSessionCommand() )
                .endMenu()
                .newTopLevelMenu( "Log graph" )
                .respondsWith( getLogGraphCommand() )
                .endMenu()
                .newTopLevelMenu( "Log command history" )
                .respondsWith( getLogCommandHistoryCommand() )
                .endMenu()
                .build();
    }

    private Command getSaveCommand() {
        return this::save;
    }

    private Command getSwitchLogLevelCommand() {
        return StunnerClientLogger::switchLogLevel;
    }

    private Command getLogGraphCommand() {
        return () -> StunnerLogger.log( getGraph() );
    }

    private Command getLogCommandHistoryCommand() {
        return () -> StunnerClientLogger.logCommandHistory( session );
    }

    private Command getLogSessionCommand() {
        return () -> StunnerClientLogger.logSessionInfo( session );
    }

    private Diagram getDiagram() {
        return null != clientSessionPresenter.getCanvasHandler() ? clientSessionPresenter.getCanvasHandler().getDiagram() : null;
    }

    private Graph getGraph() {
        return null != getDiagram() ? getDiagram().getGraph() : null;
    }

    private void save() {
        session.getCanvasValidationControl().validate( new CanvasValidatorCallback() {
            @Override
            public void onSuccess() {
                doSave( new ServiceCallback<Diagram>() {
                    @Override
                    public void onSuccess( Diagram item ) {
                        log( Level.INFO, "Save operation finished for diagram [" + item.getName() + "]." );
                    }

                    @Override
                    public void onError( ClientRuntimeError error ) {
                        showError( error.toString() );
                    }
                } );
            }

            @Override
            public void onFail( Iterable<CanvasValidationViolation> violations ) {
                log( Level.WARNING, "Validation failed [violations=" + violations.toString() + "]." );
            }
        } );
    }

    @SuppressWarnings( "unchecked" )
    private void doSave( final ServiceCallback<Diagram> diagramServiceCallback ) {
        // Update diagram's image data as thumbnail.
        final String thumbData = sessionUtils.canvasToImageData( session );
        final CanvasHandler canvasHandler = session.getCanvasHandler();
        final Diagram diagram = canvasHandler.getDiagram();
        diagram.getMetadata().setThumbData( thumbData );
        // Perform update operation remote call.
        clientDiagramServices.saveOrUpdate( diagram, diagramServiceCallback );
    }

    private void newDiagram( final String uuid,
                            final String title,
                            final String definitionSetId,
                            final String shapeSetId,
                            final Command callback ) {
        clientFactoryServices.newDiagram( uuid, definitionSetId, new ServiceCallback<Diagram>() {
            @Override
            public void onSuccess( final Diagram diagram ) {
                final Metadata metadata = diagram.getMetadata();
                metadata.setShapeSetId( shapeSetId );
                metadata.setTitle( title );
                open( diagram, callback );
            }

            @Override
            public void onError( final ClientRuntimeError error ) {
                showError( error.toString() );
                callback.execute();
            }
        } );

    }

    private void load( final String name,
                       final Command callback ) {
        final DiagramLookupRequest request = new DiagramLookupRequestImpl.Builder().withName( name ).build();
        clientDiagramServices.lookup( request, new ServiceCallback<LookupManager.LookupResponse<DiagramRepresentation>>() {
            @Override
            public void onSuccess( LookupManager.LookupResponse<DiagramRepresentation> diagramRepresentations ) {
                if ( null != diagramRepresentations && !diagramRepresentations.getResults().isEmpty() ) {
                    final Path path = diagramRepresentations.getResults().get( 0 ).getPath();
                    loadByPath( path, callback );
                }
            }

            @Override
            public void onError( ClientRuntimeError error ) {
                showError( error.toString() );
                callback.execute();
            }
        } );
    }

    private void loadByPath( final Path path, final Command callback ) {
        clientDiagramServices.getByPath( path, new ServiceCallback<Diagram>() {
            @Override
            public void onSuccess( final Diagram diagram ) {
                open( diagram, callback );
            }

            @Override
            public void onError( final ClientRuntimeError error ) {
                showError( error.toString() );
                callback.execute();
            }
        } );
    }

    private void open( final Diagram diagram,
                      final Command callback ) {
        clientSessionPresenter.open( diagram, callback );
    }

    private void updateTitle( final String title ) {
        // Change screen title.
        DiagramScreen.this.title = title;
        changeTitleNotificationEvent.fire( new ChangeTitleWidgetEvent( placeRequest, this.title ) );

    }

    @OnOpen
    public void onOpen() {
        resume();
    }

    @OnFocus
    public void onFocus() {

    }

    @OnLostFocus
    public void OnLostFocus() {

    }

    @OnClose
    public void onClose() {
        disposeSession();
    }

    @WorkbenchMenu
    public Menus getMenu() {
        return menu;
    }

    private void resume() {
        if ( null != session && session.isOpened() ) {
            canvasSessionManager.resume( session );
        }
    }

    private void disposeSession() {
        canvasSessionManager.dispose();
        if ( null != toolbar ) {
            toolbar.destroy();
        }
        this.toolbar = null;
        this.session = null;
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return title;
    }

    @WorkbenchPartView
    public IsWidget getWidget() {
        return clientSessionPresenter.getView();
    }

    @WorkbenchContextId
    public String getMyContextRef() {
        return "stunnerDiagramScreenContext";
    }

    protected void showError( String message ) {
        log( Level.SEVERE, message );
    }

    private void log( final Level level, final String message ) {
        if ( LogConfiguration.loggingIsEnabled() ) {
            LOGGER.log( level, message );
        }
    }

}
