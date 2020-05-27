package ru.ard.vnc.glavsoft.drawing;

public abstract class SoftCursor {
    public int height;
    protected int hotX;
    protected int hotY;
    public int oldHeight;
    public int oldRX = 0;
    public int oldRY = 0;
    public int oldWidth;
    public int rX = 0;
    public int rY = 0;
    public int width;
    private int x;
    private int y;

    /* access modifiers changed from: protected */
    public abstract void createNewCursorImage(int[] iArr, int i, int i2, int i3, int i4);

    public SoftCursor(int hotX2, int hotY2, int width2, int height2) {
        this.hotX = hotX2;
        this.hotY = hotY2;
        this.width = width2;
        this.oldWidth = width2;
        this.height = height2;
        this.oldHeight = height2;
    }

    public void updatePosition(int newX, int newY) {
        this.oldRX = this.rX;
        this.oldRY = this.rY;
        this.oldWidth = this.width;
        this.oldHeight = this.height;
        this.x = newX;
        this.y = newY;
        this.rX = this.x - this.hotX;
        this.rY = this.y - this.hotY;
    }

    public void setNewDimensions(int hotX2, int hotY2, int width2, int height2) {
        this.hotX = hotX2;
        this.hotY = hotY2;
        this.oldWidth = this.width;
        this.oldHeight = this.height;
        this.oldRX = this.rX;
        this.oldRY = this.rY;
        this.rX = this.x - hotX2;
        this.rY = this.y - hotY2;
        this.width = width2;
        this.height = height2;
    }

    public void createCursor(int[] cursorPixels, int hotX2, int hotY2, int width2, int height2) {
        createNewCursorImage(cursorPixels, hotX2, hotY2, width2, height2);
        setNewDimensions(hotX2, hotY2, width2, height2);
    }
}
