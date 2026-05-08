package com.yashveer.lovable_clone.service.impl;

import com.yashveer.lovable_clone.dto.project.FileContentResponse;
import com.yashveer.lovable_clone.dto.project.FileNode;
import com.yashveer.lovable_clone.dto.project.FileTreeResponse;
import com.yashveer.lovable_clone.entity.Project;
import com.yashveer.lovable_clone.entity.ProjectFile;
import com.yashveer.lovable_clone.error.ResourceNotFoundException;
import com.yashveer.lovable_clone.mapper.ProjectFileMapper;
import com.yashveer.lovable_clone.repository.ProjectFileRepository;
import com.yashveer.lovable_clone.repository.ProjectRepository;
import com.yashveer.lovable_clone.service.ProjectFileService;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectFileServiceImpl implements ProjectFileService {

    private final ProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;
    private final MinioClient minioClient;
    private final ProjectFileMapper projectFileMapper;

    @Value("${minio.project-bucket}")
    private String projectBucket;

    private static final String BUCKET_NAME = "projects";

    @Override
    public FileTreeResponse getFileTree(Long projectId) {

        List<ProjectFile> projectFileList = projectFileRepository.findByProjectId(projectId);
        List<FileNode> projectFileNodes = projectFileMapper.toListOfFileNode(projectFileList);
        return new FileTreeResponse(projectFileNodes);
    }

    @Override
    public FileContentResponse getFileContent(Long projectId, String path, Long userId) {
        return null;
    }

    @Override
    public void saveFile(Long projectId, String path, String content) {
        log.info("Saving file: {}",path);

        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new ResourceNotFoundException("Project", projectId.toString())
        );

        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        String objectKey = projectId + "/" + cleanPath;

        try {
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            InputStream inputStream = new ByteArrayInputStream(contentBytes);
            // saving the file content
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(projectBucket)
                            .object(objectKey)
                            .stream(inputStream, contentBytes.length, -1)
                            .contentType(determineContentType(path))
                            .build());

            // Saving the metaData
            ProjectFile file = projectFileRepository.findByProjectIdAndPath(projectId, cleanPath)
                    .orElseGet(() -> ProjectFile.builder()
                            .project(project)
                            .path(cleanPath)
                            .minioObjectKey(objectKey) // Use the key we generated
                            .createdAt(Instant.now())
                            .build());

            file.setUpdatedAt(Instant.now());
            projectFileRepository.save(file);
            log.info("Saved file: {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to save file {}/{}", projectId, cleanPath, e);
            throw new RuntimeException("File save failed", e);
        }
    }

    private String determineContentType(String path) {
        String type = URLConnection.guessContentTypeFromName(path);
        if (type != null) return type;
        if (path.endsWith(".jsx") || path.endsWith(".ts") || path.endsWith(".tsx")) return "text/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".css")) return "text/css";

        return "text/plain";
    }
}
