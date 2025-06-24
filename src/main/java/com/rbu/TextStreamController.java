package com.rbu;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/text")
public class TextStreamController {

    private static final String FILE_PATH = "text/largefile.txt";

    @GetMapping(value = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Flux<DataBuffer>> streamTextFile() {
        Path path = Paths.get(FILE_PATH);

        Flux<DataBuffer> data = DataBufferUtils.readInputStream(
                () -> {
                    try {
                        return new FileInputStream(path.toFile());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to open file", e);
                    }
                },
                new DefaultDataBufferFactory(),
                4096
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=largefile.txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(data);
    }
}
