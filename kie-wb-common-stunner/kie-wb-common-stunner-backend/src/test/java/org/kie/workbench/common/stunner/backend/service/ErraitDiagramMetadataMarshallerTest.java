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

package org.kie.workbench.common.stunner.backend.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.diagram.MetadataImpl;

import java.io.InputStream;

// TODO: @RunWith( MockitoJUnitRunner.class )
@Ignore
public class ErraitDiagramMetadataMarshallerTest {

    private static final String TEST1 = "org/kie/workbench/common/stunner/backend/service/test1.meta";

    private ErraitDiagramMetadataMarshaller tested;

    @Before
    public void setup() throws Exception {
        tested = new ErraitDiagramMetadataMarshaller();
    }

    @Test
    public void testMarhsall1() throws Exception {
        Metadata metadata = new MetadataImpl.MetadataImplBuilder( "org.kie.stunner.DefSet1" ).build();
        metadata.setShapeSetId( "org.kie.stunner.client.ShapeSet1" );
        metadata.setTitle( "Title1" );
        metadata.setThumbData( "thumbData1" );
        metadata.setCanvasRootUUID( "root1" );

        String result = tested.marshall( metadata );

        System.out.println( result );
    }

    private InputStream loadStream( String path ) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream( path );
    }

}
