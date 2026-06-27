package com.merging.chunks.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class DirService {
    private final String UPLOAD_DIRECTORY = "C:\\Users\\Administrator\\Desktop\\ServletJsp\\chunks\\src\\main\\resources\\uploads";


    public String uploadToDircectory(MultipartFile file, String resumableFilename,
                                     String resumableIdentifier,
                                     Integer resumableChunkNumber,
                                     Integer resumableTotalChunks) throws IOException {
        String cleanIdentifier = resumableIdentifier.contains(",") ? resumableIdentifier.split(",")[0] : resumableIdentifier;
        Path folder = Paths.get(UPLOAD_DIRECTORY, cleanIdentifier);
        Files.createDirectories(folder);
        Path resolved = folder.resolve("chunk-" + resumableChunkNumber);
        Files.copy(file.getInputStream(), resolved, StandardCopyOption.REPLACE_EXISTING);
        long count = Files.list(folder).count();
        if (resumableTotalChunks == count) {
            System.out.println(resumableFilename+" UPLOADED COMPLETELY");
            String cleanFileName = resumableFilename.contains(",") ? resumableFilename.split(",")[0] : resumableFilename;
            Path finalUpload = Path.of(UPLOAD_DIRECTORY, cleanFileName);
            try (OutputStream out = Files.newOutputStream(finalUpload)) {
                for (int i = 1; i <= resumableTotalChunks; i++) {
                    Path chunk = folder.resolve("chunk-" + i);
                    Files.copy(chunk, out);
                }
            }
        }
        return resolved.toString();
    }
}
