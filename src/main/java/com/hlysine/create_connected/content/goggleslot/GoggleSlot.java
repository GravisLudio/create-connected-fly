package com.hlysine.create_connected.content.goggleslot;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.config.FeatureToggle;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * A single equipment slot that holds Engineer's Goggles, so they can be worn without giving up the
 * helmet. Original to this port -- upstream Create: Connected has nothing like it.
 * <p>
 * <b>Why it exists at all.</b> Create Fly counts you as wearing goggles only when they are in the
 * HEAD slot, or when a mod registers another way of checking. Trinkets Updated is that mod, and
 * Create Fly already ships compat for it. This is for players who do not want Trinkets' other
 * slots -- the elytra-in-a-slot kind of thing -- and it deliberately holds goggles and nothing
 * else. Anyone wanting a general accessory system should install Trinkets Updated; when it is
 * present this slot stands down so nobody ends up with two goggle slots.
 * <p>
 * <b>No Trinkets code is used here.</b> Their implementation was read to understand the problem
 * -- it is MIT, so copying would have been allowed with its notice kept -- but everything below is
 * written against Fabric's own APIs, which is why no MIT notice accompanies it. If a later change
 * lifts code from them, that file takes the notice.
 * <p>
 * The stack lives in a data attachment rather than a capability or an extra inventory row: it is
 * persisted by the attachment's codec, synced to the owning player by the API, and survives a
 * dimension change without any code of ours. Only the wearer needs the sync, because nothing
 * renders the goggles on other players yet.
 */
public final class GoggleSlot {
    /** Also the feature toggle key, so a pack can switch the slot off in the config. */
    public static final Identifier ID = CreateConnected.asResource("goggle_slot");

    private static AttachmentType<ItemStack> WORN;

    private GoggleSlot() {
    }

    public static void register() {
        // Registered unconditionally, even when the feature is off or Trinkets is installed:
        // an unregistered attachment makes the game drop saved data, which would quietly eat a
        // player's goggles the first time they loaded with the feature disabled.
        WORN = AttachmentRegistry.<ItemStack>builder()
                .persistent(ItemStack.OPTIONAL_CODEC)
                .syncWith(ItemStack.OPTIONAL_STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
                .buildAndRegister(ID);

        FeatureToggle.register(ID);
        GoggleSlotCommand.register();

        if (standDown())
            return;

        // The extension point Create Fly offers, and the same one its own Trinkets compat uses.
        GogglesItem.addIsWearingPredicate(GoggleSlot::isWearing);

        // Both sides decide, only the server mutates. The client half matters: goggles are a helmet
        // item, so vanilla's own right click equips them to the HEAD slot. If only the server
        // claimed the click, the client would still predict that swap and then get corrected --
        // which looked like the goggles flickering into the helmet slot and vanishing.
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!isActive())
                return InteractionResult.PASS;

            ItemStack held = player.getItemInHand(hand);
            ItemStack worn = get(player);

            if (isGoggles(held) && worn.isEmpty()) {
                if (world.isClientSide())
                    return InteractionResult.SUCCESS;
                set(player, held.copyWithCount(1));
                held.shrink(1);
                equipSound(player, world);
                return InteractionResult.SUCCESS;
            }

            // Taking them off, until the slot has a screen of its own. Deliberately narrow --
            // empty hand and sneaking -- so it cannot swallow an ordinary right click.
            if (held.isEmpty() && player.isShiftKeyDown() && !worn.isEmpty()) {
                if (world.isClientSide())
                    return InteractionResult.SUCCESS;
                set(player, ItemStack.EMPTY);
                if (!player.getInventory().add(worn))
                    player.drop(worn, false);
                equipSound(player, world);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        // Attachments do not follow a player through death, so respawning is handled explicitly:
        // drop the goggles where everything else dropped, or carry them over when the rule says
        // inventories are kept. `alive` covers the other reason a player entity is replaced --
        // changing dimension -- where nothing should be lost.
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof Player player))
                return;
            ItemStack worn = get(player);
            // Game rules moved to world.level.gamerules in 26.2 and the values hang off ServerLevel,
            // not Level: serverLevel.getGameRules().get(rule).
            if (worn.isEmpty() || !(player.level() instanceof ServerLevel level)
                    || level.getGameRules().get(GameRules.KEEP_INVENTORY))
                return;
            set(player, ItemStack.EMPTY);
            player.drop(worn, true, false);
        });
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) ->
                set(newPlayer, get(oldPlayer)));
    }

    private static void equipSound(Player player, net.minecraft.world.level.Level world) {
        world.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_GENERIC.value(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Create Fly's compat already gives Trinkets Updated a head slot that goggles work in, so
     * running both would mean two places to put the same item, and the wrong one would look broken.
     */
    private static boolean standDown() {
        return FabricLoader.getInstance().isModLoaded("trinkets");
    }

    /** Checked per call, not cached: a feature toggled off mid-game takes effect immediately. */
    public static boolean isActive() {
        return !standDown() && FeatureToggle.isEnabled(ID);
    }

    public static boolean isGoggles(ItemStack stack) {
        return stack.is(AllItems.GOGGLES);
    }

    public static ItemStack get(Player player) {
        return player.getAttachedOrElse(WORN, ItemStack.EMPTY);
    }

    public static void set(Player player, ItemStack stack) {
        player.setAttached(WORN, stack.isEmpty() ? ItemStack.EMPTY : stack);
    }

    public static boolean isWearing(Player player) {
        return isActive() && isGoggles(get(player));
    }
}
