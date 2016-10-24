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

import com.google.gwt.user.client.ui.IsWidget;
import org.kie.workbench.common.stunner.client.widgets.explorer.tree.TreeExplorer;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.client.session.impl.AbstractClientSessionManager;
import org.uberfire.client.annotations.*;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.menu.Menus;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
@WorkbenchScreen( identifier = ProjectDiagramExplorerScreen.SCREEN_ID )
public class ProjectDiagramExplorerScreen {

    public static final String SCREEN_ID = "ProjectDiagramExplorerScreen";

    private final AbstractClientSessionManager clientSessionManager;
    private final TreeExplorer treeExplorer;

    private PlaceRequest placeRequest;

    protected ProjectDiagramExplorerScreen() {
        this( null, null );
    }

    @Inject
    public ProjectDiagramExplorerScreen( final AbstractClientSessionManager clientSessionManager,
                                         final TreeExplorer treeExplorer ) {
        this.clientSessionManager = clientSessionManager;
        this.treeExplorer = treeExplorer;
    }

    @OnStartup
    public void onStartup( final PlaceRequest placeRequest ) {
        this.placeRequest = placeRequest;
    }

    @OnOpen
    public void onOpen() {
        final ClientSession current = clientSessionManager.getCurrentSession();
        if ( null != current ) {
            show( current );
        }
    }

    @OnClose
    public void onClose() {
        close();
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
        return "stunnerProjectDiagramExplorerScreenContext";
    }

    public void show( final ClientSession session ) {
        treeExplorer.show( session.getCanvasHandler() );
    }

    public void close() {
        treeExplorer.clear();
    }

}
