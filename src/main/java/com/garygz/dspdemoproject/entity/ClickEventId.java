package com.garygz.dspdemoproject.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ClickEventId(UUID campaignId, OffsetDateTime occurredAt) implements Serializable {}
