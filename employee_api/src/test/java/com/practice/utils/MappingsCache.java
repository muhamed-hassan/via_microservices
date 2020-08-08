package com.practice.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MappingsCache {

    private static Map<String, String> mappingsOfInternalApi;

    private static Map<String, String> mappingsOfExternalApi;

    static {
        mappingsOfInternalApi = loadFiles("responses/internal_api");
//        mappingsOfExternalApi = loadFiles("responses/external_api");
    }

    private MappingsCache() {}

    private static Map<String, String> loadFiles(String directory) {
        try (Stream<Path> files = Files.walk(Paths.get(ClassLoader.getSystemResource(directory).toURI()))) {
            return files.filter(Files::isRegularFile)
                        .map(file -> {
                            String fileContent;
                            try {
                                fileContent = Files.readAllLines(Paths.get(file.toUri()),
                                                                    Charset.forName(StandardCharsets.UTF_8.name()))
                                                    .stream()
                                                    .collect(Collectors.joining());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            return new MappingEntry(file.getFileName().toString(), fileContent); })
                        .collect(Collectors.toMap(MappingEntry::getFileName, MappingEntry::getFileContent));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMappingFromInternalApi(String fileName) {
        return mappingsOfInternalApi.get(fileName);
    }

    public static String getMappingFromExternalApi(String fileName) {
        return mappingsOfExternalApi.get(fileName);
    }

}
