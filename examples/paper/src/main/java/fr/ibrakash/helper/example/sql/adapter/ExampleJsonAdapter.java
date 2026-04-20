package fr.ibrakash.helper.example.sql.adapter;

import fr.ibrakash.helper.example.sql.ExampleData;
import fr.ibrakash.helper.example.sql.ExampleRepository;
import fr.ibrakash.helper.persistence.adapter.json.JsonAdapter;

import java.util.Comparator;
import java.util.List;

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

    
    public List<ExampleData> getTopByScore(int max) {
        return repository.getCache().values().stream()
                .sorted(Comparator.comparingLong(ExampleData::getScore).reversed())
                .limit(max)
                .toList();
    }
}

