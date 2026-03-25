package com.guildflow.backend.model.enums;

public enum TrackingType {
    LINEAR,  // percentage-based progress (books, podcasts, videos)
    BINARY   // 0% until mentor confirms completion, then jumps to 100% (memorization)
}
