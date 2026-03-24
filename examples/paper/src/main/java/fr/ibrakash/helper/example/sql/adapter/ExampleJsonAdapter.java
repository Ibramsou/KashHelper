package fr.ibrakash.helper.example.sql.adapter;

import fr.ibrakash.helper.example.sql.ExampleData;
import fr.ibrakash.helper.example.sql.ExampleRepository;
import fr.ibrakash.helper.persistence.adapter.JsonAdapter;

import java.util.Comparator;
import java.util.List;

/**
 * JSON adapter for {@link ExampleRepository}.
 *
 * <p>On construction (= backend activation), loads the entire {@code example_data.json}
 * file into the repository cache so every subsequent read is served in-memory.
 *
 * <p>Override methods from {@link JsonAdapter} with {@code @Override} if you need to
 * change their behaviour. Non-annotated methods are custom helpers only available from
 * this class.
 */
public class ExampleJsonAdapter extends JsonAdapter<ExampleRepository> {

    public ExampleJsonAdapter(ExampleRepository repository) {
        super(repository);

        // Pre-populate the in-memory cache from the JSON file.
        // loadEntireData resolves example_data.json in the configured storage folder
        // and merges every entry into the cache map.
        loadEntireData(repository.getCache(), String.class, ExampleData.class);
    }

    // -------------------------------------------------------------------------
    // Custom helpers (only available from ExampleJsonAdapter, not the adapter API)
    // -------------------------------------------------------------------------

    /**
     * Returns the top {@code max} profiles sorted by score, derived purely from
     * the in-memory cache (fast — no I/O).
     */
    public List<ExampleData> getTopByScore(int max) {
        return repository.getCache().values().stream()
                .sorted(Comparator.comparingLong(ExampleData::getScore).reversed())
                .limit(max)
                .toList();
    }
}


