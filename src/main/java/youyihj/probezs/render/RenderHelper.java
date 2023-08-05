package youyihj.probezs.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/**
 * This and other render code borrowed from @unascribed (licensed MIT)
 * https://github.com/unascribed/BlockRenderer
 */
public class RenderHelper {
    private static float oldZLevel;

    public static void setupRenderStateWithMul(float mul) {
        setupRenderState(16 * mul);
    }

    public static void setupRenderState(float desiredSize) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc);

        // Switches from 3D to 2D
        mc.entityRenderer.setupOverlayRendering();
        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        /*
		 * The GUI scale affects us due to the call to setupOverlayRendering
		 * above. As such, we need to counteract this to always get a 512x512
		 * render. We could manually switch to orthogonal mode, but it's just
		 * more convenient to leverage setupOverlayRendering.
		 */
        float scale = desiredSize / (16f * res.getScaleFactor());
        GlStateManager.translate(0, 0, -(scale * 100));

        GlStateManager.scale(scale, scale, scale);

        oldZLevel = mc.getRenderItem().zLevel;
        mc.getRenderItem().zLevel = -50;

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlpha();
    }

    public static void tearDownRenderState() {
        GlStateManager.disableLighting();
        GlStateManager.disableColorMaterial();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();

        Minecraft.getMinecraft().getRenderItem().zLevel = oldZLevel;
    }

    public static BufferedImage readPixels(int width, int height) {
		/*
		 * Make sure we're reading from the back buffer, not the front buffer.
		 * The front buffer is what is currently on-screen, and is useful for
		 * screenshots.
		 */
        GL11.glReadBuffer(GL11.GL_BACK);
        // Allocate a native data array to fit our pixels
        ByteBuffer buf = BufferUtils.createByteBuffer(width * height * 4);
        // And finally read the pixel data from the GPU...
        GL11.glReadPixels(0, Minecraft.getMinecraft().displayHeight - height, width, height, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buf);
        // ...and turn it into a Java object we can do things to.
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[width * height];
        buf.asIntBuffer().get(pixels);
        img.setRGB(0, 0, width, height, pixels, 0, width);
        return img;
    }

    public static BufferedImage createFlipped(BufferedImage image) {
        AffineTransform at = new AffineTransform();
		/*
		 * Creates a compound affine transform, instead of just one, as we need
		 * to perform two transformations.
		 *
		 * The first one is to scale the image to 100% width, and -100% height.
		 * (That's *negative* 100%.)
		 */
        at.concatenate(AffineTransform.getScaleInstance(1, -1));
        /*
         * We then need to translate the image back up by it's height, as flipping
         * it over moves it off the bottom of the canvas.
         */
        at.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()));
        return createTransformed(image, at);
    }

    public static BufferedImage createTransformed(BufferedImage image, AffineTransform at) {
        // Create a blank image with the same dimensions as the old one...
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        // ...get it's renderer...
        Graphics2D g = newImage.createGraphics();
        /// ...and draw the old image on top of it with our transform.
        g.transform(at);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }
}
