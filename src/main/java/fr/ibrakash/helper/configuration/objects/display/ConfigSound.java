package fr.ibrakash.helper.configuration.objects.display;

import fr.ibrakash.helper.utils.LegacyUtil;
import fr.ibrakash.helper.utils.Tuple;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ConfigSound {

    private String name = "block.note_block.pling";
    private float volume = 1.0F;
    private float pitch = 1.0F;

    private transient boolean dataLoaded;
    private transient SoundCategory category;
    private transient Sound loadedSound;

    public ConfigSound() {}

    public ConfigSound(String name, float volume, float pitch) {
        this.name = name;
        this.volume = volume;
        this.pitch = pitch;
    }

    public ConfigSound(String name) {
        this(name, 1F, 1F);
    }

    private void loadData() {
        if (this.dataLoaded) return;
        this.dataLoaded = true;
        Tuple<Sound, SoundCategory> tuple = LegacyUtil.deserializeSound(this.name);
        this.loadedSound = tuple.key();
        this.category = tuple.value();
    }

    public void play(Player player) {
        this.loadData();

        if (this.loadedSound == null) return;

        if (this.category == null) {
            player.playSound(player, this.loadedSound, this.volume, this.pitch);
        } else {
            player.playSound(player, this.loadedSound, this.category, this.volume, this.pitch);
        }
    }

    public void broadcast(Location location) {
        this.loadData();

        if (this.loadedSound == null) return;

        if (this.category == null) {
            location.getWorld().playSound(location, this.loadedSound, this.volume, this.pitch);
        } else {
            location.getWorld().playSound(location, this.loadedSound, this.category, this.volume, this.pitch);
        }
    }
}
