package com.hcmus.group11.novelaggregator.plugin.novel.plugins;

import com.aspose.words.HtmlLoadOptions;
import com.aspose.words.LoadOptions;
import com.aspose.words.SaveFormat;
import com.hcmus.group11.novelaggregator.exception.type.HttpException;
import com.hcmus.group11.novelaggregator.plugin.novel.BaseCrawler;
import com.hcmus.group11.novelaggregator.type.*;
import com.hcmus.group11.novelaggregator.util.RequestAttributeUtil;
import com.hcmus.group11.novelaggregator.util.UnicodeRemover;
import com.ironsoftware.ironpdf.License;
import com.ironsoftware.ironpdf.PdfDocument;
import com.ironsoftware.ironpdf.Settings;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TangThuVien extends BaseCrawler {

    String licenseKey = "IRONSU ITE.PHUOCNHANTRANONE.GMAIL.COM.19627-FC5FEE9C2D-INXGX-VYU7MHFUBMGB-ED3PKWHT3AMU-UTUZKSBPC72L-CX5JGC3TRI7V-S2AI7AMHQ7JT-DZL6THKRKTWV-5DP5YB-T2HOSDH6JXCMUA-DEPLOYMENT.TRIAL-5ESGQZ.TRIAL.EXPIRES.04.JUL.2024";

    public TangThuVien() {
        pluginName = "tangThuVien";
        pluginUrl = "https://truyen.tangthuvien.vn";
    }

    @Override
    public String buildSearchUrl(String keyword, Integer page) {
        return pluginUrl + "/ket-qua-tim-kiem?term=" + keyword + "&page=" + page;
    }

    @Override
    protected List<NovelSearchResult> parseSearchHTML(Document html) {
        List<NovelSearchResult> novelSearchResults = new ArrayList<>();

        //        Get ul child from div.book-img-text
        Elements lis = html.select("div.book-img-text ul li");

        try {
            for (Element li : lis) {
                // Extract information
                String title = li.selectFirst("div.book-mid-info h4 a").text();
                String author = li.selectFirst("p.author a.name").text();
                String image = li.selectFirst("div.book-img-box img").attr("src");
                String url = li.selectFirst("div.book-mid-info h4 a").attr("href");
                String nChapterText = li.selectFirst("p.author span.KIBoOgno").text();
                Integer nChapter = Integer.parseInt(nChapterText);

                NovelSearchResult novelSearchResult = new NovelSearchResult(title, author, image, url, nChapter);
                novelSearchResults.add(novelSearchResult);
            }
        } catch (Exception e) {
            return novelSearchResults;
        }

        return novelSearchResults;
    }

    @Override
    protected NovelDetail parseNovelDetailHTML(Document html) {
        String title = html.selectFirst("div.book-info h1").text();
        String author = html.selectFirst("div.book-info p.tag a.blue").text();
        String image = html.selectFirst("div.book-img img").attr("src");
        String url = html.baseUri();
        String nChapterText = html.selectFirst("a#j-bookCatalogPage").text().replaceAll("[^0-9]", "");
        Integer nChapter = Integer.parseInt(nChapterText);
        String description = html.selectFirst("div.book-intro p").text();

        Elements genreElements = html.select("div.book-info p.tag a.red");
        List<String> genres = new ArrayList<>();
        for (Element genreElement : genreElements) {
            genres.add(genreElement.text());
        }

        NovelDetail novelDetail = new NovelDetail(title, author, image, url, nChapter, description, genres);
        return novelDetail;
    }

    @Override
    protected ResponseMetadata parseSearchMetadata(Document html) {
        Elements pages = html.select("ul.pagination li");
//        Get highest page number
        Integer maxPage = 1;
        for (Element page : pages) {
            Element pageElement = page.selectFirst("a") == null ? page.selectFirst("span") : page.selectFirst("a");
            if (pageElement == null) {
                continue;
            }
            String pageNumber = page.text();
            pageNumber = pageNumber.replaceAll("[^0-9]", "");
            if (pageNumber.isEmpty()) {
                continue;
            }
            maxPage = Math.max(maxPage, Integer.parseInt(pageNumber));
        }

        ResponseMetadata responseMetadata = new ResponseMetadata();
        responseMetadata.addMetadataValue("maxPage", maxPage);

        return responseMetadata;
    }

    @Override
    protected ChapterDetail parseChapterDetailHTML(Document html) {
        String novelTitle = null;

        String title = null;
        String content = null;
        try {
            novelTitle = html.selectFirst("h1.truyen-title a").text();

            title = html.selectFirst("h2").text();
            //        Replace all nbsp with space
            title = title.replaceAll("\u00A0", " ");
            content = html.selectFirst(".box-chap").text();
        } catch (Exception e) {
            throw HttpException.NOT_FOUND("NOT_FOUND", "Chapter not found");
        }

        String url = html.baseUri();

        String prevPage, nextPage;
        // Get current id from url
        String currentIdStr = url.substring(url.lastIndexOf("-") + 1);
        Integer currentId = Integer.parseInt(currentIdStr);
        // Get prevPage and nextPage
        if (currentId == 1) {
            prevPage = null;
        } else {
            prevPage = url.substring(0, url.lastIndexOf("/") + 1) + "chuong-" + (currentId - 1);
        }
        nextPage = url.substring(0, url.lastIndexOf("/") + 1) + "chuong-" + (currentId + 1);


        ChapterDetail chapterDetail = new ChapterDetail(novelTitle, title, url, content);
        ResponseMetadata metadata = new ResponseMetadata();
        metadata.addMetadataValue("prevPage", prevPage);
        metadata.addMetadataValue("nextPage", nextPage);
        metadata.addMetadataValue("name", pluginName);
        RequestAttributeUtil.setAttribute("metadata", metadata);

        return chapterDetail;
    }

    @Override
    public List<ChapterInfo> getChapterList(String novelDetailUrl, Integer page) {
        page = Math.max(page - 1, 0);
        Document html = getHtml(novelDetailUrl);
        String storyId = html.selectFirst("input#story_id_hidden").attr("value");
        String url = this.pluginUrl + "/doc-truyen/page/" + storyId + "?page=" + page + "&limit=75&web=1";

        Document chapterListHtml = getHtml(url);

        List<ChapterInfo> chapterInfos = parseChapterListHTML(chapterListHtml);
        ResponseMetadata metadata = parseChapterListMetadata(chapterListHtml);
        metadata.addMetadataValue("currentPage", page + 1);
        metadata.addMetadataValue("name", pluginName);

        RequestAttributeUtil.setAttribute("metadata", metadata);

        return chapterInfos;
    }

    @Override
    public List<ChapterInfo> getFullChapterList(String novelUrl) {
        Integer page = 0;
        Document html = getHtml(novelUrl);
        String storyId = html.selectFirst("input#story_id_hidden").attr("value");
        Integer MaxPage = maxChapterListPage(html);

        List<CompletableFuture<List<ChapterInfo>>> futures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<ChapterInfo> result = new ArrayList<>();

        for (Integer i = 0; i <= MaxPage; i++) {
            // Set waiting time for each page
            page = i;
            int delay = 100 * i;
            String urlPage = this.pluginUrl + "/doc-truyen/page/" + storyId + "?page=" + page + "&limit=75&web=1";

            CompletableFuture<List<ChapterInfo>> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            Thread.sleep(delay);
                            return getHtmlWithRetry(urlPage, 3, 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }, executor)
                    .thenApply(this::parseChapterListHTML)
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return new ArrayList<ChapterInfo>();
                    });

            futures.add(future);
        }

        List<List<ChapterInfo>> chapterInfos = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        for (List<ChapterInfo> chapterInfoList : chapterInfos) {
            result.addAll(chapterInfoList);
        }

        executor.shutdown();
        return result;
    }

    public Integer maxChapterListPage(Document html) {
        Elements paginationItems = html.select("ul.pagination li a");

        int lastLoadingNumber = -1;
        int maxLoadingNumber = -1;

        for (Element item : paginationItems) {
            String onclickAttr = item.attr("onclick");
            if (onclickAttr.startsWith("Loading(")) {
                String numberString = onclickAttr.replaceAll("[^0-9]", "");
                int number = Integer.parseInt(numberString);

                maxLoadingNumber = Math.max(maxLoadingNumber, number);

                if (item.text().contains("Trang cuối")) {
                    lastLoadingNumber = number;
                    break;
                }
            }
        }

        int result = (lastLoadingNumber != -1) ? lastLoadingNumber : maxLoadingNumber;

        return result;

    }

    private List<ChapterInfo> parseChapterListHTML(Document html) {
        List<ChapterInfo> chapterInfos = new ArrayList<>();
        Elements chapterElements = html.select("ul li");
        for (Element chapterElement : chapterElements) {
            Element chapterTitleElement = chapterElement.selectFirst("a");

            // If chapterTitleElement is null => this "li" is the Divider chap => no url, just title and span
            if (chapterTitleElement == null) {
                ChapterInfo chapterInfo = new ChapterInfo();
                chapterInfo.setTitle(chapterElement.selectFirst("span").text());
                chapterInfos.add(chapterInfo);
                continue;
            }


            String title = chapterElement.selectFirst("a").text();
            String url = chapterElement.selectFirst("a").attr("href");
//            Title format is "Chương {chapterIndex} : {chapterTitle}"
            int startIndex = title.indexOf("Chương") + "Chương".length() + 1;
            int endIndex = title.indexOf(":");
            String chapterIndex = "";
            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                chapterIndex = title.substring(startIndex, endIndex).trim();
            } else {
                continue;
            }

            if (chapterIndex.endsWith(" ")) {
                chapterIndex = chapterIndex.substring(0, chapterIndex.length() - 1);
            }

            if (endIndex != -1 && endIndex < title.length() - 1) {
                title = title.split(":")[1].trim();
            }

            ChapterInfo chapterInfo = new ChapterInfo(title, url, chapterIndex);
            chapterInfos.add(chapterInfo);
        }

        return chapterInfos;
    }

    private ResponseMetadata parseChapterListMetadata(Document html) {
        Elements pages = html.select("ul.pagination li a");
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

        // Bước 1: Loại bỏ các ký tự trong cặp dấu () hoặc [] ở đầu chuỗi
        str = str.replaceAll("^[\\[(].*?[\\])]\\s*", "");

        // Bước 2: Loại bỏ các ký tự Trung Quốc phía sau
        str = str.replaceAll("\\s*-\\s*[\\u4e00-\\u9fff]+.*$", "");

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
    protected Object convertToEpub(Document html) {
        Element header = html.selectFirst("head");

        Elements novelName = html.select("h1.truyen-title a");
        Elements chapterName = html.select("div.col-xs-12 h2");

        String novelNameHtml = "<h1>" + novelName.text() + "</h1>";
        String chapterNameHtml = "<h2>" + chapterName.text() + "</h2>";

        Element storyDetailContent = html.selectFirst("div.box-chap");

        String storyDetailContentHtml = storyDetailContent.html().replaceAll("\\.(?!\")", ".<br><br>")
                .replaceAll("\\?\"", "?\"<br><br>")
                .replaceAll("~\"", "~\"<br><br>")
                .replaceAll("!\"", "!\"<br><br>")
                .replaceAll("\\.\"", ".\"<br><br>");

        Book book = new Book();
        Metadata metadata = book.getMetadata();

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                header.html() +
                "<body>\n" +
                novelNameHtml +
                chapterNameHtml +
                "<p>----------------</p>" +
                storyDetailContentHtml +
                "</body>\n" +
                "</html>";

        // Set the title
        metadata.addTitle(novelName.text());
        try {
            // Add HTML content as a section (chapter)
            book.addSection(chapterName.text(), new Resource(htmlContent.getBytes(StandardCharsets.UTF_8), "chapter1.html"));

            String epubFilePath = UnicodeRemover.removeUnicode(chapterName.text()) + ".epub";
            System.out.println("EPUB file path: " + epubFilePath);

            // Write the book to an EPUB file
            try (FileOutputStream out = new FileOutputStream(epubFilePath)) {
                EpubWriter epubWriter = new EpubWriter();
                epubWriter.write(book, out);
            }
            System.out.println("EPUB file created successfully!");

            File epubFile = new File(epubFilePath);
            if (!epubFile.exists()) {
                throw new RuntimeException("Khong the tai file");
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
            throw new RuntimeException("Can't download a file");
        }
    }

    @Override
    protected Object convertToPDF(Document html) throws IOException {
        Element header = html.selectFirst("head");
        Elements novelName = html.select("h1.truyen-title a");
        Elements chapterName = html.select("div.col-xs-12 h2");
        String novelNameHtml = "<h1>" + novelName.text() + "</h1>";
        String chapterNameHtml = "<h2>" + chapterName.text() + "</h2>";

        Element storyDetailContent = html.selectFirst("div.box-chap");

        String storyDetailContentHtml = storyDetailContent.html().replaceAll("\\.(?!\")", ".<br><br>")
                .replaceAll("\\?\"", "?\"<br><br>")
                .replaceAll("~\"", "~\"<br><br>")
                .replaceAll("!\"", "!\"<br><br>")
                .replaceAll("\\.\"", ".\"<br><br>");

        String res = header + "\n"
                + novelNameHtml + "\n"
                + chapterNameHtml + "\n"
                + storyDetailContentHtml + "\n";

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
    protected Object convertToImg(Document html) throws IOException {
        Element header = html.selectFirst("head");
        Elements novelName = html.select("h1.truyen-title a");
        Elements chapterName = html.select("div.col-xs-12 h2");
        String novelNameHtml = "<h1>" + novelName.text() + "</h1>";
        String chapterNameHtml = "<h2>" + chapterName.text() + "</h2>";

        Element storyDetailContent = html.selectFirst("div.box-chap");

        String storyDetailContentHtml = storyDetailContent.html().replaceAll("\\.(?!\")", ".<br><br>")
                .replaceAll("\\?\"", "?\"<br><br>")
                .replaceAll("~\"", "~\"<br><br>")
                .replaceAll("!\"", "!\"<br><br>")
                .replaceAll("\\.\"", ".\"<br><br>");

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                header +
                "<body>\n" +
                novelNameHtml +
                chapterNameHtml +
                "<p>----------------</p>" +
                storyDetailContentHtml +
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
