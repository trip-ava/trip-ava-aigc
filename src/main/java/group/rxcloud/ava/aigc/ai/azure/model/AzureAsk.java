package group.rxcloud.ava.aigc.ai.azure.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AzureAsk {

    private String role;
    private String content;
}
