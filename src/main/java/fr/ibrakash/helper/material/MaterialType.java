package fr.ibrakash.helper.material;

public enum MaterialType {
    WOOD("WOOD"),
    ORES("_ORE"),
    TOOLS("PICKAXE", "_AXE", "SWORD", "HOE");

    private final String[] content;

    MaterialType(String... content) {
        this.content = content;
    }

    public String[] getContent() {
        return content;
    }
}
