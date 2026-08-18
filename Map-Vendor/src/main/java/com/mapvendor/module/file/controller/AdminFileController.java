package com.mapvendor.module.file.controller;

import com.mapvendor.common.api.ApiResponse;
import com.mapvendor.module.file.dto.ImageResourceView;
import com.mapvendor.module.file.service.FileResourceService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/files")
public class AdminFileController {
    private final FileResourceService service;
    public AdminFileController(FileResourceService service) { this.service = service; }
    @PostMapping(value="/images", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary="上传图片资源（JPEG、PNG、WebP）")
    public ApiResponse<ImageResourceView> upload(@RequestPart("file") MultipartFile file) {
        return ApiResponse.success(service.upload(file));
    }
}
