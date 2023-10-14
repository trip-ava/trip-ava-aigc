package group.rxcloud.ava.aigc.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Position {
    /**
     * 经度
     */
    private String longitude;

    /**
     * 纬度
     */
    private String latitude;

    public Position(String longitude, String latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
