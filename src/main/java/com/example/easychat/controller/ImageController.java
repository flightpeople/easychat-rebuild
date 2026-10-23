package com.example.easychat.controller;

import com.example.easychat.common.Result;
import com.example.easychat.service.ImageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 图片 Controller（M6b）——协议对齐原版：Base64 上传，不是 MultipartFile。
 *
 * 为什么用 Base64 而不是 MultipartFile：原版客户端（Qt）就是这么发的，
 * 协议兼容优先。代价是传输体积大 33%（Base64 膨胀）——这正是生产环境
 * 会改用 Multipart/OSS 直传的原因，面试可作对比谈资。
 *
 * 免鉴权（拦截器排除 image 路径）：图片是公开静态资源，登录态管不了它。
 */
@RestController
@RequestMapping("image")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /** POST /image/upload {picBase64, suffix} → imageId */
    @PostMapping("upload")
    public Result<String> upload(@RequestBody Map<String, String> body) {
        return Result.success(imageService.upload(body.get("picBase64"), body.get("suffix")));
    }

    /** GET /image/download/{imageId} 直接写字节流（不经过 Result 包装） */
    @GetMapping("download/{imageId}")
    public void download(@PathVariable String imageId,
                         jakarta.servlet.http.HttpServletResponse response) throws Exception {
        imageService.download(imageId, response);
    }
}
