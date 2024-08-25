package org.pipeman.pipo.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.pipeman.pipo.PotatoManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

import static net.minecraft.block.Block.dropStack;
import static net.minecraft.block.Block.getDroppedStacks;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Redirect(method = "afterBreak",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V")
    )
    private void afterGetDroppedStacks(BlockState state, World world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool) {
        if (world instanceof ServerWorld) {
            List<ItemStack> stacks = getDroppedStacks(state, (ServerWorld) world, pos, blockEntity, entity, tool);
            stacks.forEach(stack -> dropStack(world, pos, stack));
            state.onStacksDropped((ServerWorld) world, pos, tool, true);

            if (state.isOf(Blocks.POTATOES) && entity instanceof ServerPlayerEntity player) {
                int sum = stacks.stream()
                        .filter(stack -> stack.isOf(Items.POTATO))
                        .mapToInt(ItemStack::getCount)
                        .sum();

                PotatoManager.storePotatoes(player.getGameProfile().getId(), sum - 1);
            }
        }
    }
}
