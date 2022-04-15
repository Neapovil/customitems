package com.github.neapovil.customitems.command;

import com.github.neapovil.customitems.CustomItems;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;

public final class GetCommand
{
    private static final CustomItems plugin = CustomItems.getInstance();

    public static final void register()
    {
        new CommandAPICommand("customitems")
                .withPermission(CustomItems.USER_COMMAND_PERMISSION)
                .withArguments(new LiteralArgument("get"))
                .withArguments(new StringArgument("itemName").replaceSuggestions(ArgumentSuggestions.strings(info -> plugin.getCustomItems())))
                .executesPlayer((player, args) -> {
                    final String itemname = (String) args[0];
                    final String base64 = plugin.getFileConfig().get("customitems." + itemname);

                    if (base64 == null)
                    {
                        throw CommandAPI.fail("This item doesn't exist");
                    }

                    if (player.getInventory().firstEmpty() == -1)
                    {
                        throw CommandAPI.fail("Your inventory is full");
                    }

                    player.getInventory().addItem(plugin.deserialize(base64));

                    player.sendMessage("The item " + itemname + " is in your inventory");
                })
                .register();
    }
}
