package com.jaymetest.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaymetest.mapper.GameRecordMapper;
import com.jaymetest.model.admin.AdminRecordDTO;
import com.jaymetest.model.admin.PageResponse;
import com.jaymetest.model.entity.GameRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRecordService {

    private final GameRecordMapper gameRecordMapper;

    public PageResponse<AdminRecordDTO> list(String keyword, String mode, LocalDateTime startAt,
                                             LocalDateTime endAt, int page, int size) {
        LambdaQueryWrapper<GameRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String like = keyword.trim();
            wrapper.and(w -> w.like(GameRecord::getNickname, like).or().like(GameRecord::getRoundId, like));
        }
        if (StringUtils.hasText(mode)) {
            wrapper.eq(GameRecord::getMode, mode.trim());
        }
        if (startAt != null) {
            wrapper.ge(GameRecord::getCreatedAt, startAt);
        }
        if (endAt != null) {
            wrapper.le(GameRecord::getCreatedAt, endAt);
        }
        wrapper.orderByDesc(GameRecord::getCreatedAt);

        Page<GameRecord> result = gameRecordMapper.selectPage(
                new Page<>(Math.max(page, 1), Math.min(Math.max(size, 1), 100)), wrapper);
        List<AdminRecordDTO> records = result.getRecords().stream().map(this::toDTO).toList();
        return PageResponse.<AdminRecordDTO>builder()
                .records(records)
                .total(result.getTotal())
                .page(result.getCurrent())
                .size(result.getSize())
                .build();
    }

    private AdminRecordDTO toDTO(GameRecord record) {
        return AdminRecordDTO.builder()
                .id(record.getId())
                .roundId(record.getRoundId())
                .mode(record.getMode())
                .albumKey(record.getAlbumKey())
                .userId(record.getUserId())
                .nickname(record.getNickname())
                .totalQuestions(record.getTotalQuestions())
                .correctCount(record.getCorrectCount())
                .timeSpentSecs(record.getTimeSpentSecs())
                .usedRevival(record.getUsedRevival())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
