package com.garygz.dspdemoproject.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ImpressionEventId(UUID campaignId, OffsetDateTime occurredAt) implements Serializable {}
