package com.jaymetest.service;

import com.jaymetest.exception.BusinessException;
import com.jaymetest.config.AlbumGameProperties;
import com.jaymetest.mapper.AlbumProgressMapper;
import com.jaymetest.model.dto.AlbumDTO;
import com.jaymetest.model.dto.AlbumResultDTO;
import com.jaymetest.model.entity.AlbumProgress;
import com.jaymetest.model.enums.AlbumKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 专辑进度服务：列表、解锁、通关处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumProgressService {

    private final AlbumProgressMapper albumProgressMapper;
    private final AlbumGameProperties albumGameProperties;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取用户专辑列表（含解锁状态）
     * 新用户自动创建第一张专辑的进度记录
     */
    public List<AlbumDTO> getAlbumList(Long userId) {
        List<AlbumProgress> progressList = albumProgressMapper.selectByUserId(userId);
        Map<String, AlbumProgress> progressMap = progressList.stream()
                .collect(Collectors.toMap(AlbumProgress::getAlbumKey, p -> p));

        List<AlbumDTO> result = new ArrayList<>();
        for (AlbumKey album : AlbumKey.values()) {
            AlbumProgress progress = progressMap.get(album.getDisplayName());

            if (progress == null && album.isFirst()) {
                // 新用户：自动解锁第一张专辑
                progress = createInitialProgress(userId, album);
                progressMap.put(album.name(), progress);
            }

            result.add(AlbumDTO.builder()
                    .albumKey(album.getDisplayName())
                    .displayName(album.getDisplayName())
                    .year(album.getYear())
                    .unlocked(progress != null && progress.getUnlocked() == 1)
                    .bestScore(progress != null ? progress.getBestScore() : 0)
                    .totalAttempts(progress != null ? progress.getTotalAttempts() : 0)
                    .isFirst(album.isFirst())
                    .isLast(album.isLast())
                    .build());
        }
        return result;
    }

    /**
     * 处理专辑通关结果
     * 在 StatsService.submitResult 中调用
     */
    public AlbumResultDTO processAlbumCompletion(Long userId, String albumKey, int correctCount, int totalQuestions) {
        AlbumKey album = AlbumKey.fromDisplayName(albumKey);
        AlbumProgress progress = albumProgressMapper.selectByUserAndAlbum(userId, albumKey);

        boolean isNewRecord = false;
        int newBestScore = correctCount;
        int newTotalAttempts = 1;

        if (progress == null) {
            // 首次挑战该专辑
            progress = new AlbumProgress();
            progress.setUserId(userId);
            progress.setAlbumKey(albumKey);
            progress.setUnlocked(1); // 能挑战说明已解锁
            progress.setBestScore(correctCount);
            progress.setTotalAttempts(1);
        } else {
            newTotalAttempts = progress.getTotalAttempts() + 1;
            if (correctCount > progress.getBestScore()) {
                newBestScore = correctCount;
                isNewRecord = true;
            } else {
                newBestScore = progress.getBestScore();
            }
        }

        String now = LocalDateTime.now().format(DT_FMT);
        boolean passed = correctCount * 100 >= albumGameProperties.getPassAccuracy() * totalQuestions;

        if (progress.getId() == null) {
            // INSERT
            progress.setBestScore(newBestScore);
            progress.setTotalAttempts(newTotalAttempts);
            progress.setFirstPassedAt(passed ? now : null);
            progress.setLastAttemptedAt(now);
            albumProgressMapper.insert(progress);
        } else {
            // UPDATE
            progress.setBestScore(newBestScore);
            progress.setTotalAttempts(newTotalAttempts);
            if (passed && progress.getFirstPassedAt() == null) {
                progress.setFirstPassedAt(now);
            }
            progress.setLastAttemptedAt(now);
            albumProgressMapper.updateById(progress);
        }

        // 通关后解锁下一张专辑
        boolean unlockedNext = false;
        String nextAlbumKey = null;
        String nextAlbumDisplayName = null;

        if (passed) {
            AlbumKey next = album.next();
            if (next != null) {
                AlbumProgress nextProgress = albumProgressMapper.selectByUserAndAlbum(userId, next.getDisplayName());
                if (nextProgress == null) {
                    nextProgress = new AlbumProgress();
                    nextProgress.setUserId(userId);
                    nextProgress.setAlbumKey(next.getDisplayName());
                    nextProgress.setUnlocked(1);
                    nextProgress.setBestScore(0);
                    nextProgress.setTotalAttempts(0);
                    albumProgressMapper.insert(nextProgress);
                    log.info("解锁新专辑 userId={}, album={}", userId, next.getDisplayName());
                } else if (nextProgress.getUnlocked() == 0) {
                    nextProgress.setUnlocked(1);
                    albumProgressMapper.updateById(nextProgress);
                    log.info("解锁新专辑 userId={}, album={}", userId, next.getDisplayName());
                }
                unlockedNext = true;
                nextAlbumKey = next.getDisplayName();
                nextAlbumDisplayName = next.getDisplayName();
            }
        }

        log.info("专辑进度已更新 userId={}, album={}, correctCount={}, passed={}, isNewRecord={}",
                userId, album.getDisplayName(), correctCount, passed, isNewRecord);

        return AlbumResultDTO.builder()
                .albumKey(albumKey)
                .albumDisplayName(album.getDisplayName())
                .passed(passed)
                .albumBestScore(newBestScore)
                .isNewRecord(isNewRecord)
                .unlockedNext(unlockedNext)
                .nextAlbumKey(nextAlbumKey)
                .nextAlbumDisplayName(nextAlbumDisplayName)
                .build();
    }

    /**
     * 校验用户是否有权限访问某专辑关卡
     */
    public void canAccessAlbum(Long userId, String albumKey) {
        AlbumKey album;
        try {
            album = AlbumKey.fromDisplayName(albumKey);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "无效的专辑标识: " + albumKey);
        }

        AlbumProgress progress = albumProgressMapper.selectByUserAndAlbum(userId, albumKey);
        if (progress == null || progress.getUnlocked() == 0) {
            AlbumKey previous = album.previous();
            String prevName = previous != null ? previous.getDisplayName() : "未知";
            throw new BusinessException(403, "请先通关前置专辑「" + prevName + "」("
                    + albumGameProperties.getPassAccuracy() + "% 正确率) 才能解锁本关");
        }
    }

    /**
     * 为新用户创建第一张专辑的初始进度
     */
    private AlbumProgress createInitialProgress(Long userId, AlbumKey album) {
        AlbumProgress progress = new AlbumProgress();
        progress.setUserId(userId);
        progress.setAlbumKey(album.getDisplayName());
        progress.setUnlocked(1);
        progress.setBestScore(0);
        progress.setTotalAttempts(0);
        albumProgressMapper.insert(progress);
        log.info("为新用户初始化首张专辑进度 userId={}, album={}", userId, album.getDisplayName());
        return progress;
    }
}
