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

    static {
        try (Stream<Path> files = Files.walk(Paths.get(ClassLoader.getSystemResource("responses/internal_api").toURI()))) {
            mappingsOfInternalApi = files.filter(Files::isRegularFile)
                                            .map(MappingsCache::toMappingEntry)
                                            .collect(Collectors.toMap(MappingEntry::getFileName,
                                                                        MappingEntry::getFileContent));
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private MappingsCache() {}

    private static MappingEntry toMappingEntry(Path filePath) {
        String fileContent;
        try {
            fileContent = Files.readAllLines(Paths.get(filePath.toUri()),
                                                Charset.forName(StandardCharsets.UTF_8.name()))
                                .stream()
                                .collect(Collectors.joining());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new MappingEntry(filePath.getFileName().toString(), fileContent);
    }

    public static String getMappingFromInternalApi(String fileName) {
        return mappingsOfInternalApi.get(fileName);
    }

}
