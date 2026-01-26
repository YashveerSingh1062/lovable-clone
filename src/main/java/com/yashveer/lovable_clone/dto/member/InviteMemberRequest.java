package com.yashveer.lovable_clone.dto.member;

import com.yashveer.lovable_clone.enums.ProjectRole;

public record InviteMemberRequest(
        String email,
        ProjectRole role
) {
}
