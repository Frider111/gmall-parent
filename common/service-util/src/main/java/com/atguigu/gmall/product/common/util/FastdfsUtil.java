package com.atguigu.gmall.product.common.util;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.net.InetSocketAddress;

/**
 * @author abt
 * @date 2020/8/21 - 14:41
 */
public class FastdfsUtil {

    public static String uploadImg(MultipartFile file) throws Exception
    {
        //  获取配置文件路径
        String path = FastdfsUtil.class.getClassLoader().getResource("tracker.conf").getPath();
        // 初始化路径
        ClientGlobal.init(path);
        // 配置 tracker 客户端
        TrackerClient trackerClient = new TrackerClient();
        // 获取 tracker 服务器 （用来获取ip）
        TrackerServer trackerServer = trackerClient.getConnection();
        // 获取文件系统对象
        StorageClient storageClient = new StorageClient(trackerServer, null);
        // 上传数据

        String filename = file.getOriginalFilename();

        String filenameSuffix = filename.substring(filename.lastIndexOf('.') + 1, filename.length());

        String[] jpgs = storageClient.upload_appender_file(file.getBytes(),
                filenameSuffix, null);
        // 获取地址信息
        InetSocketAddress inetSocketAddress = trackerServer.getInetSocketAddress();
        
        // 获取 ip  根 端口号信息
        String hostString = inetSocketAddress.getHostString();

        String url = "http://";

        url = url + hostString + ":8080" ;

        for (String jpg : jpgs) {
            url += "/" + jpg ;
        }
        return url;
    }

}
