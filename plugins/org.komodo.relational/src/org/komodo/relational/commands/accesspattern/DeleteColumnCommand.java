/*
 * JBoss, Home of Professional Open Source.
 *
 * See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
 *
 * See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
 */
package org.komodo.relational.commands.accesspattern;

import static org.komodo.relational.commands.table.TableCommandMessages.DeleteColumnCommand.COLUMN_DELETED;
import static org.komodo.relational.commands.table.TableCommandMessages.General.MISSING_COLUMN_NAME;
import static org.komodo.shell.CompletionConstants.MESSAGE_INDENT;
import java.util.ArrayList;
import java.util.List;
import org.komodo.relational.model.AccessPattern;
import org.komodo.relational.model.Column;
import org.komodo.shell.api.Arguments;
import org.komodo.shell.api.WorkspaceStatus;
import org.komodo.spi.repository.Repository.UnitOfWork;

/**
 * A shell command to delete a Column from an AccessPattern.
 */
public final class DeleteColumnCommand extends AccessPatternShellCommand {

    static final String NAME = "delete-column"; //$NON-NLS-1$

    /**
     * @param status
     *        the shell's workspace status (cannot be <code>null</code>)
     */
    public DeleteColumnCommand( final WorkspaceStatus status ) {
        super( NAME, true, status );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#doExecute()
     */
    @Override
    protected boolean doExecute() throws Exception {
        final String columnName = requiredArgument( 0, getMessage(MISSING_COLUMN_NAME) );

        final AccessPattern ap = getAccessPattern();
        ap.removeColumn( getTransaction(), columnName );

        // Print success message
        print(MESSAGE_INDENT, getMessage(COLUMN_DELETED,columnName));
        
        return true;
    }
    
    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#tabCompletion(java.lang.String, java.util.List)
     */
    @Override
    public int tabCompletion( final String lastArgument,
                              final List< CharSequence > candidates ) throws Exception {
        final Arguments args = getArguments();

        final UnitOfWork uow = getTransaction();
        final AccessPattern ap = getAccessPattern();
        final Column[] columns = ap.getColumns( uow );
        List<String> existingColumnNames = new ArrayList<String>(columns.length);
        for(Column column : columns) {
            existingColumnNames.add(column.getName(uow));
        }
        
        if ( args.isEmpty() ) {
            if ( lastArgument == null ) {
                candidates.addAll( existingColumnNames );
            } else {
                for ( final String item : existingColumnNames ) {
                    if ( item.toUpperCase().startsWith( lastArgument.toUpperCase() ) ) {
                        candidates.add( item );
                    }
                }
            }

            return 0;
        }

        // no tab completion
        return -1;
    }
    
}
