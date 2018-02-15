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

package org.kie.workbench.common.stunner.forms.client.resources.i18n;

import org.jboss.errai.ui.shared.api.annotations.TranslationKey;

public interface BPMNFormsClientConstants {

    @TranslationKey(defaultValue = "Forms generation completed successfully for [{0}]")
    String BPMNFormsGenerationSuccess = "bpmn.forms.generationSuccess";

    @TranslationKey(defaultValue = "Forms generation failed for [{0}]")
    String BPMNFormsGenerationFailure = "bpmn.forms.generationFailure";

    @TranslationKey(defaultValue = "Generate forms")
    String BPMNFormsGenerateTaskForm = "bpmn.forms.generateTaskForm";
}
