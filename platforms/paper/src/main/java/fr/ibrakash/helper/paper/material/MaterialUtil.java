package fr.ibrakash.helper.paper.material;

import org.bukkit.Material;

import java.util.*;
import java.util.function.Supplier;

public class MaterialUtil {

    private static final Map<String, Set<Material>> TOOLS_MATERIALS;
    private static final Map<MaterialType, Set<Material>> MATERIALS_BY_TYPE;
    private static final Map<MaterialType, Set<Material>> ITEM_MATERIALS_BY_TYPE;
    private static final Set<Material> ALL_MATERIALS;
    private static final Set<Material> ALL_ITEMS;

    static {
        Map<String, Set<Material>> toolsMaterials = new HashMap<>();
        EnumMap<MaterialType, Set<Material>> materialTypes = new EnumMap<>(MaterialType.class);
        EnumMap<MaterialType, Set<Material>> itemMaterialTypes = new EnumMap<>(MaterialType.class);

        EnumSet<Material> allMaterials = EnumSet.noneOf(Material.class);
        EnumSet<Material> allItems = EnumSet.noneOf(Material.class);

        for (Material value : Material.values()) {
            allMaterials.add(value);

            if (!value.isItem() || value.isLegacy() || value.isAir()) continue;

            allItems.add(value);

            String name = value.name();


            for (MaterialType materialType : MaterialType.values()) {
                for (String s : materialType.getContent()) {
                    if (!name.contains(s)) continue;

                    materialTypes.computeIfAbsent(materialType, ignored -> EnumSet.noneOf(Material.class)).add(value);
                    if (value.isItem() && !value.isAir()) {
                        itemMaterialTypes.computeIfAbsent(materialType, ignored -> EnumSet.noneOf(Material.class)).add(value);
                    }
                }
            }

            if (name.contains("SWORD")) {
                registerTool(toolsMaterials, "sword", value);
            } else if (name.contains("PICKAXE")) {
                registerTool(toolsMaterials, "pickaxe", value);
            } else if (name.contains("AXE")) {
                registerTool(toolsMaterials, "axe", value);
            } else if (name.contains("HOE")) {
                registerTool(toolsMaterials, "hoe", value);
            }
        }

        TOOLS_MATERIALS = freezeStringMap(toolsMaterials);
        MATERIALS_BY_TYPE = freezeMaterialTypeMap(materialTypes);
        ITEM_MATERIALS_BY_TYPE = freezeMaterialTypeMap(itemMaterialTypes);
        ALL_MATERIALS = Collections.unmodifiableSet(EnumSet.copyOf(allMaterials));
        ALL_ITEMS = Collections.unmodifiableSet(EnumSet.copyOf(allItems));
    }

    public static void warmup() {
        // Intentionally empty: calling this method forces static cache initialization.
    }

    public static Map<MaterialType, Set<Material>> getMaterialsByType() {
        return MATERIALS_BY_TYPE;
    }

    public static Map<MaterialType, Set<Material>> getItemMaterialsByType() {
        return ITEM_MATERIALS_BY_TYPE;
    }

    public static Set<Material> getAllMaterials() {
        return ALL_MATERIALS;
    }

    public static Set<Material> getAllItems() {
        return ALL_ITEMS;
    }

    public static Set<Material> getMaterials(MaterialType type) {
        return MATERIALS_BY_TYPE.getOrDefault(type, Collections.emptySet());
    }

    public static boolean isType(Material material, MaterialType materialType) {
        Set<Material> materials = MATERIALS_BY_TYPE.get(materialType);
        return materials != null && materials.contains(material);
    }

    private static void registerTool(Map<String, Set<Material>> toolsMaterials, String identifier, Material material) {
        toolsMaterials.computeIfAbsent(identifier, ignored -> EnumSet.noneOf(Material.class)).add(material);
    }

    private static Map<MaterialType, Set<Material>> freezeMaterialTypeMap(Map<MaterialType, Set<Material>> source) {
        EnumMap<MaterialType, Set<Material>> frozen = new EnumMap<>(MaterialType.class);
        for (Map.Entry<MaterialType, Set<Material>> entry : source.entrySet()) {
            frozen.put(entry.getKey(), Collections.unmodifiableSet(EnumSet.copyOf(entry.getValue())));
        }
        return Collections.unmodifiableMap(frozen);
    }

    private static Map<String, Set<Material>> freezeStringMap(Map<String, Set<Material>> source) {
        Map<String, Set<Material>> frozen = new HashMap<>();
        for (Map.Entry<String, Set<Material>> entry : source.entrySet()) {
            frozen.put(entry.getKey(), Collections.unmodifiableSet(EnumSet.copyOf(entry.getValue())));
        }
        return Collections.unmodifiableMap(frozen);
    }

    public static Set<Material> getTools(String identifier) {
        return TOOLS_MATERIALS.get(identifier.toLowerCase());
    }

    public static Material parse(String name) {
        return parseOrElse(name, () -> Material.STONE);
    }

    public static Material parseOrElse(String name, Supplier<Material> defaultMaterial) {
        try {
            return Material.getMaterial(name);
        } catch (Exception exception) {
            return defaultMaterial.get();
        }
    }

    public static Material parseOrThrow(String name) {
        return parseOrThrow(name, "%s isn't a valid material name");
    }

    public static Material parseOrThrow(String name, String errorFormat) {
        return parseOrElse(name, () -> {
            throw new IllegalArgumentException(String.format(errorFormat, name));
        });
    }


    public static Material parseOrWarn(String name) {
        return parseOrWarn(name, "%s isn't a valid material name");
    }

    public static Material parseOrWarn(String name, String errorFormat) {
        return parseOrElse(name, () -> {
            System.out.println(errorFormat);
            return Material.PAPER;
        });
    }
}
