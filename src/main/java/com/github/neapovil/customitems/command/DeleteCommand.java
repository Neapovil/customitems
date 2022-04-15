package com.github.neapovil.customitems.command;

import com.github.neapovil.customitems.CustomItems;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;

public final class DeleteCommand
{
    private static final CustomItems plugin = CustomItems.getInstance();

    public static final void register()
    {
        new CommandAPICommand("customitems")
                .withPermission(CustomItems.ADMIN_COMMAND_PERMISSION)
                .withArguments(new LiteralArgument("delete"))
                .withArguments(new StringArgument("itemName").replaceSuggestions(ArgumentSuggestions.strings(info -> plugin.getCustomItems())))
                .executesPlayer((player, args) -> {
                    final String itemname = (String) args[0];
                    final String path = "customitems." + itemname;

                    if (plugin.getFileConfig().get(path) == null)
                    {
                        throw CommandAPI.fail("This item doesn't exist");
                    }

                    plugin.getFileConfig().remove(path);

                    player.sendMessage("Custom item removed");
                })
                .register();
    }
}
