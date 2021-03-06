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
package org.komodo.relational.commands.tabularresultset;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.komodo.relational.model.ResultSetColumn;
import org.komodo.shell.api.CommandResult;

/**
 * Test Class to test {@link DeleteColumnCommand}.
 */
@SuppressWarnings( { "javadoc",
                     "nls" } )
public final class DeleteColumnCommandTest extends TabularResultSetCommandTest {

    @Test
    public void shouldDeleteColumn() throws Exception {
        final String deletedColumn = "myColumn1";
        final String remainingColumn = "myColumn2";
        final String[] commands = { "add-column " + deletedColumn,
                                    "add-column " + remainingColumn,
                                    "delete-column " + deletedColumn };
        final CommandResult result = execute( commands );
        assertCommandResultOk(result);

        final ResultSetColumn[] cols = get().getColumns( getTransaction() );
        assertThat( cols.length, is( 1 ) );
        assertThat( cols[ 0 ].getName( getTransaction() ), is( remainingColumn ) );
    }

}
