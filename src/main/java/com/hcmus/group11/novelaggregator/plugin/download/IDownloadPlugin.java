package com.hcmus.group11.novelaggregator.plugin.download;

import com.hcmus.group11.novelaggregator.type.NovelDownloadInfo;

public interface IDownloadPlugin {
    Object execute(NovelDownloadInfo info) throws Exception;

    String getName();
}
