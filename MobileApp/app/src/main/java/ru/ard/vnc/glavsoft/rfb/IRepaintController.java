package ru.ard.vnc.glavsoft.rfb;

import ru.ard.vnc.glavsoft.drawing.Renderer;
import ru.ard.vnc.glavsoft.rfb.encoding.decoder.FramebufferUpdateRectangle;

public interface IRepaintController {
    void init(Renderer renderer);

    void repaintBitmap(int i, int i2, int i3, int i4);

    void repaintBitmap(FramebufferUpdateRectangle framebufferUpdateRectangle);

    void repaintCursor();

    void updateCursorPosition(short s, short s2);
}
