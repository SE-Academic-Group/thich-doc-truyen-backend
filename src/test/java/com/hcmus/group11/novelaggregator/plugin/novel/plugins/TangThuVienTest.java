package com.hcmus.group11.novelaggregator.plugin.novel.plugins;

import com.hcmus.group11.novelaggregator.exception.type.HttpException;
import com.hcmus.group11.novelaggregator.type.NovelDetail;
import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import com.hcmus.group11.novelaggregator.type.*;

import com.hcmus.group11.novelaggregator.util.RequestAttributeUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TangThuVienTest {

    private  TangThuVien tangThuVien;

    @BeforeEach
    public void setUp(){
        tangThuVien = new TangThuVien();
    }

    // buildSearchUrl
    @Test
    public void testBuildSearchUrl_NormalInput() {
        String keyword = "test";
        Integer page = 1;
        String expectedUrl = "https://truyen.tangthuvien.vn/ket-qua-tim-kiem?term=test&page=1";
        assertEquals(expectedUrl, tangThuVien.buildSearchUrl(keyword, page));
    }

    @Test
    public void testBuildSearchUrl_SpecialCharacters() {
        String keyword = "test@#%";
        Integer page = 1;
        String expectedUrl = "https://truyen.tangthuvien.vn/ket-qua-tim-kiem?term=test@#%&page=1";
        assertEquals(expectedUrl, tangThuVien.buildSearchUrl(keyword, page));
    }

    @Test
    public void testBuildSearchUrl_EmptyKeyword() {
        String keyword = "";
        Integer page = 1;
        String expectedUrl = "https://truyen.tangthuvien.vn/ket-qua-tim-kiem?term=&page=1";
        assertEquals(expectedUrl, tangThuVien.buildSearchUrl(keyword, page));
    }

    @Test
    public void testBuildSearchUrl_NullKeyword() {
        String keyword = null;
        Integer page = 1;
        String expectedUrl = "https://truyen.tangthuvien.vn/ket-qua-tim-kiem?term=null&page=1";
        assertEquals(expectedUrl, tangThuVien.buildSearchUrl(keyword, page));
    }

    @Test
    public void testBuildSearchUrl_ZeroPageNumber() {
        String keyword = "test";
        Integer page = 0;
        String expectedUrl = "https://truyen.tangthuvien.vn/ket-qua-tim-kiem?term=test&page=0";
        assertEquals(expectedUrl, tangThuVien.buildSearchUrl(keyword, page));
    }

    @Test
    public void testBuildSearchUrl_NegativePageNumber() {
        String keyword = "test";
        Integer page = -1;
        String expectedUrl = "https://truyen.tangthuvien.vn/ket-qua-tim-kiem?term=test&page=-1";
        assertEquals(expectedUrl, tangThuVien.buildSearchUrl(keyword, page));
    }

    @Test
    public void testBuildSearchUrl_NullPageNumber() {
        String keyword = "test";
        Integer page = null;
        String expectedUrl = "https://truyen.tangthuvien.vn/ket-qua-tim-kiem?term=test&page=null";
        assertEquals(expectedUrl, tangThuVien.buildSearchUrl(keyword, page));
    }


    // parseSearchHTML
    @Test
    public void testParseSearchHTML_NormalInput() {
        String htmlContent = "<html><body><div class='book-img-text'><ul><li>"
                + "<div class='book-mid-info'><h4><a href='url1'>Title1</a></h4></div>"
                + "<p class='author'><a class='name'>Author1</a><span class='KIBoOgno'>5</span></p>"
                + "<div class='book-img-box'><img src='image_url1'/></div>"
                + "</li></ul></div></body></html>";
        Document document = Jsoup.parse(htmlContent);
        List<NovelSearchResult> results = tangThuVien.parseSearchHTML(document);

        assertEquals(1, results.size());
        NovelSearchResult result = results.get(0);
        assertEquals("Title1", result.getTitle());
        assertEquals("Author1", result.getAuthor());
        assertEquals("image_url1", result.getImage());
        assertEquals("url1", result.getUrl());
        assertEquals(5, result.getNChapter());
    }

    @Test
    public void testParseSearchHTML_MultipleEntries() {
        String htmlContent = "<html><body><div class='book-img-text'><ul>"
                + "<li><div class='book-mid-info'><h4><a href='url1'>Title1</a></h4></div>"
                + "<p class='author'><a class='name'>Author1</a><span class='KIBoOgno'>5</span></p>"
                + "<div class='book-img-box'><img src='image_url1'/></div></li>"
                + "<li><div class='book-mid-info'><h4><a href='url2'>Title2</a></h4></div>"
                + "<p class='author'><a class='name'>Author2</a><span class='KIBoOgno'>10</span></p>"
                + "<div class='book-img-box'><img src='image_url2'/></div></li>"
                + "</ul></div></body></html>";
        Document document = Jsoup.parse(htmlContent);
        List<NovelSearchResult> results = tangThuVien.parseSearchHTML(document);

        assertEquals(2, results.size());

        NovelSearchResult result1 = results.get(0);
        assertEquals("Title1", result1.getTitle());
        assertEquals("Author1", result1.getAuthor());
        assertEquals("image_url1", result1.getImage());
        assertEquals("url1", result1.getUrl());
        assertEquals(5, result1.getNChapter());

        NovelSearchResult result2 = results.get(1);
        assertEquals("Title2", result2.getTitle());
        assertEquals("Author2", result2.getAuthor());
        assertEquals("image_url2", result2.getImage());
        assertEquals("url2", result2.getUrl());
        assertEquals(10, result2.getNChapter());
    }

    @Test
    public void testParseSearchHTML_MissingFields() {
        String htmlContent = "<html><body><div class='book-img-text'><ul><li>"
                + "<div class='book-mid-info'><h4><a href='url1'>Title1</a></h4></div>"
                + "<p class='author'><span class='KIBoOgno'>5</span></p>"
                + "<div class='book-img-box'><img src='image_url1'/></div>"
                + "</li></ul></div></body></html>";
        Document document = Jsoup.parse(htmlContent);
        List<NovelSearchResult> results = tangThuVien.parseSearchHTML(document);

        assertEquals(0, results.size());
    }

    @Test
    public void testParseSearchHTML_EmptyList() {
        String htmlContent = "<html><body><div class='book-img-text'><ul></ul></div></body></html>";
        Document document = Jsoup.parse(htmlContent);
        List<NovelSearchResult> results = tangThuVien.parseSearchHTML(document);

        assertEquals(0, results.size());
    }

    @Test
    public void testParseSearchHTML_InvalidChapterNumber() {
        String htmlContent = "<html><body><div class='book-img-text'><ul><li>"
                + "<div class='book-mid-info'><h4><a href='url1'>Title1</a></h4></div>"
                + "<p class='author'><a class='name'>Author1</a><span class='KIBoOgno'>five</span></p>"
                + "<div class='book-img-box'><img src='image_url1'/></div>"
                + "</li></ul></div></body></html>";
        Document document = Jsoup.parse(htmlContent);

        List<NovelSearchResult> result = tangThuVien.parseSearchHTML(document);
        assertEquals(0, result.size());
    }

    // parseNovelDetailHTML
    @Test
    public void testParseNovelDetailHTML_NormalInput() {
        String htmlContent = "<html><body><div class='book-info'>"
                + "<h1>Title1</h1>"
                + "<p class='tag'><a class='blue'>Author1</a></p></div>"
                + "<div class='book-img'><img src='image_url1'/></div>"
                + "<a id='j-bookCatalogPage'>5 chapters</a>"
                + "<div class='book-intro'><p>Description1<br>Line2</p></div>"
                + "<div class='book-info'><p class='tag'><a class='red'>Genre1</a><a class='red'>Genre2</a></p></div>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent, "https://example.com");
        NovelDetail result = tangThuVien.parseNovelDetailHTML(document);

        assertEquals("Title1", result.getTitle());
        assertEquals("Author1", result.getAuthor());
        assertEquals("image_url1", result.getImage());
        assertEquals("https://example.com", result.getUrl());
        assertEquals(5, result.getNChapter());
        assertEquals("Description1 Line2", result.getDescription());
        assertEquals(List.of("Genre1", "Genre2"), result.getGenres());
    }

    @Test
    public void testParseNovelDetailHTML_MissingAuthor() {
        String htmlContent = "<html><body><div class='book-info'>"
                + "<h1>Title1</h1>"
                + "<p class='tag'></p></div>"
                + "<div class='book-img'><img src='image_url1'/></div>"
                + "<a id='j-bookCatalogPage'>5 chapters</a>"
                + "<div class='book-intro'><p>Description1<br>Line2</p></div>"
                + "<div class='book-info'><p class='tag'><a class='red'>Genre1</a><a class='red'>Genre2</a></p></div>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent, "https://example.com");
        assertThrows(NullPointerException.class, () -> {
            tangThuVien.parseNovelDetailHTML(document);
        });

    }

    @Test
    public void testParseNovelDetailHTML_MissingImage() {
        String htmlContent = "<html><body><div class='book-info'>"
                + "<h1>Title1</h1>"
                + "<p class='tag'><a class='blue'>Author1</a></p></div>"
                + "<a id='j-bookCatalogPage'>5 chapters</a>"
                + "<div class='book-intro'><p>Description1<br>Line2</p></div>"
                + "<div class='book-info'><p class='tag'><a class='red'>Genre1</a><a class='red'>Genre2</a></p></div>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent, "https://example.com");

        assertThrows(NullPointerException.class, () -> {
            tangThuVien.parseNovelDetailHTML(document);
        });
    }

    @Test
    public void testParseNovelDetailHTML_MissingDescription() {
        String htmlContent = "<html><body><div class='book-info'>"
                + "<h1>Title1</h1>"
                + "<p class='tag'><a class='blue'>Author1</a></p></div>"
                + "<div class='book-img'><img src='image_url1'/></div>"
                + "<a id='j-bookCatalogPage'>5 chapters</a>"
                + "<div class='book-info'><p class='tag'><a class='red'>Genre1</a><a class='red'>Genre2</a></p></div>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent, "https://example.com");
        assertThrows(NullPointerException.class, () -> {
            tangThuVien.parseNovelDetailHTML(document);
        });
    }

    @Test
    public void testParseNovelDetailHTML_InvalidChapterNumber() {
        String htmlContent = "<html><body><div class='book-info'>"
                + "<h1>Title1</h1>"
                + "<p class='tag'><a class='blue'>Author1</a></p></div>"
                + "<div class='book-img'><img src='image_url1'/></div>"
                + "<a id='j-bookCatalogPage'>five chapters</a>"
                + "<div class='book-intro'><p>Description1<br>Line2</p></div>"
                + "<div class='book-info'><p class='tag'><a class='red'>Genre1</a><a class='red'>Genre2</a></p></div>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent, "https://example.com");

        assertThrows(NumberFormatException.class, () -> {
            tangThuVien.parseNovelDetailHTML(document);
        });
    }

    @Test
    public void testParseNovelDetailHTML_EmptyGenreList() {
        String htmlContent = "<html><body><div class='book-info'>"
                + "<h1>Title1</h1>"
                + "<p class='tag'><a class='blue'>Author1</a></p></div>"
                + "<div class='book-img'><img src='image_url1'/></div>"
                + "<a id='j-bookCatalogPage'>5 chapters</a>"
                + "<div class='book-intro'><p>Description1<br>Line2</p></div>"
                + "<div class='book-info'><p class='tag'></p></div>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent, "https://example.com");
        NovelDetail result = tangThuVien.parseNovelDetailHTML(document);

        assertEquals("Title1", result.getTitle());
        assertEquals("Author1", result.getAuthor());
        assertEquals("image_url1", result.getImage());
        assertEquals("https://example.com", result.getUrl());
        assertEquals(5, result.getNChapter());
        assertEquals("Description1 Line2", result.getDescription());
        assertEquals(0, result.getGenres().size()); // No genres
    }

    @Test
    public void testParseNovelDetailHTML_BaseUrlHandling() {
        String htmlContent = "<html><body><div class='book-info'>"
                + "<h1>Title1</h1>"
                + "<p class='tag'><a class='blue'>Author1</a></p></div>"
                + "<div class='book-img'><img src='image_url1'/></div>"
                + "<a id='j-bookCatalogPage'>5 chapters</a>"
                + "<div class='book-intro'><p>Description1<br>Line2</p></div>"
                + "<div class='book-info'><p class='tag'><a class='red'>Genre1</a><a class='red'>Genre2</a></p></div>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent, "https://example.com/path/to/novel");
        NovelDetail result = tangThuVien.parseNovelDetailHTML(document);

        assertEquals("Title1", result.getTitle());
        assertEquals("Author1", result.getAuthor());
        assertEquals("image_url1", result.getImage());
        assertEquals("https://example.com/path/to/novel", result.getUrl()); // Ensuring correct base URL
        assertEquals(5, result.getNChapter());
        assertEquals("Description1 Line2", result.getDescription());
        assertEquals(List.of("Genre1", "Genre2"), result.getGenres());
    }


    // parseSearchMetadata
    @Test
    public void testParseSearchMetadata_NormalInput() {
        String htmlContent = "<html><body>"
                + "<ul class='pagination'>"
                + "<li><a href='#'>1</a></li>"
                + "<li><a href='#'>2</a></li>"
                + "<li><a href='#'>3</a></li>"
                + "</ul>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent);
        ResponseMetadata result = tangThuVien.parseSearchMetadata(document);

        assertEquals(3, result.getMetadataValue("maxPage"));
    }

    @Test
    public void testParseSearchMetadata_SinglePage() {
        String htmlContent = "<html><body>"
                + "<ul class='pagination'>"
                + "<li><a href='#'>1</a></li>"
                + "</ul>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent);
        ResponseMetadata result = tangThuVien.parseSearchMetadata(document);

        assertEquals(1, result.getMetadataValue("maxPage"));
    }

    @Test
    public void testParseSearchMetadata_MissingPagination() {
        String htmlContent = "<html><body>"
                + "<div class='content'>No pagination here</div>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent);
        ResponseMetadata result = tangThuVien.parseSearchMetadata(document);

        assertEquals(1, result.getMetadataValue("maxPage"));
    }

    @Test
    public void testParseSearchMetadata_NonNumericPageNumbers() {
        String htmlContent = "<html><body>"
                + "<ul class='pagination'>"
                + "<li><a href='#'>A</a></li>"
                + "<li><a href='#'>B</a></li>"
                + "<li><a href='#'>C</a></li>"
                + "</ul>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent);
        ResponseMetadata result = tangThuVien.parseSearchMetadata(document);

        assertEquals(1, result.getMetadataValue("maxPage"));
    }

    @Test
    public void testParseSearchMetadata_EmptyPagination() {
        String htmlContent = "<html><body>"
                + "<ul class='pagination'>"
                + "<li><a href='#'></a></li>"
                + "<li><a href='#'></a></li>"
                + "<li><a href='#'></a></li>"
                + "</ul>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent);
        ResponseMetadata result = tangThuVien.parseSearchMetadata(document);

        assertEquals(1, result.getMetadataValue("maxPage"));
    }


    @Test
    public void testParseChapterDetailHTML_MissingNovelTitle() {
        String htmlContent = "<html><body>"
                + "<h2>ChapterTitle</h2>"
                + "<div class='box-chap'>Chapter content<br>more content</div>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent, "https://example.com/chuong-1");

        Exception exception = assertThrows(HttpException.class, () -> {
            tangThuVien.parseChapterDetailHTML(document);
        });
        assertTrue(exception.getMessage().contains("Chapter not found"));
    }

    @Test
    public void testParseChapterDetailHTML_MissingChapterTitle() {
        String htmlContent = "<html><body>"
                + "<h1 class='truyen-title'><a>NovelTitle</a></h1>"
                + "<div class='box-chap'>Chapter content<br>more content</div>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent, "https://example.com/chuong-1");

        Exception exception = assertThrows(HttpException.class, () -> {
            tangThuVien.parseChapterDetailHTML(document);
        });
        assertTrue(exception.getMessage().contains("Chapter not found"));
    }

    @Test
    public void testParseChapterDetailHTML_MissingContent() {
        String htmlContent = "<html><body>"
                + "<h1 class='truyen-title'><a>NovelTitle</a></h1>"
                + "<h2>ChapterTitle</h2>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent, "https://example.com/chuong-1");

        Exception exception = assertThrows(HttpException.class, () -> {
            tangThuVien.parseChapterDetailHTML(document);
        });
        assertTrue(exception.getMessage().contains("Chapter not found"));
    }

    @Test
    public void testParseChapterDetailHTML_NonNumericId() {
        String htmlContent = "<html><body>"
                + "<h1 class='truyen-title'><a>NovelTitle</a></h1>"
                + "<h2>ChapterTitle</h2>"
                + "<div class='box-chap'>Chapter content<br>more content</div>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent, "https://example.com/chuong-abc");

        Exception exception = assertThrows(NumberFormatException.class, () -> {
            tangThuVien.parseChapterDetailHTML(document);
        });
        assertTrue(exception.getMessage().contains("For input string"));
    }

    @Test
    public void testParseChapterDetailHTML_BaseUrlHandling() {
        String htmlContent = "<html><body>"
                + "<h1 class='truyen-title'><a>NovelTitle</a></h1>"
                + "<h2>ChapterTitle</h2>"
                + "<div class='box-chap'>Chapter content<br>more content</div>"
                + "</body></html>";
        Document document = Jsoup.parse(htmlContent, "https://example.com/path/to/chuong-1");
        ChapterDetail result = tangThuVien.parseChapterDetailHTML(document);

        assertEquals("https://example.com/path/to/chuong-1", result.getUrl());
    }


    // NormalizeString
    @Test
    public void testNormalizeString_RemoveParenthesesContent() {
        String input = "(test) Example String";
        String expected = "example string";
        assertEquals(expected, tangThuVien.normalizeString(input, true));
    }

    @Test
    public void testNormalizeString_RemoveBracketsContent() {
        String input = "[test] Example String";
        String expected = "example string";
        assertEquals(expected, tangThuVien.normalizeString(input, true));
    }

    @Test
    public void testNormalizeString_RemoveChineseCharacters() {
        String input = "Example String - 中文字符";
        String expected = "example string";
        assertEquals(expected, tangThuVien.normalizeString(input, true));
    }

    @Test
    public void testNormalizeString_RemoveDiacritics() {
        String input = "Tiếng Việt có dấu";
        String expected = "tieng viet co dau";
        assertEquals(expected, tangThuVien.normalizeString(input, true));
    }

    @Test
    public void testNormalizeString_EmptyString() {
        String input = "";
        String expected = "";
        assertEquals(expected, tangThuVien.normalizeString(input, true));
    }

    @Test
    public void testNormalizeString_OnlyParenthesesContent() {
        String input = "(test)";
        String expected = "";
        assertEquals(expected, tangThuVien.normalizeString(input, true));
    }

    @Test
    public void testNormalizeString_CombinationOfAll() {
        String input = "[Test] Example (remove) String - 中文字符!@#2024";
        String expectedSpaceTrue = "example remove string";
        String expectedSpaceFalse = "examplestring2024";
        assertEquals(expectedSpaceTrue, tangThuVien.normalizeString(input, true));
        assertNotEquals(expectedSpaceFalse, tangThuVien.normalizeString(input, false));
    }


}