package fr.ibrakash.helper.paper.configuration.readers;

import fr.ibrakash.helper.configuration.readers.LocaleConfigurationReader;
import fr.ibrakash.helper.paper.configuration.objects.display.PaperConfigMessage;
import fr.ibrakash.helper.platform.KashAddon;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public abstract class PaperConfigurationLocale extends LocaleConfigurationReader<Component, CommandSender, PaperConfigMessage> {
    protected PaperConfigurationLocale(KashAddon<?> addon) {
        super(addon);
    }

    @Override
    protected Class<PaperConfigMessage> messageType() {
        return PaperConfigMessage.class;
    }

    @Override
    protected PaperConfigMessage fromString(String value) {
        return new PaperConfigMessage(value);
    }
}
