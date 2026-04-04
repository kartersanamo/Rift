package com.kartersanamo.rift.command;

import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.command.BaseCommand;
import com.kartersanamo.rift.api.command.CommandContext;
import com.kartersanamo.rift.api.config.MessagesUtil;
import com.kartersanamo.rift.api.util.PlaceholderUtil;

public class RiftCommand extends BaseCommand {

    public RiftCommand() {
        super("rift",
                "Main plugin entry point",
                "/rift <reload>",
                "rift.command"
        );
    }

    @Override
    protected boolean onExecute(CommandContext context) {
        context.getSender().sendMessage(ChatFormat.info(
                PlaceholderUtil.replace(
                        MessagesUtil.commandUsage,
                        "%usage%", this.getUsage())
        ));
        return true;
    }
}
