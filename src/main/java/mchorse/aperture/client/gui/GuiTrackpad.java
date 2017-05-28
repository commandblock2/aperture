package mchorse.aperture.client.gui;

import mchorse.aperture.utils.Rect;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.config.GuiUtils;

/**
 * Trackpad GUI field
 *
 * This field allows users of GUI to edit a numerical floating point number
 * using either a textbox or mouse value dragging (which modifies the value
 * based on the distance from the original clicking point).
 */
public class GuiTrackpad
{
    private FontRenderer font;
    public GuiTextField text;
    public Rect area = new Rect();

    public String title = "";
    public float value;
    public float amplitude = 0.25F;
    public float min = Float.NEGATIVE_INFINITY;
    public float max = Float.POSITIVE_INFINITY;

    /* Value dragging fields */
    private boolean dragging;
    private int lastX;
    private int lastY;
    private float lastValue;

    /**
     * Trackpad listener, it can be null
     */
    private ITrackpadListener listener;

    public GuiTrackpad(ITrackpadListener listener, FontRenderer font)
    {
        this.listener = listener;
        this.text = new GuiTextField(0, font, 0, 0, 0, 0);
        this.text.setEnableBackgroundDrawing(false);
        this.font = font;
    }

    /**
     * Set the value of the field. The input value would be rounded up to 3
     * decimal places.
     */
    public void setValue(float value)
    {
        value = Math.round(value * 1000F) / 1000F;
        value = MathHelper.clamp_float(value, this.min, this.max);

        this.value = value;
        this.text.setText(String.valueOf(value));
        this.text.setCursorPositionZero();
    }

    /**
     * Set value of this field and also notify the trackpad listener so it
     * could detect the value change.
     */
    public void setValueAndNotify(float value)
    {
        if (this.listener != null)
        {
            this.listener.setTrackpadValue(this, value);
        }

        this.setValue(value);
    }

    /**
     * Update the bounding box of this GUI field
     */
    public void update(int x, int y, int w, int h)
    {
        this.area.set(x, y, w, h);

        this.text.xPosition = x + 4;
        this.text.yPosition = y + h / 2 - 4;
        this.text.width = (int) (w / 1.5F) - 2;
        this.text.height = 9;
        this.text.setCursorPositionZero();
    }

    /**
     * Handle key pressed event
     */
    public void keyTyped(char typedChar, int keyCode)
    {
        String old = this.text.getText();

        this.text.textboxKeyTyped(typedChar, keyCode);

        if (this.text.isFocused() && !this.text.equals(old))
        {
            try
            {
                this.value = Float.parseFloat(this.text.getText());

                if (this.listener != null)
                {
                    this.listener.setTrackpadValue(this, this.value);
                }
            }
            catch (Exception e)
            {}
        }
    }

    /**
     * Delegates mouse click to text field and initiate value dragging if the
     * cursor inside of trackpad's bounding box.
     */
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        this.text.mouseClicked(mouseX, mouseY, mouseButton);

        if (!this.text.isFocused() && this.area.isInside(mouseX, mouseY))
        {
            this.dragging = true;
            this.lastX = mouseX;
            this.lastY = mouseY;
            this.lastValue = this.value;
        }
    }

    /**
     * Reset value dragging
     */
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        this.dragging = false;
    }

    /**
     * Draw the trackpad
     *
     * This method will not only draw the text box, background and title label,
     * but also dragging the numerical value based on the mouse input.
     */
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        int x = this.area.x;
        int y = this.area.y;
        int w = this.area.w;
        int h = this.area.h;

        GuiUtils.drawContinuousTexturedBox(GuiPlaybackScrub.VANILLA_BUTTONS, x, y, 0, 46, w, h, 200, 20, 0, 0, 1, 1, 0);
        this.text.drawTextBox();

        if (!this.title.isEmpty())
        {
            int lw = this.font.getStringWidth(this.title);

            this.font.drawString(this.title, x + w - lw - 3, y + h / 2 - 4, 0xffaaaaaa);
        }

        if (this.dragging)
        {
            int dx = mouseX - this.lastX;
            int dy = mouseY - this.lastY;

            if (dx != 0 || dy != 0)
            {
                float diff = ((int) Math.sqrt(dx * dx + dy * dy) - 3) * this.amplitude;
                float newValue = this.lastValue + (dy < 0 ? diff : -diff);

                newValue = diff < 0 ? this.lastValue : Math.round(newValue * 1000F) / 1000F;

                if (this.value != newValue)
                {
                    this.setValueAndNotify(MathHelper.clamp_float(newValue, this.min, this.max));
                }
            }

            Gui.drawRect(this.lastX - 3, this.lastY - 3, this.lastX + 3, this.lastY + 3, 0xaaffffff);
        }
    }

    /**
     * Trackpad listener inteface
     */
    public static interface ITrackpadListener
    {
        public void setTrackpadValue(GuiTrackpad trackpad, float value);
    }
}