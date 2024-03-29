package mappinganalyzer.rectWithScroll;

import org.opencv.core.Rect;

public class RectWithScrollLevel {
    private Rect rect;
    private int scrollLevel;

    public RectWithScrollLevel(Rect rect, int scrollLevel) {
        this.rect = rect;
        this.scrollLevel = scrollLevel;
    }

    public int getScrollLevel() {
        return scrollLevel;
    }

    public Rect getRect() {
        return rect;
    }

    public void setScrollLevel(int scrollLevel) {
        this.scrollLevel = scrollLevel;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }
}