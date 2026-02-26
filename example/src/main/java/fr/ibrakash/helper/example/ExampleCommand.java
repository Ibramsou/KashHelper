package fr.ibrakash.helper.example;

import fr.ibrakash.helper.example.gui.ExampleGui;
import fr.ibrakash.helper.example.gui.ExamplePagedGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ExampleCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            String argument = args.length == 0 ? "" : args[0].toLowerCase();
            switch (argument) {
                case "gui" -> new ExampleGui().open(player);
                case "paged" -> new ExamplePagedGui().open(player);
            }
        }
        return false;
    }
}
