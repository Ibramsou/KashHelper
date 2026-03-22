package fr.ibrakash.helper.persistence.json;

import fr.ibrakash.helper.persistence.PersistenceEngine;
import fr.ibrakash.helper.persistence.Repository;
import fr.ibrakash.helper.utils.JsonUtil;

import java.io.File;

/**
 * Base class for JSON-backed repositories.
 *
 * <p>Subclasses receive the storage folder and use {@link JsonUtil} to
 * serialise/deserialise their data freely.
 *
 * <p>Example skeleton:
 * <pre>{@code
 * public class MyRepository extends JsonRepository {
 *
 *     private Map<UUID, MyData> cache = new HashMap<>();
 *
 *     @Override
 *     public void loadAll() {
 *         cache = JsonUtil.readFileMap(
 *             new File(storageFolder, "my_data.json"),
 *             HashMap::new, UUID.class, MyData.class
 *         );
 *     }
 *
 *     @Override
 *     public void saveAll() {
 *         JsonUtil.writeFile(new File(storageFolder, "my_data.json"), cache);
 *     }
 * }
 * }</pre>
 */
public abstract class JsonRepository implements Repository {

    protected File storageFolder;

    @Override
    public final void init(PersistenceEngine engine) {
        this.storageFolder = engine.getStorageFolder();
        this.onCreate();
    }

    /**
     * Called once after the storage folder is injected.
     * Override for any one-time initialisation (e.g. creating sub-directories).
     * Default: no-op.
     */
    protected void onCreate() {
        this.storageFolder.mkdirs();
    }

    /** Convenience: resolve a JSON file by name inside the storage folder. */
    protected File jsonFile(String name) {
        return new File(this.storageFolder, name + ".json");
    }
}

