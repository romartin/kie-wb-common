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

package org.kie.workbench.common.dmn.project.client.editor;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Ignore;
import org.junit.runner.RunWith;

// TODO:
@Ignore

@RunWith(GwtMockitoTestRunner.class)
public class DMNDiagramEditorTest {

    /*@Mock
    private DecisionNavigatorDock decisionNavigatorDock;

    @Mock
    private SessionManager sessionManager;

    private DMNDiagramEditor editor;

    @Before
    public void setup() {
        editor = spy(new DMNDiagramEditor(null, null, null, null, null, null, null, sessionManager, null, null, null, null, null, null, null, null, decisionNavigatorDock));
    }

    @Test
    public void testOnStartup() {

        final ObservablePath path = mock(ObservablePath.class);
        final PlaceRequest place = mock(PlaceRequest.class);

        doNothing().when(editor).superDoStartUp(path, place);

        editor.onStartup(path, place);

        verify(editor).superDoStartUp(path, place);
        verify(decisionNavigatorDock).init(PerspectiveIds.LIBRARY);
    }

    @Test
    public void testOnClose() {

        doNothing().when(editor).superOnClose();

        editor.onClose();

        verify(editor).superOnClose();
        verify(decisionNavigatorDock).close();
        verify(decisionNavigatorDock).resetContent();
    }

    @Test
    public void testOnDiagramLoadWhenCanvasHandlerIsNotNull() {

        final CanvasHandler canvasHandler = mock(CanvasHandler.class);
        final ClientSession clientSession = mock(ClientSession.class);

        when(sessionManager.getCurrentSession()).thenReturn(clientSession);
        when(clientSession.getCanvasHandler()).thenReturn(canvasHandler);

        editor.onDiagramLoad();

        verify(decisionNavigatorDock).setupContent(canvasHandler);
        verify(decisionNavigatorDock).open();
    }

    @Test
    public void testOnDiagramLoadWhenCanvasHandlerIsNull() {

        editor.onDiagramLoad();

        verify(decisionNavigatorDock, never()).setupContent(any());
        verify(decisionNavigatorDock, never()).open();
    }

    @Test
    public void testOnFocus() {

        doNothing().when(editor).superDoFocus();

        editor.onFocus();

        verify(editor).superDoFocus();
        verify(editor).onDiagramLoad();
    }*/
}
