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

package org.kie.workbench.common.stunner.forms.client.widgets;

import com.google.gwt.logging.client.LogConfiguration;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.kie.workbench.common.forms.dynamic.client.DynamicFormRenderer;
import org.kie.workbench.common.stunner.core.client.api.ClientDefinitionManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.controls.select.SelectionControl;
import org.kie.workbench.common.stunner.core.client.canvas.event.selection.CanvasClearSelectionEvent;
import org.kie.workbench.common.stunner.core.client.canvas.event.selection.CanvasElementSelectedEvent;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommandManager;
import org.kie.workbench.common.stunner.core.client.command.factory.CanvasCommandFactory;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.client.session.event.SessionDisposedEvent;
import org.kie.workbench.common.stunner.core.client.session.event.SessionOpenedEvent;
import org.kie.workbench.common.stunner.core.client.session.impl.AbstractClientFullSession;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.uberfire.mvp.Command;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

@Dependent
public class FormPropertiesWidget implements IsWidget {

    private static Logger LOGGER = Logger.getLogger( FormPropertiesWidget.class.getName() );

    private final ClientDefinitionManager clientDefinitionManager;
    private final CanvasCommandFactory commandFactory;
    private final DynamicFormRenderer formRenderer;

    private AbstractClientFullSession session;

    protected FormPropertiesWidget() {
        this( null, null, null );
    }

    @Inject
    public FormPropertiesWidget( final ClientDefinitionManager clientDefinitionManager,
                                 final CanvasCommandFactory commandFactory,
                                 final DynamicFormRenderer formRenderer ) {
        this.clientDefinitionManager = clientDefinitionManager;
        this.commandFactory = commandFactory;
        this.formRenderer = formRenderer;
    }

    @PostConstruct
    public void init() {
        log( Level.INFO, "FormPropertiesWidget instance build." );
    }

    /**
     * Binds a session.
     */
    public FormPropertiesWidget bind( final AbstractClientFullSession session ) {
        this.session = session;
        return this;
    }

    /**
     * Unbinds a session.
     */
    public FormPropertiesWidget unbind() {
        this.session = null;
        doClear();
        return this;
    }

    /**
     * Shows properties of elements in current session as:
     * 1.- If any element selected on session control, show properties for it.
     * 2.- If no element selected on session control:
     *  2.1- If no canvas root fot the diagram, show the diagram's graph properties.
     *  2.2- If diagram has a canvas root, show the properties for that element.
     */
    public void show() {
        this.show( null );
    }

    @SuppressWarnings( "unchecked" )
    public void show( final Command callback ) {
        boolean done = false;
        if ( null != session ) {
            // Obtain first element selected on session, if any.
            String selectedItemUUID = null;
            final SelectionControl selectionControl = session.getSelectionControl();
            if ( null != selectionControl ) {
                final Collection<String> selectedItems = selectionControl.getSelectedItems();
                if ( null != selectedItems && !selectedItems.isEmpty() ) {
                    selectedItemUUID = selectedItems.iterator().next();
                }
            }
            if ( null == selectedItemUUID ) {
                final  Diagram<?, ?> diagram = getDiagram();
                if ( null != diagram ) {
                    final String cRoot = diagram.getMetadata().getCanvasRootUUID();
                    // Check if there exist any canvas root element.
                    if ( !isEmpty( cRoot ) ) {
                        selectedItemUUID = cRoot;
                    }
                }
            }
            if ( null != selectedItemUUID ) {
                showByUUID( selectedItemUUID, callback );
                done = true;
            }
        }
        if ( !done ) {
            doClear();
        }
    }

    /**
     * Show properties for the element with the given identifier.
     */
    public void showByUUID( final String uuid ) {
        this.showByUUID( uuid, null );
    }

    public void showByUUID( final String uuid,
                            final Command callback ) {
        final Element<? extends Definition<?>> element = ( null != uuid && null != getCanvasHandler() ) ?
                getCanvasHandler().getGraphIndex().get( uuid ) : null;
        if ( null != element ) {
            final Object definition = element.getContent().getDefinition();
            BindableProxy proxy = ( BindableProxy ) BindableProxyFactory.getBindableProxy( definition );
            formRenderer.renderDefaultForm( proxy.deepUnwrap(), () -> {
                formRenderer.addFieldChangeHandler( ( fieldName, newValue ) -> {
                    try {
                        // TODO - Pere: We have to review this. Meanwhile, note that this is working only for properties
                        // that are direct members of the definitions ( ex: Task#width or StartEvent#radius ).
                        // But it's not working for the properties that are inside property sets, for example an error
                        // occurs when updating "documentation", as thisl callback "fieldName" = "documentation", but
                        // in order to obtain the property it should be "general.documentation".
                        final HasProperties hasProperties = ( HasProperties ) DataBinder.forModel( definition ).getModel();
                        String pId = getModifiedPropertyId( hasProperties, fieldName );
                        FormPropertiesWidget.this.executeUpdateProperty( element, pId, newValue );
                    } catch ( Exception ex ) {
                        log( Level.SEVERE, "Something wrong happened refreshing the canvas for field '" + fieldName + "': " + ex.getCause() );
                    } finally {
                        if ( null != callback ) {
                            callback.execute();
                        }
                    }
                } );
            } );
        } else {
            doClear();
            if ( null != callback ) {
                callback.execute();
            }
        }
    }

    @Override
    public Widget asWidget() {
        return formRenderer.asWidget();
    }

    private AbstractCanvasHandler getCanvasHandler() {
        return session != null ? session.getCanvasHandler() : null;
    }

    private Diagram<?, ?> getDiagram() {
        return null != getCanvasHandler() ? getCanvasHandler().getDiagram() : null;
    }

    @SuppressWarnings( "unchecked" )
    void onCanvasElementSelectedEvent( @Observes CanvasElementSelectedEvent event ) {
        checkNotNull( "event", event );
        if ( null != getCanvasHandler() ) {
            final String uuid = event.getElementUUID();
            showByUUID( uuid );
        }
    }

    void CanvasClearSelectionEvent( @Observes CanvasClearSelectionEvent clearSelectionEvent ) {
        checkNotNull( "clearSelectionEvent", clearSelectionEvent );
        doClear();
    }

    void onCanvasSessionOpened( @Observes SessionOpenedEvent sessionOpenedEvent ) {
        checkNotNull( "sessionOpenedEvent", sessionOpenedEvent );
        doOpenSession( sessionOpenedEvent.getSession() );
    }

    void onCanvasSessionDisposed( @Observes SessionDisposedEvent sessionDisposedEvent ) {
        checkNotNull( "sessionDisposedEvent", sessionDisposedEvent );
        unbind();
    }

    private void doOpenSession( final ClientSession session ) {
        try {
            bind( ( AbstractClientFullSession ) session ).show();
        } catch ( ClassCastException e ) {
            // No writteable session. Do not show properties until read mode available.
            log( Level.INFO, "Session discarded for opening as not instance of full session." );
        }
    }

    private void doClear() {
        // TODO: formRenderer.unBind(); -> NPE
        // TODO: changeTitleNotification.fire(new ChangeTitleWidgetEvent(placeRequest, "Properties"));
    }

    private void executeUpdateProperty( final Element<? extends Definition<?>> element,
                                        final String propertyId,
                                        final Object value ) {
        final CanvasCommandManager<AbstractCanvasHandler> commandManager = session.getCanvasCommandManager();
        commandManager.execute( getCanvasHandler(), commandFactory.UPDATE_PROPERTY( element, propertyId, value ) );

    }

    private void executeMove( final Element<? extends Definition<?>> element,
                              final double x,
                              final double y ) {
        final CanvasCommandManager<AbstractCanvasHandler> commandManager = session.getCanvasCommandManager();
        commandManager.execute( getCanvasHandler(), commandFactory.UPDATE_POSITION( element, x, y ) );

    }

    private String getModifiedPropertyId( HasProperties model, String fieldName ) {
        int separatorIndex = fieldName.indexOf( "." );
        // Check if it is a nested property, if it is we must obtain the nested property instead of the root one.
        if ( separatorIndex != -1 ) {
            String rootProperty = fieldName.substring( 0, separatorIndex );
            fieldName = fieldName.substring( separatorIndex + 1 );
            Object property = model.get( rootProperty );
            model = ( HasProperties ) DataBinder.forModel( property ).getModel();
            return getModifiedPropertyId( model, fieldName );
        }
        Object property = model.get( fieldName );
        return clientDefinitionManager.adapters().forProperty().getId( property );
    }

    private boolean isEmpty( final String s ) {
        return s == null || s.trim().length() == 0;
    }
    private void log( final Level level, final String message ) {
        if ( LogConfiguration.loggingIsEnabled() ) {
            LOGGER.log( level, message );
        }
    }

}
