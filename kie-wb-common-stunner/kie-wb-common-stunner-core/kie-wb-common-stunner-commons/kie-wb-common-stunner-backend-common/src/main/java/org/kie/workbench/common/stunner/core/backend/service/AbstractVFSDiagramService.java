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

package org.kie.workbench.common.stunner.core.backend.service;

import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.core.api.FactoryManager;
import org.kie.workbench.common.stunner.core.definition.adapter.binding.BindableAdapterUtils;
import org.kie.workbench.common.stunner.core.definition.service.DefinitionSetServices;
import org.kie.workbench.common.stunner.core.definition.service.DiagramMarshaller;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.diagram.MetadataImpl;
import org.kie.workbench.common.stunner.core.factory.diagram.DiagramFactory;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.registry.BackendRegistryFactory;
import org.kie.workbench.common.stunner.core.registry.diagram.DiagramRegistry;
import org.kie.workbench.common.stunner.core.service.BaseDiagramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.IOException;
import org.uberfire.java.nio.file.FileAlreadyExistsException;
import org.uberfire.java.nio.file.FileVisitResult;
import org.uberfire.java.nio.file.SimpleFileVisitor;
import org.uberfire.java.nio.file.attribute.BasicFileAttributes;

import javax.enterprise.inject.Instance;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;
import static org.uberfire.java.nio.file.Files.walkFileTree;

// TODO: Use the diagram registry cache.
public abstract class AbstractVFSDiagramService<D extends Diagram> implements BaseDiagramService<D> {

    private static final Logger LOG =
            LoggerFactory.getLogger( AbstractVFSDiagramService.class.getName() );

    private final DefinitionManager definitionManager;
    private final FactoryManager factoryManager;
    private final IOService ioService;
    private final Instance<DefinitionSetServices> definitionSetServiceInstances;
    private final BackendRegistryFactory registryFactory;
    private final DiagramFactory diagramFactory;
    private Collection<DefinitionSetServices> definitionSetServices = new LinkedList<>();
    private DiagramRegistry<D> registry;

    public AbstractVFSDiagramService( final DefinitionManager definitionManager,
                                      final FactoryManager factoryManager,
                                      final Instance<DefinitionSetServices> definitionSetServiceInstances,
                                      final IOService ioService,
                                      final BackendRegistryFactory registryFactory,
                                      final DiagramFactory diagramFactory ) {
        this.definitionManager = definitionManager;
        this.factoryManager = factoryManager;
        this.ioService = ioService;
        this.definitionSetServiceInstances = definitionSetServiceInstances;
        this.registryFactory = registryFactory;
        this.diagramFactory = diagramFactory;
    }

    protected void initialize() {
        for ( DefinitionSetServices definitionSetService : definitionSetServiceInstances ) {
            definitionSetServices.add( definitionSetService );
        }
        this.registry = registryFactory.newDiagramSynchronizedRegistry();
    }

    public Path create( Path path, String name, String defSetId ) {
        final org.uberfire.java.nio.file.Path kiePath = Paths.convert( path ).resolve( name );
        try {
            if ( ioService.exists( kiePath ) ) {
                throw new FileAlreadyExistsException( kiePath.toString() );
            }
            final D diagram = factoryManager.newDiagram( name, defSetId );
            final String[] raw = serizalize( diagram );
            ioService.write( kiePath, raw[ 0 ] );
            // TODO: Handle metadata.
            return Paths.convert( kiePath );
        } catch ( final Exception e ) {
            LOG.error( "Cannot create diagram in path [" + kiePath + "]", e );
        }
        return null;
    }

    @SuppressWarnings( "unchecked" )
    public D getDiagramByPath( final org.uberfire.backend.vfs.Path file ) {
        if ( accepts( file ) ) {
            DefinitionSetServices services = getServicesByPath( file );
            if ( null != services ) {
                final String defSetId = getDefinitionSetId( services );
                // Check if any metadata definition exist.
                Metadata metadata = null;
                InputStream metaDataStream = loadMetadataForPath( file );
                if ( null != metaDataStream ) {
                    try {
                        metadata = services.getDiagramMarshaller().getMetadataMarshaller().unmarshall( metaDataStream );
                    } catch ( java.io.IOException e ) {
                        LOG.error( "Cannot unmarshall metadata for diagram's path [" + file + "]", e );
                    }
                }
                if ( null == metadata ) {
                    metadata = new MetadataImpl.MetadataImplBuilder( defSetId, definitionManager ).build();
                }
                metadata.setPath( file );
                // Parse and load the diagram raw data.
                final InputStream is = loadPath( file );
                try {
                    Graph graph = services.getDiagramMarshaller().unmarshall( metadata, is );
                    String name = file.getFileName();
                    return ( D ) diagramFactory.build( name, metadata, graph );
                } catch ( java.io.IOException e ) {
                    LOG.error( "Cannot unmarshall diagram for diagram's path [" + file + "]", e );
                    return null;
                }
            }

        }
        throw new UnsupportedOperationException( "Diagram format not supported [" + file + "]" );

    }

    public void saveOrUpdate( D diagram ) {
        register( diagram );
    }

    public boolean delete( D diagram ) {
        Path path = diagram.getMetadata().getPath();
        return doDelete( path );
    }

    protected abstract boolean doDelete( Path path );

    @SuppressWarnings( "unchecked" )
    private void register( D diagram ) {
        try {
            String[] raw = serizalize( diagram );
            doSave( diagram, raw[0], raw[1] );
        } catch ( Exception e ) {
            LOG.error( "Error while saving diagram with UUID [" + diagram.getName() + "].", e );
            throw new RuntimeException( e );
        }
    }

    protected String[] serizalize( final D diagram ) throws java.io.IOException {
        final String uuid = diagram.getName();
        final String defSetId = diagram.getMetadata().getDefinitionSetId();
        final DefinitionSetServices services = getServicesById( defSetId );
        // Serialize using the concrete marshalling service.
        DiagramMarshaller<Graph, Metadata, Diagram<Graph, Metadata>> marshaller = services.getDiagramMarshaller();
        final String rawData = marshaller.marshall( diagram );
        final Metadata metadata =  diagram.getMetadata();
        final String metadataRaw = marshaller.getMetadataMarshaller().marshall( metadata );
        return new String[] { rawData, metadataRaw };
    }

    public boolean contains( D item ) {
        return null != getDiagramByPath( item.getMetadata().getPath() );
    }

    public Collection<D> getDiagramsByPath( final org.uberfire.java.nio.file.Path root ) {
        try {
            final Collection<D> result = new ArrayList<D>();
            if ( ioService.exists( root ) ) {
                walkFileTree( checkNotNull( "root", root ),
                        new SimpleFileVisitor<org.uberfire.java.nio.file.Path>() {
                            @Override
                            public FileVisitResult visitFile( final org.uberfire.java.nio.file.Path _file, final BasicFileAttributes attrs ) throws IOException {
                                checkNotNull( "file", _file );
                                checkNotNull( "attrs", attrs );
                                org.uberfire.backend.vfs.Path file = org.uberfire.backend.server.util.Paths.convert( _file );;
                                if ( accepts( file ) ) {
                                    // portable diagram representation.
                                    D diagram = getDiagramByPath( file );
                                    if ( null != diagram ) {
                                        result.add( diagram );
                                    }

                                }
                                return FileVisitResult.CONTINUE;
                            }
                        } );
            }
            return result;

        } catch ( Exception e ) {
            LOG.error( "Error while obtaining diagrams.", e );
            throw e;
        }
    }

    protected abstract InputStream loadMetadataForPath( org.uberfire.backend.vfs.Path path );

    protected InputStream loadPath( org.uberfire.backend.vfs.Path _path ) {
        org.uberfire.java.nio.file.Path path = Paths.convert( _path );
        final byte[] bytes = ioService.readAllBytes( path );
        return new ByteArrayInputStream( bytes );
    }

    protected InputStream loadPath( org.uberfire.java.nio.file.Path _path ) {
        final byte[] bytes = ioService.readAllBytes( _path );
        return new ByteArrayInputStream( bytes );
    }

    protected abstract void doSave( D diagram, String raw, String metadata );

    public boolean accepts( final org.uberfire.backend.vfs.Path path ) {
        if ( path != null ) {

            // Look for the specific services definition.
            for ( DefinitionSetServices definitionSetService : definitionSetServices ) {
                if ( definitionSetService.getResourceType().accept( path ) ) {
                    return true;
                }
            }

        }

        return false;
    }

    protected Set<String> getExtensionsAccepted() {
        Set<String> result = new LinkedHashSet<>();
        // Look for the specific services definition.
        for ( DefinitionSetServices definitionSetService : definitionSetServices ) {
            result.add( definitionSetService.getResourceType().getSuffix() );
        }
        return result;
    }

    protected DefinitionSetServices getServicesByPath( final org.uberfire.backend.vfs.Path path ) {
        // Look for the specific services definition.
        for ( DefinitionSetServices definitionSetService : definitionSetServices ) {
            if ( definitionSetService.getResourceType().accept( path ) ) {
                return definitionSetService;
            }
        }

        return null;
    }

    protected String getDefinitionSetId( final DefinitionSetServices services ) {
        Class<?> type = services.getResourceType().getDefinitionSetType();
        return BindableAdapterUtils.getDefinitionSetId( type );
    }

    protected DefinitionSetServices getServicesById( final String defSetId ) {

        // Look for the specific services definition.
        for ( DefinitionSetServices definitionSetService : definitionSetServices ) {
            if ( definitionSetService.accepts( defSetId ) ) {
                return definitionSetService;
            }
        }

        return null;
    }

    protected IOService getIoService() {
        return ioService;
    }

    protected DiagramRegistry<D> getRegistry() {
        return registry;
    }
}
