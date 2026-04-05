package com.kartersanamo.rift.command;

import com.kartersanamo.rift.Rift;
import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.chat.ColorUtil;
import com.kartersanamo.rift.api.command.CommandContext;
import com.kartersanamo.rift.api.config.MessagesUtil;
import com.kartersanamo.rift.api.logging.AuditLogger;

public class ReloadCommand {

     public static boolean execute(CommandContext context) {
         Rift instance = Rift.getInstance();
         instance.reloadAll();

         AuditLogger.action(context.getSender(), "config.reload", "source=command");
         context.getSender().sendMessage(ChatFormat.success(ColorUtil.translate(MessagesUtil.configsReloaded)));
         return true;
     }
}

