/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.komodo.relational.commands.model;

import static org.komodo.shell.CompletionConstants.MESSAGE_INDENT;

import java.util.ArrayList;
import java.util.List;

import org.komodo.relational.commands.workspace.WorkspaceCommandsI18n;
import org.komodo.relational.model.Model;
import org.komodo.relational.model.Procedure;
import org.komodo.relational.model.StoredProcedure;
import org.komodo.shell.CommandResultImpl;
import org.komodo.shell.api.CommandResult;
import org.komodo.shell.api.WorkspaceStatus;
import org.komodo.utils.i18n.I18n;

/**
 * A shell command to show all the {@link StoredProcedure stored procedures} of a {@link Model model}.
 */
public final class ShowStoredProceduresCommand extends ModelShellCommand {

    static final String NAME = "show-stored-procedures"; //$NON-NLS-1$

    /**
     * @param status
     *        the shell's workspace status (cannot be <code>null</code>)
     */
    public ShowStoredProceduresCommand( final WorkspaceStatus status ) {
        super( NAME, status );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#doExecute()
     */
    @Override
    protected CommandResult doExecute() {
        try {
            final String[] namePatterns = processOptionalArguments( 0 );
            final boolean hasPatterns = ( namePatterns.length != 0 );
            final Model model = getModel();
            final Procedure[] procedures = model.getProcedures( getTransaction(), namePatterns );

            if ( procedures.length == 0 ) {
                if ( hasPatterns ) {
                    print( MESSAGE_INDENT,
                           I18n.bind( ModelCommandsI18n.noMatchingStoredProcedures, model.getName( getTransaction() ) ) );
                } else {
                    print( MESSAGE_INDENT, I18n.bind( ModelCommandsI18n.noStoredProcedures, model.getName( getTransaction() ) ) );
                }
            } else {
                final List< Procedure > storedProcedures = new ArrayList< >( procedures.length );

                for ( final Procedure procedure : procedures ) {
                    if ( StoredProcedure.RESOLVER.resolvable( getTransaction(), procedure ) ) {
                        storedProcedures.add( procedure );
                    }
                }

                if ( storedProcedures.isEmpty() ) {
                    if ( hasPatterns ) {
                        print( MESSAGE_INDENT,
                               I18n.bind( ModelCommandsI18n.noMatchingStoredProcedures, model.getName( getTransaction() ) ) );
                    } else {
                        print( MESSAGE_INDENT, I18n.bind( ModelCommandsI18n.noStoredProcedures, model.getName( getTransaction() ) ) );
                    }
                } else {
                    if ( hasPatterns ) {
                        print( MESSAGE_INDENT,
                               I18n.bind( ModelCommandsI18n.matchedStoredProceduresHeader, model.getName( getTransaction() ) ) );
                    } else {
                        print( MESSAGE_INDENT,
                               I18n.bind( ModelCommandsI18n.storedProceduresHeader, model.getName( getTransaction() ) ) );
                    }

                    final int indent = (MESSAGE_INDENT * 2);

                    for ( final Procedure storedProc : storedProcedures ) {
                        print( indent,
                               I18n.bind( WorkspaceCommandsI18n.printRelationalObject,
                                                    storedProc.getName( getTransaction() ),
                                                    getWorkspaceStatus().getTypeDisplay(storedProc, null)) );
                    }
                }
            }

            return CommandResult.SUCCESS;
        } catch ( final Exception e ) {
            return new CommandResultImpl( e );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#getMaxArgCount()
     */
    @Override
    protected int getMaxArgCount() {
        return -1;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#printHelpDescription(int)
     */
    @Override
    protected void printHelpDescription( final int indent ) {
        print( indent, I18n.bind( ModelCommandsI18n.showStoredProceduresHelp, getName() ) );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#printHelpExamples(int)
     */
    @Override
    protected void printHelpExamples( final int indent ) {
        print( indent, I18n.bind( ModelCommandsI18n.showStoredProceduresExamples ) );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#printHelpUsage(int)
     */
    @Override
    protected void printHelpUsage( final int indent ) {
        print( indent, I18n.bind( ModelCommandsI18n.showStoredProceduresUsage ) );
    }

}
