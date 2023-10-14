package group.rxcloud.ava.aigc.entity;

import lombok.Getter;

@Getter
public enum SingleNoteInfoType {
    TEXT("TEXT", "文本"),
    IMAGE("IMAGE", "图像"),
    VOICE("VOICE", "音频")

    ;
    private final String code;
    private final String msg;

    SingleNoteInfoType(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
