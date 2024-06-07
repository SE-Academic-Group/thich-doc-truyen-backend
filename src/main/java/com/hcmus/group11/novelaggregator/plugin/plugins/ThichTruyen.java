package com.hcmus.group11.novelaggregator.plugin.plugins;

import com.aspose.words.HtmlLoadOptions;
import com.aspose.words.LoadOptions;
import com.aspose.words.SaveFormat;
import com.hcmus.group11.novelaggregator.exception.type.HttpException;
import com.hcmus.group11.novelaggregator.plugin.BaseCrawler;
import com.hcmus.group11.novelaggregator.type.*;
import com.hcmus.group11.novelaggregator.util.RequestAttributeUtil;
import com.hcmus.group11.novelaggregator.util.UnicodeRemover;
import com.ironsoftware.ironpdf.License;
import com.ironsoftware.ironpdf.PdfDocument;
import com.ironsoftware.ironpdf.Settings;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ThichTruyen extends BaseCrawler {

    String licenseKey = "IRONSU ITE.PHUOCNHANTRANONE.GMAIL.COM.19627-FC5FEE9C2D-INXGX-VYU7MHFUBMGB-ED3PKWHT3AMU-UTUZKSBPC72L-CX5JGC3TRI7V-S2AI7AMHQ7JT-DZL6THKRKTWV-5DP5YB-T2HOSDH6JXCMUA-DEPLOYMENT.TRIAL-5ESGQZ.TRIAL.EXPIRES.04.JUL.2024";
    public ThichTruyen() {
        pluginName = "thichTruyen";
        pluginUrl = "https://thichtruyen.vn";
    }

    @Override
    public String buildSearchUrl(String keyword, Integer page) {
        return pluginUrl + "/tim-kiem?q=" + keyword + "&page=" + page;
    }

    @Override
    protected List<NovelSearchResult> parseSearchHTML(Document html) {
        List<NovelSearchResult> novelSearchResults = new ArrayList<>();

        //        Get ul child from div.book-img-text
        Elements lis = html.select("div.view-category-item");

        for (Element li : lis) {
            // Extract information
            String title = li.selectFirst("h3.view-category-item-title").text();
            String url = this.pluginUrl + "/" + li.selectFirst("a").attr("href");

            String author = li.selectFirst("p.view-category-item-author a").text();
            String image = this.pluginUrl + "/" + li.selectFirst("div.view-category-item-image img").attr("src");
            Elements infor_list = li.select("div.view-category-item-infor p");
            String nChapterText = infor_list.get(2).text().split(" ")[0];
            Integer nChapter = Integer.parseInt(nChapterText);

            NovelSearchResult novelSearchResult = new NovelSearchResult(title, author, image, url, nChapter);
            novelSearchResults.add(novelSearchResult);
        }

        return novelSearchResults;
    }

    @Override
    protected NovelDetail parseNovelDetailHTML(Document html) {

        String title = html.selectFirst("h1.story-intro-title").text();
        String author = html.selectFirst("p.story-intro-author a").text();
        String image = pluginUrl + "/" + html.selectFirst("div.story-intro-image img").attr("src");
        String url = html.baseUri() + html.selectFirst("h1.story-intro-title a").attr("href");
        String nChapterText = html.selectFirst("p.story-intro-chapper").text().replaceAll("[^0-9]", "");
        Integer nChapter = Integer.parseInt(nChapterText);
        String description = html.selectFirst("div#tab-over div.tab-text p").text();
        if (description.equals("Đọc Truyện")) {
            description = html.selectFirst("div#tab-over div.tab-text").text();
        }

        description = description.replaceAll("\u00A0", " ");

// Lấy thể loại
        Element lst_tag = html.selectFirst("div.lst-tag");
        Elements genreElements = lst_tag.select("a");
        List<String> genres = new ArrayList<>();
        for (Element genreElement : genreElements) {
            genres.add(genreElement.text());
        }

        NovelDetail novelDetail = new NovelDetail(title, author, image, url, nChapter, description, genres);
        return novelDetail;
    }

    @Override
    protected ResponseMetadata parseSearchMetadata(Document html) {
        Elements pages = html.select("div.pagination a");
//        Get highest page number
        Integer maxPage = 1;
        for (Element page : pages) {
            String pageNumber = page.text();
            pageNumber = pageNumber.replaceAll("[^0-9]", "");
            if (pageNumber.isEmpty()) {
                continue;
            }
            maxPage = Math.max(maxPage, Integer.parseInt(pageNumber));
        }

        String currentPageString = html.selectFirst("div.pagination strong").text();
        Integer currentPage = Integer.parseInt(currentPageString);

        if (currentPage > maxPage) {
            maxPage = currentPage;
        }

        ResponseMetadata responseMetadata = new ResponseMetadata();
        responseMetadata.addMetadataValue("maxPage", maxPage);

        return responseMetadata;
    }

    @Override
    protected ChapterDetail parseChapterDetailHTML(Document html) {
        Elements lis = html.select("div.main-breadcrumb ul li");
        String novelTitle = lis.get(2).selectFirst("a").text();
        String url = this.pluginUrl + "/" + lis.get(2).selectFirst("a").attr("href");

        String title = html.selectFirst("div.story-detail-header h1").text();
//        Replace all nbsp with space
        title = title.replaceAll("\u00A0", " ");
        String content = html.selectFirst("div.story-detail-content").text();
        content = content.replaceAll("(?i)<br\\s*/?>", "");

        ChapterDetail chapterDetail = new ChapterDetail(novelTitle, title, url, content);

        String nextChapter = html.selectFirst("div.next-previous a").attr("href");
        if (nextChapter.contains("javascript")) {
            nextChapter = null;
        } else {
            nextChapter = this.pluginUrl + "/" + nextChapter;
        }

        String previousChapter = html.selectFirst("div.prev-previous a").attr("href");
        if (previousChapter.contains("javascript")) {
            previousChapter = null;
        } else {
            previousChapter = this.pluginUrl + "/" + previousChapter;
        }

        ResponseMetadata metadata = new ResponseMetadata();
        metadata.addMetadataValue("nextPage", nextChapter);
        metadata.addMetadataValue("prevPage", previousChapter);
        RequestAttributeUtil.setAttribute("metadata", metadata);

        return chapterDetail;
    }


    @Override
    public List<ChapterInfo> getChapterList(String novelDetailUrl, Integer page) {

        Document chapterListHtml = getHtml(novelDetailUrl);

        List<ChapterInfo> chapterInfos = parseChapterListHTML(chapterListHtml);
        ResponseMetadata metadata = parseChapterListMetadata(chapterListHtml);
        metadata.addMetadataValue("currentPage", page);
        metadata.addMetadataValue("name", pluginName);

        RequestAttributeUtil.setAttribute("metadata", metadata);

        return chapterInfos;
    }

    @Override
    public List<ChapterInfo> getFullChapterList(String url) {
        Document chapterListHtml = getHtml(url);

        List<ChapterInfo> chapterInfos = parseChapterListHTML(chapterListHtml);

        return chapterInfos;
    }

    private List<ChapterInfo> parseChapterListHTML(Document html) {
        List<ChapterInfo> chapterInfos = new ArrayList<>();
        Elements chapterElements = html.select("div.tab-text ul li");

        Integer chapterIndex = 0;
        for (Element chapterElement : chapterElements) {
            Element chapterInfoElement = chapterElement.selectFirst("a");

            String title = chapterInfoElement.attr("title");
            String url = this.pluginUrl + "/" + chapterInfoElement.attr("href");

            chapterIndex++;
            ChapterInfo chapterInfo = new ChapterInfo(title, url, chapterIndex.toString());
            chapterInfos.add(chapterInfo);
        }

        return chapterInfos;
    }

    private ResponseMetadata parseChapterListMetadata(Document html) {
        Elements pages = html.select("div.pagination a");
//        Get page array
        List<Integer> pageArray = new ArrayList<>();
        Integer maxPage = 1;
        for (Element page : pages) {
            String pageNumber = page.text();
            pageNumber = pageNumber.replaceAll("[^0-9]", "");
            if (pageNumber.isEmpty()) {
                continue;
            }
            pageArray.add(Integer.parseInt(pageNumber));
            maxPage = Math.max(maxPage, Integer.parseInt(pageNumber));
        }

        ResponseMetadata responseMetadata = new ResponseMetadata();
        responseMetadata.addMetadataValue("pageArray", pageArray);
        responseMetadata.addMetadataValue("maxPage", maxPage);
        return responseMetadata;
    }

    @Override
    public String normalizeString(String str, Boolean isSpace) {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = str.replaceAll("\\p{M}", ""); // Loại bỏ các dấu

        // Bước 4: Chuyển về chữ thường và loại bỏ các ký tự đặc biệt, chỉ giữ lại chữ cái và số
        str = str.toLowerCase().replaceAll("đ", "d");

        if (isSpace) {
            str = str.replaceAll("[^a-z0-9 ]", "");
        } else {
            str = str.replaceAll("[^a-z0-9]", "");
        }

        return str;
    }
    @Override
    protected Object convertToEpub(Document html) throws IOException {

        Elements storyDetailHeader = html.select("div.story-detail-header");
        Element header = html.selectFirst("head");
        Elements storyDetailContent = html.select("div.story-detail-content");
        // Get Novel name
        Elements breadCrumb = html.select("div.main-breadcrumb ul.breadcrumb li");
        String novelName = breadCrumb.get(2).text();
        String novelNameHtml = "<p>" + novelName + "</p>";
        // Get Story detail header

        // Chapter name
        Elements chapterName = html.select("div.story-detail-header h1");
        // Author name
        Elements authorName = html.select("div.story-detail-header p.story-detail-author");

        assert header != null;

        Book book = new Book();
        Metadata metadata = book.getMetadata();
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                header.html() +
                "<body>\n" +
                novelNameHtml +
                storyDetailHeader.html() +
                "<p>----------------</p>" +
                storyDetailContent.html() +
                "</body>\n" +
                "</html>";

        // Set the title
        metadata.addTitle(novelName);
        // Add an Author
        metadata.addAuthor(new Author(authorName.text()));

        try {
            ByteArrayInputStream htmlInputStream = new ByteArrayInputStream(htmlContent.getBytes(StandardCharsets.UTF_8));
//            FileInputStream htmlInputStream = new FileInputStream(htmlInputStream);
            Resource htmlResource = new Resource(htmlInputStream, "file.html");
            String epubFilePath = UnicodeRemover.removeUnicode(chapterName.text()) + ".epub";
            book.addSection(chapterName.text(), htmlResource);

            // Write the book to an EPUB file
            try (FileOutputStream out = new FileOutputStream(epubFilePath)) {
                EpubWriter epubWriter = new EpubWriter();
                epubWriter.write(book, out);
            }
            System.out.println("EPUB file created successfully!");

            File epubFile = new File(epubFilePath);
            if (!epubFile.exists()) {
                throw new RuntimeException("Can't download a file");
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(epubFilePath));

            // Thiết lập headers HTTP

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + epubFile.getName());
            headers.add(HttpHeaders.CONTENT_TYPE, "application/epub+zip");
            // Create ResponseEntity with EPUB content
            ResponseEntity<byte[]> responseEntity = ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(epubFile.length())
                    .contentType(MediaType.parseMediaType("application/epub+zip"))
                    .body(resource.getContentAsByteArray());

            epubFile.delete();

            return responseEntity;
        } catch (IOException e) {
            throw new RuntimeException("Khong the tai file");
        }
    }

    @Override
    protected Object convertToPDF(Document html) throws IOException {
        Elements storyDetailHeader = html.select("div.story-detail-header");
        Element header = html.selectFirst("head");
        Elements storyDetailContent = html.select("div.story-detail-content");
        // Get Novel name
        Elements breadCrumb = html.select("div.main-breadcrumb ul.breadcrumb li");
        String novelName = breadCrumb.get(2).text();
        String novelNameHtml = "<p>" + novelName + "</p>";
        // Get Story detail header

        // Chapter name
        Elements chapterName = html.select("div.story-detail-header h1");
        // Author name
        Elements authorName = html.select("div.story-detail-header p.story-detail-author");

        String res = header + "\n" +
                novelNameHtml + "\n" +
                chapterName + "\n" +
                authorName + "\n" +
                storyDetailContent.html();

        License.setLicenseKey(licenseKey);

        Settings.setLogPath(Paths.get("C:/tmp/IronPdfEngine.log"));

        PdfDocument myPdf = PdfDocument.renderHtmlAsPdf(res);

        String pdfFilePath = UnicodeRemover.removeUnicode(chapterName.text()) + ".pdf";
        myPdf.saveAs(Paths.get(pdfFilePath));
        File pdfFile = new File(pdfFilePath);

        try {
            InputStreamResource resource = new InputStreamResource(new FileInputStream(pdfFilePath));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + pdfFile.getName());
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
            // Return ResponseEntity with PDF content
            ResponseEntity<byte[]> responseEntity = ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfFile.length())
                    .contentType(MediaType.parseMediaType("application/pdf"))
                    .body(resource.getContentAsByteArray());

            // Delete the PDF file after it's transferred
            pdfFile.delete();

            return responseEntity;
        } catch (IOException e) {
            throw new RuntimeException("Could not read file: " + pdfFilePath, e);
        }
    }

    @Override
    protected Object convertToImg(Document html) throws Exception {
        // Chuỗi HTML cần chuyển đổi
        Elements storyDetailHeader = html.select("div.story-detail-header");
        Element header = html.selectFirst("head");
        Elements storyDetailContent = html.select("div.story-detail-content");
        // Get Novel name
        Elements breadCrumb = html.select("div.main-breadcrumb ul.breadcrumb li");
        String novelName = breadCrumb.get(2).text();
        String novelNameHtml = "<p>" + novelName + "</p>";
        // Get Story detail header

        // Chapter name
        Elements chapterName = html.select("div.story-detail-header h1");
        // Author name
        Elements authorName = html.select("div.story-detail-header p.story-detail-author");

        String chapterNameHtml = "<h2>" + chapterName.text() + "</h2>";
        String authorNameHtml = "<p>" + authorName.text() + "</p>";

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                header +
                "<body>\n" +
                novelNameHtml +
                authorNameHtml +
                chapterNameHtml +
                "<p>----------------</p>" +
                storyDetailContent.html()+
                "</body>\n" +
                "</html>";

        try {
            // Tạo tài liệu từ chuỗi HTML
            ByteArrayInputStream inputStream = new ByteArrayInputStream(htmlContent.getBytes(StandardCharsets.UTF_8));
            LoadOptions loadOptions = new HtmlLoadOptions();
            com.aspose.words.Document doc = new com.aspose.words.Document(inputStream, loadOptions);

            // Lưu tài liệu xuống file HTML
            doc.save("input.html", SaveFormat.HTML);

            // Mở tài liệu từ file HTML
            doc = new com.aspose.words.Document("input.html");

            // Thực hiện các thao tác cần thiết trên tài liệu
            for (int page = 0; page < doc.getPageCount(); page++) {
                com.aspose.words.Document extractedPage = doc.extractPages(page, 1);
                extractedPage.save(String.format(UnicodeRemover.removeUnicode(chapterName.text()) + "_%d.png", page + 1), SaveFormat.PNG);
            }

            // Create a zip file containing the images
            String zipFilePath = UnicodeRemover.removeUnicode(chapterName.text()) + ".zip";
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
                for (int page = 0; page < doc.getPageCount(); page++) {
                    String imagePath = String.format(UnicodeRemover.removeUnicode(chapterName.text()) + "_%d.png", page + 1);
                    zos.putNextEntry(new ZipEntry(imagePath));
                    try (InputStream is = new FileInputStream(imagePath)) {
                        StreamUtils.copy(is, zos);
                    }
                    zos.closeEntry();
                }
            }

            // Prepare the zip file to be sent as a response
            File zipFile = new File(zipFilePath);
            InputStreamResource resource = new InputStreamResource(new FileInputStream(zipFilePath));

            // Set HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + zipFile.getName());
            headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");

            // Create the response entity
            ResponseEntity<byte[]> responseEntity = ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(zipFile.length())
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(resource.getContentAsByteArray());

            // Delete the images and the zip file after sending the response
            for (int page = 0; page < doc.getPageCount(); page++) {
                Files.deleteIfExists(Paths.get(String.format(UnicodeRemover.removeUnicode(chapterName.text()) + "_%d.png", page + 1)));
            }
            Files.deleteIfExists(Paths.get(zipFilePath));
            File trashFile1 = new File("input.001.png");
            File trashFile2 = new File("input.html");

            trashFile1.delete();
            trashFile2.delete();
            return responseEntity;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
