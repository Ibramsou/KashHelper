package fr.ibrakash.helper.configuration.readers;

import fr.ibrakash.helper.configuration.ConfigurationLoaderType;
import fr.ibrakash.helper.configuration.Configurations;
import fr.ibrakash.helper.platform.KashAddon;
import org.spongepowered.configurate.ConfigurationNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * XML locale reader that resolves message paths like "messageId.componentId".
 */
public class XmlLocaleReader extends SingleConfigurationReader<String> {

    public XmlLocaleReader(KashAddon<?> addon, String key) {
        super(addon, key);
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public void reload() {
        this.pathMap.clear();

        Path xmlPath = this.addon.paths().get(this.key, "xml");
        ensureFileExists(xmlPath, this.key + ".xml");

        if (!Files.exists(xmlPath)) {
            return;
        }

        try {
            ConfigurationLoaderType.XML.get(xmlPath, Configurations.DEFAULT_SERIALIZERS).load();
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot load XML locale '" + this.key + "': " + exception.getMessage(), exception);
        }

        try (InputStream in = Files.newInputStream(xmlPath)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            Document document = factory.newDocumentBuilder().parse(in);
            document.getDocumentElement().normalize();

            Element root = document.getDocumentElement();
            if (!"messages".equals(root.getTagName()) && !"locales".equals(root.getTagName())) {
                return;
            }

            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (!(node instanceof Element messageElement) || !"message".equals(messageElement.getTagName())) {
                    continue;
                }

                String messageId = messageElement.getAttribute("id").trim();
                if (messageId.isBlank()) {
                    continue;
                }

                this.collectMessage(messageElement, messageId);
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot parse XML locale '" + this.key + "': " + exception.getMessage(), exception);
        }
    }

    @Override
    public String resolve(String path) {
        String direct = this.pathMap.get(path);
        if (direct != null) {
            return direct;
        }

        if (path != null && !path.endsWith(".container")) {
            return this.pathMap.get(path + ".container");
        }
        return null;
    }

    public List<String> lines(String path) {
        String value = this.resolve(path);
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split("\\R"));
    }

    @Override
    public String buildValue(ConfigurationNode node) {
        return node.getString();
    }

    @Override
    public String fallBackValue() {
        return null;
    }

    private void collectMessage(Element messageElement, String messageId) {
        List<Element> components = directChildElements(messageElement);

        if (components.isEmpty()) {
            String text = normalizedText(messageElement);
            if (!text.isBlank()) {
                this.pathMap.put(messageId, text);
            }
            return;
        }

        // Single <lines> child without an id => store directly under messageId.
        if (components.size() == 1 && "lines".equals(components.getFirst().getTagName())
                && components.getFirst().getAttribute("id").isBlank()) {
            String rendered = componentValue(components.getFirst());
            if (!rendered.isBlank()) {
                this.pathMap.put(messageId, rendered);
            }
            return;
        }

        for (Element component : components) {
            String componentId = component.getAttribute("id").trim();
            if (componentId.isBlank()) {
                componentId = component.getTagName();
            }

            String rendered = componentValue(component);
            if (!rendered.isBlank()) {
                this.pathMap.put(messageId + "." + componentId, rendered);
            }
        }
    }

    private String componentValue(Element component) {
        // <lines> is a special tag: each <line> child becomes a plain-text line (no embed).
        if ("lines".equals(component.getTagName())) {
            List<Element> lineElements = directChildElements(component);
            if (!lineElements.isEmpty()) {
                List<String> parts = new ArrayList<>();
                for (Element line : lineElements) {
                    parts.add(normalizedText(line));
                }
                return String.join("\n", parts);
            }
            return normalizedText(component);
        }

        List<Element> children = directChildElements(component);
        String attrs = renderAttributes(component, true);

        if (children.isEmpty() && attrs.isBlank()) {
            return normalizedTextDirect(component);
        }

        return renderElement(component, 0);
    }

    private String renderElement(Element element, int indent) {
        String indentation = "  ".repeat(Math.max(0, indent));
        String attrs = renderAttributes(element);

        List<Element> childElements = directChildElements(element);
        String text = normalizedTextDirect(element);

        if (childElements.isEmpty()) {
            return indentation + "<" + element.getTagName() + attrs + ">" + text + "</" + element.getTagName() + ">";
        }

        List<String> lines = new ArrayList<>();
        lines.add(indentation + "<" + element.getTagName() + attrs + ">");

        for (Element child : childElements) {
            lines.add(renderElement(child, indent + 1));
        }

        lines.add(indentation + "</" + element.getTagName() + ">");
        return String.join("\n", lines);
    }

    private static String renderAttributes(Element element) {
        return renderAttributes(element, false);
    }

    private static String renderAttributes(Element element, boolean skipId) {
        var attributes = element.getAttributes();
        if (attributes == null || attributes.getLength() == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            if (attribute == null) {
                continue;
            }

            String key = attribute.getNodeName();
            if (skipId && "id".equals(key)) {
                continue;
            }

            String value = attribute.getNodeValue();
            builder.append(" ").append(key).append("='").append(value == null ? "" : value).append("'");
        }
        return builder.toString();
    }

    private static List<Element> directChildElements(Element parent) {
        List<Element> result = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element element) {
                result.add(element);
            }
        }
        return result;
    }

    private static String normalizedText(Element element) {
        String text = element.getTextContent();
        return text == null ? "" : text.strip();
    }

    private static String normalizedTextDirect(Element element) {
        StringBuilder builder = new StringBuilder();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
                String value = node.getTextContent();
                if (value != null) {
                    builder.append(value);
                }
            }
        }
        return builder.toString().strip();
    }

    private void ensureFileExists(Path destination, String resourceName) {
        try {
            Files.createDirectories(destination.getParent());
            if (Files.exists(destination)) {
                return;
            }

            try (InputStream resourceIn = openResource(resourceName)) {
                if (resourceIn == null) {
                    Files.createFile(destination);
                    return;
                }
                Files.copy(resourceIn, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot prepare XML locale file '" + destination + "'", exception);
        }
    }

    private InputStream openResource(String fileName) {
        String resourcePath = "/" + fileName;
        Object raw = this.addon.getRaw();

        if (raw != null) {
            InputStream stream = raw.getClass().getResourceAsStream(resourcePath);
            if (stream != null) {
                return stream;
            }
        }

        InputStream addonStream = this.addon.getClass().getResourceAsStream(resourcePath);
        if (addonStream != null) {
            return addonStream;
        }

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        return contextClassLoader == null ? null : contextClassLoader.getResourceAsStream(fileName);
    }
}

