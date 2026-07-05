package de.erdbeerbaerlp.dcintegration.fabric.util;

import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;

/**
 * Fork addition: optional dedicated channel for player join/leave/timeout messages.
 *
 * The channel ID is read from the DCINTEGRATION_LOGIN_CHANNEL environment variable
 * (or the dcintegration.loginChannelID system property) rather than
 * Discord-Integration.toml, because the config schema lives in the upstream
 * dcintegration.common artifact and cannot be extended from this fork without
 * the core rewriting (and dropping) unknown keys on config save.
 *
 * When unset, behaves exactly like upstream: falls back to advanced.serverChannelID.
 */
public class ForkChannels {
    public static String loginChannelID() {
        String id = System.getenv("DCINTEGRATION_LOGIN_CHANNEL");
        if (id == null || id.isBlank()) id = System.getProperty("dcintegration.loginChannelID");
        if (id == null || id.isBlank()) return Configuration.instance().advanced.serverChannelID;
        return id.trim();
    }
}
