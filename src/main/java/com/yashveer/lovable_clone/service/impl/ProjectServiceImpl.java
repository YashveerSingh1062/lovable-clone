package com.yashveer.lovable_clone.service.impl;

import com.yashveer.lovable_clone.dto.project.ProjectRequest;
import com.yashveer.lovable_clone.dto.project.ProjectResponse;
import com.yashveer.lovable_clone.dto.project.ProjectSummaryResponse;
import com.yashveer.lovable_clone.entity.Project;
import com.yashveer.lovable_clone.entity.User;
import com.yashveer.lovable_clone.mapper.ProjectMapper;
import com.yashveer.lovable_clone.repository.ProjectRepository;
import com.yashveer.lovable_clone.repository.UserRepository;
import com.yashveer.lovable_clone.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@Transactional
public class ProjectServiceImpl implements ProjectService {

    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMapper projectMapper;

    @Override
    public List<ProjectSummaryResponse> getUserProjects(Long userId) {
        var projects = projectRepository.findAllAccessibleByUser(userId);
        return projectMapper.toListOfProjectSummaryResponse(projects);
    }

    @Override
    public ProjectResponse getUserProjectById(Long id, Long userId) {
        return null;
    }

    @Override
    public ProjectResponse createProject(ProjectRequest request, Long userId) {
        User owner = userRepository.findById(userId).orElseThrow();
        Project project = Project.builder()
                .name(request.name())
                .owner(owner)
                .isPublic(false)
                .build();

        project = projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest request, Long userId) {
        return null;
    }

    @Override
    public void softDelete(Long id, Long userId) {

    }
}
