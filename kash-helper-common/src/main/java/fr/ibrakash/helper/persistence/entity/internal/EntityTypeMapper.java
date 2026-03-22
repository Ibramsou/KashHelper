package fr.ibrakash.helper.persistence.entity.internal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

final class EntityTypeMapper {

    private EntityTypeMapper() {
    }

    static String sqlType(Class<?> type, int length, boolean sqlite) {
        if (type == String.class || type == UUID.class || type.isEnum()) {
            return sqlite ? "TEXT" : "VARCHAR(" + Math.max(length, 1) + ")";
        }
        if (type == int.class || type == Integer.class) {
            return sqlite ? "INTEGER" : "INT";
        }
        if (type == long.class || type == Long.class) {
            return sqlite ? "INTEGER" : "BIGINT";
        }
        if (type == boolean.class || type == Boolean.class) {
            return sqlite ? "INTEGER" : "BOOLEAN";
        }
        if (type == float.class || type == Float.class) {
            return sqlite ? "REAL" : "FLOAT";
        }
        if (type == double.class || type == Double.class) {
            return sqlite ? "REAL" : "DOUBLE";
        }
        throw new IllegalArgumentException("Unsupported field type: " + type.getName());
    }

    static void bind(PreparedStatement stmt, int index, Class<?> type, Object value) throws SQLException {
        if (value == null) {
            stmt.setObject(index, null);
            return;
        }
        if (type == String.class) {
            stmt.setString(index, (String) value);
            return;
        }
        if (type == UUID.class) {
            stmt.setString(index, value.toString());
            return;
        }
        if (type == int.class || type == Integer.class) {
            stmt.setInt(index, (Integer) value);
            return;
        }
        if (type == long.class || type == Long.class) {
            stmt.setLong(index, (Long) value);
            return;
        }
        if (type == boolean.class || type == Boolean.class) {
            stmt.setBoolean(index, (Boolean) value);
            return;
        }
        if (type == float.class || type == Float.class) {
            stmt.setFloat(index, (Float) value);
            return;
        }
        if (type == double.class || type == Double.class) {
            stmt.setDouble(index, (Double) value);
            return;
        }
        if (type.isEnum()) {
            stmt.setString(index, ((Enum<?>) value).name());
            return;
        }
        throw new IllegalArgumentException("Unsupported bind type: " + type.getName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static Object read(ResultSet rs, String column, Class<?> type) throws SQLException {
        if (type == String.class) {
            return rs.getString(column);
        }
        if (type == UUID.class) {
            String raw = rs.getString(column);
            return raw == null ? null : UUID.fromString(raw);
        }
        if (type == int.class || type == Integer.class) {
            int v = rs.getInt(column);
            return rs.wasNull() && type == Integer.class ? null : v;
        }
        if (type == long.class || type == Long.class) {
            long v = rs.getLong(column);
            return rs.wasNull() && type == Long.class ? null : v;
        }
        if (type == boolean.class || type == Boolean.class) {
            boolean v = rs.getBoolean(column);
            return rs.wasNull() && type == Boolean.class ? null : v;
        }
        if (type == float.class || type == Float.class) {
            float v = rs.getFloat(column);
            return rs.wasNull() && type == Float.class ? null : v;
        }
        if (type == double.class || type == Double.class) {
            double v = rs.getDouble(column);
            return rs.wasNull() && type == Double.class ? null : v;
        }
        if (type.isEnum()) {
            String raw = rs.getString(column);
            if (raw == null) return null;
            return Enum.valueOf((Class<? extends Enum>) type, raw);
        }
        throw new IllegalArgumentException("Unsupported read type: " + type.getName());
    }

    static Object parseId(String raw, Class<?> idType) {
        if (idType == String.class) return raw;
        if (idType == UUID.class) return UUID.fromString(raw);
        if (idType == Long.class || idType == long.class) return Long.parseLong(raw);
        if (idType == Integer.class || idType == int.class) return Integer.parseInt(raw);
        throw new IllegalArgumentException("Unsupported id type: " + idType.getName());
    }

    static boolean isSupportedType(Class<?> type) {
        if (type == String.class || type == UUID.class || type.isEnum()) return true;
        if (type == int.class || type == Integer.class) return true;
        if (type == long.class || type == Long.class) return true;
        if (type == boolean.class || type == Boolean.class) return true;
        if (type == float.class || type == Float.class) return true;
        return type == double.class || type == Double.class;
    }
}
