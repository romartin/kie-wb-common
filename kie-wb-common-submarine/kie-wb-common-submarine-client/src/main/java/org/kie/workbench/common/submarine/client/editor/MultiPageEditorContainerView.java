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
package org.kie.workbench.common.submarine.client.editor;

import com.google.gwt.user.client.ui.IsWidget;
import org.uberfire.client.mvp.UberView;
import org.uberfire.client.workbench.widgets.multipage.MultiPageEditor;
import org.uberfire.client.workbench.widgets.multipage.Page;

public interface MultiPageEditorContainerView
        extends UberView<MultiPageEditorContainerView.Presenter> {

    interface Presenter {

        void onEditTabSelected();

        void onEditTabUnselected();
    }

    void setEditorWidget(final IsWidget editorView);

    MultiPageEditor getMultiPage();

    void addPage(final Page page);

    void clear();

    void selectEditorTab();

    boolean isEditorTabSelected();

    void setSelectedTab(final int tabIndex);

    int getSelectedTabIndex();
}