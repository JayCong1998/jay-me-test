package com.jaymetest.service.game;

import com.jaymetest.model.dto.AlbumResultDTO;
import com.jaymetest.model.dto.GameResultDTO;
import com.jaymetest.service.AlbumProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 专辑解锁钩子 — 仅在 ALBUM 模式下生效。
 * 通关后自动解锁下一张专辑，并将专辑结果注入 GameResultDTO。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlbumUnlockHook implements PostSubmitHook {

    private final AlbumProgressService albumProgressService;

    @Override
    public void afterSubmit(String albumKey, int correctCount, int totalQuestions, GameResultDTO.GameResultDTOBuilder builder, Long userId) {
        if (albumKey == null || userId == null) {
            return;
        }
        AlbumResultDTO albumResult = albumProgressService.processAlbumCompletion(
                userId, albumKey, correctCount, totalQuestions);
        builder.albumResult(albumResult);
    }
}
