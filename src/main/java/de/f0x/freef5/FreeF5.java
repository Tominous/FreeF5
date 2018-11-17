package de.f0x.freef5;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.labymod.api.LabyModAddon;
import net.labymod.api.events.ServerMessageEvent;
import net.labymod.settings.elements.KeyElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Consumer;
import net.labymod.utils.ServerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class FreeF5 extends LabyModAddon {

    private static FreeF5 instance;

    private boolean enabled;

    private Minecraft mc;

    private boolean freeMode;
    private int lastThirdPersonView;

    private CustomRenderer customRenderer;
    private EntityRenderer defaultRenderer;

    @Override
    public void onEnable() {
        instance = this;

        this.mc = Minecraft.getMinecraft();
        defaultRenderer = mc.entityRenderer;

        customRenderer = new CustomRenderer(mc, mc.getResourceManager());
        getApi().registerForgeListener(this);

        getApi().getEventManager().registerOnJoin(new Consumer<ServerData>() {
            @Override
            public void accept(ServerData serverData) {
                enabled = true;
            }
        });
        getApi().getEventManager().register(new ServerMessageEvent() {
            @Override
            public void onServerMessage(String s, JsonElement msg) {
                if (s.equals("freef5") && !msg.getAsBoolean()) {
                    enabled = false;
                }
            }
        });
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void loadConfig() {
        if (!getConfig().has("key")) getConfig().addProperty("key", Keyboard.KEY_LMENU);
        saveConfig();
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        KeyElement key = new KeyElement("Key", null, getConfig().get("key").getAsInt(), new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                getConfig().addProperty("key", integer);
                saveConfig();
            }
        });

        list.add(key);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (enabled) {
            if (Keyboard.isKeyDown(getConfig().get("key").getAsInt())) {
                freeMode = true;
                if (mc.gameSettings.thirdPersonView < 3) {
                    lastThirdPersonView = mc.gameSettings.thirdPersonView;

                    customRenderer.setCustomYaw(mc.thePlayer.rotationYaw);
                    customRenderer.setCustomPitch(mc.thePlayer.rotationPitch);
                }
                mc.gameSettings.thirdPersonView = 3;
            } else {
                freeMode = false;
                if (mc.gameSettings.thirdPersonView == 3) mc.gameSettings.thirdPersonView = lastThirdPersonView;
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        if (enabled) {
            if (freeMode) {
                mc.entityRenderer = customRenderer;
            } else {
                mc.entityRenderer = defaultRenderer;
            }
        }
    }

    public static FreeF5 getInstance() {
        return instance;
    }
}
