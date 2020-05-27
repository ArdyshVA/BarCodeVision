package ru.ard.vnc.glavsoft.rfb.encoding.decoder;

import ru.ard.vnc.glavsoft.drawing.Renderer;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.transport.Transport;
import org.apache.poi.ss.formula.ptg.IntersectionPtg;

public class HextileDecoder extends Decoder {
    static final /* synthetic */ boolean $assertionsDisabled = (!HextileDecoder.class.desiredAssertionStatus());
    private static final int ANY_SUBRECTS_MASK = 8;
    private static final int BACKGROUND_SPECIFIED_MASK = 2;
    private static final int BG_COLOR_INDEX = 1;
    private static final int DEFAULT_TILE_SIZE = 16;
    private static final int FG_COLOR_INDEX = 0;
    private static final int FOREGROUND_SPECIFIED_MASK = 4;
    private static final int RAW_MASK = 1;
    private static final int SUBRECTS_COLOURED_MASK = 16;

    public void decode(Transport.Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        int[] colors = {-1, -1};
        int maxX = rect.x + rect.width;
        int maxY = rect.y + rect.height;
        for (int tileY = rect.y; tileY < maxY; tileY += 16) {
            int tileHeight = Math.min(maxY - tileY, 16);
            for (int tileX = rect.x; tileX < maxX; tileX += 16) {
                decodeHextileSubrectangle(reader, renderer, colors, tileX, tileY, Math.min(maxX - tileX, 16), tileHeight);
            }
        }
    }

    private void decodeHextileSubrectangle(Transport.Reader reader, Renderer renderer, int[] colors, int tileX, int tileY, int tileWidth, int tileHeight) throws TransportException {
        int subencoding = reader.readUInt8();
        if ((subencoding & 1) != 0) {
            RawDecoder.getInstance().decode(reader, renderer, tileX, tileY, tileWidth, tileHeight);
            return;
        }
        if ((subencoding & 2) != 0) {
            colors[1] = renderer.readPixelColor(reader);
        }
        if ($assertionsDisabled || colors[1] != -1) {
            renderer.fillRect(colors[1], tileX, tileY, tileWidth, tileHeight);
            if ((subencoding & 4) != 0) {
                colors[0] = renderer.readPixelColor(reader);
            }
            if ((subencoding & 8) != 0) {
                int numberOfSubrectangles = reader.readUInt8();
                boolean colorSpecified = (subencoding & 16) != 0;
                int i = 0;
                while (i < numberOfSubrectangles) {
                    if (colorSpecified) {
                        colors[0] = renderer.readPixelColor(reader);
                    }
                    int dimensions = reader.readByte();
                    int subtileX = (dimensions >> 4) & 15;
                    int subtileY = dimensions & 15;
                    byte dimensions2 = reader.readByte();
                    int subtileWidth = ((dimensions2 >> 4) & 15) + 1;
                    int subtileHeight = (dimensions2 & IntersectionPtg.sid) + 1;
                    if ($assertionsDisabled || colors[0] != -1) {
                        renderer.fillRect(colors[0], tileX + subtileX, tileY + subtileY, subtileWidth, subtileHeight);
                        i++;
                    } else {
                        throw new AssertionError();
                    }
                }
                return;
            }
            return;
        }
        throw new AssertionError();
    }
}
