/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.project.client.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.logging.client.LogConfiguration;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.stunner.client.widgets.presenters.session.SessionPresenter;
import org.kie.workbench.common.stunner.client.widgets.presenters.session.impl.SessionEditorPresenter;
import org.kie.workbench.common.stunner.core.client.canvas.CanvasHandler;
import org.kie.workbench.common.stunner.core.client.error.DiagramClientErrorHandler;
import org.kie.workbench.common.stunner.core.client.i18n.ClientTranslationService;
import org.kie.workbench.common.stunner.core.client.service.ClientRuntimeError;
import org.kie.workbench.common.stunner.core.client.service.ServiceCallback;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.client.session.command.ClientSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.event.OnSessionErrorEvent;
import org.kie.workbench.common.stunner.core.client.session.impl.EditorSession;
import org.kie.workbench.common.stunner.core.client.shape.Shape;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.rule.RuleViolation;
import org.kie.workbench.common.stunner.core.util.HashUtil;
import org.kie.workbench.common.stunner.core.validation.DiagramElementViolation;
import org.kie.workbench.common.stunner.core.validation.Violation;
import org.kie.workbench.common.stunner.core.validation.impl.ValidationUtils;
import org.kie.workbench.common.stunner.project.client.editor.event.OnDiagramFocusEvent;
import org.kie.workbench.common.stunner.project.client.editor.event.OnDiagramLoseFocusEvent;
import org.kie.workbench.common.stunner.project.client.resources.i18n.StunnerProjectClientConstants;
import org.kie.workbench.common.stunner.project.client.screens.ProjectMessagesListener;
import org.kie.workbench.common.stunner.project.client.service.ClientProjectDiagramService;
import org.kie.workbench.common.stunner.project.diagram.ProjectDiagram;
import org.kie.workbench.common.widgets.client.menu.FileMenuBuilder;
import org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants;
import org.kie.workbench.common.widgets.metadata.client.KieEditor;
import org.kie.workbench.common.widgets.metadata.client.KieEditorView;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.UberView;
import org.uberfire.client.workbench.events.AbstractPlaceEvent;
import org.uberfire.client.workbench.events.ChangeTitleWidgetEvent;
import org.uberfire.client.workbench.events.PlaceGainFocusEvent;
import org.uberfire.client.workbench.events.PlaceHiddenEvent;
import org.uberfire.client.workbench.type.ClientResourceType;
import org.uberfire.client.workbench.widgets.common.ErrorPopupPresenter;
import org.uberfire.ext.editor.commons.client.file.popups.SavePopUpPresenter;
import org.uberfire.ext.widgets.common.client.common.popups.YesNoCancelPopup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.PathPlaceRequest;
import org.uberfire.workbench.model.menu.Menus;

public abstract class AbstractProjectDiagramEditor<R extends ClientResourceType> extends KieEditor<ProjectDiagram> {

    private static Logger LOGGER = Logger.getLogger(AbstractProjectDiagramEditor.class.getName());
    private static final String TITLE_FORMAT_TEMPLATE = "#title.#suffix - #type";

    public interface View extends UberView<AbstractProjectDiagramEditor>,
                                  KieEditorView,
                                  RequiresResize,
                                  ProvidesResize,
                                  IsWidget {

        void setWidget(IsWidget widget);
    }

    private final PlaceManager placeManager;
    private final ErrorPopupPresenter errorPopupPresenter;
    private final Event<ChangeTitleWidgetEvent> changeTitleNotificationEvent;
    private final R resourceType;
    private final ClientProjectDiagramService projectDiagramServices;
    private final ProjectEditorMenuSessionItems menuSessionItems;
    private final ProjectMessagesListener projectMessagesListener;
    private final Event<OnDiagramFocusEvent> onDiagramFocusEvent;
    private final Event<OnDiagramLoseFocusEvent> onDiagramLostFocusEvent;
    private final DiagramClientErrorHandler diagramClientErrorHandler;
    private final ClientTranslationService translationService;
    private SessionEditorPresenter<EditorSession> presenter;
    private String title = "Project Diagram Editor";

    @Inject
    public AbstractProjectDiagramEditor(final View view,
                                        final PlaceManager placeManager,
                                        final ErrorPopupPresenter errorPopupPresenter,
                                        final Event<ChangeTitleWidgetEvent> changeTitleNotificationEvent,
                                        final SavePopUpPresenter savePopUpPresenter,
                                        final R resourceType,
                                        final ClientProjectDiagramService projectDiagramServices,
                                        final SessionEditorPresenter<EditorSession> presenter,
                                        final ProjectEditorMenuSessionItems menuSessionItems,
                                        final Event<OnDiagramFocusEvent> onDiagramFocusEvent,
                                        final Event<OnDiagramLoseFocusEvent> onDiagramLostFocusEvent,
                                        final ProjectMessagesListener projectMessagesListener,
                                        final DiagramClientErrorHandler diagramClientErrorHandler,
                                        final ClientTranslationService translationService) {
        super(view);
        this.placeManager = placeManager;
        this.errorPopupPresenter = errorPopupPresenter;
        this.changeTitleNotificationEvent = changeTitleNotificationEvent;
        this.savePopUpPresenter = savePopUpPresenter;
        this.resourceType = resourceType;
        this.projectDiagramServices = projectDiagramServices;
        this.presenter = presenter;
        this.menuSessionItems = menuSessionItems;
        this.projectMessagesListener = projectMessagesListener;
        this.diagramClientErrorHandler = diagramClientErrorHandler;
        this.onDiagramFocusEvent = onDiagramFocusEvent;
        this.onDiagramLostFocusEvent = onDiagramLostFocusEvent;
        this.translationService = translationService;
    }

    protected abstract int getCanvasWidth();

    protected abstract int getCanvasHeight();

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        title = translationService.getValue(StunnerProjectClientConstants.DIAGRAM_EDITOR_DEFAULT_TITLE);
        getView().init(this);
        projectMessagesListener.enable();
        menuSessionItems
                .setLoadingStarts(this::showLoadingViews)
                .setLoadingCompleted(this::hideLoadingViews)
                .setErrorConsumer(this::showError);
    }

    protected void doStartUp(final ObservablePath path,
                             final PlaceRequest place) {
        init(path,
             place,
             resourceType);
    }

    @Override
    protected void loadContent() {
        projectDiagramServices.getByPath(versionRecordManager.getCurrentPath(),
                                         new ServiceCallback<ProjectDiagram>() {
                                             @Override
                                             public void onSuccess(final ProjectDiagram item) {
                                                 open(item);
                                             }

                                             @Override
                                             public void onError(final ClientRuntimeError error) {
                                                 onLoadError(error);
                                             }
                                         });
    }

    @SuppressWarnings("unchecked")
    protected void open(final ProjectDiagram diagram) {
        showLoadingViews();
        setOriginalHash(diagram.hashCode());
        getView().setWidget(presenter.getView());
        presenter
                .withToolbar(false)
                .withPalette(true)
                .displayNotifications(type -> true)
                .open(diagram,
                      new SessionPresenter.SessionPresenterCallback<Diagram>() {
                          @Override
                          public void afterSessionOpened() {

                          }

                          @Override
                          public void afterCanvasInitialized() {

                          }

                          @Override
                          public void onSuccess() {
                              menuSessionItems.bind(getSession());
                              updateTitle(diagram.getMetadata().getTitle());
                              hideLoadingViews();
                              setOriginalHash(getCurrentDiagramHash());
                              resetEditorPages(diagram.getMetadata().getOverview());
                              onDiagramLoad();
                          }

                          @Override
                          public void onError(final ClientRuntimeError error) {
                              onLoadError(error);
                          }
                      });
    }

    protected void onDiagramLoad() {
        /* Override this method to trigger some action after a Diagram is loaded. */
    }

    @Override
    protected void onValidate(final Command finished) {
        onValidationSuccess();
        hideLoadingViews();
        finished.execute();
    }

    /**
     * This method is called just once clicking on save.
     * Before starting the save process, let's perform a diagram validation
     * to check all it's valid.
     * It's allowed to continue with the save process event if warnings found,
     * but cannot save if any error is present in order to
     * guarantee the diagram's consistency.
     */
    @Override
    protected void save() {
        final Command continueSaveOnceValid = () -> super.save();
        menuSessionItems
                .getCommands()
                .getValidateSessionCommand()
                .execute(new ClientSessionCommand.Callback<Collection<DiagramElementViolation<RuleViolation>>>() {
                    @Override
                    public void onSuccess() {
                        continueSaveOnceValid.execute();
                    }

                    @Override
                    public void onError(final Collection<DiagramElementViolation<RuleViolation>> violations) {
                        final Violation.Type maxSeverity = ValidationUtils.getMaxSeverity(violations);
                        if (maxSeverity.equals(Violation.Type.ERROR)) {
                            onValidationFailed(violations);
                        } else {
                            // Allow saving when only warnings founds.
                            continueSaveOnceValid.execute();
                        }
                    }
                });
    }

    /**
     * Considering the diagram valid at this point ,
     * it delegates the save operation to the diagram services bean.
     * @param commitMessage The commit's message.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void save(final String commitMessage) {
        super.save(commitMessage);
        showSavingViews();
        // Perform update operation remote call.
        final ObservablePath diagramPath = versionRecordManager.getCurrentPath();
        projectDiagramServices.saveOrUpdate(diagramPath,
                                            getDiagram(),
                                            metadata,
                                            commitMessage,
                                            new ServiceCallback<ProjectDiagram>() {
                                                @Override
                                                public void onSuccess(final ProjectDiagram item) {
                                                    getSaveSuccessCallback(item.hashCode()).callback(diagramPath);
                                                    onSaveSuccess();
                                                    hideLoadingViews();
                                                }

                                                @Override
                                                public void onError(final ClientRuntimeError error) {
                                                    onSaveError(error);
                                                }
                                            });
    }

    @Override
    public RemoteCallback<Path> getSaveSuccessCallback(final int newHash) {
        return (path) -> {
            versionRecordManager.reloadVersions(path);
            setOriginalHash(newHash);
        };
    }

    public void hideDiagramEditorDocks(@Observes PlaceHiddenEvent event) {
        if (verifyEventIdentifier(event)) {
            onDiagramLostFocusEvent.fire(new OnDiagramLoseFocusEvent());
        }
    }

    public void showDiagramEditorDocks(@Observes PlaceGainFocusEvent event) {
        if (verifyEventIdentifier(event)) {
            onDiagramFocusEvent.fire(new OnDiagramFocusEvent());
        }
    }

    @Override
    protected void makeMenuBar() {
        menuSessionItems.populateMenu(fileMenuBuilder);
        if (canUpdateProject()) {
            fileMenuBuilder
                    .addSave(versionRecordManager.newSaveMenuItem(this::saveAction))
                    .addCopy(versionRecordManager.getCurrentPath(),
                             assetUpdateValidator)
                    .addRename(versionRecordManager.getPathToLatest(),
                               assetUpdateValidator)
                    .addDelete(versionRecordManager.getPathToLatest(),
                               assetUpdateValidator);
        }

        fileMenuBuilder
                .addNewTopLevelMenu(versionRecordManager.buildMenu())
                .addNewTopLevelMenu(alertsButtonMenuItemBuilder.build());
    }

    protected void doOpen() {
        presenter.resume();
    }

    protected void showLoadingViews() {
        getView().showLoading();
    }

    protected void showSavingViews() {
        getView().showSaving();
    }

    protected void hideLoadingViews() {
        getView().hideBusyIndicator();
    }

    protected void doClose() {
        menuSessionItems.destroy();
        destroySession();
    }

    protected void doFocus() {
        // TODO: presenter.resume();
    }

    protected void doLostFocus() {
        // TODO: presenter.pause();
    }

    void onSessionErrorEvent(final @Observes OnSessionErrorEvent errorEvent) {
        if (isSameSession(errorEvent.getSession())) {
            executeWithConfirm(translationService.getValue(StunnerProjectClientConstants.ON_ERROR_CONFIRM_UNDO_LAST_ACTION,
                                                           errorEvent.getError()),
                               () -> menuSessionItems.getCommands().getUndoSessionCommand().execute());
        }
    }

    private boolean isSameSession(final ClientSession other) {
        return null != other && null != getSession() && other.equals(getSession());
    }

    protected abstract String getEditorIdentifier();

    public String getTitleText() {
        return title;
    }

    protected Menus getMenus() {
        if (menus == null) {
            makeMenuBar();
        }
        return menus;
    }

    @Override
    protected void onSave() {
        if (hasUnsavedChanges()) {
            super.onSave();
        } else if (!versionRecordManager.isCurrentLatest()) {
            //If VersionRecordManager is not showing the latest the save represents a "Restore" operation.
            super.onSave();
        } else {
            final String message = CommonConstants.INSTANCE.NoChangesSinceLastSave();
            log(Level.INFO,
                message);
            presenter.getView().showMessage(message);
        }
    }

    private void destroySession() {
        menuItems.clear();
        if (Objects.nonNull(presenter)) {
            presenter.destroy();
            presenter = null;
        }
    }

    private void updateTitle(final String title) {
        // Change editor's title.
        this.title = formatTitle(title);
        changeTitleNotificationEvent.fire(new ChangeTitleWidgetEvent(this.place,
                                                                     this.title));
    }

    /**
     * Format the Diagram title to be displayed on the Editor.
     * This method can be override to customization and the default implementation just return the title from the diagram metadata.
     * @param title diagram metadata title
     * @return formatted title
     */
    protected String formatTitle(final String title) {
        if (Objects.isNull(resourceType)) {
            return title;
        }
        return TITLE_FORMAT_TEMPLATE
                .replace("#title",
                         title)
                .replace("#suffix",
                         resourceType.getSuffix())
                .replace("#type",
                         resourceType.getShortName());
    }

    private EditorSession getSession() {
        return null != presenter ? presenter.getInstance() : null;
    }

    @SuppressWarnings("unchecked")
    protected int getCurrentDiagramHash() {
        if (null == getDiagram()) {
            return 0;
        }
        int hash = getDiagram().hashCode();
        if (null == getCanvasHandler() ||
                null == getCanvasHandler().getCanvas() ||
                null == getCanvasHandler().getCanvas().getShapes()) {
            return hash;
        }
        Collection<Shape> collectionOfShapes = getCanvasHandler().getCanvas().getShapes();
        ArrayList<Shape> shapes = new ArrayList<>();
        shapes.addAll(collectionOfShapes);
        shapes.sort((a, b) -> (a.getShapeView().getShapeX() == b.getShapeView().getShapeX()) ?
                (int) Math.round(a.getShapeView().getShapeY() - b.getShapeView().getShapeY()) :
                (int) Math.round(a.getShapeView().getShapeX() - b.getShapeView().getShapeX()));
        for (Shape shape : shapes) {
            hash = HashUtil.combineHashCodes(hash,
                                             Double.hashCode(shape.getShapeView().getShapeX()),
                                             Double.hashCode(shape.getShapeView().getShapeY()));
        }
        return hash;
    }

    protected CanvasHandler getCanvasHandler() {
        return null != getSession() ? getSession().getCanvasHandler() : null;
    }

    protected ProjectDiagram getDiagram() {
        return null != getCanvasHandler() ? (ProjectDiagram) getCanvasHandler().getDiagram() : null;
    }

    private void executeWithConfirm(final String message,
                                    final Command command) {
        final Command yesCommand = command::execute;
        final Command noCommand = () -> {
        };
        final YesNoCancelPopup popup =
                YesNoCancelPopup.newYesNoCancelPopup(message,
                                                     null,
                                                     yesCommand,
                                                     noCommand,
                                                     noCommand);
        popup.show();
    }

    protected View getView() {
        return (View) baseView;
    }

    protected void onSaveSuccess() {
        final String message = translationService.getValue(StunnerProjectClientConstants.DIAGRAM_SAVE_SUCCESSFUL);
        log(Level.INFO,
            message);
        presenter.getView().showMessage(message);
        setOriginalHash(getCurrentDiagramHash());
    }

    protected void onSaveError(final ClientRuntimeError error) {
        showError(error);
    }

    private void onValidationSuccess() {
        log(Level.INFO,
            "Validation SUCCESS.");
    }

    private void onValidationFailed(final Collection<DiagramElementViolation<RuleViolation>> violations) {
        log(Level.WARNING,
            "Validation FAILED [violations=" + violations.toString() + "]");
        hideLoadingViews();
    }

    private void onLoadError(final ClientRuntimeError error) {
        showError(error);

        //close editor in case of error when opening the editor
        placeManager.forceClosePlace(new PathPlaceRequest(versionRecordManager.getCurrentPath(),
                                                          getEditorIdentifier()));
    }

    protected void showError(final ClientRuntimeError error) {
        diagramClientErrorHandler.handleError(error, this::showError);
        log(Level.SEVERE, error.toString());
    }

    protected void showError(final String message) {
        errorPopupPresenter.showMessage(message);
        hideLoadingViews();
    }

    protected void log(final Level level,
                       final String message) {
        if (LogConfiguration.loggingIsEnabled()) {
            LOGGER.log(level,
                       message);
        }
    }

    protected boolean hasUnsavedChanges() {
        return getCurrentDiagramHash() != originalHash;
    }

    protected ProjectEditorMenuSessionItems getMenuSessionItems() {
        return menuSessionItems;
    }

    private boolean verifyEventIdentifier(AbstractPlaceEvent event) {
        return (Objects.equals(getEditorIdentifier(),
                               event.getPlace().getIdentifier()) &&
                Objects.equals(place,
                               event.getPlace()));
    }

    protected ClientTranslationService getTranslationService() {
        return translationService;
    }

    protected void makeAdditionalStunnerMenus(final FileMenuBuilder fileMenuBuilder) {
    }

    public SessionPresenter<EditorSession, ?, Diagram> getSessionPresenter() {
        return presenter;
    }

    void setSessionPresenter(final SessionEditorPresenter<EditorSession> presenter) {
        this.presenter = presenter;
    }
}
