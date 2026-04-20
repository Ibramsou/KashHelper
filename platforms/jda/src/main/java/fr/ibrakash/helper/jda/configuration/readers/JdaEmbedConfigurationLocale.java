package fr.ibrakash.helper.jda.configuration.readers;

import fr.ibrakash.helper.configuration.readers.XmlLocaleReader;
import fr.ibrakash.helper.jda.configuration.objects.display.JdaConfigMessage;
import fr.ibrakash.helper.platform.KashAddon;

import java.util.List;

public abstract class JdaEmbedConfigurationLocale extends JdaConfigurationLocale {

    private final XmlLocaleReader xmlLocaleReader;

    protected JdaEmbedConfigurationLocale(KashAddon<?> addon) {
        super(addon);
        this.xmlLocaleReader = new XmlLocaleReader(addon, this.key());
        this.xmlLocaleReader.reload();
    }

    @Override
    protected boolean autoLoad() {
        return false;
    }

    @Override
    public JdaConfigMessage message(String path) {
        String value = this.xmlLocaleReader.resolve(path);
        return value == null ? null : new JdaConfigMessage(value);
    }

    @Override
    public List<String> lines(String path) {
        return this.xmlLocaleReader.lines(path);
    }

    @Override
    public void reload() {
        this.xmlLocaleReader.reload();
    }
}
