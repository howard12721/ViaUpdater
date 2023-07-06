package jp.xhw.viaupdater.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

@AllArgsConstructor
@Data
public class PluginEntry {

    private File file;
    private String name;
    private String version;

}
