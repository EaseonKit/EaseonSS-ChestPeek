package com.easeon.ss.chestpeek;

import com.easeon.ss.core.wrapper.EaseonPlayer;
import com.easeon.ss.core.wrapper.EaseonWorld;
import net.minecraft.block.*;
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
    public static ActionResult useEntityCallback(ServerPlayerEntity mcPlayer, World mcWorld, Entity entity, Hand hand) {
        var world = new EaseonWorld(mcWorld);
        if (world.isClient()) return ActionResult.SUCCESS;

        var player = new EaseonPlayer(mcPlayer);
        if (hand != Hand.MAIN_HAND || player.isSneaking() || !(entity instanceof ItemFrameEntity itemFrame))
            return ActionResult.PASS;

        if (itemFrame.getHeldItemStack().isEmpty() && !player.getStackInHand(hand).isEmpty())
            return ActionResult.PASS;

        var behind = itemFrame.getBlockPos().offset(itemFrame.getHorizontalFacing().getOpposite());
        var state = world.getBlockState(behind);

        if (state.of(ChestBlock.class, EnderChestBlock.class)) {
            var facing = itemFrame.getHorizontalFacing().getOpposite();
            return OpenChest(state.get(), world.get(), player.get(), Vec3d.ofCenter(behind), facing, behind);
        }

        return OpenBarrel(world.get(), player.get(), behind);
    }

    public static ActionResult useBlockCallback(ServerPlayerEntity mcPlayer, World mcWorld, Hand hand, BlockHitResult hitResult) {
        var world = new EaseonWorld(mcWorld);
        if (world.isClient()) return ActionResult.SUCCESS;

        var player = new EaseonPlayer(mcPlayer);
        if (player.isSneaking()) return ActionResult.PASS;

        var pos = hitResult.getBlockPos();
        var state = world.getBlockState(pos);
        if (state.not(WallSignBlock.class))
            return ActionResult.PASS;

        var facing = state.get(WallSignBlock.FACING);
        var behind = pos.offset(facing.getOpposite());
        var blockState = world.getBlockState(behind);

        if (blockState.of(ChestBlock.class, EnderChestBlock.class)) {
            return OpenChest(blockState.get(), world.get(), player.get(), Vec3d.ofCenter(behind), facing, behind);
        }

        return OpenBarrel(world.get(), player.get(), behind);
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