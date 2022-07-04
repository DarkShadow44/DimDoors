package org.dimdev.dimdoors.command;

import org.dimdev.dimdoors.pockets.PocketTemplate;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;

public class WorldeditHelper {
	static int load(ServerCommandSource source, PocketTemplate template) throws CommandSyntaxException {
		return Command.SINGLE_SUCCESS;
	}

}
