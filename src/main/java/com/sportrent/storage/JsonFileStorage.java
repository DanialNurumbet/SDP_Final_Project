package com.sportrent.storage;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

@Component
public class JsonFileStorage {
    private final Path path;
    private final ObjectMapper mapper;

    @Autowired
    public JsonFileStorage(ObjectMapper mapper, @Value("${sportrent.storage.path:data/sportrent.json}") String path) {
        this(Path.of(path), mapper);
    }

    public JsonFileStorage(Path path, ObjectMapper mapper) {
        this.path = path.toAbsolutePath().normalize();
        this.mapper = mapper.copy()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    boolean isMissing() { return Files.notExists(path); }

    StorageData read() {
        try {
            return mapper.readValue(path.toFile(), StorageData.class);
        } catch (IOException | RuntimeException error) {
            throw new StorageException("Не удалось прочитать хранилище. Проверьте JSON-файл и права доступа; данные не сброшены.", error);
        }
    }

    void write(StorageData data) {
        Path temporary = null;
        try {
            Files.createDirectories(path.getParent());
            temporary = Files.createTempFile(path.getParent(), "sportrent-", ".tmp");
            mapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), data);
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            // Если атомарная замена недоступна, завершаемся с ошибкой, сохраняя основной файл.
            Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException error) {
            throw new StorageException("Не удалось сохранить данные. Проверьте права и свободное место; повторите действие.", error);
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); } catch (IOException ignored) { /* основной файл не затрагиваем */ }
            }
        }
    }
}
