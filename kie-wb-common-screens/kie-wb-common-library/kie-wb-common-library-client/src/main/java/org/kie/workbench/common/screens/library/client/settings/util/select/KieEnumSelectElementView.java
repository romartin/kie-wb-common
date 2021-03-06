/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
 *
 */

package org.kie.workbench.common.screens.library.client.settings.util.select;

import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.kie.workbench.common.widgets.client.widget.KieSelectElement;
import org.kie.workbench.common.widgets.client.widget.KieSelectOption;

@Templated
public class KieEnumSelectElementView implements KieEnumSelectElement.View, IsElement {

    @Inject
    @DataField
    private KieSelectElement kieSelect;

    private KieEnumSelectElement presenter;

    @Override
    public void init(KieEnumSelectElement presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setupKieSelectElement(List<KieSelectOption> options, String initialValue, Consumer<String> onChange) {
        kieSelect.setup(options, initialValue, onChange);
    }
}
