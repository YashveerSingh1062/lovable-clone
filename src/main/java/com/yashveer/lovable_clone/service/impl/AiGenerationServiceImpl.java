package com.yashveer.lovable_clone.service.impl;

import com.yashveer.lovable_clone.dto.chat.StreamResponse;
import com.yashveer.lovable_clone.entity.ChatSession;
import com.yashveer.lovable_clone.entity.ChatSessionId;
import com.yashveer.lovable_clone.entity.Project;
import com.yashveer.lovable_clone.entity.User;
import com.yashveer.lovable_clone.error.ResourceNotFoundException;
import com.yashveer.lovable_clone.llm.PromptUtils;
import com.yashveer.lovable_clone.repository.ChatSessionRepository;
import com.yashveer.lovable_clone.repository.ProjectRepository;
import com.yashveer.lovable_clone.repository.UserRepository;
import com.yashveer.lovable_clone.security.AuthUtil;
import com.yashveer.lovable_clone.service.AiGenerationService;
import com.yashveer.lovable_clone.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerationServiceImpl implements AiGenerationService {

    private final ChatClient chatClient;
    private final AuthUtil authUtil;
    private final ChatSessionRepository chatSessionRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectFileService projectFileService;

    private static final Pattern FILE_TAG_PATTERN = Pattern.compile("<file path=\"([^\"]+)\">(.*?)</file>", Pattern.DOTALL);


    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public Flux<StreamResponse> streamResponse(String userMessage, Long projectId) {

        Long userId = authUtil.getCurrentUserId();
        ChatSession chatSession = createChatSessionIfNotExists(projectId, userId);

        Map<String, Object> advisorParams = Map.of(
                "userId", userId,
                "projectId", projectId
        );

        StringBuilder fullResponseBuffer = new StringBuilder();

        return chatClient.prompt()
                .system(PromptUtils.CODE_GENERATION_SYSTEM_PROMPT)
                .user(userMessage)
                .advisors(advisorSpec -> {
                            advisorSpec.params(advisorParams);
                        }
                )
                .stream()
                .chatResponse()
                .doOnNext(response -> {
                    String content = response.getResult().getOutput().getText();
                    fullResponseBuffer.append(content);
                })
                .doOnComplete(() -> {
                    Schedulers.boundedElastic().schedule(() -> {
                        parseAndSaveFiles(fullResponseBuffer.toString(), projectId);

                    });
                })
                .doOnError(error -> log.error("Error during streaming for projectId: {}", projectId))
                .map(response -> {
                    String text = response.getResult().getOutput().getText();
                    return new StreamResponse(text != null ? text : "");
                });
    }

    private void parseAndSaveFiles(String fullResponse, Long projectId) {
        String dummy = """
                        
                        """;

        Matcher matcher = FILE_TAG_PATTERN.matcher(fullResponse);

        while(matcher.find()){
            String filePath = matcher.group(1);
            String fileContent = matcher.group(2).trim();

            projectFileService.saveFile(projectId,filePath,fileContent);
        }
    }

    private ChatSession createChatSessionIfNotExists(Long projectId, Long userId) {
        ChatSessionId chatSessionId = new ChatSessionId(projectId, userId);
        ChatSession chatSession = chatSessionRepository.findById(chatSessionId).orElse(null);

        if(chatSession == null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", projectId.toString()));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

            chatSession = ChatSession.builder()
                    .id(chatSessionId)
                    .project(project)
                    .user(user)
                    .build();

            chatSession = chatSessionRepository.save(chatSession);
        }
        return chatSession;
    }
}
