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

import com.google.gwt.user.client.ui.IsWidget;
import org.kie.workbench.common.stunner.client.widgets.event.SessionDiagramOpenedEvent;
import org.kie.workbench.common.stunner.client.widgets.explorer.tree.TreeExplorer;
import org.kie.workbench.common.stunner.core.client.canvas.Canvas;
import org.kie.workbench.common.stunner.core.client.canvas.CanvasHandler;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.client.session.event.SessionDisposedEvent;
import org.kie.workbench.common.stunner.core.client.session.event.SessionOpenedEvent;
import org.uberfire.client.annotations.*;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.menu.Menus;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

@Dependent
@WorkbenchScreen( identifier = TreeExplorerScreen.SCREEN_ID )
public class TreeExplorerScreen {

    public static final String SCREEN_ID = "TreeExplorerScreen";

    @Inject
    TreeExplorer treeExplorer;

    private PlaceRequest placeRequest;
    private ClientSession session;

    @PostConstruct
    public void init() {
    }

    @OnStartup
    public void onStartup( final PlaceRequest placeRequest ) {
        this.placeRequest = placeRequest;
    }

    @OnOpen
    public void onOpen() {
    }

    @OnClose
    public void onClose() {
        treeExplorer.clear();
    }

    @WorkbenchMenu
    public Menus getMenu() {
        return null;
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Explorer";
    }

    @WorkbenchPartView
    public IsWidget getWidget() {
        return treeExplorer.asWidget();
    }

    @WorkbenchContextId
    public String getMyContextRef() {
        return "TreeExplorerScreenContext";
    }

    void onCanvasSessionOpened( @Observes SessionOpenedEvent sessionOpenedEvent ) {
        checkNotNull( "sessionOpenedEvent", sessionOpenedEvent );
        doOpenSession( sessionOpenedEvent.getSession() );
    }

    void onCanvasSessionDisposed( @Observes SessionDisposedEvent sessionDisposedEvent ) {
        checkNotNull( "sessionDisposedEvent", sessionDisposedEvent );
        doCloseSession();
    }

    void onSessionDiagramOpenedEvent( @Observes SessionDiagramOpenedEvent sessionDiagramOpenedEvent ) {
        checkNotNull( "sessionDiagramOpenedEvent", sessionDiagramOpenedEvent );
        if ( null != getCanvas() && getCanvas().equals( sessionDiagramOpenedEvent.getSession().getCanvas() ) ) {
            // Force to reload current session, for example, when a new diagram is just created.
            doOpenSession( session );
        }
    }

    private boolean isAlreadyOpen( final CanvasHandler canvasHandler ) {
        return null != treeExplorer && null != treeExplorer.getCanvasHandler()
                && canvasHandler.equals( treeExplorer.getCanvasHandler() );
    }

    private CanvasHandler getCanvasHandler() {
        return null != session ? session.getCanvasHandler() : null;
    }

    private Canvas getCanvas() {
        return null != session ? session.getCanvas() : null;
    }

    private void doOpenSession( final ClientSession session ) {
        this.session = session;
        if ( null != getCanvasHandler() && !isAlreadyOpen( getCanvasHandler() ) ) {
            treeExplorer.show( getCanvasHandler() );
        }
    }

    private void doCloseSession() {
        treeExplorer.clear();
        this.session = null;
    }

}
