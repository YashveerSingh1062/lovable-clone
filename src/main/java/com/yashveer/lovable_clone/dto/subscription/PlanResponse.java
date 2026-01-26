package com.yashveer.lovable_clone.dto.subscription;

public record PlanResponse(
        Long id,
        Long name,
        Integer maxProjects,
        Integer maxTokensPerDay,
        Boolean unlimitedAi,
        String price
) {
}
