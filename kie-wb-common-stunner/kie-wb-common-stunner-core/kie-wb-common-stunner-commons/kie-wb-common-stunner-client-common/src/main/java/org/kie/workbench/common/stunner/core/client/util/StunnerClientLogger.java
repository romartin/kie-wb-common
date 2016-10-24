/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.core.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.logging.client.LogConfiguration;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.CanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.controls.builder.ElementBuilderControl;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommandManager;
import org.kie.workbench.common.stunner.core.client.command.CanvasViolation;
import org.kie.workbench.common.stunner.core.client.service.ClientRuntimeError;
import org.kie.workbench.common.stunner.core.client.session.ClientFullSession;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.client.session.impl.AbstractClientFullSession;
import org.kie.workbench.common.stunner.core.client.session.impl.AbstractClientSession;
import org.kie.workbench.common.stunner.core.command.Command;
import org.kie.workbench.common.stunner.core.command.stack.StackCommandManager;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StunnerClientLogger {

    private static Logger LOGGER = Logger.getLogger( StunnerClientLogger.class.getName() );

    
    public static String getErrorMessage( final ClientRuntimeError error ) {
        final String message = error.getMessage();
        final Throwable t1 = error.getThrowable();
        final Throwable t2 = t1 != null ? t1.getCause() : null;
        if ( null != t2 ) {
            return t2.getMessage();
        } else if ( null != t1 ) {
            return t1.getMessage();
        }
        return null != message ? message : " -- No message -- ";
    }

    public static void logSessionInfo( final AbstractClientSession session ) {
        log( "************ Session Info ****************");
        if ( null != session ) {
            log( "Session = " + session.toString() );
            log( "Canvas = " + session.getCanvas().toString() );
            if ( null != session.getCanvasHandler() ) {
                final CanvasHandler canvasHandler = session.getCanvasHandler();
                log( "CanvasHandler = " + canvasHandler.toString() );
                final Diagram diagram = canvasHandler.getDiagram();
                if ( null != diagram ) {
                    log( "Diagram name = "  + diagram.getName() );
                    log( "Graph uuid = " + ( null != diagram.getGraph() ? diagram.getGraph().getUUID() : "null") );
                    final Metadata metadata = diagram.getMetadata();
                    if ( null != metadata ) {
                        log( "Metadata defSetId = " + metadata.getDefinitionSetId() );
                        log( "Metadata shapeSetId = " + metadata.getShapeSetId() );
                        log( "Metadata canvas root = " + metadata.getCanvasRootUUID() );
                        log( "Metadata title = " + metadata.getTitle() );
                    } else {
                        log( "Metadata = null" );
                    }
                } else {
                    log( "Diagram = null" );
                }
            } else {
                log( "CanvasHandler = null" );
            }

            if ( session instanceof AbstractClientFullSession ) {
                logFullSessionInfo( ( AbstractClientFullSession ) session );
            }
        } else {
            log( "Session is null" );
        }
        log( "******************************************");
    }

    private static void logFullSessionInfo( final AbstractClientFullSession session ) {
        final ElementBuilderControl<AbstractCanvasHandler> builderControl = session.getBuilderControl();
        final CanvasCommandManager<AbstractCanvasHandler> canvasCommandManager =
                session.getCanvasCommandManager();
        log( "Builder control = "  + ( null != builderControl ? builderControl.toString() : "null" ) );
        log( "Canvas command mgr = "  + ( null != canvasCommandManager ? canvasCommandManager.toString() : "null" ) );
    }

        @SuppressWarnings( "unchecked" )
    public static void logCommandHistory( final ClientFullSession session ) {
        if ( null != session ) {
            final Iterable<Iterable<Command<AbstractCanvasHandler, CanvasViolation>>> history =
                    ( ( StackCommandManager<AbstractCanvasHandler, CanvasViolation> ) session.getCanvasCommandManager()).getRegistry().getCommandHistory();
            logCommandHistory( history );
        }
    }

    private static void logCommandHistory( final Iterable<Iterable<Command<AbstractCanvasHandler, CanvasViolation>>> history ) {
        log( "**** COMMAND HISTORY START *********");

        if ( null == history ) {

            log( "History is null");

        } else {


            int x = 0;
            for ( final Iterable<Command<AbstractCanvasHandler, CanvasViolation>> entry : history ) {

                log( "--------------- History Entry Start ---------------");

                if ( entry.iterator().hasNext() ) {

                    log( "No commands");

                } else {

                    int c = 0;
                    for ( final Command<AbstractCanvasHandler, CanvasViolation> command : entry ) {

                        logCommand( c, command );

                        c++;
                    }

                }

                log( "--------------- History Entry End ---------------");

                x++;

            }

            log( " ( FOUND " + x + " ENTRIES )");


        }

        log( "**** COMMAND HISTORY END *********");

    }

    private static void logCommand( final int count,
                             final Command<AbstractCanvasHandler, CanvasViolation> command ) {

        if ( null == command ) {

            log( "Command is null");

        } else {

            log( "Command [" + count + "] => " + command.toString() );
        }

    }

    public static void switchLogLevel() {
        final Level level = Logger.getLogger("org.kie.workbench.common.stunner").getLevel();
        final int idx = getLevelIndex( level );
        final Level newLevel = ( idx > -1 && ( ( idx + 1 ) < LOG_LEVELS.length ) ) ? LOG_LEVELS[ idx + 1 ] : LOG_LEVELS[0];
        GWT.log( "*** Switching to log level: " + newLevel.toString() );
        Logger.getLogger("org.kie.workbench.common.stunner").setLevel(newLevel);
    }

    private static final Level[] LOG_LEVELS = new Level[] {
            Level.FINE, Level.INFO, Level.WARNING, Level.SEVERE
    };

    private static int getLevelIndex( final Level level ) {
        int idx = -1;
        if ( null != level ) {
            for ( final Level l : LOG_LEVELS ) {
                if ( level.equals( l ) ) {
                    return idx + 1;
                }
                idx++;
            }
        }
        return idx;
    }

    private static void log(final String message) {
        if (LogConfiguration.loggingIsEnabled()) {
            LOGGER.log(Level.INFO, message);
        }
    }

    private static void log( final Level level, final String message ) {
        if ( LogConfiguration.loggingIsEnabled() ) {
            LOGGER.log( level, message );
        }
    }
}
