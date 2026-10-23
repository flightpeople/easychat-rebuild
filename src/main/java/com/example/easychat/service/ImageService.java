package com.example.easychat.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

/**
 * 图片服务（M6b）：Base64 解码落盘 + 按 ID 读回。
 *
 * 生产环境怎么做（对比谈资）：文件存本地只适合单机/演示；
 * 生产用对象存储（OSS/COS/S3）+ CDN，返回 URL 而不是 imageId——
 * 本地盘的问题：扩容难、无备份、多实例不同步。
 */
@Service
public class ImageService {

    @Value("${pic.save.path:uploads}")
    private String savePath;

    /** 上传：Base64 → 文件，返回 imageId（不含后缀，下载时靠文件本身） */
    public String upload(String picBase64, String suffix) {
        if (picBase64 == null || picBase64.isEmpty()) {
            throw new IllegalArgumentException("picBase64 不能为空");
        }
        // 容错：客户端可能带 data:image/png;base64, 前缀，剥掉
        int comma = picBase64.indexOf(',');
        String pure = comma > 0 ? picBase64.substring(comma + 1) : picBase64;

        String imageId = UUID.randomUUID().toString().replace("-", "");
        try {
            Path dir = Path.of(savePath);
            Files.createDirectories(dir);
            byte[] bytes = Base64.getDecoder().decode(pure);
            Files.write(dir.resolve(imageId + (suffix == null ? "" : "." + suffix)), bytes);
            System.out.println("[ImageService] 图片上传成功: imageId=" + imageId + ", 大小=" + bytes.length + "B");
            return imageId;
        } catch (IOException e) {
            throw new RuntimeException("图片保存失败", e);
        }
    }

    /** 下载：按 imageId 找文件（任意后缀），写字节流 */
    public void download(String imageId, HttpServletResponse response) throws Exception {
        Path dir = Path.of(savePath);
        File file;
        try (var stream = Files.list(dir)) {
            file = stream.filter(f -> f.getFileName().toString().startsWith(imageId))
                    .map(Path::toFile).findFirst().orElse(null);
        }
        if (file == null || !file.exists()) {
            response.setStatus(404);
            return;
        }
        String name = file.getName();
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "png";
        response.setContentType("image/" + ext);
        try (FileInputStream in = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
        }
    }
}
