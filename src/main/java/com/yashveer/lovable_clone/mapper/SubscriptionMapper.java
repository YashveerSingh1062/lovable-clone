package com.yashveer.lovable_clone.mapper;

import com.yashveer.lovable_clone.dto.subscription.PlanResponse;
import com.yashveer.lovable_clone.dto.subscription.SubscriptionResponse;
import com.yashveer.lovable_clone.entity.Plan;
import com.yashveer.lovable_clone.entity.Subscription;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionResponse toSubscriptionResponse(Subscription subscription);

    PlanResponse toPlanResponse(Plan plan);
}
