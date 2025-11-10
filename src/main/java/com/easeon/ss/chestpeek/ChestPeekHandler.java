package com.easeon.ss.chestpeek;

import com.easeon.ss.core.wrapper.EaseonBlockHit;
import com.easeon.ss.core.wrapper.EaseonEntity;
import com.easeon.ss.core.wrapper.EaseonPlayer;
import com.easeon.ss.core.wrapper.EaseonWorld;
import net.minecraft.block.*;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ChestPeekHandler {
    public static ActionResult useEntityCallback(EaseonWorld world, EaseonPlayer player, Hand hand, EaseonEntity entity) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (hand != Hand.MAIN_HAND || player.isSneaking() || !(entity.get() instanceof ItemFrameEntity itemFrame))
            return ActionResult.PASS;

        if (itemFrame.getHeldItemStack().isEmpty() && !player.getStackInHand(hand).isEmpty())
            return ActionResult.PASS;

        var facing = itemFrame.getHorizontalFacing().getOpposite();
        var behind = itemFrame.getBlockPos().offset(itemFrame.getHorizontalFacing().getOpposite());

        return OpenChest(world, player, hand, facing, behind);
    }

    public static ActionResult useBlockCallback(EaseonWorld world, EaseonPlayer player, Hand hand, EaseonBlockHit hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (player.isSneaking()) return ActionResult.PASS;

        var pos = hit.getBlockPos();
        var state = world.getBlockState(pos);
        if (state.not(WallSignBlock.class))
            return ActionResult.PASS;

        var facing = state.get(WallSignBlock.FACING);
        var behind = pos.offset(facing.getOpposite());

        return OpenChest(world, player, hand, facing, behind);
    }

    private static ActionResult OpenChest(EaseonWorld world, EaseonPlayer player, Hand hand, Direction direction, BlockPos behind)
    {
        ActionResult result = ActionResult.PASS;

        var block = world.getBlockState(behind);
        if (block.of(ChestBlock.class, EnderChestBlock.class)) {
            var actionResult = block.onUse(world, player, new BlockHitResult(Vec3d.ofCenter(behind), direction, behind, false));
            if (actionResult.isAccepted())
                result = ActionResult.SUCCESS;
        }

        var blockEntity = world.getBlockEntity(behind);
        if (blockEntity instanceof LootableContainerBlockEntity chest) {
            player.openHandledScreen(chest);
            result = ActionResult.SUCCESS;
        }

        if (result == ActionResult.SUCCESS)
            player.swingHand(hand);

        return result;
    }
}