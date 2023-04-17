package org.demo.pojo.base;

/**
 * 视频标签
 */
public enum Tag {

    动漫(0),
    游戏(1),
    电竞(2),
    校园(3),
    时尚(4),
    治愈(5);


    int value;

    private Tag(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * 通过枚举值获得枚举类型
     * @param value
     * @return
     */
    public static Tag getTagByValue(int value) {
        for (final Tag tag : values()) {
            if (tag.value == value)
                return tag;
        }
        return null;
    }
}
