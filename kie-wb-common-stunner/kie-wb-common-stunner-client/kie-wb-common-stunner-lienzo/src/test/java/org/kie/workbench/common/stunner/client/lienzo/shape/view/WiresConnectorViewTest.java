/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.client.lienzo.shape.view;

import com.ait.lienzo.client.core.shape.MultiPath;
import com.ait.lienzo.client.core.shape.MultiPathDecorator;
import com.ait.lienzo.client.core.shape.OrthogonalPolyLine;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.test.LienzoMockitoTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.client.lienzo.canvas.wires.WiresUtils;

import static org.junit.Assert.*;

@RunWith(LienzoMockitoTestRunner.class)
public class WiresConnectorViewTest {

    @Test
    @SuppressWarnings("unchecked")
    public void setUUID() {
        final String uuid = "uuid";
        final WiresConnectorView view = new WiresConnectorView(new OrthogonalPolyLine(new Point2D(0,
                                                                                                  0)),
                                                               new MultiPathDecorator(new MultiPath()),
                                                               new MultiPathDecorator(new MultiPath()));

        view.setUUID(uuid);

        assertTrue(view.getGroup().getUserData() instanceof WiresUtils.UserData);

        assertEquals(uuid,
                     ((WiresUtils.UserData) view.getGroup().getUserData()).getUuid());
    }
}