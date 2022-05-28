package mod.chiselsandbits.data.advancement;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.registry.ModItems;
import mod.chiselsandbits.registry.ModTags;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChiselsAndBitsAdvancementGenerator extends AbstractAdvancementGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event) {
        event.getGenerator().addProvider(new ChiselsAndBitsAdvancementGenerator(event.getGenerator()));
    }

    private ChiselsAndBitsAdvancementGenerator(final DataGenerator generator) {
        super(generator, ChiselsAndBitsAdvancementGenerator::build);
    }

    private static void build(Consumer<Advancement> register) {
        Advancement root = Advancement.Builder.advancement()
                .display(ModItems.ITEM_CHISEL_DIAMOND.get(),
                        new TranslatableComponent("mod.chiselsandbits.advancements.root.title"),
                        new TranslatableComponent("mod.chiselsandbits.advancements.root.description"),
                        new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"),
                        FrameType.CHALLENGE,
                        true,
                        true,
                        true)
                .addCriterion("chisel", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemPredicate(
                        ModTags.Items.CHISEL,
                        null,
                        MinMaxBounds.Ints.ANY,
                        MinMaxBounds.Ints.ANY,
                        new EnchantmentPredicate[0],
                        new EnchantmentPredicate[0],
                        null,
                        NbtPredicate.ANY
                )))
                .save(register, Constants.MOD_ID + ":chiselsandbits/root");

        Advancement findChiselables = Advancement.Builder.advancement()
                .parent(root)
                .display(ModItems.ITEM_MAGNIFYING_GLASS.get(),
                        new TranslatableComponent("mod.chiselsandbits.advancements.find-chiselables.title"),
                        new TranslatableComponent("mod.chiselsandbits.advancements.find-chiselables.description"),
                        new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"),
                        FrameType.TASK,
                        true,
                        true,
                        true)
                .addCriterion("magnifier_glass", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ITEM_MAGNIFYING_GLASS.get()))
                .save(register, Constants.MOD_ID + ":chiselsandbits/find_chiselables");

        Advancement collectBits = Advancement.Builder.advancement()
                .parent(root)
                .display(ModItems.ITEM_BIT_BAG_DEFAULT.get(),
                        new TranslatableComponent("mod.chiselsandbits.advancements.collect-bits.title"),
                        new TranslatableComponent("mod.chiselsandbits.advancements.collect-bits.description"),
                        new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"),
                        FrameType.TASK,
                        true,
                        true,
                        true)
                .addCriterion("bit_bag", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemPredicate(
                        ModTags.Items.BIT_BAG,
                        null,
                        MinMaxBounds.Ints.ANY,
                        MinMaxBounds.Ints.ANY,
                        new EnchantmentPredicate[0],
                        new EnchantmentPredicate[0],
                        null,
                        NbtPredicate.ANY
                )))
                .save(register, Constants.MOD_ID + ":chiselsandbits/collect_bits");

        Advancement makeTank = Advancement.Builder.advancement()
                .parent(root)
                .display(ModBlocks.BIT_STORAGE_BLOCK_ITEM.get(),
                        new TranslatableComponent("mod.chiselsandbits.advancements.make-tank.title"),
                        new TranslatableComponent("mod.chiselsandbits.advancements.make-tank.description"),
                        new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"),
                        FrameType.TASK,
                        true,
                        true,
                        true)
                .addCriterion("bit_tank", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.BIT_STORAGE_BLOCK_ITEM.get()))
                .save(register, Constants.MOD_ID + ":chiselsandbits/make_tank");
    }

    @Override
    public String getName()
    {
        return "Chisels and bits default advancement generator";
    }
}
