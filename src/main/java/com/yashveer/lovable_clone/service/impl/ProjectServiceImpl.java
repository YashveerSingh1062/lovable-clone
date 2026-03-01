package com.yashveer.lovable_clone.service.impl;

import com.yashveer.lovable_clone.dto.project.ProjectRequest;
import com.yashveer.lovable_clone.dto.project.ProjectResponse;
import com.yashveer.lovable_clone.dto.project.ProjectSummaryResponse;
import com.yashveer.lovable_clone.entity.Project;
import com.yashveer.lovable_clone.entity.ProjectMember;
import com.yashveer.lovable_clone.entity.ProjectMemberId;
import com.yashveer.lovable_clone.entity.User;
import com.yashveer.lovable_clone.enums.ProjectRole;
import com.yashveer.lovable_clone.error.BadRequestException;
import com.yashveer.lovable_clone.error.ResourceNotFoundException;
import com.yashveer.lovable_clone.mapper.ProjectMapper;
import com.yashveer.lovable_clone.repository.ProjectMemberRepository;
import com.yashveer.lovable_clone.repository.ProjectRepository;
import com.yashveer.lovable_clone.repository.UserRepository;
import com.yashveer.lovable_clone.security.AuthUtil;
import com.yashveer.lovable_clone.service.ProjectService;
import com.yashveer.lovable_clone.service.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@Transactional
public class ProjectServiceImpl implements ProjectService {

    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMapper projectMapper;
    ProjectMemberRepository projectMemberRepository;
    AuthUtil authUtil;
    SubscriptionService subscriptionService;

    @Override
    public List<ProjectSummaryResponse> getUserProjects() {
        Long userId = authUtil.getCurrentUserId();
        var projects = projectRepository.findAllAccessibleByUser(userId);
        return projectMapper.toListOfProjectSummaryResponse(projects);
    }

    @Override
    @PreAuthorize("@security.canViewProject(#projectId)")
    public ProjectResponse getUserProjectById(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(projectId,userId);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public ProjectResponse createProject(ProjectRequest request) {

        if(!subscriptionService.canCreateNewProject()){
            throw new BadRequestException("User cannot create a New project with current Plan, Upgrade plan now.");
        }

        Long userId = authUtil.getCurrentUserId();
//        User owner = userRepository.findById(userId).orElseThrow(
//                () -> new ResourceNotFoundException("User",userId.toString())
//        );
        User owner = userRepository.getReferenceById(userId);
        Project project = Project.builder()
                .name(request.name())
                .isPublic(false)
                .build();

        project = projectRepository.save(project);

        ProjectMemberId projectMemberId = new ProjectMemberId(project.getId(),owner.getId());
        ProjectMember projectMember = ProjectMember.builder()
                .id(projectMemberId)
                .projectRole(ProjectRole.OWNER)
                .user(owner)
                .acceptedAt(Instant.now())
                .invitedAt(Instant.now())
                .project(project)
                .build();
        projectMemberRepository.save(projectMember);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public ProjectResponse updateProject(Long projectId, ProjectRequest request) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(projectId,userId);
        project.setName(request.name());
        project = projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    @PreAuthorize("@security.canDeleteProject(#projectId)")
    public void softDelete(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(projectId,userId);
        project.setDeletedAt(Instant.now());
        projectRepository.save(project);
    }

    // Internal functions
    public Project getAccessibleProjectById(Long projectId,Long userId){
        return projectRepository.findAccessibleProjectById(projectId,userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project",projectId.toString()));
    }
}
