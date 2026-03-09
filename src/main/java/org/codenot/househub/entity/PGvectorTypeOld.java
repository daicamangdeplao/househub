package org.codenot.househub.entity;


import com.pgvector.PGvector;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@Deprecated
public class PGvectorTypeOld implements UserType<PGvector> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<PGvector> returnedClass() {
        return PGvector.class;
    }

    @Override
    public PGvector nullSafeGet(
            ResultSet rs, int position,
            SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        Object obj = rs.getObject(position);
        return obj == null ? null : (PGvector) obj;
    }

    @Override
    public void nullSafeSet(
            PreparedStatement st, PGvector value, int index,
            SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value, Types.OTHER);
        }
    }

    @Override
    public PGvector deepCopy(PGvector pGvector) {
        return null;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(PGvector pGvector) {
        return null;
    }

    @Override
    public PGvector assemble(Serializable serializable, Object o) {
        return null;
    }


    @Override
    public boolean equals(PGvector pGvector, PGvector j1) {
        return false;
    }

    @Override
    public int hashCode(PGvector pGvector) {
        return 0;
    }
}
