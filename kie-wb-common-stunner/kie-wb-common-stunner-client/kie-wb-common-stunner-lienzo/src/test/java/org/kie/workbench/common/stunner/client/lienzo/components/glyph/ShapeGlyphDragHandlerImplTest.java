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

package org.kie.workbench.common.stunner.client.lienzo.components.glyph;

// TODO: lienzo-to-native  @RunWith(LienzoMockitoTestRunner.class)
public class ShapeGlyphDragHandlerImplTest {

    /*private static final int DRAG_PROXY_WIDTH = 150;
    private static final int DRAG_PROXY_HEIGHT = 300;

    @Mock
    private LienzoGlyphRenderers glyphLienzoGlyphRenderer;

    @Mock
    private AbstractCanvas canvas;

    @Mock
    private Glyph glyph;

    @Mock
    private ShapeGlyphDragHandler.Item glyphDragItem;

    @Mock
    private AbsolutePanel rootPanel;

    @Mock
    private com.google.gwt.user.client.Element proxyElement;

    @Mock
    private Style proxyStyle;

    @Mock
    private HandlerRegistration moveHandlerReg;

    @Mock
    private HandlerRegistration upHandlerReg;

    @Mock
    private HandlerRegistration keyHandlerReg;

    private ShapeGlyphDragHandlerImpl tested;
    private List<HandlerRegistration> handlerRegistrations;
    private LienzoPanelImpl proxyPanel;
    private Group glyphGroup;

    @Before
    public void setUp() throws Exception {
        proxyPanel = spy(new LienzoPanelImpl(DRAG_PROXY_WIDTH, DRAG_PROXY_HEIGHT));
        when(proxyPanel.getElement()).thenReturn(proxyElement);
        when(proxyElement.getStyle()).thenReturn(proxyStyle);
        handlerRegistrations = new ArrayList<>();
        glyphGroup = new Group();
        when(glyphLienzoGlyphRenderer.render(eq(glyph), anyDouble(), anyDouble())).thenReturn(glyphGroup);
        when(glyphDragItem.getHeight()).thenReturn(DRAG_PROXY_WIDTH);
        when(glyphDragItem.getWidth()).thenReturn(DRAG_PROXY_HEIGHT);
        when(glyphDragItem.getShape()).thenReturn(glyph);
        when(rootPanel.addDomHandler(any(MouseMoveHandler.class), eq(MouseMoveEvent.getType())))
                .thenReturn(moveHandlerReg);
        when(rootPanel.addDomHandler(any(MouseUpHandler.class), eq(MouseUpEvent.getType())))
                .thenReturn(upHandlerReg);
        when(rootPanel.addDomHandler(any(KeyDownHandler.class), eq(KeyDownEvent.getType())))
                .thenReturn(keyHandlerReg);
        tested = new ShapeGlyphDragHandlerImpl(glyphLienzoGlyphRenderer,
                                               handlerRegistrations,
                                               () -> rootPanel,
                                               item -> proxyPanel,
                                               (task, timeout) -> task.execute());
    }

    @Test
    public void testShowProxy() throws Exception {
        tested.show(glyphDragItem, 11, 33, mock(DragProxyCallback.class));
        ArgumentCaptor<Layer> layerArgumentCaptor = ArgumentCaptor.forClass(Layer.class);
        verify(proxyPanel, times(1)).add(layerArgumentCaptor.capture());
        Layer layer = layerArgumentCaptor.getValue();
        assertEquals(glyphGroup, layer.getChildNodes().get(0));
        verify(proxyStyle, times(1)).setCursor(eq(Style.Cursor.AUTO));
        verify(proxyStyle, times(1)).setPosition(eq(Style.Position.ABSOLUTE));
        verify(proxyStyle, times(1)).setLeft(eq(11d), eq(Style.Unit.PX));
        verify(proxyStyle, times(1)).setTop(eq(33d), eq(Style.Unit.PX));
        verify(rootPanel, times(1)).add(eq(proxyPanel));
    }

    @Test
    public void testProxyhHandlers() throws Exception {
        DragProxyCallback callback = mock(DragProxyCallback.class);
        tested.show(glyphDragItem, 11, 33, callback);

        // Check keyboard event handling.
        assertEquals(keyHandlerReg, handlerRegistrations.get(2));

        // Check mouse move event handling.
        assertEquals(moveHandlerReg, handlerRegistrations.get(0));
        MouseMoveEvent moveEvent = mock(MouseMoveEvent.class);
        when(moveEvent.getX()).thenReturn(7);
        when(moveEvent.getY()).thenReturn(9);
        when(moveEvent.getClientX()).thenReturn(3);
        when(moveEvent.getClientY()).thenReturn(5);
        tested.onMouseMove(moveEvent,
                           callback);
        verify(proxyStyle, times(1)).setLeft(eq(7d), eq(Style.Unit.PX));
        verify(proxyStyle, times(1)).setTop(eq(9d), eq(Style.Unit.PX));
        verify(callback, times(1)).onMove(eq(3), eq(5));

        // Check mouse up event handling.
        assertEquals(upHandlerReg, handlerRegistrations.get(1));
        MouseUpEvent upEvent = mock(MouseUpEvent.class);
        when(upEvent.getX()).thenReturn(7);
        when(upEvent.getY()).thenReturn(9);
        when(upEvent.getClientX()).thenReturn(3);
        when(upEvent.getClientY()).thenReturn(5);
        tested.onMouseUp(upEvent,
                         callback);
        verify(moveHandlerReg, times(1)).removeHandler();
        verify(upHandlerReg, times(1)).removeHandler();
        verify(rootPanel, times(1)).remove(eq(proxyPanel));
        verify(callback, times(1)).onComplete(eq(3), eq(5));
        assertTrue(handlerRegistrations.isEmpty());
    }

    @Test
    public void testKeyboardHandling() throws Exception {
        DragProxyCallback callback = mock(DragProxyCallback.class);
        tested.show(glyphDragItem, 11, 33, callback);
        assertEquals(keyHandlerReg, handlerRegistrations.get(2));
        KeyDownEvent event = mock(KeyDownEvent.class);
        tested.onKeyDown(event);
        verify(moveHandlerReg, times(1)).removeHandler();
        verify(upHandlerReg, times(1)).removeHandler();
        verify(rootPanel, times(1)).remove(eq(proxyPanel));
        assertTrue(handlerRegistrations.isEmpty());
    }

    @Test
    public void testClear() throws Exception {
        DragProxyCallback callback = mock(DragProxyCallback.class);
        tested.show(glyphDragItem, 11, 33, callback);
        tested.clear();
        verify(proxyPanel, times(1)).clear();
        verify(proxyPanel, never()).destroy();
        verify(moveHandlerReg, times(1)).removeHandler();
        verify(upHandlerReg, times(1)).removeHandler();
        verify(rootPanel, times(1)).remove(eq(proxyPanel));
        assertTrue(handlerRegistrations.isEmpty());
    }

    @Test
    public void testDestroy() throws Exception {
        DragProxyCallback callback = mock(DragProxyCallback.class);
        tested.show(glyphDragItem, 11, 33, callback);
        tested.destroy();
        verify(proxyPanel, times(1)).destroy();
        verify(proxyPanel, never()).clear();
        verify(moveHandlerReg, times(1)).removeHandler();
        verify(upHandlerReg, times(1)).removeHandler();
        verify(rootPanel, times(1)).remove(eq(proxyPanel));
        assertTrue(handlerRegistrations.isEmpty());
    }*/
}