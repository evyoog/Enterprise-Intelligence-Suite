package com.vyoog.eisplatform.modules.toolsync.gateway;

/** The tool (or the identity provider) could not be reached, or did not answer in time: the delivery is retried later, and the other tools carry on. */
public class ToolUnavailableException extends RuntimeException {

    public ToolUnavailableException(String message) {
        super(message);
    }

    public ToolUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
