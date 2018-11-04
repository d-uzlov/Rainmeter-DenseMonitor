package rxtd.flexmon.templates;

public class Layout {
    private final static double FONT_SIZE_FACTOR = 1.4;
    private final int width;
    private final int borderWidth;
    private final int padding;
    private final int fontSize;
    private final int contentLeft;
    private final int contentWidth;
    private int elementInterval;
    private int currentY;
    private boolean firstElement = true;
    private IntegerBounds currentBounds = null;

    public Layout(int width, int borderWidth, int padding, int elementInterval, int fontSize) {
        this.width = width;
        this.borderWidth = borderWidth;
        this.padding = padding;
        this.elementInterval = elementInterval;
        this.fontSize = fontSize;

        this.contentLeft = borderWidth + padding;
        this.contentWidth = this.width - 2 * this.contentLeft;

        this.currentY = borderWidth + padding;
    }

    public int getElementInterval() {
        return this.elementInterval;
    }

    public void setElementInterval(int elementInterval) {
        this.elementInterval = elementInterval;
    }

    private void addIntervalOddset() {
        if (!this.firstElement) {
            this.currentY += elementInterval;
        } else {
            this.firstElement = false;
        }
    }

    public IntegerBounds getCurrentBounds() {
        return currentBounds;
    }

    public IntegerBounds nextLine() {
        this.addIntervalOddset();
        int lineHeight = (int) Math.round(this.fontSize * FONT_SIZE_FACTOR);
        return this.nextElement(lineHeight);
    }

    public IntegerBounds nextElement(int height) {
        return this.nextElement(height, this.elementInterval);
    }

    public IntegerBounds nextElement(int height, int elementInterval) {
        this.addIntervalOddset();
        IntegerBounds integerBounds = new IntegerBounds(this.contentLeft, this.currentY, this.contentWidth, height);
        this.currentBounds = integerBounds;
        this.currentY += height;
        return integerBounds;
    }

    public IntegerBounds getInnerBounds() {
        return new IntegerBounds(this.borderWidth, this.borderWidth, this.width - this.borderWidth * 2, this.currentY + this.padding - this.borderWidth);
    }

    public IntegerBounds getFullBounds() {
        return new IntegerBounds(0, 0, this.width, this.currentY + this.padding + this.borderWidth);
    }
}
