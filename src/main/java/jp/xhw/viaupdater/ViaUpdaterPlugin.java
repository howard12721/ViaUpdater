package jp.xhw.viaupdater;

import jp.xhw.viaupdater.provider.IPluginProvider;
import jp.xhw.viaupdater.provider.impl.GithubPluginProvider;
import jp.xhw.viaupdater.data.PluginEntry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ViaUpdaterPlugin extends JavaPlugin {

    private boolean updated = false;

    @Override
    public void onEnable() {
        updatePlugin("ViaVersion", new GithubPluginProvider("ViaVersion", "ViaVersion"));
        updatePlugin("ViaBackwards", new GithubPluginProvider("ViaVersion", "ViaBackwards"));
        updatePlugin("ViaRewind", new GithubPluginProvider("ViaVersion", "ViaRewind"));
        if (updated) Bukkit.getScheduler().runTask(this, () -> getServer().spigot().restart());
    }

    @Override
    public void onDisable() {

    }

    private void updatePlugin(String name, IPluginProvider provider) {
        final List<PluginEntry> entries = getPluginEntries(name);
        if (entries.stream()
                .map(PluginEntry::getVersion)
                .noneMatch(Predicate.isEqual(provider.getLatestVersion()))) {
            entries.stream()
                    .map(PluginEntry::getFile)
                    .forEach(file -> {
                        Bukkit.getLogger().info(file.getName());
                        if (!file.delete()) {
                            file.deleteOnExit();
                        }
                    });
            provider.downloadFile();
            this.updated = true;
        }
    }

    private List<PluginEntry> getPluginEntries(String name) {
        final List<PluginEntry> response = new ArrayList<>();
        final File pluginDir = new File("plugins");
        if (!pluginDir.exists()) pluginDir.mkdir();
        for (File pluginFile : Objects.requireNonNull(pluginDir.listFiles())) {
            if (!pluginFile.getName().substring(pluginFile.getName().lastIndexOf(".") + 1).equals("jar")) {
                continue;
            }
            try (ZipFile zipFile = new ZipFile(pluginFile.getPath())) {
                final ZipEntry entry = zipFile.getEntry("plugin.yml");
                final InputStream inputStream = zipFile.getInputStream(entry);
                final FileConfiguration pluginConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
                if (pluginConfig.getString("name", "").equals(name)) {
                    response.add(new PluginEntry(pluginFile, name, pluginConfig.getString("version", "")));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

}
