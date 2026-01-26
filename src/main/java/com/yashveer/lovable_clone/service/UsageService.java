package com.yashveer.lovable_clone.service;

import com.yashveer.lovable_clone.dto.subscription.PlanLimitsResponse;
import com.yashveer.lovable_clone.dto.subscription.UsageTodayResponse;
import org.jspecify.annotations.Nullable;

public interface UsageService {
    UsageTodayResponse getTodayUsage(Long userId);

    PlanLimitsResponse getCurrentSubscriptionLimitsOfUser(Long userId);
}
