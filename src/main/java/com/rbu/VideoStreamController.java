package com.rbu;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/video")
public class VideoStreamController {

    private static final String VIDEO_FILE = "videos/springvideo.mp4";

    @GetMapping(value = "/stream", produces = "video/mp4")
    public ResponseEntity<Flux<DataBuffer>> streamVideo(@RequestHeader HttpHeaders headers) throws IOException {
        Path path = Paths.get(VIDEO_FILE);

        long fileSize = path.toFile().length();
        long start = 0;
        long end = fileSize - 1;

        if (headers.getRange() != null && !headers.getRange().isEmpty()) {
            HttpRange range = headers.getRange().get(0);
            start = range.getRangeStart(fileSize);
            end = range.getRangeEnd(fileSize);
        }

        long finalStart = start;
        long chunkSize = end - start + 1;

        Flux<DataBuffer> body = DataBufferUtils.readByteChannel(
                () -> {
                    RandomAccessFile raf = new RandomAccessFile(VIDEO_FILE, "r");
                    FileChannel fc = raf.getChannel();
                    fc.position(finalStart);
                    return fc;
                },
                new DefaultDataBufferFactory(),
                4096
        );

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_TYPE, "video/mp4");
        responseHeaders.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        responseHeaders.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(chunkSize));
        responseHeaders.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);

        return new ResponseEntity<>(body, responseHeaders, HttpStatus.PARTIAL_CONTENT);
    }
}
