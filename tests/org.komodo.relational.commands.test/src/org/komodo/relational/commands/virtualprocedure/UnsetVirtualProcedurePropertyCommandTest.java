/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.komodo.relational.commands.virtualprocedure;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.komodo.relational.commands.AbstractCommandTest;
import org.komodo.relational.model.Model;
import org.komodo.relational.model.Procedure;
import org.komodo.relational.model.VirtualProcedure;
import org.komodo.relational.vdb.Vdb;
import org.komodo.relational.workspace.WorkspaceManager;
import org.komodo.shell.api.CommandResult;

/**
 * Test Class to test UnsetVirtualProcedurePropertyCommand
 *
 */
@SuppressWarnings( {"javadoc", "nls"} )
public class UnsetVirtualProcedurePropertyCommandTest extends AbstractCommandTest {

    @Test
    public void testUnsetProperty1() throws Exception {
        final String[] commands = { 
            "workspace",
            "create-vdb myVdb vdbPath",
            "cd myVdb",
            "add-model myModel",
            "cd myModel",
            "add-virtual-procedure myVirtualProcedure",
            "cd myVirtualProcedure",
            "set-property name-in-source myNameInSource",
            "unset-property name-in-source" };

        setup( commands );

        CommandResult result = execute();
        assertCommandResultOk(result);

        WorkspaceManager wkspMgr = WorkspaceManager.getInstance(_repo);
        Vdb[] vdbs = wkspMgr.findVdbs(getTransaction());

        assertEquals(vdbs.length,1);

        Model[] models = vdbs[0].getModels(getTransaction());
        assertEquals(1, models.length);
        assertEquals("myModel", models[0].getName(getTransaction())); //$NON-NLS-1$

        Procedure[] procs = models[0].getProcedures(getTransaction());
        assertEquals(1, procs.length);
        assertEquals(true, procs[0] instanceof VirtualProcedure);
        assertEquals("myVirtualProcedure", procs[0].getName(getTransaction())); //$NON-NLS-1$

        assertEquals(null, ((VirtualProcedure)procs[0]).getNameInSource(getTransaction()));
    }

}