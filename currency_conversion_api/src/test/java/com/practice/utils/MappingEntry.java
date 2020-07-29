package com.practice.utils;

public final class MappingEntry {

    private final String fileName;

    private final String fileContent;

    public MappingEntry(String fileName, String fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileContent() {
        return fileContent;
    }

}
