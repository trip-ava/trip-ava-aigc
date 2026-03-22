package group.rxcloud.ava.aigc.config;

import com.amazonaws.regions.Regions;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;

/**
 * AWS 配置类
 * 所有配置从环境变量读取，确保敏感信息不会硬编码在代码中
 */
@Slf4j
public class AwsConfig {

    public static String accessKey = "";
    public static String secretKey = "";
    public static String region = "ap-southeast-1";

    public static Regions regions = Regions.AP_SOUTHEAST_1;
    public static Region regionsV2 = Region.AP_SOUTHEAST_1;

    static {
        loadConfigFromEnv();
    }

    /**
     * 从环境变量加载 AWS 配置
     */
    private static void loadConfigFromEnv() {
        accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        String regionEnv = System.getenv("AWS_REGION");

        if (accessKey == null || accessKey.isEmpty()) {
            log.warn("AWS_ACCESS_KEY_ID not set in environment variables");
            accessKey = "";
        }

        if (secretKey == null || secretKey.isEmpty()) {
            log.warn("AWS_SECRET_ACCESS_KEY not set in environment variables");
            secretKey = "";
        }

        if (regionEnv != null && !regionEnv.isEmpty()) {
            region = regionEnv;
        }

        try {
            regions = Regions.fromName(region);
            regionsV2 = Region.of(region);
        } catch (Exception e) {
            log.error("Invalid AWS region: {}, using default: ap-southeast-1", region);
            region = "ap-southeast-1";
            regions = Regions.AP_SOUTHEAST_1;
            regionsV2 = Region.AP_SOUTHEAST_1;
        }
    }

    /**
     * 验证 AWS 配置是否完整
     */
    public static boolean isConfigValid() {
        return accessKey != null && !accessKey.isEmpty()
                && secretKey != null && !secretKey.isEmpty();
    }
}
