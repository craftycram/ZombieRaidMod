package net.craftycram.zombiemod;

import com.mojang.realmsclient.dto.PlayerInfo;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.stats.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.raid.RaidManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omg.PortableServer.ImplicitActivationPolicyOperations;

import java.util.stream.Collectors;

        @Mod("zombiemod")
        public class ZombieMod {

            // Directly reference a log4j logger.
            private static final Logger LOGGER = LogManager.getLogger();

            public ZombieMod() {
                // Register the setup method for modloading
                FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
                // Register the enqueueIMC method for modloading
                FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
                // Register the processIMC method for modloading
                FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
                // Register the doClientStuff method for modloading
                FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

                // Register ourselves for server and other game events we are interested in
                MinecraftForge.EVENT_BUS.register(this);
            }

            private void setup(final FMLCommonSetupEvent event)
            {
                // some preinit code
                LOGGER.info("HELLO FROM PREINIT");
            }

            private void doClientStuff(final FMLClientSetupEvent event) {
                // do something that can only be done on the client
                // LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
            }

            private void enqueueIMC(final InterModEnqueueEvent event)
            {
                // some example code to dispatch IMC to another mod
                InterModComms.sendTo("zombiemod", "helloworld", () -> { LOGGER.info("Hello world from ZombieMod"); return "Hello world";});
            }

            private void processIMC(final InterModProcessEvent event)
            {
                // some example code to receive and process InterModComms from other mods
                LOGGER.info("Got IMC {}", event.getIMCStream().
                        map(m->m.getMessageSupplier().get()).
                        collect(Collectors.toList()));
            }
            // You can use SubscribeEvent and let the Event Bus discover methods to call
            @SubscribeEvent
            public void onServerStarting(FMLServerStartingEvent event) {
                // do something when the server starts
                LOGGER.info("HELLO from server starting");
            }

            // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
            // Event bus for receiving Registry Events)
            @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
            public static class RegistryEvents {
                @SubscribeEvent
                public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
                    // register a new block here
                    LOGGER.info("HELLO from Register Block");
                }
            }

            @SubscribeEvent
            public void playerSleepInBed(PlayerSleepInBedEvent event) {
                double rand = Math.random();
                if (rand < 0.2) {
                    // Hier spawnen
                    BlockPos playerPos = Minecraft.getInstance().player.getPosition();
                    World world = event.getPlayer().world;
                    ItemEntity item = new ItemEntity(world, playerPos.getX(), playerPos.getY(), playerPos.getZ(), new ItemStack(Items.STONE));
                    ZombieEntity mob = new ZombieEntity(world);
                    mob.setPosition(playerPos.getX(), playerPos.getY(),playerPos.getZ());
                    world.addEntity(mob);
                    System.out.println("test");
                }

    }
}
