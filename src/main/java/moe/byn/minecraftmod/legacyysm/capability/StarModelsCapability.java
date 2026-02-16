package moe.byn.minecraftmod.legacyysm.capability;

import com.google.common.collect.Sets;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Set;

public class StarModelsCapability implements INBTSerializable<ListTag> {
    private Set<ResourceLocation> starModels = Sets.newHashSet();

    public void addModel(ResourceLocation modelId) {
        starModels.add(modelId);
    }

    public void copyFrom(StarModelsCapability source) {
        this.starModels = source.starModels;
    }

    public void removeModel(ResourceLocation modelId) {
        starModels.remove(modelId);
    }

    public boolean containModel(ResourceLocation modelId) {
        return starModels.contains(modelId);
    }

    public Set<ResourceLocation> getStarModels() {
        return starModels;
    }

    public void setStarModels(Set<ResourceLocation> starModels) {
        this.starModels = starModels;
    }

    public void clear() {
        starModels.clear();
    }

    @Override
    public ListTag serializeNBT(HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();
        for (ResourceLocation modelId : starModels) {
            listTag.add(StringTag.valueOf(modelId.toString()));
        }
        return listTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, ListTag nbt) {
        this.starModels.clear();
        for (Tag tag : nbt) {
            starModels.add(ResourceLocation.parse(tag.getAsString()));
        }
    }
}
