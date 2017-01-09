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
package org.komodo.relational.commands.pushdownfunction;

import java.util.List;
import org.komodo.relational.commands.workspace.WorkspaceCommandsI18n;
import org.komodo.relational.model.AbstractProcedure;
import org.komodo.relational.model.Function;
import org.komodo.relational.model.Function.Determinism;
import org.komodo.relational.model.PushdownFunction;
import org.komodo.shell.CommandResultImpl;
import org.komodo.shell.api.Arguments;
import org.komodo.shell.api.CommandResult;
import org.komodo.shell.api.TabCompletionModifier;
import org.komodo.shell.api.WorkspaceStatus;
import org.komodo.shell.commands.UnsetPropertyCommand;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.utils.StringUtils;
import org.komodo.utils.i18n.I18n;

/**
 * A shell command to set PushdownFunction properties
 */
public final class UnsetPushdownFunctionPropertyCommand extends PushdownFunctionShellCommand {

    static final String NAME = UnsetPropertyCommand.NAME;

    /**
     * @param status
     *        the shell's workspace status (cannot be <code>null</code>)
     */
    public UnsetPushdownFunctionPropertyCommand( final WorkspaceStatus status ) {
        super( NAME, status );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#doExecute()
     */
    @Override
    protected CommandResult doExecute() {
        CommandResult result = null;

        try {
            final String name = requiredArgument( 0, I18n.bind( WorkspaceCommandsI18n.unsetMissingPropertyName ) );

            final PushdownFunction func = getPushdownFunction();
            final UnitOfWork transaction = getTransaction();
            String errorMsg = null;

            if ( AGGREGATE.equals( name ) ) {
                func.setAggregate( transaction, Function.DEFAULT_AGGREGATE );
            } else if ( ALLOWS_DISTINCT.equals( name ) ) {
                func.setAllowsDistinct( transaction, Function.DEFAULT_ALLOWS_DISTINCT );
            } else if ( ALLOWS_ORDERBY.equals( name ) ) {
                func.setAllowsOrderBy( transaction, Function.DEFAULT_ALLOWS_ORDER_BY );
            } else if ( ANALYTIC.equals( name ) ) {
                func.setAnalytic( transaction, Function.DEFAULT_ANALYTIC );
            } else if ( DECOMPOSABLE.equals( name ) ) {
                func.setDecomposable( transaction, Function.DEFAULT_DECOMPOSABLE );
            } else if ( DESCRIPTION.equals( name ) ) {
                func.setDescription( getTransaction(), null );
            } else if ( DETERMINISM.equals( name ) ) {
                func.setDeterminism( transaction, Determinism.DEFAULT_VALUE );
            } else if ( NAME_IN_SOURCE.equals( name ) ) {
                func.setNameInSource( getTransaction(), null );
            } else if ( NULL_ON_NULL.equals( name ) ) {
                func.setNullOnNull( transaction, Function.DEFAULT_NULL_ON_NULL );
            } else if ( SCHEMA_ELEMENT_TYPE.equals( name ) ) {
                func.setSchemaElementType( transaction, null );
            } else if ( UPDATE_COUNT.equals( name ) ) {
                func.setUpdateCount( transaction, AbstractProcedure.DEFAULT_UPDATE_COUNT );
            } else if ( USES_DISTINCT_ROWS.equals( name ) ) {
                func.setUsesDistinctRows( transaction, Function.DEFAULT_USES_DISTINCT_ROWS );
            } else if ( UUID.equals( name ) ) {
                func.setUuid( getTransaction(), null );
            } else if ( VAR_ARGS.equals( name ) ) {
                func.setVarArgs( transaction, Function.DEFAULT_VARARGS );
            } else {
                errorMsg = I18n.bind( WorkspaceCommandsI18n.invalidPropertyName, name, PushdownFunction.class.getSimpleName() );
            }

            if ( StringUtils.isBlank( errorMsg ) ) {
                result = new CommandResultImpl( I18n.bind( WorkspaceCommandsI18n.unsetPropertySuccess, name ) );
            } else {
                result = new CommandResultImpl( false, errorMsg, null );
            }
        } catch ( final Exception e ) {
            result = new CommandResultImpl( e );
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#getMaxArgCount()
     */
    @Override
    protected int getMaxArgCount() {
        return 1;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#printHelpDescription(int)
     */
    @Override
    protected void printHelpDescription( final int indent ) {
        print( indent, I18n.bind( PushdownFunctionCommandsI18n.unsetPushdownFunctionPropertyHelp, getName() ) );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#printHelpExamples(int)
     */
    @Override
    protected void printHelpExamples( final int indent ) {
        print( indent, I18n.bind( PushdownFunctionCommandsI18n.unsetPushdownFunctionPropertyExamples ) );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#printHelpUsage(int)
     */
    @Override
    protected void printHelpUsage( final int indent ) {
        print( indent, I18n.bind( PushdownFunctionCommandsI18n.unsetPushdownFunctionPropertyUsage ) );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#tabCompletion(java.lang.String, java.util.List)
     */
    @Override
    public TabCompletionModifier tabCompletion( final String lastArgument,
                              final List< CharSequence > candidates ) throws Exception {
        final Arguments args = getArguments();

        if ( args.isEmpty() ) {
            if ( lastArgument == null ) {
                candidates.addAll( ALL_PROPS );
            } else {
                for ( final String item : ALL_PROPS ) {
                    if ( item.toUpperCase().startsWith( lastArgument.toUpperCase() ) ) {
                        candidates.add( item );
                    }
                }
            }
        }
        return TabCompletionModifier.AUTO;
    }
}
