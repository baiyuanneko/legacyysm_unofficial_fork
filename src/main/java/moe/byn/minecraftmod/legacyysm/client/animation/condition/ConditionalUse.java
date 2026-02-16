package moe.byn.minecraftmod.legacyysm.client.animation.condition;

import com.google.common.collect.Lists;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ConditionalUse {
    private static final String EMPTY = "";
    private final int preSize;
    private final String idPre;
    private final String tagPre;
    private final String extraPre;
    private final List<ResourceLocation> idTest = Lists.newArrayList();
    private final List<TagKey<Item>> tagTest = Lists.newArrayList();
    private final List<UseAnim> extraTest = Lists.newArrayList();

    private static boolean isValidResourceLocation(String location) {
        String[] parts = location.split(":", 2);
        if (parts.length == 1) {
            return ResourceLocation.isValidNamespace(parts[0]);
        }
        return ResourceLocation.isValidNamespace(parts[0]) && ResourceLocation.isValidPath(parts[1]);
    }

    public ConditionalUse(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            idPre = "use_mainhand$";
            tagPre = "use_mainhand#";
            extraPre = "use_mainhand:";
            preSize = 13;
        } else {
            idPre = "use_offhand$";
            tagPre = "use_offhand#";
            extraPre = "use_offhand:";
            preSize = 12;
        }
    }

    public void addTest(String name) {
        if (name.length() <= preSize) {
            return;
        }
        String substring = name.substring(preSize);
        if (name.startsWith(idPre) && isValidResourceLocation(substring)) {
            idTest.add(ResourceLocation.parse(substring));
        }
        if (name.startsWith(tagPre) && isValidResourceLocation(substring)) {
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, ResourceLocation.parse(substring));
            tagTest.add(tagKey);
        }
        if (name.startsWith(extraPre)) {
            if (substring.equals(UseAnim.NONE.name().toLowerCase(Locale.US))) {
                return;
            }
            Arrays.stream(UseAnim.values()).filter(a -> a.name().toLowerCase(Locale.US).equals(substring)).findFirst().ifPresent(extraTest::add);
        }
    }

    public String doTest(Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).isEmpty()) {
            return EMPTY;
        }
        String result = doIdTest(player, hand);
        if (result.isEmpty()) {
            result = doTagTest(player, hand);
            if (result.isEmpty()) {
                return doExtraTest(player, hand);
            }
            return result;
        }
        return result;
    }

    private String doIdTest(Player player, InteractionHand hand) {
        if (idTest.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemInHand = player.getItemInHand(hand);
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(itemInHand.getItem());
        if (registryName == null) {
            return EMPTY;
        }
        if (idTest.contains(registryName)) {
            return idPre + registryName;
        }
        return EMPTY;
    }

    private String doTagTest(Player player, InteractionHand hand) {
        if (tagTest.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemInHand = player.getItemInHand(hand);
        return tagTest.stream().filter(itemInHand::is).findFirst().map(itemTagKey -> tagPre + itemTagKey.location()).orElse(EMPTY);
    }

    private String doExtraTest(Player player, InteractionHand hand) {
        if (extraTest.isEmpty()) {
            return EMPTY;
        }
        UseAnim anim = player.getItemInHand(hand).getUseAnimation();
        if (this.extraTest.contains(anim)) {
            return extraPre + anim.name().toLowerCase(Locale.US);
        }
        return EMPTY;
    }
}
