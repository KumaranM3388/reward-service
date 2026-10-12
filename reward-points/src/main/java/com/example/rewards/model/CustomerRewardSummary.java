package com.example.rewards.model;

import java.util.List;

public record CustomerRewardSummary(long customerId, List<MonthlyReward> monthlyRewards, int totalPoints) { }
