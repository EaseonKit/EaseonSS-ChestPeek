package com.easeon.ss.chestpeek;

import com.easeon.ss.core.helper.BlockHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ChestPeekHandler {
    public static ActionResult useEntityCallback(ServerPlayerEntity player, World world, Entity entity, Hand hand) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (hand != Hand.MAIN_HAND || player.isSneaking() || !(entity instanceof ItemFrameEntity itemFrame))
            return ActionResult.PASS;

        if (itemFrame.getHeldItemStack().isEmpty() && !player.getStackInHand(hand).isEmpty())
            return ActionResult.PASS;

        var behind = itemFrame.getBlockPos().offset(itemFrame.getHorizontalFacing().getOpposite());
        var state = world.getBlockState(behind);

        if (BlockHelper.of(state.getBlock(), ChestBlock.class, EnderChestBlock.class)) {
            var facing = itemFrame.getHorizontalFacing().getOpposite();
            return OpenChest(state, world, player, Vec3d.ofCenter(behind), facing, behind);
        }

        return OpenBarrel(world, player, behind);
    }

    public static ActionResult useBlockCallback(ServerPlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        var pos = hitResult.getBlockPos();
        var state = world.getBlockState(pos);
        var block = state.getBlock();

        if (world.isClient()) return ActionResult.SUCCESS;
        if (player.isSneaking() || BlockHelper.not(block, WallSignBlock.class))
            return ActionResult.PASS;

        var facing = state.get(WallSignBlock.FACING);
        var behind = pos.offset(facing.getOpposite());
        var blockState = world.getBlockState(behind);

        if (BlockHelper.of(blockState.getBlock(), ChestBlock.class, EnderChestBlock.class)) {
            return OpenChest(blockState, world, player, Vec3d.ofCenter(behind), facing, behind);
        }

        return OpenBarrel(world, player, behind);
    }

    private static ActionResult OpenChest(
            BlockState block,
            World world, PlayerEntity player,
            Vec3d vec3d, Direction direction, BlockPos behind)
    {
        var actionResult = block.onUse(world, player, new BlockHitResult(vec3d, direction, behind, false));
        if (actionResult.isAccepted()) {
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private static ActionResult OpenBarrel(World world, PlayerEntity player, BlockPos behind) {
        var blockEntity = world.getBlockEntity(behind);
        if (blockEntity instanceof LootableContainerBlockEntity chest) {
            player.openHandledScreen(chest);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}