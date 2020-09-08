package com.atguigu.gmall.product;

import com.atguigu.gmall.common.util.FastdfsUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author abt
 * @date 2020/8/21 - 11:37
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class FastDemo {

    @Test
    public void fastdfsTest() throws Exception {

//        String path = ServiceProductApplication.class.getClassLoader().getResource("tracker.conf").getPath();
//////
//////        ClientGlobal.init(path);
//////
//////        TrackerClient trackerClient = new TrackerClient();
//////        TrackerServer trackerServer = trackerClient.getConnection();
//////
//////        StorageClient storageClient = new StorageClient(trackerServer, null);
//////
//////        String[] jpgs = storageClient.upload_appender_file("C:\\Users\\Frider\\Pictures\\Saved Pictures\\4fd82ff96517d94177123ac1f6e3bfc4.jpg",
//////                "jpg", null);
//////
//////        System.out.println("jpgs = " + jpgs);
//////        for (String jpg : jpgs) {
//////            System.out.println("jpg = " + jpg);
//////        }
//////
//////        System.out.println("true = " + true);

        String s = FastdfsUtil.uploadImg(null);
        System.out.println("s = " + s);





        


    }




}
