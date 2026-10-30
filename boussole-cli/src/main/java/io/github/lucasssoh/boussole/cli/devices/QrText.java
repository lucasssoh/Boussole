package io.github.lucasssoh.boussole.cli.devices;

import io.nayuki.qrcodegen.QrCode;

/**
 * A QR code drawn in the terminal: two rows of modules per line with half blocks, in black and
 * white set explicitly, so that it scans on a dark terminal as on a light one.
 */
final class QrText {

    private static final int MARGIN = 2;
    private static final String WHITE = "\u001b[97m";
    private static final String BLACK = "\u001b[30m";
    private static final String ON_WHITE = "\u001b[107m";
    private static final String ON_BLACK = "\u001b[40m";
    private static final String RESET = "\u001b[0m";

    private QrText() {
    }

    static String of(String text) {
        QrCode qr = QrCode.encodeText(text, QrCode.Ecc.MEDIUM);
        StringBuilder out = new StringBuilder();
        for (int y = -MARGIN; y < qr.size + MARGIN; y += 2) {
            for (int x = -MARGIN; x < qr.size + MARGIN; x++) {
                boolean top = qr.getModule(x, y);
                boolean bottom = qr.getModule(x, y + 1);
                out.append(top ? BLACK : WHITE).append(bottom ? ON_BLACK : ON_WHITE).append('▀');
            }
            out.append(RESET).append('\n');
        }
        return out.toString();
    }
}
