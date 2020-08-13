package net.craftycram.zombiemod;

import com.mojang.realmsclient.dto.PlayerInfo;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.stats.Stats;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.raid.RaidManager;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
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

    /*
    * TODOs
    * - Bett zerstört bei Hard
    * - Molotov
    *
    * */

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

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        // LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("zombiemod", "helloworld", () -> {
            LOGGER.info("Hello world from ZombieMod");
            return "Hello world";
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.getMessageSupplier().get()).
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
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }

    public static int ticks;
    public int timingState = 0;
    public int[] timings = {10, 20, 30, 40};
    public int eventStartTick = ticks;
    public BlockPos playerPos = null;
    public World world = null;
    public boolean spawnLightnings = false;
    public PlayerEntity player = null;
    public BlockPos bedPos = null;
    @SubscribeEvent
    public void tickEvent (TickEvent.ClientTickEvent event) {
        if (ticks > timings[timingState] + eventStartTick && spawnLightnings) {
            double posX = playerPos.getX() + (Math.random() * 10);
            // double posY = playerPos.getY() + (Math.random() * 1);
            double posY = playerPos.getY();
            double posZ = playerPos.getZ() + (Math.random() * 10);
            LightningBoltEntity lighting = new LightningBoltEntity(world, posX, posY, posZ, true);
            world.getServer().getWorld(world.getDimension().getType()).addLightningBolt(lighting);
            //world.addEntity(lighting);
            timingState++;
        }
        if (timingState > timings.length - 1) {
            player.addPotionEffect(new EffectInstance(Effects.GLOWING, 600, 100, false, false));
            player.setPositionAndRotation(playerPos.getX(), playerPos.getY() + 5, playerPos.getZ(), -130, 0);
            timingState = 0;
        }
        if (ticks > eventStartTick + 100 && spawnLightnings) {
            world.destroyBlock(bedPos, true);
            Minecraft.getInstance().gameSettings.thirdPersonView = 0;
            player.setPositionAndRotation(playerPos.getX(), playerPos.getY(), playerPos.getZ(), player.getRotationYawHead(), 0);
            spawnLightnings = false;
        }
        ticks++;
    }

    @SubscribeEvent
    public void playerSleepInBed(PlayerSleepInBedEvent event) {
        eventStartTick = ticks;
        bedPos = event.getPos();
        player = event.getPlayer();
        playerPos = Minecraft.getInstance().player.getPosition();
        world = event.getPlayer().world;
        if (world.isNightTime()) {
            double rand = Math.random();
            if (rand < 0.5) {
                spawnLightnings = true;
                world.playSound(null, playerPos.getX(), playerPos.getY(), playerPos.getZ(), SoundEvents.AMBIENT_CAVE, SoundCategory.MASTER, 100, 0);
                world.playSound(null, playerPos.getX(), playerPos.getY(), playerPos.getZ(), SoundEvents.AMBIENT_CAVE, SoundCategory.MASTER, 100, 0);
                world.playSound(null, playerPos.getX(), playerPos.getY(), playerPos.getZ(), SoundEvents.EVENT_RAID_HORN, SoundCategory.MASTER, 80, 0);
                //player.playSound(SoundEvents.AMBIENT_CAVE, 100, 0);
                //player.playSound(SoundEvents.AMBIENT_CAVE, 100, 0);
                //player.playSound(SoundEvents.EVENT_RAID_HORN, 80, 0);
                Minecraft.getInstance().gameSettings.thirdPersonView = 2;
                //player.setPositionAndRotation(playerPos.getX(), playerPos.getY() + 5, playerPos.getZ(), player.getRotationYawHead(), 90);
                ItemEntity item = new ItemEntity(world, playerPos.getX(), playerPos.getY(), playerPos.getZ(), new ItemStack(Items.STONE));
                ZombieEntity mob = new ZombieEntity(world);
                mob.setPosition(playerPos.getX(), playerPos.getY(), playerPos.getZ());
                //world.addEntity(mob);
                world.addEntity(item);
                System.out.println("test");
            }
        }

    }
}
