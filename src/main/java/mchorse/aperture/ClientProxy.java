package mchorse.aperture;

import mchorse.aperture.camera.CameraControl;
import mchorse.aperture.camera.CameraRenderer;
import mchorse.aperture.camera.CameraRunner;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.FixtureRegistry.FixtureInfo;
import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.ModifierRegistry.ModifierInfo;
import mchorse.aperture.camera.fixtures.*;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.camera.modifiers.AngleModifier;
import mchorse.aperture.camera.modifiers.DragModifier;
import mchorse.aperture.camera.modifiers.FollowModifier;
import mchorse.aperture.camera.modifiers.LookModifier;
import mchorse.aperture.camera.modifiers.MathModifier;
import mchorse.aperture.camera.modifiers.OrbitModifier;
import mchorse.aperture.camera.modifiers.RemapperModifier;
import mchorse.aperture.camera.modifiers.ShakeModifier;
import mchorse.aperture.camera.modifiers.TranslateModifier;
import mchorse.aperture.client.KeyboardHandler;
import mchorse.aperture.client.RenderingHandler;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.panels.GuiRollercoasterFixturePanel;
import mchorse.aperture.client.gui.panels.GuiCircularFixturePanel;
import mchorse.aperture.client.gui.panels.GuiDollyFixturePanel;
import mchorse.aperture.client.gui.panels.GuiIdleFixturePanel;
import mchorse.aperture.client.gui.panels.GuiKeyframeFixturePanel;
import mchorse.aperture.client.gui.panels.GuiManualFixturePanel;
import mchorse.aperture.client.gui.panels.GuiNullFixturePanel;
import mchorse.aperture.client.gui.panels.GuiPathFixturePanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiAngleModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiDragModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiFollowModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiLookModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiMathModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiOrbitModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiRemapperModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiShakeModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiTranslateModifierPanel;
import mchorse.aperture.commands.CommandCamera;
import mchorse.aperture.commands.CommandLoadChunks;
import mchorse.mclib.McLib;
import mchorse.mclib.utils.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.GameType;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.HashMap;

/**
 * Client proxy
 *
 * This class is responsible for registering client side event handlers, client 
 * commands, storing camera related stuff, and "proxify" everything related to 
 * client side.
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    public static boolean server = false;

    /* Camera stuff */
    public static CameraRenderer renderer = new CameraRenderer();
    public static CameraControl control = new CameraControl();
    public static CameraRunner runner;

    public static KeyboardHandler keys;

    /**
     * Event bus for handling camera editor events (I don't want to spam a lot 
     * of events in the main event bus). 
     */
    public static EventBus EVENT_BUS = new EventBus();

    /* Files */
    public static File config;
    public static File cameras;

    /**
     * An instance of a camera editor
     */
    public static GuiCameraEditor cameraEditor;

    /**
     * Get camera editor
     */
    public static GuiCameraEditor getCameraEditor()
    {
        if (cameraEditor == null)
        {
            cameraEditor = new GuiCameraEditor(Minecraft.getMinecraft(), runner);
        }

        return cameraEditor;
    }

    /**
     * Open the camera editor
     */
    public static GuiCameraEditor openCameraEditor()
    {
        Minecraft mc = Minecraft.getMinecraft();
        GuiCameraEditor editor = ClientProxy.getCameraEditor();

        editor.updateCameraEditor(mc.player);
        mc.player.setVelocity(0, 0, 0);
        mc.displayGuiScreen(editor);

        return editor;
    }

    /**
     * Get client cameras storage location
     * 
     * This method returns File pointer to a folder where client side camera 
     * profiles are stored. This method will return {@code null} if it was 
     * invoked when the game is in the main menu.
     */
    public static File getClientCameras()
    {
        Minecraft mc = Minecraft.getMinecraft();
        ServerData data = mc.getCurrentServerData();

        File file = null;

        if (data != null)
        {
            /* Removing port, because this will distort the folder name */
            file = new File(cameras, data.serverIP.replaceAll(":[\\w]{1,5}$", "").replaceAll("[^\\w\\d_\\- ]", "_"));
        }
        else if (mc.isSingleplayer())
        {
            /* I probably should've used getFolderName() in the beginning ... */
            file = new File(cameras, mc.getIntegratedServer().getWorldName().replaceAll("[^\\w\\d_\\- ]", "_"));
        }

        if (file != null)
        {
            file.mkdirs();
        }

        return file;
    }

    /**
     * Get game mode of the player
     */
    public static GameType getGameMode()
    {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        NetworkPlayerInfo networkplayerinfo = mc.getConnection().getPlayerInfo(player.getGameProfile().getId());

        return networkplayerinfo != null ? networkplayerinfo.getGameType() : GameType.CREATIVE;
    }

    /**
     * Register mod items, blocks, tile entites and entities, load item,
     * block models and register entity renderer.
     */
    @Override
    public void preLoad(FMLPreInitializationEvent event)
    {
        FixtureRegistry.CLIENT = new HashMap<Class<? extends AbstractFixture>, FixtureInfo>();
        ModifierRegistry.CLIENT = new HashMap<Class<? extends AbstractModifier>, ModifierInfo>();

        config = new File(event.getModConfigurationDirectory(), "aperture");
        cameras = new File(config, "cameras");

        super.preLoad(event);

        runner = new CameraRunner();
    }

    /**
     * Subscribe all event listeners to EVENT_BUS and attach any client-side
     * commands to the ClientCommandRegistry.
     */
    @Override
    public void load(FMLInitializationEvent event)
    {
        /* Register camera fixtures */
        GuiCameraEditor.PANELS.put(IdleFixture.class, GuiIdleFixturePanel.class);
        GuiCameraEditor.PANELS.put(PathFixture.class, GuiPathFixturePanel.class);
        GuiCameraEditor.PANELS.put(CircularFixture.class, GuiCircularFixturePanel.class);
        GuiCameraEditor.PANELS.put(KeyframeFixture.class, GuiKeyframeFixturePanel.class);
        GuiCameraEditor.PANELS.put(NullFixture.class, GuiNullFixturePanel.class);
        GuiCameraEditor.PANELS.put(ManualFixture.class, GuiManualFixturePanel.class);
        GuiCameraEditor.PANELS.put(DollyFixture.class, GuiDollyFixturePanel.class);
        GuiCameraEditor.PANELS.put(RollercoasterFixture.class, GuiRollercoasterFixturePanel.class);

        FixtureRegistry.registerClient(IdleFixture.class, "aperture.gui.fixtures.idle", new Color(0.085F, 0.62F, 0.395F));
        FixtureRegistry.registerClient(PathFixture.class, "aperture.gui.fixtures.path", new Color(0.408F, 0.128F, 0.681F));
        FixtureRegistry.registerClient(CircularFixture.class, "aperture.gui.fixtures.circular", new Color(0.298F, 0.631F, 0.247F));
        FixtureRegistry.registerClient(KeyframeFixture.class, "aperture.gui.fixtures.keyframe", new Color(0.874F, 0.184F, 0.625F));
        FixtureRegistry.registerClient(NullFixture.class, "aperture.gui.fixtures.null", new Color(0.1F, 0.1F, 0.12F));
        FixtureRegistry.registerClient(ManualFixture.class, "aperture.gui.fixtures.manual", new Color().set(0x0050b3));
        FixtureRegistry.registerClient(DollyFixture.class, "aperture.gui.fixtures.dolly", new Color().set(0xffa500));
        FixtureRegistry.registerClient(RollercoasterFixture.class, "aperture.gui.fixtures.rollercoaster", new Color().set(0xff0000));

        /* Register camera modifiers */
        GuiModifiersManager.PANELS.put(ShakeModifier.class, GuiShakeModifierPanel.class);
        GuiModifiersManager.PANELS.put(MathModifier.class, GuiMathModifierPanel.class);
        GuiModifiersManager.PANELS.put(LookModifier.class, GuiLookModifierPanel.class);
        GuiModifiersManager.PANELS.put(FollowModifier.class, GuiFollowModifierPanel.class);
        GuiModifiersManager.PANELS.put(TranslateModifier.class, GuiTranslateModifierPanel.class);
        GuiModifiersManager.PANELS.put(AngleModifier.class, GuiAngleModifierPanel.class);
        GuiModifiersManager.PANELS.put(OrbitModifier.class, GuiOrbitModifierPanel.class);
        GuiModifiersManager.PANELS.put(DragModifier.class, GuiDragModifierPanel.class);
        GuiModifiersManager.PANELS.put(RemapperModifier.class, GuiRemapperModifierPanel.class);

        ModifierRegistry.registerClient(ShakeModifier.class, "aperture.gui.modifiers.shake", new Color(0.085F, 0.62F, 0.395F));
        ModifierRegistry.registerClient(MathModifier.class, "aperture.gui.modifiers.math", new Color(0.408F, 0.128F, 0.681F));
        ModifierRegistry.registerClient(LookModifier.class, "aperture.gui.modifiers.look", new Color(0.1F, 0.5F, 1F));
        ModifierRegistry.registerClient(FollowModifier.class, "aperture.gui.modifiers.follow", new Color(0.85F, 0.137F, 0.329F));
        ModifierRegistry.registerClient(TranslateModifier.class, "aperture.gui.modifiers.translate", new Color(0.298F, 0.631F, 0.247F));
        ModifierRegistry.registerClient(AngleModifier.class, "aperture.gui.modifiers.angle", new Color(0.847F, 0.482F, 0.043F));
        ModifierRegistry.registerClient(OrbitModifier.class, "aperture.gui.modifiers.orbit", new Color(0.874F, 0.184F, 0.625F));
        ModifierRegistry.registerClient(DragModifier.class, "aperture.gui.modifiers.drag", new Color(0.298F, 0.690F, 0.972F));
        ModifierRegistry.registerClient(RemapperModifier.class, "aperture.gui.modifiers.remapper", new Color().set(0x111111));

        super.load(event);

        /* Event listeners */
        RenderingHandler handler = new RenderingHandler();

        McLib.EVENT_BUS.register(handler);
        MinecraftForge.EVENT_BUS.register(handler);
        MinecraftForge.EVENT_BUS.register(keys = new KeyboardHandler());
        MinecraftForge.EVENT_BUS.register(renderer);

        renderer.smooth.enabled = Aperture.smooth;
        renderer.smooth.fricX = Aperture.smoothFricX;
        renderer.smooth.fricY = Aperture.smoothFricY;

        renderer.roll.friction = Aperture.rollFriction;
        renderer.roll.factor = Aperture.rollFactor;

        renderer.fov.friction = Aperture.fovFriction;
        renderer.fov.factor = Aperture.fovFactor;

        /* Client commands */
        ClientCommandHandler.instance.registerCommand(new CommandCamera());
        ClientCommandHandler.instance.registerCommand(new CommandLoadChunks());
    }

    /**
     * Client version of get language string.
     */
    @Override
    public String getLanguageString(String key, String defaultComment)
    {
        String comment = I18n.format(key);

        return comment.equals(key) ? defaultComment : comment;
    }
}