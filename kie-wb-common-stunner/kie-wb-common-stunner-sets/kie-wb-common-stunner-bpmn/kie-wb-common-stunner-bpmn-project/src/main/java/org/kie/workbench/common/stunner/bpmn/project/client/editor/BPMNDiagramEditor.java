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

package org.kie.workbench.common.stunner.bpmn.project.client.editor;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.stunner.bpmn.project.client.resource.BPMNDiagramResourceType;
import org.kie.workbench.common.stunner.client.widgets.palette.bs3.factory.BS3PaletteFactory;
import org.kie.workbench.common.stunner.client.widgets.session.presenter.impl.AbstractClientSessionPresenter;
import org.kie.workbench.common.stunner.core.client.session.command.impl.SessionCommandFactory;
import org.kie.workbench.common.stunner.core.client.util.ClientSessionUtils;
import org.kie.workbench.common.stunner.project.client.editor.AbstractProjectDiagramEditor;
import org.kie.workbench.common.stunner.project.client.editor.ProjectDiagramEditorMenuItemsBuilder;
import org.kie.workbench.common.stunner.project.client.service.ClientProjectDiagramServices;
import org.kie.workbench.common.stunner.project.client.session.impl.ClientProjectSessionManager;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.client.annotations.*;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.events.ChangeTitleWidgetEvent;
import org.uberfire.client.workbench.widgets.common.ErrorPopupPresenter;
import org.uberfire.ext.editor.commons.client.file.popups.SavePopUpPresenter;
import org.uberfire.lifecycle.*;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.menu.Menus;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@Dependent
@WorkbenchEditor( identifier = BPMNDiagramEditor.EDITOR_ID, supportedTypes = {BPMNDiagramResourceType.class} )
public class BPMNDiagramEditor extends AbstractProjectDiagramEditor<BPMNDiagramResourceType> {

    public static final String EDITOR_ID = "BPMNDiagramEditor";

    @Inject
    public BPMNDiagramEditor( final View view,
                              final PlaceManager placeManager,
                              final ErrorPopupPresenter errorPopupPresenter,
                              final Event<ChangeTitleWidgetEvent> changeTitleNotificationEvent,
                              final SavePopUpPresenter savePopUpPresenter,
                              final BPMNDiagramResourceType resourceType,
                              final ClientProjectDiagramServices projectDiagramServices,
                              final ClientProjectSessionManager clientSessionManager,
                              final AbstractClientSessionPresenter clientSessionPresenter,
                              final BS3PaletteFactory paletteFactory,
                              final ClientSessionUtils sessionUtils,
                              final SessionCommandFactory sessionCommandFactory,
                              final ProjectDiagramEditorMenuItemsBuilder menuItemsBuilder ) {
        super( view, placeManager, errorPopupPresenter, changeTitleNotificationEvent, savePopUpPresenter,
                resourceType, projectDiagramServices, clientSessionManager, clientSessionPresenter,
                paletteFactory, sessionUtils, sessionCommandFactory, menuItemsBuilder );
    }

    @PostConstruct
    public void init() {
        super.init();
    }

    @OnStartup
    public void onStartup( final ObservablePath path,
                               final PlaceRequest place ) {
        super._onStartup( path, place );
    }

    @OnOpen
    public void onOpen() {
        super._onOpen();
    }

    @OnClose
    public void onClose() {
        super._onClose();
    }

    @OnFocus
    public void onFocus() {
        super._onFocus();
    }

    @OnLostFocus
    public void onLostFocus() {
        super._onLostFocus();
    }

    @WorkbenchPartTitleDecoration
    public IsWidget getTitle() {
        return super.getTitle();
    }

    @WorkbenchPartTitle
    public String getTitleText() {
        return super._getTitleText();
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return super._getMenus();
    }

    @WorkbenchPartView
    public Widget getWidget() {
        return getView().asWidget();
    }

    @OnMayClose
    public boolean onMayClose() {
        return super.mayClose( getCurrentDiagramHash() );
    }

}
