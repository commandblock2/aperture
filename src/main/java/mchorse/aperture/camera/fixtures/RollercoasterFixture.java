package mchorse.aperture.camera.fixtures;

import com.google.gson.annotations.Expose;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.utils.L10n;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.util.math.MathHelper.*;

public class RollercoasterFixture extends PathFixture{

    /**
     * List of points in this fixture
     */

    @Expose
    public float fovModifier;

    @Expose
    public float gravity;
    
    final float granularity = 1e-1f;

    public RollercoasterFixture(long duration)
    {
        super(duration);
    }

    @Override
    public void applyFixture(long ticks, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {

        final int length = super.points.size() - 1;

        float x = (ticks / (float) this.duration) + (1.0F / duration) * previewPartialTick;
        x = clamp(x * length, 0, length);

        final int index = (int) Math.floor(x);
        x -= index;

        super.applyPoint(pos.point, index, x);
        this.applyAngle(pos.angle, index, x);
    }

    @Override
    public AbstractFixture create(long duration)
    {
        return new RollercoasterFixture(duration);
    }

    void applyAngle(Angle angle, int index, float progress)
    {
        final float totalProgress = index + progress,
                prevTotalProgress = totalProgress - granularity,
                nextTotalProgress = totalProgress + granularity;

        final int prevIndex = (int) Math.floor(prevTotalProgress),
                nextIndex = (int) Math.floor(nextTotalProgress);

        final float prevProgress = prevTotalProgress - prevIndex,
                nextProgress = nextTotalProgress - nextIndex;

        Point prevPoint = new Point(0,0,0),
                thisPoint = new Point(0, 0, 0),
                nextPoint = new Point(0, 0, 0);
        super.applyPoint(prevPoint, prevIndex, prevProgress);
        super.applyPoint(thisPoint, index, progress);
        super.applyPoint(nextPoint, nextIndex, nextProgress);


        double prevLen = prevPoint.length(thisPoint),
                nextLen = thisPoint.length(nextPoint);

        if (prevLen == 0 || nextLen == 0)
            return;

        angle.fov = (float) (70f + fovModifier * (nextLen - prevLen));

        //TODO: calculate ROLL
        Vec3d forwardVector = new Vec3d(nextPoint.x - prevPoint.x,
                nextPoint.y - prevPoint.y,
                nextPoint.z - prevPoint.z);


        angle.yaw = (float) -Math.toDegrees(Math.atan2(forwardVector.x, forwardVector.z));
        angle.pitch = (float) -Math.toDegrees(Math.atan2(forwardVector.y, Math.sqrt(Math.pow(forwardVector.x, 2) + Math.pow(forwardVector.z, 2))));


        Vec3d rightVector = new Vec3d(nextPoint.x - thisPoint.x,
                nextPoint.y - thisPoint.y,
                nextPoint.z - thisPoint.z).crossProduct(forwardVector);





        Vec3d yAxis = new Vec3d(0, 1 ,0);
        Vec3d idkVector = forwardVector.crossProduct(yAxis);

        rightVector = rightVector.add(idkVector.scale(-gravity));

        angle.roll = (float) Math.toDegrees(Math.acos(rightVector.dotProduct(idkVector) /
                        (rightVector.lengthVector() * idkVector.lengthVector()))) + 180F;

        angle.roll *= sign(-rightVector.y);

        // how?

    }

    int sign(double op)
    {
        return (op > 0 ? 1 : (op < 0 ? -1 : 0));
    }

    //TODO: toJSON/fromJSON/toByteBuf/fromByteBuf
}
