package com.jaymetest.service;

import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.model.dto.AnswerResultDTO;
import com.jaymetest.model.dto.RoundDTO;
import com.jaymetest.model.entity.Question;
import com.jaymetest.service.game.ClassicGameStrategy;
import com.jaymetest.service.game.AlbumGameStrategy;
import com.jaymetest.service.game.AbyssGameStrategy;
import com.jaymetest.service.game.GameRoundCache;
import com.jaymetest.service.game.GameStrategyFactory;
import com.jaymetest.service.game.RoundCacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 题目服务 — 对外统一入口，内部委托给各游戏模式策略。
 *
 * <p>Controller 层可直接注入策略/工厂，也可继续通过本 Service 调用（向后兼容）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionMapper questionMapper;
    private final GameStrategyFactory strategyFactory;
    private final RoundCacheManager cacheManager;

    // ---- 显式注入策略 Bean（供 Controller 直接使用） ----

    private final ClassicGameStrategy classicStrategy;
    private final AlbumGameStrategy albumStrategy;
    private final AbyssGameStrategy abyssStrategy;

    /**
     * 随机抽取一局题目（经典/专辑模式）。
     * @deprecated Controller 可改为直接调用 {@link ClassicGameStrategy} 或 {@link AlbumGameStrategy}
     */
    @Deprecated
    public RoundDTO generateRound(int count, String album) {
        if (album != null && !album.isEmpty()) {
            return albumStrategy.generateRound(count, album, cacheManager);
        }
        return classicStrategy.generateRound(count, null, cacheManager);
    }

    /**
     * 校验单题答案（经典/专辑模式共用）。
     */
    public AnswerResultDTO checkAnswer(String roundId, Long questionId, String selectedOption) {
        GameRoundCache cache = cacheManager.getOrThrow(roundId);

        String correctOption = cache.getAnswerMap().get(questionId);
        if (correctOption == null) {
            throw new BusinessException(404, "题目不存在于该回合中");
        }

        boolean correct = correctOption.equalsIgnoreCase(selectedOption.trim().toUpperCase());
        Question question = questionMapper.selectById(questionId);

        return AnswerResultDTO.builder()
                .correct(correct)
                .correctOption(correctOption)
                .explanation(question != null ? question.getExplanation() : "暂无解析")
                .build();
    }

    /**
     * 获取正确答案（复活用）。
     */
    public String getCorrectAnswer(String roundId, Long questionId) {
        GameRoundCache cache = cacheManager.getOrThrow(roundId);
        String correctOption = cache.getAnswerMap().get(questionId);
        if (correctOption == null) {
            throw new BusinessException(404, "题目不存在于该回合中");
        }
        return correctOption;
    }

    // ============================================================
    // 策略直通方法（Controller 可直接获取策略实例）
    // ============================================================

    public ClassicGameStrategy classic() {
        return classicStrategy;
    }

    public AlbumGameStrategy album() {
        return albumStrategy;
    }

    public AbyssGameStrategy abyss() {
        return abyssStrategy;
    }

    public GameStrategyFactory factory() {
        return strategyFactory;
    }

    public RoundCacheManager cacheManager() {
        return cacheManager;
    }
}
