package jp.xhw.viaupdater.provider;

public interface IPluginProvider {

    String getLatestVersion();

    void downloadFile();

}
