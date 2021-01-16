package mchorse.aperture.camera.fixtures;

import com.google.gson.annotations.Expose;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import net.minecraft.util.math.MathHelper;

public class RollercoasterFixture extends PathFixture{

    /**
     * List of points in this fixture
     */

    @Expose
    public boolean changeFOV;

    @Expose
    public float fovModifier;

    @Expose
    public float granularity;

    public RollercoasterFixture(long duration)
    {
        super(duration);
    }

    @Override
    public void applyFixture(long ticks, float partialTick, float previewPartialTick, CameraProfile profile, Position pos)
    {

        final int length = super.points.size() - 1;

        float x = (ticks / (float) this.duration) + (1.0F / duration) * previewPartialTick;
        x = MathHelper.clamp(x * length, 0, length);

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

        if (changeFOV)
            angle.fov += fovModifier * (nextLen - prevLen);

        //TODO: cauculate angle
    }
}
