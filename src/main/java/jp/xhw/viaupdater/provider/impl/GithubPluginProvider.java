package jp.xhw.viaupdater.provider.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jp.xhw.viaupdater.provider.IPluginProvider;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class GithubPluginProvider implements IPluginProvider {

    private final String ownerName;
    private final String repositoryName;

    private String latestVersion = null;
    private String assetUrl = null;
    private String assetName = null;

    public GithubPluginProvider(String ownerName, String repositoryName) {
        this.ownerName = ownerName;
        this.repositoryName = repositoryName;
    }

    @Override
    public String getLatestVersion() {
        if (latestVersion == null) requestData();
        return latestVersion;
    }

    @Override
    public void downloadFile() {
        if (assetName == null || assetUrl == null) {
            boolean requestSucceeded = requestData();
            if (!requestSucceeded) {
                return;
            }
        }
        try {
            final URL apiUrl = new URL(this.assetUrl);
            final URLConnection connection = apiUrl.openConnection();
            connection.setRequestProperty("Accept", "application/octet-stream");

            InputStream inputStream = connection.getInputStream();
            File dest = new File("plugins/" + File.separator + this.assetName);
            FileUtils.copyInputStreamToFile(inputStream, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean requestData() {
        try {
            final URL apiUrl = new URL(String.format("https://api.github.com/repos/%s/%s/releases/latest", ownerName, repositoryName));
            final URLConnection connection = apiUrl.openConnection();
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            final JsonObject json = JsonParser.parseReader(new InputStreamReader(connection.getInputStream())).getAsJsonObject();

            this.latestVersion = json.get("tag_name").getAsString();
            if (json.has("assets")) {
                final JsonArray assets = json.get("assets").getAsJsonArray();
                for (JsonElement asset : assets) {
                    final JsonObject assetJson = asset.getAsJsonObject();
                    final String assetName = assetJson.get("name").getAsString();
                    final String assetUrl = assetJson.get("url").getAsString();
                    if (assetName.contains("original-")) continue;
                    this.assetName = assetName;
                    this.assetUrl = assetUrl;
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
