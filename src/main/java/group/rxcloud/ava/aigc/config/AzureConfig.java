package group.rxcloud.ava.aigc.config;

import group.rxcloud.vrml.resource.Resources;
import io.vavr.control.Try;

import java.util.Properties;

public class AzureConfig {

    public static String azureToken = "";

    public static String chatgptUrl = "";

    static {
        Try<Properties> propertiesTry = Resources.loadResourcesProperties("/azure-config.properties");
        Properties properties = propertiesTry.get();

        azureToken = properties.getProperty("azureToken");
        chatgptUrl = properties.getProperty("chatgptUrl");
    }
}
