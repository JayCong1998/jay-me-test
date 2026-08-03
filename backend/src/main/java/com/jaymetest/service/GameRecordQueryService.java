package com.jaymetest.service;

import cn.dev33.satoken.stp.StpUtil;
import com.jaymetest.mapper.GameRecordMapper;
import com.jaymetest.model.dto.GameRecordDTO;
import com.jaymetest.service.game.support.GameRecordDTOAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameRecordQueryService {
    private final GameRecordMapper gameRecordMapper;
    private final GameRecordDTOAssembler gameRecordDTOAssembler;

    public List<GameRecordDTO> getMyRecords(int page, int size) {
        return gameRecordMapper.selectByUserId(StpUtil.getLoginIdAsLong(), size, (page - 1) * size)
                .stream().map(gameRecordDTOAssembler::toDTO).toList();
    }
}
