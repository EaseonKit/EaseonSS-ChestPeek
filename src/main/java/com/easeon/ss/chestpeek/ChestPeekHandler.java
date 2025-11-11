package com.easeon.ss.chestpeek;

import com.easeon.ss.core.wrapper.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

public class ChestPeekHandler {
    public static ActionResult useEntityCallback(EaseonWorld world, EaseonPlayer player, Hand hand, EaseonEntity entity) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (hand != Hand.MAIN_HAND || player.isSneaking() || !(entity.get() instanceof ItemFrameEntity itemFrame))
            return ActionResult.PASS;

        if (itemFrame.getHeldItemStack().isEmpty() && !player.getStackInHand(hand).isEmpty())
            return ActionResult.PASS;

        final var facing = itemFrame.getHorizontalFacing().getOpposite();
        final var behind = itemFrame.getBlockPos().offset(itemFrame.getHorizontalFacing().getOpposite());

        return OpenChest(world, player, hand, facing, behind);
    }

    public static ActionResult useBlockCallback(EaseonWorld world, EaseonPlayer player, Hand hand, EaseonBlockHit hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (player.isSneaking()) return ActionResult.PASS;

        final var pos = hit.getBlockPos();
        final var state = world.getBlockState(pos);
        if (state.not(WallSignBlock.class))
            return ActionResult.PASS;

        final var facing = state.get(WallSignBlock.FACING);
        final var behind = pos.offset(facing.getOpposite());

        return OpenChest(world, player, hand, facing, behind);
    }

    private static ActionResult OpenChest(EaseonWorld world, EaseonPlayer player, Hand hand, Direction direction, BlockPos behind)
    {
        ActionResult result = ActionResult.PASS;

        var block = world.getBlockState(behind);
        if (block.of(ChestBlock.class, EnderChestBlock.class)) {
            final var actionResult = block.onUse(world, player, new BlockHitResult(Vec3d.ofCenter(behind), direction, behind, false));
            if (actionResult.isAccepted())
                result = ActionResult.SUCCESS;
        }

        final var blockEntity = world.getBlockEntity(behind);
        if (blockEntity instanceof LootableContainerBlockEntity chest) {
            player.openHandledScreen(chest);
            result = ActionResult.SUCCESS;
        }

        if (result == ActionResult.SUCCESS)
            player.swingHand(hand);

        return result;
    }
}