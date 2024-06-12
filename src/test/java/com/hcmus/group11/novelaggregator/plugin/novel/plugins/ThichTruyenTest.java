package com.hcmus.group11.novelaggregator.plugin.novel.plugins;

import com.hcmus.group11.novelaggregator.type.ChapterDetail;
import com.hcmus.group11.novelaggregator.type.NovelDetail;
import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import com.hcmus.group11.novelaggregator.type.ResponseMetadata;
import com.hcmus.group11.novelaggregator.util.RequestAttributeUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ThichTruyenTest {

    private ThichTruyen thichTruyen;

    @BeforeEach
    void setUp() {
        thichTruyen = new ThichTruyen();
    }


    @Test
    public void testBuildSearchUrl_NormalInput() {
        String keyword = "novel";
        int page = 2;
        String expectedUrl = "https://thichtruyen.vn/tim-kiem?q=novel&page=2";
        assertEquals(expectedUrl, thichTruyen.buildSearchUrl(keyword, page));
    }

    @Test
    public void testBuildSearchUrl_SpecialCharacters() {
        String keyword = "a+b&c=d";
        int page = 3;
        String expectedUrl = "https://thichtruyen.vn/tim-kiem?q=a+b&c=d&page=3";
        assertEquals(expectedUrl, thichTruyen.buildSearchUrl(keyword, page));
    }

    @Test
    public void testBuildSearchUrl_UnicodeCharacters() {
        String keyword = "truyện";
        int page = 5;
        String expectedUrl = "https://thichtruyen.vn/tim-kiem?q=truyện&page=5";
        assertEquals(expectedUrl, thichTruyen.buildSearchUrl(keyword, page));
    }

    @Test
    public void testBuildSearchUrl_EmptyKeyword() {
        String keyword = "";
        int page = 1;
        String expectedUrl = "https://thichtruyen.vn/tim-kiem?q=&page=1";
        assertEquals(expectedUrl, thichTruyen.buildSearchUrl(keyword, page));
    }


    //
    @Test
    public void testParseSearchHTML() {
        String html = "<div class='view-category-item'>"
                + "<h3 class='view-category-item-title'>Test Title</h3>"
                + "<a href='novel/test-url'></a>"
                + "<p class='view-category-item-author'><a>Test Author</a></p>"
                + "<div class='view-category-item-image'><img src='test.jpg'/></div>"
                + "<div class='view-category-item-infor'><p>Test</p><p>Test</p><p>10 chapters</p></div>"
                + "</div>";

        Document doc = Jsoup.parse(html);
        List<NovelSearchResult> results = thichTruyen.parseSearchHTML(doc);

        assertEquals(1, results.size());
        NovelSearchResult result = results.get(0);
        assertEquals("Test Title", result.getTitle());
        assertEquals("https://thichtruyen.vn/novel/test-url", result.getUrl());
        assertEquals("Test Author", result.getAuthor());
        assertEquals("https://thichtruyen.vn/test.jpg", result.getImage());
        assertEquals(10, result.getNChapter());
    }

    @Test
    public void testParseNovelDetailHTML() {
        String html = "<header><base href='https://thichtruyen.vn/'></header>"
                + "<h1 class='story-intro-title'>Test Novel</h1>"
                + "<p class='story-intro-author'><a>Test Author</a></p>"
                + "<div class='story-intro-image'><img src='test.jpg'/></div>"
                + "<h1 class='story-intro-title'><a href='test-href-title'>Test Title</a></h1>"
                + "<p class='story-intro-chapper'>10 chapters</p>"
                + "<div id='tab-over'><div class='tab-text'><p>Test Description</p></div></div>"
                + "<div class='lst-tag'><a>Genre1</a><a>Genre2</a></div>";

        Document doc = Jsoup.parse(html);
        NovelDetail detail = thichTruyen.parseNovelDetailHTML(doc);

        assertEquals("Test Novel", detail.getTitle());
        assertEquals("Test Author", detail.getAuthor());
        assertEquals("https://thichtruyen.vn/test.jpg", detail.getImage());
        assertEquals("https://thichtruyen.vn/test-href-title", detail.getUrl());
        assertEquals("Test Description", detail.getDescription());
        assertEquals(10, detail.getNChapter());
        List<String> genres = detail.getGenres();
        assertEquals(2, genres.size());
        assertTrue(genres.contains("Genre1"));
        assertTrue(genres.contains("Genre2"));
    }

    @Test
    public void testParseChapterDetailHTML() {
        String html = "<div class='main-breadcrumb'><ul><li><a></a></li><li><a></a></li><li><a href='novel/test-novel'>Test Novel</a></li></ul></div>"
                + "<div class='story-detail-header'><h1>Test Chapter</h1></div>"
                + "<div class='story-detail-content'>Test Content</div>"
                + "<div class='next-previous'><a href='next-chapter'></a></div>"
                + "<div class='prev-previous'><a href='/prev-chapter'></a></div>";

        Document doc = Jsoup.parse(html);

        ResponseMetadata mockMetadata = mock(ResponseMetadata.class);
        when(mockMetadata.getMetadataValue("nextPage")).thenReturn("https://thichtruyen.vn/next-chapter");
        when(mockMetadata.getMetadataValue("prevPage")).thenReturn("https://thichtruyen.vn/prev-chapter");

        RequestAttributeUtil.setAttribute("metadata", mockMetadata);

        ChapterDetail detail = thichTruyen.parseChapterDetailHTML(doc);

        // Assert the parsed details
        assertEquals("Test Novel", detail.getNovelTitle());
        assertEquals("Test Chapter", detail.getTitle());
        assertEquals("https://thichtruyen.vn/novel/test-novel", detail.getUrl());
        assertEquals("Test Content", detail.getContent());

    }

    @Test
    public void testNormalizeString_RemoveParenthesesContent() {
        String input = "(test) Example String";
        String expected = "test example string";
        assertEquals(expected, thichTruyen.normalizeString(input, true));
    }

    @Test
    public void testNormalizeString_RemoveBracketsContent() {
        String input = "[test] Example String";
        String expected = "test example string";
        assertEquals(expected, thichTruyen.normalizeString(input, true));
    }

    @Test
    public void testNormalizeString_RemoveChineseCharacters() {
        String input = "Example String - 中文字符";
        String expected = "example string  ";
        assertEquals(expected, thichTruyen.normalizeString(input, true));
    }

    @Test
    public void testNormalizeString_RemoveDiacritics() {
        String input = "Tiếng Việt có dấu";
        String expected = "tieng viet co dau";
        assertEquals(expected, thichTruyen.normalizeString(input, true));
    }

    @Test
    public void testNormalizeString_CombinationOfAll() {
        String input = "[Test] Example (remove) String - 中文字符!@#2024";
        String expectedSpaceTrue = "test example remove string  2024";
        String expectedSpaceFalse = "examplestring2024";
        assertEquals(expectedSpaceTrue, thichTruyen.normalizeString(input, true));
        assertNotEquals(expectedSpaceFalse, thichTruyen.normalizeString(input, false));
    }
}