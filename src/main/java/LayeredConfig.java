import java.util.Map;

final class LayeredConfig {
    private final Map<String, String> environment;

    LayeredConfig(Map<String, String> environment) {
        this.environment = environment;
    }

    String required(String name) {
        String value = System.getProperty(name);
        if (value == null || value.isBlank()) value = environment.get(name);
        if (value == null || value.isBlank()) throw new IllegalStateException("Set " + name + " before running this lesson.");
        return value;
    }

    String optional(String name, String fallback) {
        String value = System.getProperty(name);
        if (value == null || value.isBlank()) value = environment.get(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
