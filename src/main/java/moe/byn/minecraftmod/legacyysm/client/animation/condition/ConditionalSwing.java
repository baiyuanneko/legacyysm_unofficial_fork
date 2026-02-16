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

import java.util.List;

public class ConditionalSwing {
    private static final String ID_PRE = "swing$";
    private static final String TAG_PRE = "swing#";
    private static final String EMPTY = "";
    private static final int PRE_SIZE = 6;
    private final List<ResourceLocation> idTest = Lists.newArrayList();
    private final List<TagKey<Item>> tagTest = Lists.newArrayList();

    private static boolean isValidResourceLocation(String location) {
        String[] parts = location.split(":", 2);
        if (parts.length == 1) {
            return ResourceLocation.isValidNamespace(parts[0]);
        }
        return ResourceLocation.isValidNamespace(parts[0]) && ResourceLocation.isValidPath(parts[1]);
    }

    public void addTest(String name) {
        if (name.length() <= PRE_SIZE) {
            return;
        }
        String substring = name.substring(PRE_SIZE);
        if (name.startsWith(ID_PRE) && isValidResourceLocation(substring)) {
            idTest.add(ResourceLocation.parse(substring));
        }
        if (name.startsWith(TAG_PRE) && isValidResourceLocation(substring)) {
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, ResourceLocation.parse(substring));
            tagTest.add(tagKey);
        }
    }

    public String doTest(Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).isEmpty()) {
            return EMPTY;
        }
        String result = doIdTest(player, hand);
        if (result.isEmpty()) {
            return doTagTest(player, hand);
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
            return ID_PRE + registryName;
        }
        return EMPTY;
    }

    private String doTagTest(Player player, InteractionHand hand) {
        if (tagTest.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemInHand = player.getItemInHand(hand);
        return tagTest.stream().filter(itemInHand::is).findFirst().map(itemTagKey -> TAG_PRE + itemTagKey.location()).orElse(EMPTY);
    }
}
