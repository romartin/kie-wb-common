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

package org.kie.workbench.common.stunner.client.widgets.canvas;

// TODO: lienzo-to-native  @RunWith(LienzoMockitoTestRunner.class)
public class ScalableLienzoPanelTest {

    /*@Mock
    private StunnerLienzoBoundsPanel panel;

    private ScalableLienzoPanel tested;

    @Before
    public void init() {
        tested = new ScalableLienzoPanel(panel);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInit() {
        tested.init();
        ArgumentCaptor<BiFunction> builderCaptor = ArgumentCaptor.forClass(BiFunction.class);
        verify(panel, times(1)).setPanelBuilder(builderCaptor.capture());
        BiFunction<OptionalInt, OptionalInt, LienzoBoundsPanel> builder = builderCaptor.getValue();
        LienzoBoundsPanel result = builder.apply(OptionalInt.of(300), OptionalInt.of(450));
        assertTrue(result instanceof ScalablePanel);
        assertEquals(300, result.getWidthPx());
        assertEquals(450, result.getHeightPx());
    }*/
}
