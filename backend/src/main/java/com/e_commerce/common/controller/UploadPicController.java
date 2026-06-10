package com.e_commerce.common.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.common.utils.AliOssUtil;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 图片上传控制器
 * 支持图片压缩后上传到阿里云OSS
 */
@RestController
@RequestMapping("/upload")
public class UploadPicController {

    // 图片压缩配置
    private static final int MAX_WIDTH = 1920;      // 最大宽度
    private static final int MAX_HEIGHT = 1080;     // 最大高度
    private static final double QUALITY = 0.8;      // 压缩质量(0.0-1.0)
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 超过5MB的图片强制压缩

    /**
     * 上传图片到阿里云OSS（支持压缩）
     */
    @PostMapping
    public Result<String> uploadPic(@RequestParam("file") MultipartFile file) throws IOException {
        // 1. 校验文件
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // 2. 获取原文件名，处理文件名防止重复
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.error("文件格式非法");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID() + extension;

        // 3. 判断是否需要压缩（仅对图片文件进行压缩）
        InputStream inputStream = file.getInputStream();
        if (isImageFile(originalFilename) && file.getSize() > MAX_SIZE) {
            // 图片超过5MB，进行压缩
            inputStream = compressImage(file.getInputStream(), extension);
        }

        // 4. 调用阿里云OSS工具类上传文件，获取URL
        String picUrl = AliOssUtil.uploadFile(filename, inputStream);

        // 5. 返回OSS的图片URL给前端
        return Result.success(picUrl);
    }

    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String filename) {
        String lowerFilename = filename.toLowerCase();
        return lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")
                || lowerFilename.endsWith(".png") || lowerFilename.endsWith(".gif")
                || lowerFilename.endsWith(".bmp") || lowerFilename.endsWith(".webp");
    }

    /**
     * 压缩图片
     * @param inputStream 原始图片输入流
     * @param extension 文件扩展名
     * @return 压缩后的图片输入流
     */
    private InputStream compressImage(InputStream inputStream, String extension) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputStream);
        
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // 计算压缩后的尺寸（保持比例）
        int newWidth = originalWidth;
        int newHeight = originalHeight;
        
        if (originalWidth > MAX_WIDTH) {
            newWidth = MAX_WIDTH;
            newHeight = (int) ((double) originalHeight * MAX_WIDTH / originalWidth);
        }
        
        if (newHeight > MAX_HEIGHT) {
            newHeight = MAX_HEIGHT;
            newWidth = (int) ((double) newWidth * MAX_HEIGHT / newHeight);
        }
        
        // 使用Thumbnailator压缩图片
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String format = extension.substring(1).toUpperCase(); // 去除点号并转大写
        
        Thumbnails.of(originalImage)
                .size(newWidth, newHeight)
                .outputQuality(QUALITY)
                .outputFormat(format)
                .toOutputStream(outputStream);
        
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
