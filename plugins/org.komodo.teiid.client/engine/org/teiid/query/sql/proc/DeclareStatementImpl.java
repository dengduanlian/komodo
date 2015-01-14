/* Generated By:JJTree: Do not edit this line. DeclareStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.teiid.query.sql.proc;

import org.komodo.spi.query.sql.proc.DeclareStatement;
import org.teiid.query.parser.TCLanguageVisitorImpl;
import org.teiid.query.parser.TeiidClientParser;
import org.teiid.query.sql.symbol.BaseExpression;

/**
 *
 */
public class DeclareStatementImpl extends AssignmentStatementImpl implements DeclareStatement<BaseExpression, TCLanguageVisitorImpl> {

    // type of the variable
    private String varType;

    /**
     * @param p
     * @param id
     */
    public DeclareStatementImpl(TeiidClientParser p, int id) {
        super(p, id);
    }

    /**
     * Return the type for this statement, this is one of the types
     * defined on the statement object.
     * @return The statement type
     */
    @Override
    public StatementType getType() {
        return StatementType.TYPE_DECLARE;
    }

    /**
     * Get the type of this variable declared in this statement.
     * @return A string giving the variable type
     */
    @Override
    public String getVariableType() {
        return varType;
    }
    
    /**
     * Set the type of this variable declared in this statement.
     * @param varType A string giving the variable type
     */
    public void setVariableType(String varType) {
        this.varType = varType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.varType == null) ? 0 : this.varType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        DeclareStatementImpl other = (DeclareStatementImpl)obj;
        if (this.varType == null) {
            if (other.varType != null) return false;
        } else if (!this.varType.equals(other.varType)) return false;
        return true;
    }

    /** Accept the visitor. **/
    @Override
    public void acceptVisitor(TCLanguageVisitorImpl visitor) {
        visitor.visit(this);
    }

    @SuppressWarnings( "deprecation" )
    @Override
    public DeclareStatementImpl clone() {
        DeclareStatementImpl clone = new DeclareStatementImpl(this.parser, this.id);

        if(getVariableType() != null)
            clone.setVariableType(getVariableType());
        if(getExpression() != null)
            clone.setExpression(getExpression().clone());
        if(getCommand() != null)
            clone.setCommand(getCommand().clone());
        if(getVariable() != null)
            clone.setVariable(getVariable().clone());
        if(getValue() != null)
            clone.setValue(getValue().clone());

        return clone;
    }

}
/* JavaCC - OriginalChecksum=b8ea9db34b18b50cd3c1a83bc3fa40cd (do not edit this line) */