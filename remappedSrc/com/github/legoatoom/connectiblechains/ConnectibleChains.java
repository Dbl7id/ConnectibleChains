/*
 * Copyright (C) 2021 legoatoom
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.legoatoom.connectiblechains;


import com.github.legoatoom.connectiblechains.config.ModConfig;
import com.github.legoatoom.connectiblechains.enitity.ChainKnotEntity;
import com.github.legoatoom.connectiblechains.enitity.ModEntityTypes;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

/**
 * Mod Initializer for Connectible chains.
 */
public class ConnectibleChains implements ModInitializer {

    public static final String MODID = "connectiblechains";
    public static ModConfig config;

    @Override
    public void onInitialize() {

        ModEntityTypes.init();
        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player == null) return ActionResult.PASS;
            ItemStack stack = player.getStackInHand(hand);
            BlockPos blockPos = hitResult.getBlockPos();
            Block block = world.getBlockState(blockPos).getBlock();
            if (stack.getItem() == Items.CHAIN) {
                if (ChainKnotEntity.canConnectTo(block) && !player.isSneaking()) {
                    if (!world.isClient) {
                        ChainKnotEntity knot = ChainKnotEntity.getOrCreate(world, blockPos, false);
                        if (!ChainKnotEntity.tryAttachHeldChainsToBlock(player, world, blockPos, knot)) {
                            // If this didn't work connect the player to the new chain instead.
                            assert knot != null; // This can never happen as long as getOrCreate has false as parameter.
                            if (knot.getHoldingEntities().contains(player)) {
                                knot.detachChain(player, true, false);
                                knot.onBreak(null);
                                if (!player.isCreative())
                                    stack.increment(1);
                            } else {
                                knot.attachChain(player, true, 0);
                                knot.onPlace();
                                if (!player.isCreative())
                                    stack.decrement(1);
                            }
                        }
                    }
                    return ActionResult.success(world.isClient);
                }
            }
            if (block.isIn(BlockTags.FENCES) || block.isIn(BlockTags.WALLS)) {
                if (world.isClient) {
                    ItemStack itemStack = player.getStackInHand(hand);
                    return itemStack.getItem() == Items.CHAIN ? ActionResult.SUCCESS : ActionResult.PASS;
                } else {
                    return ChainKnotEntity.tryAttachHeldChainsToBlock(player, world, blockPos, ChainKnotEntity.getOrCreate(world, blockPos, true)) ? ActionResult.SUCCESS : ActionResult.PASS;
                }
            }
            return ActionResult.PASS;
        });
    }

}