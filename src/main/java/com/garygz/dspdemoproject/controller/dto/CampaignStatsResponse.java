package com.garygz.dspdemoproject.controller.dto;

import java.util.List;

public record CampaignStatsResponse(List<DailyCount> impressionsPerDay, List<DailyCount> clicksPerDay) {}
