package ru.itmo.cs.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioTransactionResource implements TwoPhaseCommitResource {

    @Getter
    private final MinIOService minIOService;

    private String tempFileName;
    private String finalFileName;
    private byte[] fileData;

    public void setFileData(String finalFileName, byte[] fileData) {
        this.finalFileName = finalFileName;
        this.fileData = fileData;
    }

    @Override
    public void prepare() throws Exception {
        log.info("MinIO: Starting prepare phase. Uploading file {} as temporary {}", finalFileName, finalFileName + ".tmp");
        this.tempFileName = finalFileName + ".tmp";
        minIOService.uploadFile(tempFileName, fileData);
        log.info("MinIO: File {} uploaded successfully as {}", finalFileName, tempFileName);
    }

    @Override
    public void commit() throws Exception {
        log.info("MinIO: Committing transaction. Renaming {} to {}", tempFileName, finalFileName);
        minIOService.renameObject(tempFileName, finalFileName);
        log.info("MinIO: File renamed to {} successfully.", finalFileName);
    }

    @Override
    public void rollback() throws Exception {
        log.info("MinIO: Rolling back transaction. Deleting temporary file {}", tempFileName);
        minIOService.deleteObject(tempFileName);
        log.info("MinIO: Temporary file {} deleted successfully.", tempFileName);
    }
}

