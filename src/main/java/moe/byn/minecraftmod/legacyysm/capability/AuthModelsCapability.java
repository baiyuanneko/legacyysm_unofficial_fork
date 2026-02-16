package moe.byn.minecraftmod.legacyysm.capability;

import com.google.common.collect.Sets;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Set;

public class AuthModelsCapability implements INBTSerializable<ListTag> {
    private Set<ResourceLocation> authModels = Sets.newHashSet();

    public void addModel(ResourceLocation modelId) {
        authModels.add(modelId);
    }

    public void copyFrom(AuthModelsCapability source) {
        this.authModels = source.authModels;
    }

    public void removeModel(ResourceLocation modelId) {
        authModels.remove(modelId);
    }

    public boolean containModel(ResourceLocation modelId) {
        return authModels.contains(modelId);
    }

    public Set<ResourceLocation> getAuthModels() {
        return authModels;
    }

    public void setAuthModels(Set<ResourceLocation> authModels) {
        this.authModels = authModels;
    }

    public void clear() {
        authModels.clear();
    }

    @Override
    public ListTag serializeNBT(HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();
        for (ResourceLocation modelId : authModels) {
            listTag.add(StringTag.valueOf(modelId.toString()));
        }
        return listTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, ListTag nbt) {
        this.authModels.clear();
        for (Tag tag : nbt) {
            authModels.add(ResourceLocation.parse(tag.getAsString()));
        }
    }
}
