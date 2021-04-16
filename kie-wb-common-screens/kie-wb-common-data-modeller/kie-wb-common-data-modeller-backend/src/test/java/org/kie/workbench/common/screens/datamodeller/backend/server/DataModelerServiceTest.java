/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.datamodeller.backend.server;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;

import org.guvnor.common.services.backend.metadata.MetadataServerSideService;
import org.guvnor.common.services.project.model.Module;
import org.guvnor.common.services.project.model.Package;
import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.screens.datamodeller.backend.server.file.DataModelerCopyHelper;
import org.kie.workbench.common.screens.datamodeller.backend.server.helper.DataModelerRenameWorkaroundHelper;
import org.kie.workbench.common.screens.datamodeller.backend.server.helper.DataModelerSaveHelper;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.datamodeller.core.impl.DataObjectImpl;
import org.kie.workbench.common.services.shared.project.KieModuleService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.ext.editor.commons.service.CopyService;
import org.uberfire.ext.editor.commons.service.RenameService;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.base.options.CommentedOption;
import org.uberfire.java.nio.file.FileAlreadyExistsException;
import org.uberfire.java.nio.file.FileSystem;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataModelerServiceTest {

    private static final Path PATH = PathFactory.newPath("Sample.java",
                                                         "default://module/src/main/java/old/package/Sample.java");
    private static final String NEW_NAME = "NewSample";
    private static final String NEW_PACKAGE_NAME = "new.package";
    private static final Path TARGET_DIRECTORY = PathFactory.newPath("/",
                                                                     "default://module/src/main/java/new/package");
    private static final String COMMENT = "comment";

    @Mock
    private DataModelerCopyHelper copyHelper;

    @Mock
    private CopyService copyService;

    @Mock
    private KieModuleService moduleService;

    @Mock
    private IOService ioService;

    @Mock
    private DataModelerServiceHelper serviceHelper;

    @Mock(name = "saveHelperInstance")
    private Instance<DataModelerSaveHelper> saveHelperInstance;

    @Mock(name = "renameHelperInstance")
    private Instance<DataModelerRenameWorkaroundHelper> renameHelperInstance;

    @Mock
    private MetadataServerSideService metadataService;

    @Mock
    private RenameService renameService;

    @Spy
    @InjectMocks
    private DataModelerServiceImpl dataModelerService;

    @Test
    public void copyToAnotherPackageTest() {
        makeCopy(true);

        verify(dataModelerService).refactorClass(eq(PATH),
                                                 eq(NEW_PACKAGE_NAME),
                                                 eq(NEW_NAME));
        verify(copyService).copy(eq(PATH),
                                 eq(NEW_NAME),
                                 eq(TARGET_DIRECTORY),
                                 eq(COMMENT));
    }

    @Test
    public void copyToAnotherPackageWithoutRefactorTest() {
        makeCopy(false);

        verify(dataModelerService,
               never()).refactorClass(eq(PATH),
                                      eq(NEW_PACKAGE_NAME),
                                      eq(NEW_NAME));
        verify(copyService).copy(eq(PATH),
                                 eq(NEW_NAME),
                                 eq(TARGET_DIRECTORY),
                                 eq(COMMENT));
    }

    private void makeCopy(boolean refactor) {
        dataModelerService.copy(PATH,
                                NEW_NAME,
                                NEW_PACKAGE_NAME,
                                TARGET_DIRECTORY,
                                COMMENT,
                                refactor);
    }

    @Test
    public void saveSourcePackageNameChanged() {
        testSaveSource("newPackageName",
                       null,
                       new DataObjectImpl("dataobjects",
                                          "TestDataObject"));
    }

    @Test
    public void saveSourceFileNameChanged() {
        testSaveSource(null,
                       "newFileName",
                       new DataObjectImpl("dataobjects",
                                          "TestDataObject"));
    }

    @Test
    public void saveSourceBothPackageAndFileNameChanged() {
        testSaveSource("newPackageName",
                       "newFileName",
                       new DataObjectImpl("dataobjects",
                                          "TestDataObject"));
    }
    
    @Test
    public void saveSourceBothPackageAndFileNameChangedNullDataObj() {
        testSaveSource("newPackageName",
                       "newFileName",
                       null);
    }

    @Test
    public void saveSourceNoPackageAndFileNameChange() {
        testSaveSource(null,
                       null,
                       new DataObjectImpl("dataobjects",
                                          "TestDataObject"));
    }

    private void testSaveSource(String newPackageName,
                                String newFileName,
                                DataObject dataObject) {
        Path dataObjectPath = PathFactory.newPath("TestDataObject",
                                                  "file:///dataobjects/TestDataObject.java");
        Path srcPath = PathFactory.newPath("src",
                                           "file:///src");
        Package packageMock = mock(Package.class);
        when(packageMock.getPackageMainSrcPath()).thenReturn(srcPath);
        when(moduleService.resolvePackage(dataObjectPath)).thenReturn(packageMock);
        when(packageMock.getPackageName()).thenReturn("dataobjects");
        when(serviceHelper.ensurePackageStructure(Mockito.<Module>any(),
                                                  Mockito.<String>any())).thenReturn(packageMock);

        DataModelerSaveHelper saveHelper = mock(DataModelerSaveHelper.class);
        List<DataModelerSaveHelper> saveHelpers = Arrays.asList(saveHelper);
        when(saveHelperInstance.iterator()).thenReturn(saveHelpers.iterator());

        dataModelerService.saveSource("Source",
                                      dataObjectPath,
                                      dataObject,
                                      mock(Metadata.class),
                                      "Commit message",
                                      newPackageName,
                                      newFileName);

        verify(ioService,
               times(1)).startBatch(Mockito.<FileSystem>any());

        if (newPackageName == null && newFileName == null) {
            verify(ioService,
                   times(1)).write(Mockito.<org.uberfire.java.nio.file.Path>any(),
                                   Mockito.<String>any(),
                                   Mockito.<Map>any(),
                                   Mockito.<CommentedOption>any());
        } else if (newPackageName != null && newFileName != null) {
            verify(ioService,
                   times(1)).write(Mockito.<org.uberfire.java.nio.file.Path>any(),
                                   Mockito.<String>any(),
                                   Mockito.<Map>any(),
                                   Mockito.<CommentedOption>any());
            verify(ioService,
                   times(1)).move(Mockito.<org.uberfire.java.nio.file.Path>any(),
                                  Mockito.<org.uberfire.java.nio.file.Path>any(),
                                  Mockito.<CommentedOption>any());
        } else if (newPackageName != null) {
            verify(ioService,
                   times(1)).write(Mockito.<org.uberfire.java.nio.file.Path>any(),
                                   Mockito.<String>any(),
                                   Mockito.<Map>any(),
                                   Mockito.<CommentedOption>any());
            verify(ioService,
                   times(1)).move(Mockito.<org.uberfire.java.nio.file.Path>any(),
                                  Mockito.<org.uberfire.java.nio.file.Path>any(),
                                  Mockito.<CommentedOption>any());
        } else {
            verify(renameService,
                   times(1)).rename(Mockito.<Path>any(),
                                    Mockito.<String>any(),
                                    Mockito.<String>any());
        }

        verify(saveHelper,
               times(1)).postProcess(Mockito.<Path>any(),
                                     Mockito.<Path>any());
        verify(ioService,
               times(1)).endBatch();
    }

    @Test
    public void testRenameWorkaround() {
        Path dataObjectPath = PathFactory.newPath("TestDataObject",
                                                  "file:///dataobjects/TestDataObject.java");
        Path targetPath = PathFactory.newPath("TestNewDataObject",
                                              "file:///dataobjects/TestNewDataObject.java");
        DataModelerRenameWorkaroundHelper renameHelper = mock(DataModelerRenameWorkaroundHelper.class);
        List<DataModelerRenameWorkaroundHelper> renameHelpers = Arrays.asList(renameHelper);
        when(renameHelperInstance.iterator()).thenReturn(renameHelpers.iterator());

        dataModelerService.renameWorkaround(dataObjectPath,
                                            targetPath,
                                            "New content",
                                            "Comment");

        verify(ioService,
               times(1)).startBatch(Mockito.<FileSystem>any());
        verify(ioService,
               times(1)).move(Mockito.<org.uberfire.java.nio.file.Path>any(),
                              Mockito.<org.uberfire.java.nio.file.Path>any(),
                              Mockito.<CommentedOption>any());
        verify(ioService,
               times(1)).write(Mockito.<org.uberfire.java.nio.file.Path>any(),
                               eq("New content"),
                               Mockito.<CommentedOption>any());
        verify(renameHelper,
               times(1)).postProcess(Mockito.<Path>any(),
                                     Mockito.<Path>any());
        verify(ioService,
               times(1)).endBatch();
    }

    @Test
    public void testRenameWorkaroundWithoutNewContent() {
        Path dataObjectPath = PathFactory.newPath("TestDataObject",
                                                  "file:///dataobjects/TestDataObject.java");
        Path targetPath = PathFactory.newPath("TestNewDataObject",
                                              "file:///newdataobjects/TestNewDataObject.java");
        DataModelerRenameWorkaroundHelper renameHelper = mock(DataModelerRenameWorkaroundHelper.class);
        List<DataModelerRenameWorkaroundHelper> renameHelpers = Arrays.asList(renameHelper);
        when(renameHelperInstance.iterator()).thenReturn(renameHelpers.iterator());

        dataModelerService.renameWorkaround(dataObjectPath,
                                            targetPath,
                                            null,
                                            "Comment");

        verify(ioService,
               times(1)).startBatch(Mockito.<FileSystem>any());
        verify(ioService,
               times(1)).move(Mockito.<org.uberfire.java.nio.file.Path>any(),
                              Mockito.<org.uberfire.java.nio.file.Path>any(),
                              Mockito.<CommentedOption>any());
        verify(ioService,
               times(0)).write(Mockito.<org.uberfire.java.nio.file.Path>any(),
                               any(String.class),
                               Mockito.<CommentedOption>any());
        verify(renameHelper,
               times(1)).postProcess(Mockito.<Path>any(),
                                     Mockito.<Path>any());
        verify(ioService,
               times(1)).endBatch();
    }

    @Test
    public void testCreateDataObjectAlreadyExists() {
        final Path path = PathFactory.newPath("DataObject.java", "file:///DataObject.java");

        when(ioService.exists(Mockito.<org.uberfire.java.nio.file.Path>any())).thenReturn(true);
        assertThatThrownBy(() -> dataModelerService.createJavaFile(path, "", ""))
                .isInstanceOf(FileAlreadyExistsException.class);
    }
}
