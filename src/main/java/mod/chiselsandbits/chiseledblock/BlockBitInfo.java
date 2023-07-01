package mod.chiselsandbits.chiseledblock;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import mod.chiselsandbits.api.IgnoreBlockLogic;
import mod.chiselsandbits.chiseledblock.data.VoxelType;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.registry.ModTags;
import mod.chiselsandbits.render.helpers.ModelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;
import java.util.HashMap;

public class BlockBitInfo {
    // imc api...
    private static final HashMap<Block, Boolean> ignoreLogicBlocks = new HashMap<>();

    static {
        ignoreLogicBlocks.put(Blocks.ACACIA_LEAVES, true);
        ignoreLogicBlocks.put(Blocks.BIRCH_LEAVES, true);
        ignoreLogicBlocks.put(Blocks.DARK_OAK_LEAVES, true);
        ignoreLogicBlocks.put(Blocks.JUNGLE_LEAVES, true);
        ignoreLogicBlocks.put(Blocks.OAK_LEAVES, true);
        ignoreLogicBlocks.put(Blocks.SPRUCE_LEAVES, true);
        ignoreLogicBlocks.put(Blocks.SNOW, true);
    }

    // cache data..
    // 缓存数据
    private static final HashMap<Block, SupportsAnalysisResult> supportedBlocks = new HashMap<>();
    private static final HashMap<Block, Boolean> forcedBlocks = new HashMap<>();
    private static final HashMap<Block, Fluid> fluidBlocks = new HashMap<>();
    private static final IntObjectMap<Fluid> fluidStates = new IntObjectHashMap<>();
    private static final HashMap<BlockState, Integer> bitColor = new HashMap<>();

    public static int getColorFor(BlockState state, int tint) {

        Integer out = bitColor.get(state);

        if (out == null) {

            final Block block = state.getBlock();

            // TODO 液体颜色
            final Fluid fluid = BlockBitInfo.getFluidFromBlock(block);
			if (fluid != null) {
                out = IClientFluidTypeExtensions.of(fluid).getTintColor();
			}
            //else
            {
                final ItemStack target = ModUtil.getItemStackFromBlockState(state);

                if (ModUtil.isEmpty(target)) {
                    out = 0xffffff;
                } else {
                    out = ModelUtil.getItemStackColor(target, tint);
                }
            }

            bitColor.put(state, out);
        }

        return out;
    }

    public static void recalculate() {
        recalculateFluids();
    }

    public static void recalculateFluids() {
        fluidBlocks.clear();

        for (final Fluid o : ForgeRegistries.FLUIDS) {
            if (o.defaultFluidState().isSource())
                BlockBitInfo.addFluidBlock(o);
        }
    }

    public static void addFluidBlock(Fluid fluid) {
        fluidBlocks.put(fluid.defaultFluidState().createLegacyBlock().getBlock(), fluid);

        for (final BlockState state : fluid.defaultFluidState().createLegacyBlock().getBlock().getStateDefinition().getPossibleStates()) {
            try {
                fluidStates.put(ModUtil.getStateId(state), fluid);
            } catch (final Throwable t) {
                Log.logError("Error while determining fluid state.", t);
            }
        }
        supportedBlocks.clear();
    }

    static public Fluid getFluidFromBlock(final Block blk) {
        return fluidBlocks.get(blk);
    }

    public static VoxelType getTypeFromStateID(
            final int bit) {
        if (bit == 0) {
            return VoxelType.AIR;
        }

        return fluidStates.containsKey(bit) ? VoxelType.FLUID : VoxelType.SOLID;
    }

    public static void ignoreBlockLogic(
            final Block which) {
        ignoreLogicBlocks.put(which, true);
        reset();
    }

    public static void forceStateCompatibility(Block which, boolean forceStatus) {
        forcedBlocks.put(which, forceStatus);
        reset();
    }

    public static void reset() {
        supportedBlocks.clear();
    }

    /**
     * 判断某个方块状态是否可以被雕刻，以及其原因
     */
    @SuppressWarnings("deprecation")
    public static SupportsAnalysisResult doSupportAnalysis(final BlockState state) {
        final Block block = state.getBlock();
        // 本身就是雕刻方块
        if (block instanceof BlockChiseled) {
            return new SupportsAnalysisResult(
                    true,
                    LocalStrings.ChiselSupportGenericNotSupported,
                    LocalStrings.ChiselSupportIsAlreadyChiseled
            );
        }
        // 在强制禁用/启用列表内
        if (forcedBlocks.containsKey(block)) {
            final boolean forcing = forcedBlocks.get(block);
            return new SupportsAnalysisResult(
                    forcing,
                    LocalStrings.ChiselSupportForcedUnsupported,
                    LocalStrings.ChiselSupportForcedSupported
            );
        }

        // 一个缓存，避免每次都要进行下面的分析
        if (supportedBlocks.containsKey(block)) {
            return supportedBlocks.get(block);
        }

        if (state.is(ModTags.Blocks.BLOCKED_CHISELABLE)) {
            final SupportsAnalysisResult result = new SupportsAnalysisResult(
                    false,
                    LocalStrings.ChiselSupportTagBlackListed,
                    LocalStrings.ChiselSupportTagWhitelisted
            );
            supportedBlocks.put(block, result);
            return result;
        }

        if (state.is(ModTags.Blocks.FORCED_CHISELABLE)) {
            final SupportsAnalysisResult result = new SupportsAnalysisResult(
                    true,
                    LocalStrings.ChiselSupportTagBlackListed,
                    LocalStrings.ChiselSupportTagWhitelisted
            );
            supportedBlocks.put(block, result);

            return result;
        }

        try {
            // require basic hardness behavior...
//			final ReflectionHelperBlock pb = new ReflectionHelperBlock();
            final Class<? extends Block> blkClass = block.getClass();
            Method method;
//            // custom dropping behavior? 判断是否有自定义的掉落物，但我们纯创造不需要
////			pb.getDrops(state, null);
//            method = ObfuscationReflectionHelper.findMethod(BlockBehaviour.class, "m_7381_", BlockState.class, LootContext.Builder.class);
//            final Class<?> wc = getDeclaringClass(blkClass, method.getName(), BlockState.class, LootContext.Builder.class);
//            final boolean quantityDroppedTest = wc == Block.class || wc == BlockBehaviour.class;


            final boolean isNotSlab = Item.byBlock(block) != Items.AIR; // TODO what's this??? 判断掉落物？
            boolean itemExistsOrNotSpecialDrops = /* quantityDroppedTest || */ isNotSlab;
            // ignore blocks with custom collision.
//			pb.getShape( null, null, null, null );
            method = ObfuscationReflectionHelper.findMethod(BlockBehaviour.class, "m_5940_", BlockState.class, BlockGetter.class, BlockPos.class, CollisionContext.class);
            Class<?> collisionClass = getDeclaringClass(blkClass, method.getName(), BlockState.class, BlockGetter.class, BlockPos.class, CollisionContext.class);
            boolean noCustomCollision = collisionClass == BlockBehaviour.class;

            // full cube specifically is tied to lighting... so for glass
            // Compatibility use isFullBlock which can be true for glass.
            boolean isFullBlock = state.canOcclude() || block instanceof AbstractGlassBlock;

            final boolean tickingBehavior = block.isRandomlyTicking(state) && ChiselsAndBits.getConfig().getServer().blackListRandomTickingBlocks.get();
            boolean hasBehavior = (state.hasBlockEntity() || tickingBehavior);

//            final boolean supportedMaterial = ModBlocks.convertGivenStateToChiseledBlock(state) != null;

            final Boolean IgnoredLogic = ignoreLogicBlocks.get(block);
            if (blkClass.isAnnotationPresent(IgnoreBlockLogic.class) || IgnoredLogic != null && IgnoredLogic) {
                isFullBlock = true;
                noCustomCollision = true;
                hasBehavior = false;
                itemExistsOrNotSpecialDrops = true;
            }

            if (noCustomCollision && isFullBlock && !hasBehavior && itemExistsOrNotSpecialDrops) {
                final SupportsAnalysisResult result = new SupportsAnalysisResult(
                        true,
                        LocalStrings.ChiselSupportGenericNotSupported,
                        (blkClass.isAnnotationPresent(IgnoreBlockLogic.class) || IgnoredLogic != null && IgnoredLogic) ? LocalStrings.ChiselSupportLogicIgnored : LocalStrings.ChiselSupportGenericSupported
                );

                supportedBlocks.put(block, result);
                return result;
            }

            if (fluidBlocks.containsKey(block)) {
                final SupportsAnalysisResult result = new SupportsAnalysisResult(
                        true,
                        LocalStrings.ChiselSupportGenericNotSupported,
                        LocalStrings.ChiselSupportGenericFluidSupport
                );

                supportedBlocks.put(block, result);
                return result;
            }

            SupportsAnalysisResult result = null;
            if (!noCustomCollision) {
                result = new SupportsAnalysisResult(
                        false,
                        LocalStrings.ChiselSupportCustomCollision,
                        LocalStrings.ChiselSupportGenericSupported
                );
            } else if (!isNotSlab) {
                result = new SupportsAnalysisResult(
                        false,
                        LocalStrings.ChiselSupportIsSlab,
                        LocalStrings.ChiselSupportGenericSupported
                );
            } else if (!isFullBlock) {
                result = new SupportsAnalysisResult(
                        false,
                        LocalStrings.ChiselSupportNotFullBlock,
                        LocalStrings.ChiselSupportGenericSupported
                );
            } else if (hasBehavior) {
                result = new SupportsAnalysisResult(
                        false,
                        LocalStrings.ChiselSupportHasBehaviour,
                        LocalStrings.ChiselSupportGenericSupported
                );
            }
//            } else if (!quantityDroppedTest) {
//                result = new SupportsAnalysisResult(
//                        false,
//                        LocalStrings.ChiselSupportHasCustomDrops,
//                        LocalStrings.ChiselSupportGenericSupported
//                );
//            }

            supportedBlocks.put(block, result);
            return result;
        } catch (final Throwable t) {
            final SupportsAnalysisResult result = new SupportsAnalysisResult(
                    false,
                    LocalStrings.ChiselSupportFailureToAnalyze,
                    LocalStrings.ChiselSupportGenericSupported
            );
            // if the above test fails for any reason, then the block cannot be
            // supported.
            supportedBlocks.put(block, result);
            return result;
        }
    }

    public static boolean isSupported(final BlockState state) {
        return doSupportAnalysis(state).supported();
    }

    public record SupportsAnalysisResult(boolean supported, LocalStrings unsupportedReason, LocalStrings supportedReason) {
    }

    private static Class<?> getDeclaringClass(
            final Class<?> blkClass,
            final String methodName,
            final Class<?>... args) {
        try {
            Class<?> clz = blkClass.getMethod(methodName, args).getDeclaringClass();
            return clz;
        } catch (final NoSuchMethodException e) {
            // nothing here...
        } catch (final SecurityException e) {
            // nothing here..
        } catch (final NoClassDefFoundError e) {
            Log.eligibility("Unable to determine blocks eligibility for chiseling, " + blkClass.getName() + " attempted to load " + e.getMessage() + " missing @OnlyIn( Dist.CLIENT ) or @Optional?");
            return blkClass;
        } catch (final Throwable t) {
            return blkClass;
        }

        return getDeclaringClass(blkClass.getSuperclass(), methodName, args);
    }

    public static boolean canChisel(final BlockState state) {
        return state.getBlock() instanceof BlockChiseled || isSupported(state);
    }

    public static boolean canChisel(
            final ItemStack stack) {
        if (stack.isEmpty())
            return false;

        if (stack.getItem() instanceof ItemBlockChiseled)
            return true;

        if (stack.getItem() instanceof BlockItem) {
            final BlockItem blockItem = (BlockItem) stack.getItem();
            final Block block = blockItem.getBlock();
            final BlockState blockState = block.defaultBlockState();
            final BlockBitInfo.SupportsAnalysisResult result = BlockBitInfo.doSupportAnalysis(blockState);

            return result.supported;
        }

        return false;
    }

    public static boolean isChiseled(
            final ItemStack stack) {
        if (stack.isEmpty())
            return false;

        if (stack.getItem() instanceof ItemBlockChiseled)
            return true;

        return false;
    }
}
