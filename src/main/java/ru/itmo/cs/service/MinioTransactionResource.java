package ru.itmo.cs.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
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
        this.tempFileName = finalFileName + ".tmp";
        minIOService.uploadFile(tempFileName, fileData);
    }

    @Override
    public void commit() throws Exception {
        minIOService.renameObject(tempFileName, finalFileName);
    }

    @Override
    public void rollback() throws Exception {
        // Удаляем временный файл
        minIOService.deleteObject(tempFileName);
    }
}

