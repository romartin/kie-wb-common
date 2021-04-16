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

package org.kie.workbench.common.stunner.project.backend.service;

import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.project.diagram.ProjectDiagram;
import org.kie.workbench.common.stunner.project.diagram.impl.ProjectDiagramImpl;
import org.kie.workbench.common.stunner.project.service.ProjectDiagramService;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.uberfire.backend.vfs.Path;
import org.uberfire.ext.editor.commons.backend.service.SaveAndRenameServiceImpl;
import org.uberfire.ext.editor.commons.service.RenameService;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectDiagramResourceServiceImplTest {

    @Mock
    private ProjectDiagramService projectDiagramService;

    @Mock
    private RenameService renameService;

    @Mock
    private SaveAndRenameServiceImpl<ProjectDiagram, Metadata> saveAndRenameService;

    private ProjectDiagramResourceServiceImpl service;

    @Before
    public void setup() {
        service = new ProjectDiagramResourceServiceImpl(projectDiagramService, renameService, saveAndRenameService);
    }

    @Test
    public void testInit() {

        service.init();

        verify(saveAndRenameService).init(service);
    }

    @Test
    public void testSaveWhenResourceIsProjectDiagram() {

        final Path path = mock(Path.class);
        final Metadata metadata = mock(Metadata.class);
        final ProjectDiagramImpl diagram = mock(ProjectDiagramImpl.class);
        final Path expectedPath = mock(Path.class);
        final String comment = "comment";

        when(projectDiagramService.save(path, diagram, metadata, comment)).thenReturn(expectedPath);

        final Path actualPath = service.save(path, diagram, metadata, comment);

        assertSame(expectedPath, actualPath);
    }

    @Test
    public void testRename() {

        final Path path = mock(Path.class);
        final String newName = "newName";
        final String comment = "comment";

        service.rename(path, newName, comment);

        verify(renameService).rename(path, newName, comment);
    }

    @Test
    public void testSaveAndRename() {

        final Path path = mock(Path.class);
        final Metadata metadata = mock(Metadata.class);
        final ProjectDiagramImpl diagram = mock(ProjectDiagramImpl.class);
        final String newName = "newName";
        final String comment = "comment";

        service.saveAndRename(path, newName, metadata, diagram, comment);

        verify(saveAndRenameService).saveAndRename(path, newName, metadata, diagram, comment);
    }
}
