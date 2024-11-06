package com.example;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import java.util.Random;
import java.lang.String;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class RandintMod implements ModInitializer {
	// randintコマンド
	public static final String MOD_ID = "randint-mod";
	private final Random random = new Random();

	// checkchestコマンド
	private static final int MAX_VERTICAL_RANGE = 30;
	private static final int MAX_RADIUS = 20;


	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("randint")
					.then(CommandManager.argument("min", IntegerArgumentType.integer())
							.then(CommandManager.argument("max", IntegerArgumentType.integer())
									.executes(this::executeRandInt))));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("checkchest")
					.requires(source -> source.hasPermissionLevel(2)) // オプション: コマンド実行権限
					.then(CommandManager.argument("radius", IntegerArgumentType.integer(1, MAX_RADIUS)) // 半径 (1〜20)
							.then(CommandManager.argument("verticalRange", IntegerArgumentType.integer(1, MAX_VERTICAL_RANGE)) // 垂直範囲 (1〜30)
									.executes(context -> {
										int radius = IntegerArgumentType.getInteger(context, "radius");
										int verticalRange = IntegerArgumentType.getInteger(context, "verticalRange");
										ServerPlayerEntity player = context.getSource().getPlayer();
										if (player != null) {
											boolean chestFound = scanForChests(player.getWorld(), player.getBlockPos(), radius, verticalRange);
											if (chestFound) {
												player.sendMessage(Text.literal("チェストが範囲内に見つかりました！"), false);
											} else {
												player.sendMessage(Text.literal("範囲内にチェストはありません。"), false);
											}
										}
										return 1;
									})
							)
					)
			);
		});
		
		
	}
	private int executeRandInt(CommandContext<ServerCommandSource> context) {
		int min = IntegerArgumentType.getInteger(context, "最小値");
		int max = IntegerArgumentType.getInteger(context, "最大値");

		if (min > max) {
			context.getSource().sendFeedback(() -> Text.literal("エラー: 最小値は最大値以下である必要があります。"), false);
			return 0;
		}

		int randomValue = min + random.nextInt((max - min) + 1);
		context.getSource().sendFeedback(() -> Text.literal("乱数: " + randomValue), false);

		return 1;
	}

	private boolean scanForChests(World world, BlockPos centerPos, int radius, int verticalRange) {
		// スキャン範囲を指定
		int minX = centerPos.getX() - radius;
		int maxX = centerPos.getX() + radius;
		int minY = centerPos.getY() - verticalRange;
		int maxY = centerPos.getY() + verticalRange;
		int minZ = centerPos.getZ() - radius;
		int maxZ = centerPos.getZ() + radius;

		// 指定範囲内のすべてのブロックを走査
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					BlockEntity blockEntity = world.getBlockEntity(pos);
					if (blockEntity instanceof ChestBlockEntity) {
						return true; // チェストが見つかった場合
					}
				}
			}
		}
		return false; // チェストが見つからなかった場合
	}


}