package com.example;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import java.util.Random;

public class RandintMod implements ModInitializer {
	public static final String MOD_ID = "randint-mod";
	private final Random random = new Random();


	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		// ワールドに入ったときにメッセージを表示
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("randint")
					.then(CommandManager.argument("min", IntegerArgumentType.integer())
							.then(CommandManager.argument("max", IntegerArgumentType.integer())
									.executes(this::executeRandInt))));
		});


		
		
	}
	private int executeRandInt(CommandContext<ServerCommandSource> context) {
		int min = IntegerArgumentType.getInteger(context, "min");
		int max = IntegerArgumentType.getInteger(context, "max");

		if (min > max) {
			context.getSource().sendFeedback(() -> Text.literal("エラー: minはmax以下である必要があります。"), false);
			return 0;
		}

		int randomValue = min + random.nextInt((max - min) + 1);
		context.getSource().sendFeedback(() -> Text.literal("乱数: " + randomValue), false);

		return 1;
	}


}