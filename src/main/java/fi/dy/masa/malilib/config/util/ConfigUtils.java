package fi.dy.masa.malilib.config.util;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.util.text.TextFormatting;
import fi.dy.masa.malilib.MaLiLibConfigs;
import fi.dy.masa.malilib.action.ActionContext;
import fi.dy.masa.malilib.action.ActionExecutionWidgetManager;
import fi.dy.masa.malilib.action.ActionRegistryImpl;
import fi.dy.masa.malilib.config.ConfigManagerImpl;
import fi.dy.masa.malilib.config.option.ConfigInfo;
import fi.dy.masa.malilib.gui.config.ConfigTab;
import fi.dy.masa.malilib.input.ActionResult;
import fi.dy.masa.malilib.input.CustomHotkeyManager;
import fi.dy.masa.malilib.overlay.message.MessageDispatcher;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.data.ConfigOnTab;
import fi.dy.masa.malilib.util.data.ModInfo;

public class ConfigUtils
{
    /**
     * @return the currently active config directory. This takes into account a possible active config profile.
     */
    public static File getActiveConfigDirectory()
    {
        String profile = MaLiLibConfigs.Internal.ACTIVE_CONFIG_PROFILE.getStringValue();
        return getActiveConfigDirectory(profile);
    }

    public static File getActiveConfigDirectory(String profile)
    {
        File baseConfigDir = FileUtils.getConfigDirectory();

        if (StringUtils.isBlank(profile) == false)
        {
            try
            {
                return baseConfigDir.toPath().resolve("config_profiles").resolve(profile).toFile();
            }
            catch (InvalidPathException ignore) {}
        }

        return baseConfigDir;
    }

    public static void sortConfigsByDisplayName(ArrayList<ConfigInfo> configs)
    {
        configs.sort(Comparator.comparing((c) -> TextFormatting.getTextWithoutFormattingCodes(c.getDisplayName())));
    }

    public static List<? extends ConfigInfo> getExtendedList(List<? extends ConfigInfo> baseList)
    {
        return Registry.CONFIG_TAB_EXTENSION.getExtendedList(baseList, MaLiLibConfigs.Generic.SORT_EXTENSION_MOD_OPTIONS.getBooleanValue());
    }

    /**
     * Creates a map of all the configs on the provided config tabs, using
     * an identifier key that is in the form "modId.tabName.configName".
     */
    public static Map<String, ConfigOnTab> getConfigIdToConfigMapFromTabs(List<ConfigTab> tabs)
    {
        Map<String, ConfigOnTab> map = new HashMap<>();

        for (ConfigTab tab : tabs)
        {
            ModInfo mod = tab.getModInfo();
            String modCategory = mod.getModId() + "." + tab.getName() + ".";

            for (ConfigInfo config : tab.getExpandedConfigs())
            {
                String id = modCategory + config.getName();
                map.put(id, new ConfigOnTab(tab, config));
            }
        }

        return map;
    }

    /**
     * Loads all configs and other systems from file.<br>
     * <b>Note:</b> You are not supposed to call this from mod code!<br>
     * This is a wrapper for loading all the different systems at once.
     */
    public static void loadAllConfigsFromFile()
    {
        Registry.ICON.loadFromFile();
        ((ConfigManagerImpl) Registry.CONFIG_MANAGER).loadAllConfigs();
        ((ActionRegistryImpl) Registry.ACTION_REGISTRY).loadFromFile();
        CustomHotkeyManager.INSTANCE.loadFromFile();
        Registry.INFO_WIDGET_MANAGER.loadFromFile();
        Registry.MESSAGE_REDIRECT_MANAGER.loadFromFile();
        Registry.HOTKEY_MANAGER.updateUsedKeys();
    }

    /**
     * Saves all configs and other systems to file.<br>
     * <b>Note:</b> You are not supposed to call this from mod code!<br>
     * This is a wrapper for saving all the different systems at once.
     */
    public static void saveAllConfigsToFileIfDirty()
    {
        ((ConfigManagerImpl) Registry.CONFIG_MANAGER).saveIfDirty();
        Registry.INFO_WIDGET_MANAGER.saveToFileIfDirty();
        Registry.MESSAGE_REDIRECT_MANAGER.saveToFileIfDirty();
        ActionExecutionWidgetManager.INSTANCE.clear();

        // These should always already be saved when closing the corresponding config screens
        Registry.ICON.saveToFileIfDirty();
        ((ActionRegistryImpl) Registry.ACTION_REGISTRY).saveToFileIfDirty();
        CustomHotkeyManager.INSTANCE.saveToFileIfDirty();
    }

    private static void copyConfigsIfProfileNotExist(String profile)
    {
        if (StringUtils.isBlank(profile) == false)
        {
            File dir = getActiveConfigDirectory();

            if (dir.exists() == false && dir.mkdirs())
            {
                Registry.ICON.saveToFile();
                ((ConfigManagerImpl) Registry.CONFIG_MANAGER).saveAllConfigs();
                ((ActionRegistryImpl) Registry.ACTION_REGISTRY).saveToFile();
                ActionExecutionWidgetManager.INSTANCE.saveAllLoadedToFile();
                CustomHotkeyManager.INSTANCE.saveToFile();
                Registry.INFO_WIDGET_MANAGER.saveToFile();
                Registry.MESSAGE_REDIRECT_MANAGER.saveToFile();
            }
        }
    }

    public static ActionResult switchConfigProfile(ActionContext ctx, String profile)
    {
        String current = MaLiLibConfigs.Internal.ACTIVE_CONFIG_PROFILE.getStringValue();

        if ("default".equals(profile))
        {
            profile = "";
        }

        if (current.equals(profile) == false)
        {
            saveAllConfigsToFileIfDirty();
            MaLiLibConfigs.Internal.ACTIVE_CONFIG_PROFILE.setValue(profile);
            copyConfigsIfProfileNotExist(profile);
            loadAllConfigsFromFile();

            MessageDispatcher.success("malilib.message.success.switched_config_profile", profile);

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
