/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.client.widgets.toolbar.command;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.kie.workbench.common.stunner.core.client.canvas.CanvasGrid;
import org.kie.workbench.common.stunner.core.client.session.command.impl.SessionCommandFactory;
import org.kie.workbench.common.stunner.core.client.session.command.impl.SwitchGridSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.impl.AbstractClientReadOnlySession;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class SwitchGridToolbarCommand extends AbstractToolbarSessionCommand<AbstractClientReadOnlySession, SwitchGridSessionCommand> {

    private CanvasGrid grid;

    @Inject
    public SwitchGridToolbarCommand( final SessionCommandFactory sessionCommandFactory ) {
        super( sessionCommandFactory.newSwitchGridCommand() );
    }

    @Override
    protected boolean requiresConfirm() {
        return false;
    }

    @Override
    public IconType getIcon() {
        return IconType.TH;
    }

    @Override
    public String getCaption() {
        return null;
    }

    @Override
    public String getTooltip() {
        return "Switch grid";
    }

    @Override
    protected void doDestroy() {
        super.doDestroy();
        this.grid = null;

    }

}
