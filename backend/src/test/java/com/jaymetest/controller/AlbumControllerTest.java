package com.jaymetest.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.jaymetest.model.dto.AlbumDTO;
import com.jaymetest.model.dto.RoundDTO;
import com.jaymetest.service.AlbumProgressService;
import com.jaymetest.service.QuestionService;
import com.jaymetest.service.game.strategy.impl.AlbumGameStrategy;
import com.jaymetest.service.game.cache.RoundCacheManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlbumControllerTest {

    @Test
    void albumListAndRoundStayInTheAlbumControllerEntry() {
        AlbumProgressService albumProgressService = mock(AlbumProgressService.class);
        QuestionService questionService = mock(QuestionService.class);
        AlbumGameStrategy albumStrategy = mock(AlbumGameStrategy.class);
        RoundCacheManager cacheManager = mock(RoundCacheManager.class);
        List<AlbumDTO> albums = List.of(new AlbumDTO());
        RoundDTO round = new RoundDTO();
        when(albumProgressService.getAlbumList(7L)).thenReturn(albums);
        when(questionService.album()).thenReturn(albumStrategy);
        when(questionService.cacheManager()).thenReturn(cacheManager);
        when(albumStrategy.generateRound("JAY", cacheManager)).thenReturn(round);
        AlbumController controller = new AlbumController(albumProgressService, questionService);

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(7L);

            assertSame(albums, controller.getAlbumList().getData());
            assertSame(round, controller.getAlbumRound("JAY").getData());
        }

        verify(albumProgressService).canAccessAlbum(7L, "JAY");
        verify(albumStrategy).generateRound("JAY", cacheManager);
    }
}
