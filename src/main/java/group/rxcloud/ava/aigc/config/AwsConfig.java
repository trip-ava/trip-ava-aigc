package group.rxcloud.ava.aigc.config;

import com.amazonaws.regions.Regions;
import group.rxcloud.vrml.resource.Resources;
import io.vavr.control.Try;
import software.amazon.awssdk.regions.Region;

import java.util.Properties;

public class AwsConfig {

    public static String accessKey = "";
    public static String secretKey = "";
    public static String region = "ap-southeast-1";

    public static Regions regions = Regions.AP_SOUTHEAST_1;
    public static Region regionsV2 = Region.AP_SOUTHEAST_1;

    static {
        Try<Properties> propertiesTry = Resources.loadResourcesProperties("/aws-config.properties");
        Properties properties = propertiesTry.get();

        accessKey = properties.getProperty("accessKey");
        secretKey = properties.getProperty("secretKey");
        region = properties.getProperty("region");
        regions = Regions.fromName(region);
    }
}
