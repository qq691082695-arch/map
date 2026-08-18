package com.mapvendor.module.file.controller;

import com.mapvendor.module.file.service.FileResourceService;
import com.mapvendor.module.file.service.FileResourceService.Download;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UrlPathHelper;

@RestController
public class PublicFileController {
    private final FileResourceService service;
    public PublicFileController(FileResourceService service) { this.service = service; }
    @GetMapping("/files/**")
    public ResponseEntity<org.springframework.core.io.Resource> get(HttpServletRequest request) {
        String path = new UrlPathHelper().getPathWithinApplication(request).substring("/files/".length());
        Download download = service.download(path);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(download.getMimeType()))
                .contentLength(download.getSize()).cacheControl(CacheControl.noCache())
                .header("X-Content-Type-Options", "nosniff").body(download.getResource());
    }
}
