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

package org.kie.workbench.common.stunner.core.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.DataResource;

public interface StunnerCommonImageResources extends ClientBundleWithLookup {

    public static final StunnerCommonImageResources INSTANCE = GWT.create(StunnerCommonImageResources.class);

    @ClientBundle.Source("images/check.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource check();

    @ClientBundle.Source("images/delete.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource delete();

    @ClientBundle.Source("images/edit.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource edit();

    @ClientBundle.Source("images/gears.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource gears();

    @ClientBundle.Source("images/expand.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource expand();
}
