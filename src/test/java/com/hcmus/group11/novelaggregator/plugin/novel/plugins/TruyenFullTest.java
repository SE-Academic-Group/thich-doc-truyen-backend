package com.hcmus.group11.novelaggregator.plugin.novel.plugins;

import com.hcmus.group11.novelaggregator.type.ChapterDetail;
import com.hcmus.group11.novelaggregator.type.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TruyenFullTest {
    private TruyenFull truyenFull;

    @BeforeEach
    public void setUp() {
        truyenFull = new TruyenFull();
    }

    // GetChapterDetailFromJsonString
    @Test
    void testGetChapterDetailFromJsonString() {
        //String json = "{ \"data\": { \"story_name\": \"Test Novel\", \"chapter_name\": \"Chapter 1\", \"content\": \"This is a test content\", \"chapter_id\": 1, \"chapter_next\": 2, \"chapter_prev\": null }, \"status\": \"success\" }";
        String json = """
        {
            "status": "success",
            "data": {
                "story_name": "Test Novel",
                "chapter_name": "Chapter 1",
                "content": "This is a test content",
                "chapter_id": 1,
                "chapter_next": 2,
                "chapter_prev": null
            }
        }
        """;

        ChapterDetail chapterDetail = truyenFull.getChapterDetailFromJsonString(json);

        assertNotNull(chapterDetail);
        assertEquals("Test Novel", chapterDetail.getNovelTitle());
        assertEquals("Chapter 1", chapterDetail.getTitle());
        assertEquals("This is a test content", chapterDetail.getContent());
        assertEquals("https://api.truyenfull.vn/v1/chapter/detail/1", chapterDetail.getUrl());
    }

    @Test
    void testGetChapterDetailFromJsonString_ErrorStatus() {
        String json = "{ \"status\": \"error\" }";
        ChapterDetail chapterDetail = truyenFull.getChapterDetailFromJsonString(json);

        assertNull(chapterDetail);
    }

    @Test
    void testGetChapterDetailFromJsonString_MalformedJson() {
        String json = "{ \"data\": { \"story_name\": \"Test Novel\", \"chapter_name\": \"Chapter 1\", \"content\": \"This is a test content\", \"chapter_id\": 1, \"chapter_next\": 2, \"chapter_prev\": null, \"status\": \"success\" ";
        assertThrows(RuntimeException.class, () -> {
            truyenFull.getChapterDetailFromJsonString(json);
        }, "Malformed JSON string should throw an exception");
    }

    @Test
    void testGetChapterDetailFromJsonString_NullJson() {
        String json = null;
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            truyenFull.getChapterDetailFromJsonString(json);
        });

        String expectedMessage = "null";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }


    // GetChapterListFromJsonString
    @Test
    void testGetChapterListFromJsonString() {
        String json = "{ \"data\": [{ \"title\": \"Chapter 1\", \"id\": 1 }, { \"title\": \"Chapter 2\", \"id\": 2 }], \"status\": \"success\", \"meta\": { \"pagination\": { \"total_pages\": 1, \"current_page\": 1, \"per_page\": 2 } } }";
        List<ChapterInfo> chapterList = truyenFull.getChapterListFromJsonString(json);

        assertNotNull(chapterList);
        assertEquals(2, chapterList.size());
        assertEquals("Chapter 1", chapterList.get(0).getTitle());
        assertEquals("https://api.truyenfull.vn/v1/chapter/detail/1", chapterList.get(0).getUrl());
        assertEquals("Chapter 2", chapterList.get(1).getTitle());
        assertEquals("https://api.truyenfull.vn/v1/chapter/detail/2", chapterList.get(1).getUrl());
    }

    @Test
    void testGetChapterListFromJsonStringError() {
        String json = "{ \"status\": \"error\" }";
        List<ChapterInfo> chapterList = truyenFull.getChapterListFromJsonString(json);

        assertNull(chapterList);
    }

    // GetNovelDetailFromJsonString
    @Test
    void testGetNovelDetailFromJsonString() {
        String json = "{ \"data\": { \"title\": \"Test Novel\", \"author\": \"Test Author\", \"image\": \"test_image.jpg\", \"id\": 1, \"total_chapters\": 10, \"description\": \"Test description\", \"categories\": \"Category1, Category2\" }, \"status\": \"success\" }";
        NovelDetail novelDetail = truyenFull.getNovelDetailFromJsonString(json);

        assertNotNull(novelDetail);
        assertEquals("Test Novel", novelDetail.getTitle());
        assertEquals("Test Author", novelDetail.getAuthor());
        assertEquals("test_image.jpg", novelDetail.getImage());
        assertEquals("https://api.truyenfull.vn/v1/story/detail/1", novelDetail.getUrl());
        assertEquals(10, novelDetail.getNChapter());
        assertEquals("Test description", novelDetail.getDescription());
        assertEquals(2, novelDetail.getGenres().size());
        assertEquals("Category1", novelDetail.getGenres().get(0));
        assertEquals("Category2", novelDetail.getGenres().get(1));
    }

    @Test
    void testGetNovelDetailFromJsonStringError() {
        String json = "{ \"status\": \"error\" }";
        NovelDetail novelDetail = truyenFull.getNovelDetailFromJsonString(json);

        assertNull(novelDetail);
    }

    @Test
    void testGetSearchDataFromJsonString() {
        String json = "{ \"data\": [{ \"title\": \"Novel 1\", \"author\": \"Author 1\", \"image\": \"image1.jpg\", \"id\": 1, \"total_chapters\": 5 }, { \"title\": \"Novel 2\", \"author\": \"Author 2\", \"image\": \"image2.jpg\", \"id\": 2, \"total_chapters\": 10 }], \"meta\": { \"pagination\": { \"total_pages\": 1, \"current_page\": 1 } } }";
        List<NovelSearchResult> searchResults = truyenFull.getSearchDataFromJsonString(json);

        assertNotNull(searchResults);
        assertEquals(2, searchResults.size());
        assertEquals("Novel 1", searchResults.get(0).getTitle());
        assertEquals("Author 1", searchResults.get(0).getAuthor());
        assertEquals("image1.jpg", searchResults.get(0).getImage());
        assertEquals("https://api.truyenfull.vn/v1/story/detail/1", searchResults.get(0).getUrl());
        assertEquals(5, searchResults.get(0).getNChapter());
        assertEquals("Novel 2", searchResults.get(1).getTitle());
        assertEquals("Author 2", searchResults.get(1).getAuthor());
        assertEquals("image2.jpg", searchResults.get(1).getImage());
        assertEquals("https://api.truyenfull.vn/v1/story/detail/2", searchResults.get(1).getUrl());
        assertEquals(10, searchResults.get(1).getNChapter());
    }


    // BuildSearchUrl
    @Test
    void testBuildSearchUrl_SimpleKeyword() {
        String keyword = "example";
        int page = 1;
        String expectedUrl = "https://api.truyenfull.vn/v1/tim-kiem?title=example&page=1";
        String actualUrl = truyenFull.buildSearchUrl(keyword, page);

        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void testBuildSearchUrl_KeywordWithSpecialCharacters() {
        String keyword = "example@keyword#test";
        int page = 1;
        String expectedUrl = "https://api.truyenfull.vn/v1/tim-kiem?title=example@keyword#test&page=1";
        String actualUrl = truyenFull.buildSearchUrl(keyword, page);

        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void testBuildSearchUrl_NullKeyword() {
        String keyword = null;
        int page = 1;

        Exception exception = assertThrows(NullPointerException.class, () -> {
            truyenFull.buildSearchUrl(keyword, page);
        });

        String expectedMessage = "Cannot invoke \"java.lang.CharSequence.toString()\" because \"replacement\" is null";
        String actualMessage = exception.getMessage();
        System.out.println(actualMessage);

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testBuildSearchUrl_EmptyKeyword() {
        String keyword = "";
        int page = 1;
        String expectedUrl = "https://api.truyenfull.vn/v1/tim-kiem?title=&page=1";
        String actualUrl = truyenFull.buildSearchUrl(keyword, page);

        assertEquals(expectedUrl, actualUrl);
    }


    // NormalizeString
    @Test
    void testNormalizeString() {
        String input = "Hà Nội, Việt Nam!";
        String expectedOutput = "ha noi viet nam";
        String actualOutput = truyenFull.normalizeString(input, true);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void testNormalizeStringWithoutSpace() {
        String input = "Hà Nội, Việt Nam!";
        String expectedOutput = "hanoivietnam";
        String actualOutput = truyenFull.normalizeString(input, false);
        assertEquals(expectedOutput, actualOutput);
    }




}