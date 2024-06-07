package com.hcmus.group11.novelaggregator.plugin.plugins;

import com.aspose.words.HtmlLoadOptions;
import com.aspose.words.LoadOptions;
import com.aspose.words.SaveFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmus.group11.novelaggregator.exception.type.HttpException;
import com.hcmus.group11.novelaggregator.plugin.BaseApi;
import com.hcmus.group11.novelaggregator.type.*;
import com.hcmus.group11.novelaggregator.util.LevenshteinDistance;
import com.hcmus.group11.novelaggregator.util.RequestAttributeUtil;
import com.hcmus.group11.novelaggregator.util.UnicodeRemover;
import com.ironsoftware.ironpdf.License;
import com.ironsoftware.ironpdf.PdfDocument;
import com.ironsoftware.ironpdf.Settings;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class TruyenFull extends BaseApi {

    String licenseKey = "IRONSU ITE.PHUOCNHANTRANONE.GMAIL.COM.19627-FC5FEE9C2D-INXGX-VYU7MHFUBMGB-ED3PKWHT3AMU-UTUZKSBPC72L-CX5JGC3TRI7V-S2AI7AMHQ7JT-DZL6THKRKTWV-5DP5YB-T2HOSDH6JXCMUA-DEPLOYMENT.TRIAL-5ESGQZ.TRIAL.EXPIRES.04.JUL.2024";
    public TruyenFull() {
        this.pluginName = "truyenFull";
        this.pluginUrl = "https://truyenfull.vn";
    }

    public final String BASE_URL = "https://api.truyenfull.vn/v1";
    public final String SEARCH_URL = BASE_URL + "/tim-kiem?title={{keyword}}&page={{page}}";
    public final String NOVEL_DETAIL_BASE_URL = BASE_URL + "/story/detail/";
    public final String CHAPTER_DETAIL_BASE_URL = BASE_URL + "/chapter/detail/";

    @Override
    protected ChapterDetail getChapterDetailFromJsonString(String jsonChapterDetail) {
        ChapterDetail chapterDetail = new ChapterDetail();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(jsonChapterDetail, Map.class);
            Map<String, Object> data = (Map<String, Object>) jsonMap.get("data");
            String status = (String) jsonMap.get("status");

            if (status.equals("error")) {
                return null;
            } else {
                chapterDetail.setNovelTitle((String) data.get("story_name"));
                chapterDetail.setTitle((String) data.get("chapter_name"));

                String content = (String) data.get("content");
                chapterDetail.setContent(content);

                Integer id = (Integer) data.get("chapter_id");
                chapterDetail.setUrl(buildChapterDetailUrl(id));

                Integer nextChapterId = (Integer) data.get("chapter_next");
                Integer prevChapterId = (Integer) data.get("chapter_prev");

                Map<String, Optional> map = new HashMap<>();

                String nextChapterDetailUrl = null;
                if (nextChapterId != null) {
                    nextChapterDetailUrl = buildChapterDetailUrl(nextChapterId);
                }

                String prevChapterDetailUrl = null;
                if (prevChapterId != null) {
                    prevChapterDetailUrl = buildChapterDetailUrl(prevChapterId);
                }

                map.put("nextPage", Optional.ofNullable(nextChapterDetailUrl));

                map.put("prevPage", Optional.ofNullable(prevChapterDetailUrl));

                addMetaData(map);

                return chapterDetail;

            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected List<ChapterInfo> getChapterListFromJsonString(String jsonChapterList) {
        List<ChapterInfo> chapterList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(jsonChapterList, Map.class);
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) jsonMap.get("data");
            String status = (String) jsonMap.get("status");

            if (status.equals("error")) {
                return null;
            } else {
                Map<String, Object> meta = (Map<String, Object>) jsonMap.get("meta");
                Map<String, Object> pagination = (Map<String, Object>) meta.get("pagination");
                Map<String, Object> links = (Map<String, Object>) pagination.get("links");

                Integer perPage = (Integer) pagination.get("per_page");
                Integer maxPage = (Integer) pagination.get("total_pages");
                Integer currentPage = (Integer) pagination.get("current_page");
                //            String nextPageUrl = (String) links.get("next");
                //            String prevPageUrl = (String) links.get("previous");

                Integer startId = (currentPage - 1) * perPage;
                for (Map<String, Object> data : dataList) {
                    ChapterInfo chapterInfo = new ChapterInfo();
                    chapterInfo.setTitle((String) data.get("title"));
                    chapterInfo.setUrl(buildChapterDetailUrl((Integer) data.get("id")));
                    startId++;
                    chapterInfo.setIndex(startId.toString());

                    chapterList.add(chapterInfo);
                }

                Map<String, Optional> map = new HashMap<>();
                map.put("maxPage", Optional.ofNullable(maxPage));
                map.put("currentPage", Optional.ofNullable(currentPage));
                //            map.put("prevPageUrl", Optional.ofNullable(prevPageUrl));
                //            map.put("nextPageUrl", Optional.ofNullable(nextPageUrl));

                addMetaData(map);
                return chapterList;

            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected NovelDetail getNovelDetailFromJsonString(String jsonDetail) {
        NovelDetail novelDetail = new NovelDetail();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(jsonDetail, Map.class);
            Map<String, Object> data = (Map<String, Object>) jsonMap.get("data");
            String status = (String) jsonMap.get("status");
            if (status.equals("error")) {
                return null;
            } else {
                novelDetail.setTitle((String) data.get("title"));
                novelDetail.setAuthor((String) data.get("author"));
                novelDetail.setImage((String) data.get("image"));
                novelDetail.setUrl(buildNovelDetailUrl((Integer) data.get("id"), null));
                novelDetail.setNChapter((Integer) data.get("total_chapters"));
                String description = (String) data.get("description");
                novelDetail.setDescription(description);

                List<String> listCategories = Arrays.asList(((String) data.get("categories")).split(",\\s*"));
                novelDetail.setGenres(listCategories);
                return novelDetail;
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected List<NovelSearchResult> getSearchDataFromJsonString(String jsonString) {
        List<NovelSearchResult> novelSearchResults = new ArrayList<>();
        // Parse JSON using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(jsonString, Map.class);
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) jsonMap.get("data");

            for (Map<String, Object> data : dataList) {
                NovelSearchResult novelSearchResult = new NovelSearchResult();
                novelSearchResult.setTitle((String) data.get("title"));
                novelSearchResult.setAuthor((String) data.get("author"));
                novelSearchResult.setImage((String) data.get("image"));
                novelSearchResult.setUrl(buildNovelDetailUrl((Integer) data.get("id"), null));
                novelSearchResult.setNChapter((Integer) data.get("total_chapters"));

                novelSearchResults.add(novelSearchResult);

            }

            Map<String, Object> meta = (Map<String, Object>) jsonMap.get("meta");
            Map<String, Object> pagination = (Map<String, Object>) meta.get("pagination");

            Integer maxPage = (Integer) pagination.get("total_pages");
            Integer currentPage = (Integer) pagination.get("current_page");
            Map<String, Optional> map = new HashMap<>();
            map.put("maxPage", Optional.ofNullable(maxPage));
            map.put("currentPage", Optional.ofNullable(currentPage));

            addMetaData(map);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return novelSearchResults;
    }

    @Override
    protected String buildSearchUrl(String keyword, Integer page) {
        return SEARCH_URL
                .replace("{{keyword}}", keyword)
                .replace("{{page}}", page.toString());
    }

    @Override
    protected String buildNovelDetailUrl(Integer id, String type) {

        String result = NOVEL_DETAIL_BASE_URL + id;

        if (type != null) {
            result += "/" + type;
        }

        return result;
    }

    @Override
    protected String buildChapterListUrlFromNovelDetailUrl(String url, Integer page) {
        String result = url;
        if (!url.contains("/chapters?page")) {
            result += "/chapters?page=" + page;
        }

        return result;
    }

    @Override
    protected String buildChapterDetailUrl(Integer chapterId) {
        return CHAPTER_DETAIL_BASE_URL + chapterId;
    }

    @Override
    protected void addMetaData(Map<String, Optional> map) {
        ResponseMetadata metadata = new ResponseMetadata();
        for (Map.Entry<String, Optional> entry : map.entrySet()) {
            metadata.addMetadataValue(entry.getKey(), entry.getValue());
        }

        RequestAttributeUtil.setAttribute("metadata", metadata);
    }

    @Override
    public List<ChapterInfo> getFullChapterList(String url) {
        try {
            Integer currentPage = 1;

            // Get first page to get total pages
            String firstChapterListUrl = buildChapterListUrlFromNovelDetailUrl(url, 1);
            String firstJsonChapterListString = getJsonString(firstChapterListUrl);

            Integer maxPage = getMaxPage(firstJsonChapterListString);
            List<CompletableFuture<List<ChapterInfo>>> futures = new ArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<ChapterInfo> result = new ArrayList<>();

            // Add first page to result
            result.addAll(parseChapterListJson(firstJsonChapterListString, 1, url));

            // Use CompletableFuture to get all other pages concurrently
            for (int i = 2; i <= maxPage; i++) {
                int page = i;
                String chapterListUrl = buildChapterListUrlFromNovelDetailUrl(url, page);
                CompletableFuture<List<ChapterInfo>> future = CompletableFuture.supplyAsync(() -> getJsonString(chapterListUrl), executor).thenApply(json -> parseChapterListJson(json, page, url));
                futures.add(future);
            }

            // Wait for all futures to complete
            List<List<ChapterInfo>> chapterInfos = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            for (List<ChapterInfo> chapterInfoList : chapterInfos) {
                result.addAll(chapterInfoList);
            }

            executor.shutdown();

            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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


    private List<ChapterInfo> parseChapterListJson(String json, Integer currentPage, String url) {
        List<ChapterInfo> chapterList = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMap = null;
        try {
            jsonMap = objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw HttpException.NOT_FOUND("NOT_FOUND", "No result found for novel url: " + url + " page: " + currentPage);
        }
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) jsonMap.get("data");

        String status = (String) jsonMap.get("status");
        if (status.equals("error")) {
            throw HttpException.NOT_FOUND("NOT_FOUND", "No result found for novel url: " + url + " page: " + currentPage);
        } else {

            Map<String, Object> meta = (Map<String, Object>) jsonMap.get("meta");
            Map<String, Object> pagination = (Map<String, Object>) meta.get("pagination");

            Integer perPage = (Integer) pagination.get("per_page");

            Integer startId = (currentPage - 1) * perPage + 1;
            for (Map<String, Object> data : dataList) {
                ChapterInfo chapterInfo = new ChapterInfo();
                chapterInfo.setTitle((String) data.get("title"));
                chapterInfo.setUrl(buildChapterDetailUrl((Integer) data.get("id")));

                chapterInfo.setIndex(startId.toString());
                startId++;

                chapterList.add(chapterInfo);
            }
        }

        return chapterList;
    }

    private Integer getMaxPage(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMap = objectMapper.readValue(json, Map.class);
        Map<String, Object> meta = (Map<String, Object>) jsonMap.get("meta");
        Map<String, Object> pagination = (Map<String, Object>) meta.get("pagination");

        return (Integer) pagination.get("total_pages");
    }

    @Override
    public Object convertToPdf(String jsonChapterDetail) {
        ChapterDetail chapterDetail = new ChapterDetail();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(jsonChapterDetail, Map.class);
            Map<String, Object> data = (Map<String, Object>) jsonMap.get("data");
            String status = (String) jsonMap.get("status");

            if (status.equals("error")) {
                return null;
            } else {
                chapterDetail.setNovelTitle((String) data.get("story_name"));
                chapterDetail.setTitle((String) data.get("chapter_name"));

                System.out.println((String) data.get("story_name"));
                System.out.println((String) data.get("chapter_name"));
                System.out.println((String) data.get("content"));

                String story_name =  (String) data.get("story_name");
                String chapter_name = (String) data.get("chapter_name");
                String storyDetailContent = (String) data.get("content");

                String res = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head></head>\n" +
                        "<body>\n" +
                        "<h1>" + story_name + "</h1>" +
                        "<h2>" + chapter_name + "</h2>" +
                        "<p>----------------</p>" +
                        storyDetailContent +
                        "</body>\n" +
                        "</html>";
                License.setLicenseKey(licenseKey);

                Settings.setLogPath(Paths.get("C:/tmp/IronPdfEngine.log"));

                PdfDocument myPdf = PdfDocument.renderHtmlAsPdf(res);
                String pdfFilePath = UnicodeRemover.removeUnicode(chapter_name) + ".pdf";
                myPdf.saveAs(Paths.get(pdfFilePath));
                File pdfFile = new File(pdfFilePath);

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
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected Object convertToEpub(String jsonChapterDetail) throws JsonProcessingException {
        ChapterDetail chapterDetail = new ChapterDetail();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(jsonChapterDetail, Map.class);
            Map<String, Object> data = (Map<String, Object>) jsonMap.get("data");
            String status = (String) jsonMap.get("status");

            if (status.equals("error")) {
                return null;
            } else {
                chapterDetail.setNovelTitle((String) data.get("story_name"));
                chapterDetail.setTitle((String) data.get("chapter_name"));

                String story_name = (String) data.get("story_name");
                String chapter_name = (String) data.get("chapter_name");
                String storyDetailContent = (String) data.get("content");

                Book book = new Book();
                Metadata metadata = book.getMetadata();
                String htmlContent = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head></head>\n" +
                        "<body>\n" +
                        "<h1>" + story_name + "</h1>" +
                        "<h2>" + chapter_name + "</h2>" +
                        "<p>----------------</p>" +
                        storyDetailContent +
                        "</body>\n" +
                        "</html>";
                // Set the title
                metadata.addTitle(story_name);
                ByteArrayInputStream htmlInputStream = new ByteArrayInputStream(htmlContent.getBytes(StandardCharsets.UTF_8));
                Resource htmlResource = new Resource(htmlInputStream, "file.html");
                String epubFilePath = UnicodeRemover.removeUnicode(chapter_name) + ".epub";
                book.addSection(chapter_name, htmlResource);

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
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object convertToImg(String jsonString) {
        ChapterDetail chapterDetail = new ChapterDetail();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(jsonString, Map.class);
            Map<String, Object> data = (Map<String, Object>) jsonMap.get("data");
            String status = (String) jsonMap.get("status");

            if (status.equals("error")) {
                return null;
            } else {
                chapterDetail.setNovelTitle((String) data.get("story_name"));
                chapterDetail.setTitle((String) data.get("chapter_name"));

                String story_name = (String) data.get("story_name");
                String chapter_name = (String) data.get("chapter_name");
                String storyDetailContent = (String) data.get("content");

                Book book = new Book();
                Metadata metadata = book.getMetadata();
                String htmlContent = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head></head>\n" +
                        "<body>\n" +
                        "<h1>" + story_name + "</h1>" +
                        "<h2>" + chapter_name + "</h2>" +
                        "<p>----------------</p>" +
                        storyDetailContent +
                        "</body>\n" +
                        "</html>";
                // Set the title
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
                    extractedPage.save(String.format(UnicodeRemover.removeUnicode(chapter_name) + "_%d.png", page + 1), SaveFormat.PNG);
                }


                // Create a zip file containing the images
                String zipFilePath = UnicodeRemover.removeUnicode(chapter_name) + ".zip";
                try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
                    for (int page = 0; page < doc.getPageCount(); page++) {
                        String imagePath = String.format(UnicodeRemover.removeUnicode(chapter_name) + "_%d.png", page + 1);
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
                    Files.deleteIfExists(Paths.get(String.format(UnicodeRemover.removeUnicode(chapter_name) + "_%d.png", page + 1)));
                }
                Files.deleteIfExists(Paths.get(zipFilePath));
                File trashFile1 = new File("input.001.png");
                File trashFile2 = new File("input.html");

                trashFile1.delete();
                trashFile2.delete();
                return responseEntity;
            }
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
