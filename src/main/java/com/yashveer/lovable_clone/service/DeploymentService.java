package com.yashveer.lovable_clone.service;

import com.yashveer.lovable_clone.dto.deploy.DeployResponse;

public interface DeploymentService {

    DeployResponse deploy(Long projectId);
}
