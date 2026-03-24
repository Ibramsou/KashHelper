package fr.ibrakash.helper.paper.configuration.objects.display;

import fr.ibrakash.helper.configuration.objects.ConfigMessage;
import fr.ibrakash.helper.paper.text.PaperTextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Arrays;
import java.util.Optional;

@ConfigSerializable
public class PaperConfigMessage extends ConfigMessage<Component, CommandSender> {

    private Optional<ConfigSound> sound = Optional.empty();
    private Optional<ConfigTitle> title = Optional.empty();
    private Optional<String> actionBar = Optional.empty();

    public PaperConfigMessage() {
        super();
    }

    public PaperConfigMessage(String message) {
        super(message);
    }

    public PaperConfigMessage sound(ConfigSound sound) {
        this.sound = Optional.ofNullable(sound);
        return this;
    }

    public PaperConfigMessage title(ConfigTitle title) {
        this.title = Optional.ofNullable(title);
        return this;
    }

    public PaperConfigMessage actionBar(String actionBar) {
        this.actionBar = Optional.ofNullable(actionBar);
        return this;
    }

    @Override
    protected void sendSerialized(CommandSender audience, Component serialized, Object... replacers) {
        audience.sendMessage(serialized);

        if (audience instanceof Player player) {
            this.sendTitle(player, replacers);
            this.sendActionBar(player, replacers);
            this.playSound(player);
        }
    }

    @Override
    protected void broadcastSerialized(Component serialized, Object... replacers) {
        Bukkit.broadcast(serialized);

        if (this.actionBar.isEmpty() && this.title.isEmpty() && this.sound.isEmpty()) {
            return;
        }

        Bukkit.getOnlinePlayers().forEach(player -> {
            this.sendActionBar(player, replacers);
            this.playSound(player);
            this.sendTitle(player, replacers);
        });
    }

    private void sendActionBar(Player player, Object... replacers) {
        this.actionBar.ifPresent(string ->
                player.sendActionBar(PaperTextUtil.get().replacedComponent(string, Arrays.asList(replacers))));
    }

    private void playSound(Player player) {
        this.sound.ifPresent(sound -> sound.play(player));
    }

    private void sendTitle(Player player, Object... replacers) {
        this.title.ifPresent(title -> title.send(player, replacers));
    }

    @Override
    public Component serialized(Object... replacers) {
        return PaperTextUtil.get().replacedComponent(this.getMessage(), Arrays.asList(replacers));
    }

    public ConfigSound getSound() {
        return this.sound.orElse(null);
    }

    public ConfigTitle getTitle() {
        return this.title.orElse(null);
    }

    public String getActionBar() {
        return this.actionBar.orElse(null);
    }
}