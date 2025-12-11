package com.easeon.ss.chestpeek;

import com.easeon.ss.core.wrapper.EaseonBlockHit;
import com.easeon.ss.core.wrapper.EaseonEntity;
import com.easeon.ss.core.wrapper.EaseonPlayer;
import com.easeon.ss.core.wrapper.EaseonWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ChestPeekHandler {
    public static InteractionResult useEntityCallback(EaseonWorld world, EaseonPlayer player, InteractionHand hand, EaseonEntity entity) {
        if (world.isClient()) return InteractionResult.SUCCESS;
        if (hand != InteractionHand.MAIN_HAND || player.isSneaking() || !(entity.get() instanceof ItemFrame itemFrame))
            return InteractionResult.PASS;

        if (itemFrame.getItem().isEmpty() && !player.getStackInHand(hand).isEmpty())
            return InteractionResult.PASS;

        final var facing = itemFrame.getDirection().getOpposite();
        final var behind = itemFrame.getPos().relative(itemFrame.getDirection().getOpposite());

        return OpenChest(world, player, hand, facing, behind);
    }

    public static InteractionResult useBlockCallback(EaseonWorld world, EaseonPlayer player, InteractionHand hand, EaseonBlockHit hit) {
        if (world.isClient()) return InteractionResult.SUCCESS;
        if (player.isSneaking()) return InteractionResult.PASS;

        final var pos = hit.getBlockPos();
        final var state = world.getBlockState(pos);
        if (state.not(WallSignBlock.class))
            return InteractionResult.PASS;

        final var facing = state.get(WallSignBlock.FACING);
        final var behind = pos.relative(facing.getOpposite());

        return OpenChest(world, player, hand, facing, behind);
    }

    private static InteractionResult OpenChest(EaseonWorld world, EaseonPlayer player, InteractionHand hand, Direction direction, BlockPos behind)
    {
        final var block = world.getBlockState(behind);
        if (block.of(ChestBlock.class, EnderChestBlock.class)) {
            var interactionResult = block.onUse(world, player, new BlockHitResult(Vec3.atCenterOf(behind), direction, behind, false));
            if (interactionResult.consumesAction()) {
                player.swingHand(hand);
                return InteractionResult.SUCCESS;
            }
        }

        final var blockEntity = world.getBlockEntity(behind);
        if (blockEntity instanceof RandomizableContainerBlockEntity chest) {
            player.openHandledScreen(chest);
            player.swingHand(hand);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}