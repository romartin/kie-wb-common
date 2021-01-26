/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.client.lienzo.components.views;

import com.ait.lienzo.tools.client.event.EventType;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLDivElement;
import org.kie.workbench.common.stunner.client.lienzo.canvas.LienzoPanel;
import org.uberfire.mvp.Command;

public class LienzoPanelFocusHandler {

    EventListener mouseOverListener;
    EventListener mouseOutListener;

    HTMLDivElement panel;
    static final String ON_MOUSE_OVER = EventType.MOUSE_OVER.getType();
    static final String ON_MOUSE_OUT = EventType.MOUSE_OUT.getType();

    public LienzoPanelFocusHandler listen(final LienzoPanel panel,
                                          final Command onFocus,
                                          final Command onLostFocus) {
        clear();

        this.panel = panel.getView().getElement();
        this.mouseOverListener = mouseOverEvent -> onFocus.execute();
        this.mouseOutListener = mouseOutEvent -> onLostFocus.execute();
        this.panel.addEventListener(ON_MOUSE_OVER, mouseOverListener);
        this.panel.addEventListener(ON_MOUSE_OUT, mouseOutListener);

        return this;
    }

    public LienzoPanelFocusHandler clear() {
        if (null != panel) {
            if (null != mouseOverListener) {
                panel.removeEventListener(ON_MOUSE_OVER, mouseOverListener);
            }
            if (null != mouseOutListener) {
                panel.removeEventListener(ON_MOUSE_OUT, mouseOutListener);
            }
        }

        mouseOverListener = null;
        mouseOutListener = null;
        panel = null;

        return this;
    }
}
