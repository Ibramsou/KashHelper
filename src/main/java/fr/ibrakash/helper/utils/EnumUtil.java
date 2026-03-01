package fr.ibrakash.helper.utils;

public class EnumUtil {

    public static String friendlyName(Enum<?> e) {
        StringBuilder sb = new StringBuilder();
        for (String string : e.name().split("_")) {
            sb.append(string.substring(0, 1).toUpperCase()).append(string.substring(1).toLowerCase());
        }

        return sb.toString();
    }
}
