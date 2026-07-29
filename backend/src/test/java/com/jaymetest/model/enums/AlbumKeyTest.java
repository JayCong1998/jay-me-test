package com.jaymetest.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlbumKeyTest {

    @Test
    void includesChildrenOfTheSunAsTheSixteenthAlbum() {
        assertEquals(16, AlbumKey.values().length);
        assertEquals("太阳之子", AlbumKey.SUN_CHILD.getDisplayName());
        assertEquals(2026, AlbumKey.SUN_CHILD.getYear());
        assertTrue(AlbumKey.SUN_CHILD.isLast());
    }
}
