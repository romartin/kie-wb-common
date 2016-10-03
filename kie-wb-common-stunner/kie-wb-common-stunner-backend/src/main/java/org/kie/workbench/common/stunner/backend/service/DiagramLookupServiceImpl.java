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

package org.kie.workbench.common.stunner.backend.service;

import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.lookup.criteria.AbstractCriteriaLookupManager;
import org.kie.workbench.common.stunner.core.lookup.diagram.DiagramLookupRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.io.IOService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Map;

@ApplicationScoped
@Service
public class DiagramLookupServiceImpl
        extends AbstractDiagramLookupService<Diagram> {

    private static final Logger LOG =
            LoggerFactory.getLogger( DiagramLookupServiceImpl.class.getName() );

    protected DiagramLookupServiceImpl() {
        this( null, null );
    }

    @Inject
    public DiagramLookupServiceImpl( @Named( "ioStrategy" ) IOService ioService,
                                     DiagramServiceImpl diagramService ) {
        super( ioService, diagramService );
    }

    protected org.uberfire.java.nio.file.Path parseCriteriaPath( DiagramLookupRequest request ) {
        String criteria = request.getCriteria();
        if ( isEmpty( criteria ) ) {
            return getServiceImpl().getDiagramsPath();
        } else {
            Map<String, String> criteriaMap = AbstractCriteriaLookupManager.parseCriteria( criteria );
            String name = criteriaMap.get( "name" );
            if ( !isEmpty( name) ) {
                Collection<Diagram> diagrams = getItemsByPath( getServiceImpl().getDiagramsPath() );
                if ( null != diagrams && !diagrams.isEmpty() ) {
                    for ( final Diagram diagram : diagrams ) {
                        if ( diagram.getName().equals( name ) ) {
                            return Paths.convert( diagram.getMetadata().getPath() );
                        }
                    }

                }
                LOG.error( "Diagram with name [" + name + "] not found." );
                return null;
            }
        }
        String m = "Criteria [" + criteria + "] not supported.";
        LOG.error( m );
        throw new UnsupportedOperationException( m );
    }

    private DiagramServiceImpl getServiceImpl() {
        return ( DiagramServiceImpl ) getDiagramService();
    }

}
