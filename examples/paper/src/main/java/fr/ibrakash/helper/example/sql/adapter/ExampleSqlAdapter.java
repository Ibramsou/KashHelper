package fr.ibrakash.helper.example.sql.adapter;

import fr.ibrakash.helper.example.sql.ExampleData;
import fr.ibrakash.helper.example.sql.ExampleRepository;
import fr.ibrakash.helper.persistence.EntityStore;
import fr.ibrakash.helper.persistence.adapter.sql.SqlAdapter;

public class ExampleSqlAdapter extends SqlAdapter<ExampleRepository> {

    private final EntityStore<ExampleData, String> store;

    public ExampleSqlAdapter(ExampleRepository repository) {
        super(repository);

        // Create the example_data table (+ index) if it does not exist yet.
        // All DDL is auto-generated from @PersistedEntity / @PersistedColumn / @PersistedIndex.
        this.store = initTable(ExampleData.class, String.class);

        // Warm up the in-memory cache from the database.
        loadEntireData(repository.getCache(), store);
    }

    // -------------------------------------------------------------------------
    // Custom helpers (SQL-only)
    // -------------------------------------------------------------------------

    
    public long totalPoints() {
        return query(stmt -> {
            var rs = stmt.executeQuery("SELECT COALESCE(SUM(points), 0) FROM example_data");
            return rs.next() ? rs.getLong(1) : 0L;
        });
    }
}
