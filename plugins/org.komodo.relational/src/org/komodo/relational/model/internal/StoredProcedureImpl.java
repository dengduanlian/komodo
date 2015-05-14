/*
 * JBoss, Home of Professional Open Source.
 *
 * See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
 *
 * See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
 */
package org.komodo.relational.model.internal;

import java.util.ArrayList;
import java.util.List;
import org.komodo.relational.Messages;
import org.komodo.relational.Messages.Relational;
import org.komodo.relational.RelationalProperties;
import org.komodo.relational.internal.AdapterFactory;
import org.komodo.relational.internal.RelationalModelFactory;
import org.komodo.relational.internal.TypeResolver;
import org.komodo.relational.model.DataTypeResultSet;
import org.komodo.relational.model.Model;
import org.komodo.relational.model.ProcedureResultSet;
import org.komodo.relational.model.StatementOption;
import org.komodo.relational.model.StoredProcedure;
import org.komodo.relational.model.TabularResultSet;
import org.komodo.repository.ObjectImpl;
import org.komodo.spi.KException;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.Repository;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;
import org.komodo.utils.ArgCheck;
import org.modeshape.sequencer.ddl.dialect.teiid.TeiidDdlLexicon.CreateProcedure;
import org.modeshape.sequencer.ddl.dialect.teiid.TeiidDdlLexicon.SchemaElement;

/**
 * An implementation of a stored procedure.
 */
public final class StoredProcedureImpl extends AbstractProcedureImpl implements StoredProcedure {

    private enum StandardOptions {

        NATIVE_QUERY( "native-query" ), //$NON-NLS-1$
        NON_PREPARED( "non-prepared" ); //$NON-NLS-1$

        private final String name;

        private StandardOptions( final String optionName ) {
            this.name = optionName;
        }

        public String getName() {
            return this.name;
        }

    }

    /**
     * The resolver of a {@link StoredProcedure}.
     */
    public static final TypeResolver< StoredProcedure > RESOLVER = new TypeResolver< StoredProcedure >() {

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.internal.TypeResolver#create(org.komodo.spi.repository.Repository.UnitOfWork,
         *      org.komodo.spi.repository.Repository, org.komodo.spi.repository.KomodoObject, java.lang.String,
         *      org.komodo.relational.RelationalProperties)
         */
        @Override
        public StoredProcedure create( final UnitOfWork transaction,
                                       final Repository repository,
                                       final KomodoObject parent,
                                       final String id,
                                       final RelationalProperties properties ) throws KException {
            final AdapterFactory adapter = new AdapterFactory( repository );
            final Model parentModel = adapter.adapt( transaction, parent, Model.class );
            return RelationalModelFactory.createStoredProcedure( transaction, repository, parentModel, id );
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.internal.TypeResolver#identifier()
         */
        @Override
        public KomodoType identifier() {
            return IDENTIFIER;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.internal.TypeResolver#owningClass()
         */
        @Override
        public Class< StoredProcedureImpl > owningClass() {
            return StoredProcedureImpl.class;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.internal.TypeResolver#resolvable(org.komodo.spi.repository.Repository.UnitOfWork,
         *      org.komodo.spi.repository.KomodoObject)
         */
        @Override
        public boolean resolvable( final UnitOfWork transaction,
                                   final KomodoObject kobject ) throws KException {
            return ObjectImpl.validateType( transaction, kobject.getRepository(), kobject, CreateProcedure.PROCEDURE_STATEMENT )
                   && ObjectImpl.validatePropertyValue( transaction,
                                                        kobject.getRepository(),
                                                        kobject,
                                                        SchemaElement.TYPE,
                                                        SchemaElementType.FOREIGN.name() );
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.internal.TypeResolver#resolve(org.komodo.spi.repository.Repository.UnitOfWork,
         *      org.komodo.spi.repository.KomodoObject)
         */
        @Override
        public StoredProcedure resolve( final UnitOfWork transaction,
                                        final KomodoObject kobject ) throws KException {
            return new StoredProcedureImpl( transaction, kobject.getRepository(), kobject.getAbsolutePath() );
        }

    };

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the relational object exists (cannot be <code>null</code>)
     * @param workspacePath
     *        the workspace relative path (cannot be empty)
     * @throws KException
     *         if an error occurs or if node at specified path is not a procedure
     */
    public StoredProcedureImpl( final UnitOfWork uow,
                                final Repository repository,
                                final String workspacePath ) throws KException {
        super( uow, repository, workspacePath );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.OptionContainer#getCustomOptions(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public StatementOption[] getCustomOptions( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        // get super custom options then delete any subclass standard options
        final StatementOption[] superOptions = super.getCustomOptions( transaction );
        StatementOption[] result = StatementOption.NO_OPTIONS;

        if ( superOptions.length != 0 ) {
            final List< StatementOption > temp = new ArrayList<>( superOptions.length );

            for ( final StatementOption option : superOptions ) {
                if ( StandardOptions.valueOf( option.getName( transaction ) ) == null ) {
                    temp.add( option );
                }
            }

            if ( !temp.isEmpty() ) {
                result = temp.toArray( new StatementOption[ temp.size() ] );
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.StoredProcedure#getNativeQuery(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getNativeQuery( final UnitOfWork transaction ) throws KException {
        final StatementOption option = Utils.getOption( transaction, this, StandardOptions.NATIVE_QUERY.getName() );

        if (option == null) {
            return null;
        }

        return option.getOption( transaction );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.StoredProcedure#getResultSet(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public ProcedureResultSet getResultSet( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        ProcedureResultSet result = null;

        if ( hasChild( transaction, CreateProcedure.RESULT_SET ) ) {
            final KomodoObject kobject = getChild( transaction, CreateProcedure.RESULT_SET );

            if ( DataTypeResultSetImpl.RESOLVER.resolvable( transaction, kobject ) ) {
                result = DataTypeResultSetImpl.RESOLVER.resolve( transaction, kobject );
            } else if ( TabularResultSetImpl.RESOLVER.resolvable( transaction, kobject ) ) {
                result = TabularResultSetImpl.RESOLVER.resolve( transaction, kobject );
            } else {
                LOGGER.error( Messages.getString( Relational.UNEXPECTED_RESULT_SET_TYPE, kobject.getAbsolutePath() ) );
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.StoredProcedure#isNonPrepared(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public boolean isNonPrepared( final UnitOfWork transaction ) throws KException {
        final StatementOption option = Utils.getOption( transaction, this, StandardOptions.NON_PREPARED.getName() );

        if (option == null) {
            return DEFAULT_NON_PREPARED;
        }

        return Boolean.parseBoolean( option.getOption( transaction ) );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.StoredProcedure#removeResultSet(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public void removeResultSet( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        // delete existing result set
        final ProcedureResultSet resultSet = getResultSet( transaction );

        if ( resultSet == null ) {
            throw new KException( Messages.getString( Relational.RESULT_SET_NOT_FOUND_TO_REMOVE, getAbsolutePath() ) );
        }

        resultSet.remove( transaction );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.StoredProcedure#setNativeQuery(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String)
     */
    @Override
    public void setNativeQuery( final UnitOfWork transaction,
                                final String newNativeQuery ) throws KException {
        setStatementOption( transaction, StandardOptions.NATIVE_QUERY.getName(), newNativeQuery );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.StoredProcedure#setNonPrepared(org.komodo.spi.repository.Repository.UnitOfWork, boolean)
     */
    @Override
    public void setNonPrepared( final UnitOfWork transaction,
                                final boolean newNonPrepared ) throws KException {
        setStatementOption( transaction, StandardOptions.NON_PREPARED.getName(), Boolean.toString( newNonPrepared ) );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.StoredProcedure#setResultSet(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.Class)
     */
    @SuppressWarnings( "unchecked" )
    @Override
    public < T extends ProcedureResultSet > T setResultSet( final UnitOfWork transaction,
                                                            final Class< T > resultSetType ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( resultSetType, "resultSetType" ); //$NON-NLS-1$

        // delete existing result set (don't call removeResultSet here as it will throw exception if one does not exist)
        final ProcedureResultSet resultSet = getResultSet( transaction );

        if ( resultSet != null ) {
            resultSet.remove( transaction );
        }

        T result = null;

        if ( resultSetType == TabularResultSet.class ) {
            result = ( T )RelationalModelFactory.createTabularResultSet( transaction, getRepository(), this );
        } else if ( resultSetType == DataTypeResultSet.class ) {
            result = ( T )RelationalModelFactory.createDataTypeResultSet( transaction, getRepository(), this );
        } else {
            throw new UnsupportedOperationException( Messages.getString( Relational.UNEXPECTED_RESULT_SET_TYPE,
                                                                         resultSetType.getName() ) );
        }

        return result;
    }

}
