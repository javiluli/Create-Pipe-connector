package com.javiluli.createpipeconnector.feature.placement.client;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.placement.config.PlacementAnimationClientConfig;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.catnip.config.ui.ConfigScreen;
import net.createmod.catnip.config.ui.ConfigScreenList;
import net.createmod.catnip.config.ui.SubMenuConfigScreen;
import net.createmod.catnip.config.ui.entries.BooleanEntry;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.RenderElement;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.ponder.enums.PonderGuiTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Aplica dependencias visuales entre entradas de la pantalla automatica de Create.
 *
 * <p>Catnip 1.20.1 permite marcar entradas como no editables, pero no expone
 * publicamente ese metodo ni ofrece dependencias declarativas. Este adaptador
 * conserva su pantalla original y limita la reflexion a una llamada cacheada.</p>
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlacementAnimationConfigDependencyController {
    private static final Method SET_EDITABLE_METHOD = findSetEditableMethod();
    private static Screen lastScreen;
    private static ConfigScreenList lastConfigList;
    private static Boolean lastAnimationEnabled;

    /** Impide crear instancias del controlador de pantalla. */
    private PlacementAnimationConfigDependencyController() {
    }

    /** Actualiza las entradas dependientes cuando cambia el interruptor principal. */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || SET_EDITABLE_METHOD == null) {
            return;
        }

        Screen screen = Minecraft.getInstance().screen;
        if (!(screen instanceof SubMenuConfigScreen) || !Constants.MOD_ID.equals(ConfigScreen.modID)) {
            lastScreen = null;
            lastConfigList = null;
            lastAnimationEnabled = null;
            return;
        }

        ConfigScreenList configList = findConfigList(screen.children());
        if (configList == null || configList.children().size() < 2) {
            return;
        }

        boolean animationEnabled = PlacementAnimationClientConfig.isAnimationEnabledInConfigScreen();
        if (screen == lastScreen
                && configList == lastConfigList
                && Boolean.valueOf(animationEnabled).equals(lastAnimationEnabled)) {
            return;
        }

        List<ConfigScreenList.Entry> entries = configList.children();
        for (int index = 1; index < entries.size(); index++) {
            setEditable(entries.get(index), animationEnabled);
        }
        lastScreen = screen;
        lastConfigList = configList;
        lastAnimationEnabled = animationEnabled;
    }

    /** Localiza la lista central sin depender de campos internos de la pantalla. */
    private static ConfigScreenList findConfigList(List<? extends GuiEventListener> listeners) {
        for (GuiEventListener listener : listeners) {
            if (listener instanceof ConfigScreenList configScreenList) {
                return configScreenList;
            }
        }
        return null;
    }

    /** Cambia el estado usando el mismo mecanismo visual que Create. */
    private static void setEditable(ConfigScreenList.Entry entry, boolean editable) {
        try {
            SET_EDITABLE_METHOD.invoke(entry, editable);
            if (entry instanceof BooleanEntry booleanEntry) {
                updateBooleanVisual(booleanEntry, editable);
            }
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            // La pantalla sigue siendo funcional si una version futura cambia esta API interna.
        }
    }

    /** Completa el estado visual que BooleanEntry no actualiza por si mismo. */
    private static void updateBooleanVisual(BooleanEntry entry, boolean editable) {
        BoxWidget valueButton = findLastBoxWidget(entry.getGuiListeners());
        if (valueButton == null) {
            return;
        }

        boolean value = entry.getValue();
        RenderElement icon = (value
                ? PonderGuiTextures.ICON_CONFIRM
                : PonderGuiTextures.ICON_DISABLE)
                .asStencil()
                .withElementRenderer(editable
                        ? (graphics, width, height, alpha) -> UIRenderHelper.angledGradient(
                                graphics,
                                0,
                                0,
                                height / 2,
                                height,
                                width,
                                value ? AbstractSimiWidget.COLOR_SUCCESS : AbstractSimiWidget.COLOR_FAIL
                        )
                        : BaseConfigScreen.DISABLED_RENDERER)
                .at(10, 0);
        valueButton.showingElement(icon);
        valueButton.animateGradientFromState();
    }

    /** Devuelve el boton de valor, situado despues del boton de reinicio. */
    private static BoxWidget findLastBoxWidget(List<? extends GuiEventListener> listeners) {
        BoxWidget lastBoxWidget = null;
        for (GuiEventListener listener : listeners) {
            if (listener instanceof BoxWidget boxWidget) {
                lastBoxWidget = boxWidget;
            }
        }
        return lastBoxWidget;
    }

    /** Resuelve una sola vez el metodo protegido de Catnip. */
    private static Method findSetEditableMethod() {
        try {
            Method method = ConfigScreenList.Entry.class.getDeclaredMethod("setEditable", boolean.class);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }
}
