package com.easeon.ss.chestpeek;

import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ChestPeekHandler {
    // 아이템 프레임 뒤 상자 열기
    public static ActionResult useEntityCallback(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        if (!(entity instanceof ItemFrameEntity itemFrame)) return ActionResult.PASS;
        if (player.isSneaking()) return ActionResult.PASS;

        ItemStack stack = itemFrame.getHeldItemStack();
        if (stack.isEmpty()) return ActionResult.PASS;

        // 설정 확인
        if (!Easeon.CONFIG.isEnabled()) return ActionResult.PASS;

        if (world.isClient()) return ActionResult.SUCCESS;

        var behind = itemFrame.getBlockPos().offset(itemFrame.getHorizontalFacing().getOpposite());
        var blockState = world.getBlockState(behind);

        if (player instanceof ServerPlayerEntity serverPlayer) {
            var actionResult = blockState.onUse(world, serverPlayer,
                    new BlockHitResult(Vec3d.ofCenter(behind), itemFrame.getHorizontalFacing(), behind, false));

            if (actionResult.isAccepted()) {
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    // 벽 표지판 뒤 상자 열기
    public static ActionResult useBlockCallback(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        var pos = hitResult.getBlockPos();
        var state = world.getBlockState(pos);
        var block = state.getBlock();

        if (block instanceof WallSignBlock) {
            if (!player.isSneaking()) {
                if (!Easeon.CONFIG.isEnabled()) return ActionResult.PASS;
                if (world.isClient()) return ActionResult.SUCCESS;

                var facing = state.get(WallSignBlock.FACING);
                var behind = pos.offset(facing.getOpposite());
                var blockStateBehind = world.getBlockState(behind);
                var blockBehind = blockStateBehind.getBlock();

                // ChestBlock인 경우 (더블 체스트 지원)
                if (blockBehind instanceof ChestBlock) {
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        var actionResult = blockStateBehind.onUse(world, serverPlayer,
                                new BlockHitResult(Vec3d.ofCenter(behind), facing, behind, false));

                        if (actionResult.isAccepted()) {
                            return ActionResult.SUCCESS;
                        }
                    }
                }
                // 엔더 상자인 경우
                else if (blockBehind instanceof EnderChestBlock) {
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        EnderChestInventory enderChestInventory = serverPlayer.getEnderChestInventory();

                        serverPlayer.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                            (syncId, inventory, p) -> GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, enderChestInventory),
                            Text.translatable("container.enderchest")
                        ));

                        serverPlayer.incrementStat(Stats.OPEN_ENDERCHEST);
                        return ActionResult.SUCCESS;
                    }
                }
                // 다른 컨테이너들 (일반 상자, 배럴 등)
                else {
                    var blockEntityBehind = world.getBlockEntity(behind);
                    if (blockEntityBehind instanceof LootableContainerBlockEntity chest) {
                        if (player instanceof ServerPlayerEntity serverPlayer) {
                            serverPlayer.openHandledScreen(chest);
                            return ActionResult.SUCCESS;
                        }
                    }
                }
            }
        }

        return ActionResult.PASS;
    }
}