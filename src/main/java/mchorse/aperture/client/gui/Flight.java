package mchorse.aperture.client.gui;

import mchorse.aperture.Aperture;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import org.lwjgl.input.Keyboard;

import mchorse.aperture.camera.data.Position;
import net.minecraft.util.math.Vec3d;

public class Flight
{
    public boolean enabled;
    public boolean vertical;
    public int speed = 1000;

    private boolean dragging;
    private int lastX;
    private int lastY;

    public void mouseClicked(GuiContext context)
    {
        if (this.enabled)
        {
            this.dragging = true;
            this.lastX = context.mouseX;
            this.lastY = context.mouseY;
        }
    }

    public void mouseReleased(GuiContext context)
    {
        this.dragging = false;
    }

    public void animate(GuiContext context, Position position)
    {
        float f = this.speed / 1000F;
        float multiplier = 1F;

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
        {
            multiplier *= 5;
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
        {
            multiplier *= 0.2F;
        }

        double factor = 0.1 * f * multiplier;
        double angleFactor = 0.35 * f * multiplier;

        float yaw = position.angle.yaw;
        float pitch = position.angle.pitch;
        float roll = position.angle.roll;
        float fov = position.angle.fov;

        if (this.dragging)
        {
            yaw += (context.mouseX - this.lastX) * (multiplier * 0.35F);
            pitch += (context.mouseY - this.lastY) * (multiplier * 0.35F);

            this.lastX = context.mouseX;
            this.lastY = context.mouseY;
        }

        if (Keyboard.isKeyDown(Aperture.flightCameraUp.get()) || Keyboard.isKeyDown(Aperture.flightCameraDown.get()))
        {
            pitch += (Keyboard.isKeyDown(Aperture.flightCameraUp.get()) ? -angleFactor : angleFactor);
        }

        if (Keyboard.isKeyDown(Aperture.flightCameraLeft.get()) || Keyboard.isKeyDown(Aperture.flightCameraRight.get()))
        {
            yaw += (Keyboard.isKeyDown(Aperture.flightCameraLeft.get()) ? -angleFactor : angleFactor);
        }

        if (Keyboard.isKeyDown(Aperture.flightCameraRollMinus.get()) || Keyboard.isKeyDown(Aperture.flightCameraRollPlus.get()))
        {
            roll += (Keyboard.isKeyDown(Aperture.flightCameraRollMinus.get()) ? -angleFactor : angleFactor);
        }

        if (Keyboard.isKeyDown(Aperture.flightCameraFovMinus.get()) || Keyboard.isKeyDown(Aperture.flightCameraFovPlus.get()))
        {
            fov += (Keyboard.isKeyDown(Aperture.flightCameraFovMinus.get()) ? -angleFactor : angleFactor);
        }

        double x = position.point.x;
        double y = position.point.y;
        double z = position.point.z;

        double xx = 0;
        double yy = 0;
        double zz = 0;

        if (Keyboard.isKeyDown(Aperture.flightUp.get()) || Keyboard.isKeyDown(Aperture.flightDown.get()))
        {
            yy = (Keyboard.isKeyDown(Aperture.flightUp.get()) ? factor : -factor);
        }

        if (Keyboard.isKeyDown(Aperture.flightLeft.get()) || Keyboard.isKeyDown(Aperture.flightRight.get()))
        {
            xx = (Keyboard.isKeyDown(Aperture.flightLeft.get()) ? factor : -factor);
        }

        if (Keyboard.isKeyDown(Aperture.flightForward.get()) || Keyboard.isKeyDown(Aperture.flightBackward.get()))
        {
            zz = (Keyboard.isKeyDown(Aperture.flightForward.get()) ? factor : -factor);
        }

        if (xx != 0 || yy != 0 || zz != 0 || yaw != position.angle.yaw || pitch != position.angle.pitch || roll != position.angle.roll || fov != position.angle.fov)
        {
            Vec3d vec = new Vec3d(xx, yy, zz);

            if (this.vertical)
            {
                vec = vec.rotatePitch(-pitch / 180 * (float) Math.PI);
            }

            vec = vec.rotateYaw(-yaw / 180 * (float) Math.PI);

            x += vec.x;
            y += vec.y;
            z += vec.z;

            position.point.set(x, y, z);
            position.angle.set(yaw, pitch, roll, fov);
        }
    }
}