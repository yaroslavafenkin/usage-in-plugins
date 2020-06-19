package org.jenkinsci.deprecatedusage;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

public class UpdateCenter {
    private final JenkinsFile core;
    private final List<JenkinsFile> plugins = new ArrayList<>();

    public UpdateCenter(JSONObject metadata, String workDir) {
        JSONObject jsonCore = metadata.getJSONObject("core");
        core = parse(jsonCore, workDir);
        JSONObject jsonPlugins = metadata.getJSONObject("plugins");
        for (Object pluginId : jsonPlugins.keySet()) {
            JSONObject jsonPlugin = jsonPlugins.getJSONObject(pluginId.toString());
            JenkinsFile plugin = parse(jsonPlugin, workDir);
            plugins.add(plugin);
        }
        plugins.sort(Comparator.comparing(JenkinsFile::getName, String.CASE_INSENSITIVE_ORDER));
    }

    private static JenkinsFile parse(JSONObject jsonObject, String workDir) throws JSONException {
        final String wiki;
        if (jsonObject.has("wiki")) {
            wiki = jsonObject.getString("wiki");
        } else {
            wiki = null;
        }
        final Base64.Decoder decoder = Base64.getDecoder();
        final Checksum checksum;
        if (jsonObject.has("sha256")) {
            byte[] digest = decoder.decode(jsonObject.getString("sha256"));
            checksum = data -> MessageDigest.isEqual(digest, DigestUtils.sha256(data));
        } else if (jsonObject.has("sha1")) {
            byte[] digest = decoder.decode(jsonObject.getString("sha1"));
            checksum = data -> MessageDigest.isEqual(digest, DigestUtils.sha1(data));
        } else {
            checksum = null;
        }
        return new JenkinsFile(jsonObject.getString("name"), jsonObject.getString("version"), workDir,
                jsonObject.getString("url"), wiki, checksum);
    }

    public JenkinsFile getCore() {
        return core;
    }

    public List<JenkinsFile> getPlugins() {
        return plugins;
    }
}
