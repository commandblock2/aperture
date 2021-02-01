package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.fixtures.RollercoasterFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiRollercoasterFixturePanel extends GuiPathFixturePanel {

    public GuiTrackpadElement fovModifier;
    public GuiTrackpadElement gravity;

    public GuiRollercoasterFixturePanel(Minecraft mc, GuiCameraEditor editor) {
        super(mc, editor);

        this.fovModifier = new GuiTrackpadElement(mc, (value) -> fixture().fovModifier = value.floatValue());
        this.gravity = new GuiTrackpadElement(mc, (value) -> fixture().gravity = value.floatValue());

        this.left.add(fovModifier);
        this.left.add(gravity);
        this.right.remove(super.angle);
        this.interp.remove(this.interp.angle);
    }

    private RollercoasterFixture fixture()
    {
        return (RollercoasterFixture) this.fixture;
    }
}
