package com.easeon.ss.chestpeek;

import com.easeon.ss.core.api.common.base.BaseToggleModule;
import com.easeon.ss.core.api.definitions.enums.EventPhase;
import com.easeon.ss.core.api.events.EaseonBlockUse;
import com.easeon.ss.core.api.events.EaseonBlockUse.BlockUseTask;
import com.easeon.ss.core.api.events.EaseonEntityInteract;
import com.easeon.ss.core.api.events.EaseonEntityInteract.EntityInteractTask;
import net.fabricmc.api.ModInitializer;

public class Easeon extends BaseToggleModule implements ModInitializer {
    private BlockUseTask blockUseTask;
    private EntityInteractTask entityInteractTask;
    public static Easeon instance;

    public Easeon() {
        instance = this;
    }

    @Override
    public void onInitialize() {
        logger.info("Initialized!");
    }

    public void updateTask() {
        if (config.enabled && blockUseTask == null && entityInteractTask == null) {
            blockUseTask = EaseonBlockUse.register(EventPhase.BEFORE, ChestPeekHandler::useBlockCallback);
            entityInteractTask = EaseonEntityInteract.register(EventPhase.BEFORE, ChestPeekHandler::useEntityCallback);
        }
        if (!config.enabled && blockUseTask != null && entityInteractTask != null) {
            EaseonBlockUse.unregister(blockUseTask);
            EaseonEntityInteract.unregister(entityInteractTask);
            blockUseTask = null;
            entityInteractTask = null;
        }
    }
}