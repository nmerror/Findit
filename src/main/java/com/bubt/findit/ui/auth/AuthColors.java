package com.bubt.findit.ui.auth;

import java.awt.Color;

/**
 * The colours of the entry screens - the login, the registration and the
 * password reset.
 *
 * <p>Only three families are used: black, white and grey. The window that
 * opens the application is dark, the application behind it is light - a
 * simple contrast that makes the entrance feel like a gate.
 */
final class AuthColors {

    /** The background of the window. */
    static final Color BACKGROUND = new Color(0x111418);
    /** The card that holds the form. */
    static final Color CARD = new Color(0x1B2027);
    /** The surface of an input field. */
    static final Color FIELD = new Color(0x232830);
    /** The border of a card or a field. */
    static final Color BORDER = new Color(0x343B45);
    /** The main text. */
    static final Color TEXT = new Color(0xFFFFFF);
    /** The text of a label. */
    static final Color TEXT_DIM = new Color(0xC3CAD3);
    /** The text of a hint. */
    static final Color TEXT_MUTE = new Color(0x8A94A1);
    /** The colour of a link and of the focus border. */
    static final Color ACCENT = new Color(0x7FD4E8);
    /** The colour of an error message. */
    static final Color ERROR = new Color(0xFF9AA8);
    /** The colour of a success message. */
    static final Color SUCCESS = new Color(0x86E0B4);

    private AuthColors() {
    }
}
