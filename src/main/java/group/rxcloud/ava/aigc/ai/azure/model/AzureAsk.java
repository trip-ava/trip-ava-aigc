package group.rxcloud.ava.aigc.ai.azure.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Azure Ask model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AzureAsk {

    private String role;
    private String content;
}
