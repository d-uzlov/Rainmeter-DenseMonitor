package rxtd.flexmon.templates;

public class IntegerBounds {
    public final int x;
    public final int y;
    public final int w;
    public final int h;

    public IntegerBounds(int x, int y) {
        this.x = x;
        this.y = y;
        this.w = 0;
        this.h = 0;
    }

    public IntegerBounds(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public IntegerBounds excludeBorder(int width) {
        return excludeBorder(width, width, width, width);
    }

    public IntegerBounds excludeBorder(int left, int right, int top, int bottom) {
        return new IntegerBounds(this.x + left, this.y + top, this.w - left - right, this.h - top - bottom);
    }

    public IntegerBounds move(int dx, int dy) {
        return new IntegerBounds(this.x + dx, this.y + dy, this.w, this.h);
    }
}
