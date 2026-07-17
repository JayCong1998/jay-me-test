package com.jaymetest.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.jaymetest.model.dto.AiQueryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 问答接口（需登录）
 *
 * @author pyc
 * @since 2026-07-16 16:30
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@SaCheckLogin
public class AiController implements InitializingBean {

    private final ChatModel dashScopeChatModel;

    private ChatClient chatClient;

    @PostMapping("/query")
    public Flux<String> queryJayChouQuestion(@Valid @RequestBody AiQueryRequest request) {
        log.info("AI 问答请求: message={}", request.getMessage());
        return chatClient.prompt()
                .user(request.getMessage())
                .stream()
                .content();
    }

    @Override
    public void afterPropertiesSet() {
        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .temperature(0.7)
                                .build()
                )
                .build();
    }
}
