package ru.ard.vnc.glavsoft.rfb;

public interface ClipboardController {
    String getClipboardText();

    String getRenuedClipboardText();

    void setEnabled(boolean z);

    void updateSystemClipboard(String str);
}
