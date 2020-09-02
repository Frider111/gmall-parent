package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.product.common.result.Result;
import com.atguigu.gmall.product.common.util.FastdfsUtil;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author abt
 * @date 2020/8/21 - 15:00
 */
@RestController
@RequestMapping("admin/product")
@CrossOrigin
public class FileUploadController {

    /**
     * 上传图片的方法
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping("fileUpload")
    public Result<String> fileUpload(MultipartFile file) throws Exception{
        String imageUrl = FastdfsUtil.uploadImg(file);
        return Result.ok(imageUrl);
    }

}
