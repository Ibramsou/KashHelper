package fr.ibrakash.helper.sql;

import java.sql.SQLException;
import java.sql.Statement;

@FunctionalInterface
public interface SqlResultStatement<T extends Statement, V> {

    V result(T statement) throws SQLException, InterruptedException;
}
