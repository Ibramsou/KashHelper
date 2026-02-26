package fr.ibrakash.helper.configuration.objects.display;

import fr.ibrakash.helper.utils.TextUtil;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ConfigTitle {

    private String title;
    private String subtitle;
    private int fadeInTicks = 10;
    private int stayTicks = 70;
    private int fadeOutTicks = 20;

    public ConfigTitle() {
        this("Not configured", "Not configured");
    }

    public ConfigTitle(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public ConfigTitle fadeInTicks(int fadeInTicks) {
        this.fadeInTicks = fadeInTicks;
        return this;
    }

    public ConfigTitle stayTicks(int stayTicks) {
        this.stayTicks = stayTicks;
        return this;
    }

    public ConfigTitle fadeOutTicks(int fadeOutTicks) {
        this.fadeOutTicks = fadeOutTicks;
        return this;
    }

    public void send(Player player, Object... replacers) {
        Title title = Title.title(
                TextUtil.replacedComponent(this.title, replacers),
                TextUtil.replacedComponent(this.subtitle, replacers),
                Title.Times.times(Ticks.duration(fadeInTicks), Ticks.duration(stayTicks), Ticks.duration(fadeOutTicks)));
        player.showTitle(title);
    }
}
