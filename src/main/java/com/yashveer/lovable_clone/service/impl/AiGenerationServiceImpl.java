package com.yashveer.lovable_clone.service.impl;

import com.yashveer.lovable_clone.dto.chat.StreamResponse;
import com.yashveer.lovable_clone.service.AiGenerationService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
@Service
public class AiGenerationServiceImpl implements AiGenerationService {

    @Override
    public Flux<StreamResponse> streamResponse(String message, Long projectId) {
        return null;
    }
}
