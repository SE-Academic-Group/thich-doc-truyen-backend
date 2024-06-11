package com.hcmus.group11.novelaggregator.plugin.download.plugins;

import com.aspose.words.HtmlLoadOptions;
import com.aspose.words.LoadOptions;
import com.aspose.words.SaveFormat;
import com.hcmus.group11.novelaggregator.plugin.download.IDownloadPlugin;
import com.hcmus.group11.novelaggregator.type.NovelDownloadInfo;
import com.hcmus.group11.novelaggregator.util.UnicodeRemover;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class Image implements IDownloadPlugin {
    @Override
    public Object execute(NovelDownloadInfo info) throws Exception {
        String htmlContent = info.getHtml();
        String chapterName = info.getChapterName();

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
            extractedPage.save(String.format(UnicodeRemover.removeUnicode(chapterName) + "_%d.png", page + 1), SaveFormat.PNG);
        }

        // Create a zip file containing the images
        String zipFilePath = UnicodeRemover.removeUnicode(chapterName) + ".zip";
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            for (int page = 0; page < doc.getPageCount(); page++) {
                String imagePath = String.format(UnicodeRemover.removeUnicode(chapterName) + "_%d.png", page + 1);
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
            Files.deleteIfExists(Paths.get(String.format(UnicodeRemover.removeUnicode(chapterName) + "_%d.png", page + 1)));
        }
        Files.deleteIfExists(Paths.get(zipFilePath));
        File trashFile1 = new File("input.001.png");
        File trashFile2 = new File("input.html");

        trashFile1.delete();
        trashFile2.delete();
        return responseEntity;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().toLowerCase();
    }
}
