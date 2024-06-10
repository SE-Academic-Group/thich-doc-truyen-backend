package com.hcmus.group11.novelaggregator.plugin.download;

import java.io.IOException;

public interface IDownloadPlugin {
    Object convertHtmlToPdf(String url) throws IOException;

    Object convertHtmlToEpub(String url) throws IOException;

    Object convertHtmlToImg(String url) throws Exception;
}
