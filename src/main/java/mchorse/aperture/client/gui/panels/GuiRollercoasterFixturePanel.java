package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.fixtures.RollercoasterFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiRollercoasterFixturePanel extends GuiPathFixturePanel {

    public GuiTrackpadElement fovModifier, granularity;
    public GuiToggleElement changeFOV;

    public GuiRollercoasterFixturePanel(Minecraft mc, GuiCameraEditor editor) {
        super(mc, editor);

        this.fovModifier = new GuiTrackpadElement(mc, (value) -> fixture().fovModifier = value.floatValue());
        this.granularity = new GuiTrackpadElement(mc, (value) -> fixture().granularity = value.floatValue());

        this.changeFOV = new GuiToggleElement(mc, IKey.lang("aperture.gui.panels.change_fov_enable"), false, (value) -> fixture().changeFOV = value.isToggled());

        this.left.add(fovModifier);
        this.left.add(granularity);
        this.left.add(changeFOV);
    }

    private RollercoasterFixture fixture()
    {
        return (RollercoasterFixture) this.fixture;
    }
}
