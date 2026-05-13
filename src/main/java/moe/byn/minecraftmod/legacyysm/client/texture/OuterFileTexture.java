package moe.byn.minecraftmod.legacyysm.client.texture;

import moe.byn.minecraftmod.legacyysm.util.Keep;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import rip.ysm.imagestream.webp.WebpDecoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OuterFileTexture extends AbstractTexture {
    private final byte[] data;

    public OuterFileTexture(byte[] data) {
        this.data = data;
    }

    @Override
    @Keep
    public void load(@NotNull ResourceManager resourceManager) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(this::doLoad);
        } else {
            this.doLoad();
        }
    }

    private void doLoad() {
        try {
            byte[] imageData = ensurePng(data);
            NativeImage imageIn = NativeImage.read(new ByteArrayInputStream(imageData));
            int width = imageIn.getWidth();
            int height = imageIn.getHeight();
            TextureUtil.prepareImage(this.getId(), 0, width, height);
            imageIn.upload(0, 0, 0, 0, 0, width, height, false, false, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] ensurePng(byte[] rawData) {
        if (rawData.length < 4) {
            return rawData;
        }
        if (rawData[0] == 'R' && rawData[1] == 'I' && rawData[2] == 'F' && rawData[3] == 'F') {
            try {
                BufferedImage image = new WebpDecoder().read(rawData);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                return baos.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rawData;
    }
}
