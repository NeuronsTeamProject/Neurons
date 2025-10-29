package com.example.resume.resume.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service // ⬅️ 빈 등록
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    @Value("${app.storage.local-root:uploads}")
    private String rootDir;

    @Override
    public String store(MultipartFile file) {
        try {
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String key = UUID.randomUUID() + (ext != null ? "." + ext : "");
            Path root = Path.of(rootDir);
            Files.createDirectories(root);
            Files.copy(file.getInputStream(), root.resolve(key), StandardCopyOption.REPLACE_EXISTING);
            return root.resolve(key).toString(); // storageKey
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}
