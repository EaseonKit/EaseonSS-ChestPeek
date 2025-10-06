package com.easeon.ss.chestpeek;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Easeon implements ModInitializer {
    public static final String MOD_ID = "easeon-chestpeek";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ConfigManager CONFIG = new ConfigManager();

    @Override
    public void onInitialize() {
        LOGGER.info("Chest Peek Mod Initializing...");

        // Config 로드
        CONFIG.load();

        // 명령어 등록
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            EaseonCommand.register(dispatcher);
            LOGGER.info("Chest Peek commands registered!");
        });

        // 이벤트 등록
        UseEntityCallback.EVENT.register(ChestPeekHandler::useEntityCallback);
        UseBlockCallback.EVENT.register(ChestPeekHandler::useBlockCallback);

        LOGGER.info("Chest Peek Mod Initialized!");
    }
}