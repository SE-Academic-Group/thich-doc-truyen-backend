package com.hcmus.group11.novelaggregator.plugin.download.plugins;

import com.hcmus.group11.novelaggregator.plugin.download.IDownloadPlugin;
import com.hcmus.group11.novelaggregator.type.NovelDownloadInfo;
import com.hcmus.group11.novelaggregator.util.UnicodeRemover;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class Epub implements IDownloadPlugin {
    @Override
    public Object execute(NovelDownloadInfo info) throws IOException {
        String html = info.getHtml();
        String chapterName = info.getChapterName();

        // Create a new EPUB book
        Book book = new Book();
        Metadata metadata = book.getMetadata();
        metadata.addTitle(chapterName);
        book.addSection(chapterName, new Resource(html.getBytes(StandardCharsets.UTF_8), "chapter1.html"));

        // Add HTML content as a section (chapter)
        String epubFilePath = UnicodeRemover.removeUnicode(chapterName) + ".epub";
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
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().toLowerCase();
    }
}
