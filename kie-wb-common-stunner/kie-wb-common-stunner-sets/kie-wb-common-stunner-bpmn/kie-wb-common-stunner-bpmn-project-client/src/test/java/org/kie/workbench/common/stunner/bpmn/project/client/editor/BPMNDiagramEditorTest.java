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

package org.kie.workbench.common.stunner.bpmn.project.client.editor;

import java.util.function.Consumer;

import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.workbench.common.stunner.bpmn.project.client.resources.BPMNClientConstants;
import org.kie.workbench.common.stunner.bpmn.project.client.type.BPMNDiagramResourceType;
import org.kie.workbench.common.stunner.client.widgets.popups.PopupUtil;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.i18n.ClientTranslationService;
import org.kie.workbench.common.stunner.core.client.service.ServiceCallback;
import org.kie.workbench.common.stunner.core.client.session.command.ClientSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.command.impl.ValidateSessionCommand;
import org.kie.workbench.common.stunner.project.client.editor.AbstractProjectDiagramEditor;
import org.kie.workbench.common.stunner.project.client.editor.AbstractProjectDiagramEditorTest;
import org.kie.workbench.common.stunner.project.diagram.ProjectDiagram;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.client.views.pfly.widgets.Button;
import org.uberfire.client.views.pfly.widgets.InlineNotification;
import org.uberfire.mocks.EventSourceMock;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.mvp.PlaceRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// TODO @RunWith(GwtMockitoTestRunner.class)
@Ignore
public class BPMNDiagramEditorTest extends AbstractProjectDiagramEditorTest {

    private static final String MIGRATE_ACTION_TITLE = "MIGRATE_ACTION_TITLE";
    private static final String MIGRATE_ACTION_WARNING = "MIGRATE_ACTION_WARNING";
    private static final String MIGRATE_ACTION = "MIGRATE_ACTION";
    private static final String MIGRATE_CONFIRM_ACTION = "MIGRATE_CONFIRM_ACTION";
    private static final String COMMIT_MESSAGE = "COMMIT_MESSAGE";

    @Mock
    private ClientTranslationService translationService;

    @Mock
    private EventSourceMock<BPMNMigrateDiagramEvent> migrateDiagramEvent;

    @Mock
    private PopupUtil popupUtil;

    @Mock
    private PlaceRequest currentPlace;

    @Mock
    private AbstractCanvasHandler canvasHandler;

    private ArgumentCaptor<Command> commandCaptor;

    private ArgumentCaptor<BPMNMigrateDiagramEvent> migrateDiagramEventCaptor;

    private ArgumentCaptor<ClientSessionCommand.Callback> sessionCommandCallback;

    private ArgumentCaptor<ParameterizedCommand> parameterizedCommandCaptor;

    private ArgumentCaptor<ServiceCallback> serviceCallbackCaptor;

    private BPMNDiagramEditor diagramEditor;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        commandCaptor = ArgumentCaptor.forClass(Command.class);
        migrateDiagramEventCaptor = ArgumentCaptor.forClass(BPMNMigrateDiagramEvent.class);
        sessionCommandCallback = ArgumentCaptor.forClass(ClientSessionCommand.Callback.class);
        parameterizedCommandCaptor = ArgumentCaptor.forClass(ParameterizedCommand.class);
        serviceCallbackCaptor = ArgumentCaptor.forClass(ServiceCallback.class);
        when(menuSessionItems.setErrorConsumer(any(Consumer.class))).thenReturn(menuSessionItems);
        when(menuSessionItems.setLoadingCompleted(any(Command.class))).thenReturn(menuSessionItems);
        when(menuSessionItems.setLoadingStarts(any(Command.class))).thenReturn(menuSessionItems);

        when(clientFullSession.getCanvasHandler()).thenReturn(canvasHandler);
        when(canvasHandler.getDiagram()).thenReturn(diagram);

        when(translationService.getValue(BPMNClientConstants.EditorMigrateActionTitle)).thenReturn(MIGRATE_ACTION_TITLE);
        when(translationService.getValue(BPMNClientConstants.EditorMigrateActionWarning)).thenReturn(MIGRATE_ACTION_WARNING);
        when(translationService.getValue(BPMNClientConstants.EditorMigrateAction)).thenReturn(MIGRATE_ACTION);
        when(translationService.getValue(BPMNClientConstants.EditorMigrateConfirmAction)).thenReturn(MIGRATE_CONFIRM_ACTION);

        super.setUp();
    }

    @Override
    protected BPMNDiagramResourceType mockResourceType() {
        final BPMNDiagramResourceType resourceType = mock(BPMNDiagramResourceType.class);
        when(resourceType.getSuffix()).thenReturn("bpmn");
        when(resourceType.getShortName()).thenReturn("Business Process");
        return resourceType;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractProjectDiagramEditor createDiagramEditor() {
        // TODO
        /*diagramEditor = spy(new BPMNDiagramEditor(view,
                                                  placeManager,
                                                  errorPopupPresenter,
                                                  changeTitleNotificationEvent,
                                                  savePopUpPresenter,
                                                  (BPMNDiagramResourceType) getResourceType(),
                                                  clientProjectDiagramService,
                                                  sessionEditorPresenter,
                                                  menuSessionItems,
                                                  onDiagramFocusEvent,
                                                  onDiagramLostFocusEvent,
                                                  projectMessagesListener,
                                                  diagramClientErrorHandler,
                                                  translationService,
                                                  migrateDiagramEvent,
                                                  popupUtil) {
            {
                fileMenuBuilder = BPMNDiagramEditorTest.this.fileMenuBuilder;
                workbenchContext = BPMNDiagramEditorTest.this.workbenchContext;
                projectController = BPMNDiagramEditorTest.this.projectController;
                versionRecordManager = BPMNDiagramEditorTest.this.versionRecordManager;
                alertsButtonMenuItemBuilder = BPMNDiagramEditorTest.this.alertsButtonMenuItemBuilder;
                place = BPMNDiagramEditorTest.this.currentPlace;
                // presenter = BPMNDiagramEditorTest.this.sessionEditorPresenter;
                kieView = BPMNDiagramEditorTest.this.kieView;
                overviewWidget = BPMNDiagramEditorTest.this.overviewWidget;
            }

            @Override
            protected void log(Level level,
                               String message) {
                //avoid GWT log initialization.
            }
        });*/
        return diagramEditor;
    }

    @Test
    public void testMigrateWhenNotDirty() {
        ObservablePath currentPath = mock(ObservablePath.class);
        when(versionRecordManager.getCurrentPath()).thenReturn(currentPath);
        doReturn(false).when(diagramEditor).isDirty(any(Integer.class));

        diagramEditor.onMigrate();
        verify(popupUtil,
               times(1)).showConfirmPopup(eq(MIGRATE_ACTION_TITLE),
                                          eq(MIGRATE_ACTION_WARNING),
                                          eq(InlineNotification.InlineNotificationType.WARNING),
                                          eq(MIGRATE_ACTION),
                                          eq(Button.ButtonStyleType.PRIMARY),
                                          eq(MIGRATE_CONFIRM_ACTION),
                                          commandCaptor.capture());
        commandCaptor.getValue().execute();
        verify(migrateDiagramEvent,
               times(1)).fire(migrateDiagramEventCaptor.capture());
        assertEquals(currentPath,
                     migrateDiagramEventCaptor.getValue().getSourcePath());
        assertEquals(currentPlace,
                     migrateDiagramEventCaptor.getValue().getSourcePlace());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMigrateWhenDirty() {
        ObservablePath currentPath = mock(ObservablePath.class);
        when(versionRecordManager.getCurrentPath()).thenReturn(currentPath);
        doReturn(true).when(diagramEditor).isDirty(any(Integer.class));

        diagramEditor.onMigrate();
        verify(popupUtil,
               times(1)).showConfirmPopup(eq(MIGRATE_ACTION_TITLE),
                                          eq(MIGRATE_ACTION_WARNING),
                                          eq(InlineNotification.InlineNotificationType.WARNING),
                                          eq(MIGRATE_ACTION),
                                          eq(Button.ButtonStyleType.PRIMARY),
                                          eq(MIGRATE_CONFIRM_ACTION),
                                          commandCaptor.capture());
        commandCaptor.getValue().execute();

        ValidateSessionCommand validateSessionCommand = mock(ValidateSessionCommand.class);
        verify(validateSessionCommand,
               times(1)).execute(sessionCommandCallback.capture());
        sessionCommandCallback.getValue().onSuccess();

        verify(savePopUpPresenter,
               times(1)).show(eq(currentPath),
                              parameterizedCommandCaptor.capture());
        parameterizedCommandCaptor.getValue().execute(COMMIT_MESSAGE);

        verify(clientProjectDiagramService,
               times(1)).saveOrUpdate(eq(currentPath),
                                      any(ProjectDiagram.class),
                                      any(Metadata.class),
                                      eq(COMMIT_MESSAGE),
                                      serviceCallbackCaptor.capture());
        serviceCallbackCaptor.getValue().onSuccess(diagram);
        verify(migrateDiagramEvent,
               times(1)).fire(migrateDiagramEventCaptor.capture());
        assertEquals(currentPath,
                     migrateDiagramEventCaptor.getValue().getSourcePath());
        assertEquals(currentPlace,
                     migrateDiagramEventCaptor.getValue().getSourcePlace());
    }

    @Test
    @Override
    public void testCloseEditor() {
        super.testCloseEditor();
    }
}
