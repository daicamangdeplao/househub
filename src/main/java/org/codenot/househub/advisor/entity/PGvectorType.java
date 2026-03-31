package org.codenot.househub.advisor.entity;

import com.pgvector.PGvector;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

public class PGvectorType implements UserType<PGvector> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<PGvector> returnedClass() {
        return PGvector.class;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public PGvector nullSafeGet(
            ResultSet rs,
            int position,
            SharedSessionContractImplementor session,
            Object owner) throws SQLException {

        Object value = rs.getObject(position);
        return value == null ? null : (PGvector) value;
    }

    @Override
    public void nullSafeSet(
            PreparedStatement st,
            PGvector value,
            int index,
            SharedSessionContractImplementor session) throws SQLException {

        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value, Types.OTHER);
        }
    }

    @Override
    public PGvector deepCopy(PGvector value) {
        if (value == null) return null;
        return new PGvector(Arrays.copyOf(value.toArray(), value.toArray().length));
    }

    @Override
    public boolean equals(PGvector x, PGvector y) {
        if (x == y) return true;
        if (x == null || y == null) return false;
        return Arrays.equals(x.toArray(), y.toArray());
    }

    @Override
    public int hashCode(PGvector x) {
        return x == null ? 0 : Arrays.hashCode(x.toArray());
    }

    @Override
    public Serializable disassemble(PGvector value) {
        return value == null ? null : value.toArray();
    }

    @Override
    public PGvector assemble(Serializable cached, Object owner) {
        return cached == null ? null : new PGvector((float[]) cached);
    }
}
