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

package org.kie.workbench.common.stunner.bpmn.backend;

import bpsim.impl.BpsimPackageImpl;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.jboss.drools.DroolsPackage;
import org.jboss.drools.impl.DroolsPackageImpl;
import org.kie.workbench.common.stunner.backend.service.XMLEncoderDiagramMetadataMarshaller;
import org.kie.workbench.common.stunner.bpmn.backend.legacy.profile.impl.DefaultProfileImpl;
import org.kie.workbench.common.stunner.bpmn.backend.legacy.resource.JBPMBpmn2ResourceFactoryImpl;
import org.kie.workbench.common.stunner.bpmn.backend.legacy.resource.JBPMBpmn2ResourceImpl;
import org.kie.workbench.common.stunner.bpmn.backend.marshall.json.Bpmn2Marshaller;
import org.kie.workbench.common.stunner.bpmn.backend.marshall.json.Bpmn2UnMarshaller;
import org.kie.workbench.common.stunner.bpmn.backend.marshall.json.builder.BPMNGraphObjectBuilderFactory;
import org.kie.workbench.common.stunner.bpmn.backend.marshall.json.oryx.Bpmn2OryxManager;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagram;
import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.core.api.FactoryManager;
import org.kie.workbench.common.stunner.core.definition.service.DiagramMarshaller;
import org.kie.workbench.common.stunner.core.definition.service.DiagramMetadataMarshaller;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandManager;
import org.kie.workbench.common.stunner.core.graph.command.factory.GraphCommandFactory;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;
import org.kie.workbench.common.stunner.core.graph.util.GraphUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Dependent
public class BPMNDiagramMarshaller implements DiagramMarshaller<Graph, Metadata, Diagram<Graph, Metadata>> {

    private static final Logger LOG = LoggerFactory.getLogger( BPMNDiagramMarshaller.class );

    private XMLEncoderDiagramMetadataMarshaller diagramMetadataMarshaller;
    private BPMNGraphObjectBuilderFactory bpmnGraphBuilderFactory;
    private DefinitionManager definitionManager;
    private GraphUtils graphUtils;
    private Bpmn2OryxManager oryxManager;
    private FactoryManager factoryManager;
    private GraphCommandManager graphCommandManager;
    private GraphCommandFactory commandFactory;

    protected BPMNDiagramMarshaller() {
        this( null, null, null, null, null, null, null, null );
    }

    @Inject
    public BPMNDiagramMarshaller( XMLEncoderDiagramMetadataMarshaller diagramMetadataMarshaller,
                                  BPMNGraphObjectBuilderFactory bpmnGraphBuilderFactory,
                                  DefinitionManager definitionManager,
                                  GraphUtils graphUtils,
                                  Bpmn2OryxManager oryxManager,
                                  FactoryManager factoryManager,
                                  GraphCommandManager graphCommandManager,
                                  GraphCommandFactory commandFactory ) {
        this.diagramMetadataMarshaller = diagramMetadataMarshaller;
        this.bpmnGraphBuilderFactory = bpmnGraphBuilderFactory;
        this.definitionManager = definitionManager;
        this.graphUtils = graphUtils;
        this.oryxManager = oryxManager;
        this.factoryManager = factoryManager;
        this.graphCommandManager = graphCommandManager;
        this.commandFactory = commandFactory;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public String marshall( Diagram diagram ) throws IOException {
        LOG.debug( "Starting BPMN diagram marshalling..." );
        Bpmn2Marshaller marshaller = new Bpmn2Marshaller( definitionManager, graphUtils, oryxManager );
        String result = null;
        try {
            // Marhsall the bpmn definitions.
            result = marshaller.marshall( diagram );
            // Update diagram's settings.
            updateSettings( diagram.getMetadata(), diagram.getGraph() );
            LOG.debug( "Marhall result=" + result );
        } catch ( IOException e ) {
            LOG.error( "Error marshalling bpmn file.", e );
        }
        LOG.debug( "BPMN diagram marshalling finished successfully." );
        return result;
    }

    @Override
    public Graph unmarshall( Metadata metadata, InputStream inputStream ) throws IOException {
        LOG.debug( "Starting BPMN diagram loading..." );
        try {
            Definitions definitions = parseDefinitions( inputStream );
            // No rule checking for marshalling/unmarshalling, current jbpm designer marshallers should do it for us.
            Bpmn2UnMarshaller parser = new Bpmn2UnMarshaller( bpmnGraphBuilderFactory,
                    definitionManager,
                    factoryManager,
                    graphUtils,
                    oryxManager,
                    graphCommandManager,
                    commandFactory );
            parser.setProfile( new DefaultProfileImpl() );
            Graph result = parser.unmarshall( definitions, null );
            updateSettings( metadata, result );
            LOG.debug( "BPMN diagram loading finished successfully." );
            return result;
        } catch ( IOException e ) {
            LOG.error( "Error parsing bpmn file.", e );
        }
        return null;
    }

    public static void updateSettings( final Metadata settings,
                                       final Graph graph ) {
        // Update settings's root UUID.
        final String rootUUID = BPMNDiagramMarshaller.getRootUUID( graph );
        settings.setCanvasRootUUID( rootUUID );
        // Update settings's title.
        final String title = BPMNDiagramMarshaller.getTitle( graph );
        settings.setTitle( title );
    }

    private static String getRootUUID( final Graph graph ) {
        final Node diagramNode = getFirstDiagramNode( graph );
        return null != diagramNode ? diagramNode.getUUID() : null;
    }

    private static String getTitle( final Graph graph ) {
        final Node diagramNode = getFirstDiagramNode( graph );
        final BPMNDiagram diagramBean = null != diagramNode ?
                ( BPMNDiagram ) ( ( Definition ) diagramNode.getContent() ).getDefinition() : null;
        return getTitle( diagramBean );
    }

    private Definitions parseDefinitions( final InputStream inputStream ) throws IOException {
        try {
            DroolsPackageImpl.init();
            BpsimPackageImpl.init();

            ResourceSet resourceSet = new ResourceSetImpl();
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                    .put(Resource.Factory.Registry.DEFAULT_EXTENSION, new JBPMBpmn2ResourceFactoryImpl());
            resourceSet.getPackageRegistry().put("http://www.omg.org/spec/BPMN/20100524/MODEL", Bpmn2Package.eINSTANCE);
            resourceSet.getPackageRegistry().put("http://www.jboss.org/drools", DroolsPackage.eINSTANCE);

            JBPMBpmn2ResourceImpl resource = (JBPMBpmn2ResourceImpl) resourceSet.createResource( URI.createURI("inputStream://dummyUriWithValidSuffix.xml" ) );
            resource.getDefaultLoadOptions().put( JBPMBpmn2ResourceImpl.OPTION_ENCODING, "UTF-8" );
            resource.setEncoding( "UTF-8" );
            Map<String, Object> options = new HashMap<String, Object>();
            options.put( JBPMBpmn2ResourceImpl.OPTION_ENCODING, "UTF-8" );
            options.put( JBPMBpmn2ResourceImpl.OPTION_DEFER_IDREF_RESOLUTION, true );
            options.put( JBPMBpmn2ResourceImpl.OPTION_DISABLE_NOTIFY, true );
            options.put( JBPMBpmn2ResourceImpl.OPTION_PROCESS_DANGLING_HREF, JBPMBpmn2ResourceImpl.OPTION_PROCESS_DANGLING_HREF_RECORD );
            resource.load( inputStream, options );

            DocumentRoot root = (DocumentRoot) resource.getContents().get( 0 );
            return root.getDefinitions();

        } catch ( Exception e ) {
            e.printStackTrace();
        } finally {
            if ( inputStream != null ) {
                inputStream.close();
            }
        }
        return null;
    }
    @SuppressWarnings( "unchecked" )
    private static  Node getFirstDiagramNode( final Graph graph ) {
        if ( null != graph ) {
            Iterable<Node> nodesIterable = graph.nodes();
            if ( null != nodesIterable ) {
                Iterator<Node> nodesIt = nodesIterable.iterator();
                if ( null != nodesIt ) {
                    while ( nodesIt.hasNext() ) {
                        Node node = nodesIt.next();
                        Object content = node.getContent();
                        if ( content instanceof Definition ) {
                            Definition definitionContent = ( Definition ) content;
                            if ( definitionContent.getDefinition() instanceof BPMNDiagram ) {
                                return node;
                            }
                        }

                    }
                }
            }

        }
        return null;
    }

    private static String getTitle( BPMNDiagram diagram ) {
        final String title = diagram.getGeneral().getName().getValue();
        return title != null && title.trim().length() > 0 ? title : "-- Untitled BPMN2 diagram --";
    }

    @Override
    public DiagramMetadataMarshaller<Metadata> getMetadataMarshaller() {
        return diagramMetadataMarshaller;
    }
}
