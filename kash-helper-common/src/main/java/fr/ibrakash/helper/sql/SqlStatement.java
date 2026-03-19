package fr.ibrakash.helper.sql;

import java.sql.SQLException;
import java.sql.Statement;

public interface SqlStatement<T extends Statement> {

    void execute(T statement) throws SQLException, InterruptedException;
}
