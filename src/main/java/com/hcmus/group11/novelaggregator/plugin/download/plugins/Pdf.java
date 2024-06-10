package com.hcmus.group11.novelaggregator.plugin.download.plugins;

import com.hcmus.group11.novelaggregator.config.IronPdfPropertiesConfig;
import com.hcmus.group11.novelaggregator.plugin.download.IDownloadPlugin;
import com.hcmus.group11.novelaggregator.type.NovelDownloadInfo;
import com.hcmus.group11.novelaggregator.util.UnicodeRemover;
import com.ironsoftware.ironpdf.License;
import com.ironsoftware.ironpdf.PdfDocument;
import com.ironsoftware.ironpdf.Settings;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

@Component
public class Pdf implements IDownloadPlugin {
    private final IronPdfPropertiesConfig ironPdfPropertiesConfig;

    public Pdf(IronPdfPropertiesConfig ironPdfPropertiesConfig) {
        this.ironPdfPropertiesConfig = ironPdfPropertiesConfig;
    }

    @Override
    public Object execute(NovelDownloadInfo info) throws IOException {
        License.setLicenseKey(ironPdfPropertiesConfig.getLicenseKey());
        String html = info.getHtml();
        String chapterName = info.getChapterName();

        Settings.setLogPath(Paths.get("C:/tmp/IronPdfEngine.log"));

        PdfDocument myPdf = PdfDocument.renderHtmlAsPdf(info.getHtml());

        String pdfFilePath = UnicodeRemover.removeUnicode(chapterName) + ".pdf";
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
    public String getName() {
        return this.getClass().getSimpleName().toLowerCase();
    }
}
