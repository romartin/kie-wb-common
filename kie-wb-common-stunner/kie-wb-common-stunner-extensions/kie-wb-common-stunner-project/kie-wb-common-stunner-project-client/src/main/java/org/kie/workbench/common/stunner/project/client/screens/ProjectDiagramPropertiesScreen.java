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

package org.kie.workbench.common.stunner.project.client.screens;

import com.google.gwt.logging.client.LogConfiguration;
import com.google.gwt.user.client.ui.IsWidget;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.client.session.impl.AbstractClientFullSession;
import org.kie.workbench.common.stunner.core.client.session.impl.AbstractClientSessionManager;
import org.kie.workbench.common.stunner.forms.client.widgets.FormPropertiesWidget;
import org.kie.workbench.common.stunner.project.client.view.ProjectScreenView;
import org.uberfire.client.annotations.*;
import org.uberfire.client.workbench.events.ChangeTitleWidgetEvent;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.menu.Menus;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
@WorkbenchScreen( identifier = ProjectDiagramPropertiesScreen.SCREEN_ID )
public class ProjectDiagramPropertiesScreen {

    private static Logger LOGGER = Logger.getLogger( ProjectDiagramPropertiesScreen.class.getName() );
    public static final String SCREEN_ID = "ProjectDiagramPropertiesScreen";

    private final FormPropertiesWidget formPropertiesWidget;
    private final AbstractClientSessionManager clientSessionManager;
    private final Event<ChangeTitleWidgetEvent> changeTitleNotification;
    private final ProjectScreenView view;

    private PlaceRequest placeRequest;

    protected ProjectDiagramPropertiesScreen() {
        this( null, null, null, null );
    }

    @Inject
    public ProjectDiagramPropertiesScreen( final FormPropertiesWidget formPropertiesWidget,
                                           final AbstractClientSessionManager clientSessionManager,
                                           final Event<ChangeTitleWidgetEvent> changeTitleNotification,
                                           final ProjectScreenView view ) {
        this.formPropertiesWidget = formPropertiesWidget;
        this.clientSessionManager = clientSessionManager;
        this.changeTitleNotification = changeTitleNotification;
        this.view = view;
    }

    @PostConstruct
    public void init() {
        view.setWidget( formPropertiesWidget.asWidget() );
    }

    @OnStartup
    public void onStartup( final PlaceRequest placeRequest ) {
        this.placeRequest = placeRequest;
    }

    @OnOpen
    public void onOpen() {
        log( Level.INFO, "Opening ProjectDiagramPropertiesScreen." );
        final ClientSession current = clientSessionManager.getCurrentSession();
        handleSession( current );
    }

    @OnClose
    public void onClose() {
        log( Level.INFO, "Closing ProjectDiagramPropertiesScreen." );
        handleSession( null );
    }

    @SuppressWarnings( "unchecked" )
    private void handleSession( final ClientSession session ) {
        boolean done = false;
        view.showLoading();
        if ( null != session ) {
            try {
                final AbstractClientFullSession fullSession = ( AbstractClientFullSession ) session;
                // Show the loading view.
                view.showLoading();
                // Open the forms properties widget for the current session.
                formPropertiesWidget
                        .bind( fullSession )
                        .show( () -> view.hideLoading() );
                done = true;
            } catch ( ClassCastException e ) {
                // No writteable session. Do not show properties until read mode available.
                log( Level.INFO, "Session discarded for opening as not instance of full session." );
            }
        }

        if ( !done ) {
            formPropertiesWidget.unbind();
            view.hideLoading();
        }
    }

    @WorkbenchMenu
    public Menus getMenu() {
        return null;
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Properties";
    }

    @WorkbenchPartView
    public IsWidget getWidget() {
        // TODO: return view;
        return formPropertiesWidget.asWidget();
    }

    @WorkbenchContextId
    public String getMyContextRef() {
        return "stunnerProjectDiagramPropertiesScreenContext";
    }

    private void log( final Level level, final String message ) {
        if ( LogConfiguration.loggingIsEnabled() ) {
            LOGGER.log( level, message );
        }
    }

}
