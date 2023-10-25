package org.example;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

/**
 * @Title：文件上传S3
 * @Author：gongbin
 * @Date 2022/9/16 11:16
 * @Description
 * @Version
 */
@Slf4j
@Component

public class UploadS3Util {

    private static String accessKey;

    private static String secretKey;

    private static String endpoint="http://s3-zone2-yj-pbs.hendp.com";

    private static String showpoint;

    private static String bucket;

    private static AmazonS3 client;

    @Value("${s3.accessKey:0PABSWI1SB07D8TLLTFA}")
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    @Value("${s3.secretKey:NY4Rnqm4UE4sb9CnKsyNnzyAraCOur1A9ksDJyIB}")
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @Value("${s3.endpoint:http://s3-zone2-yj-pbs.hendp.com}")
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Value("${s3.showpoint:testrgw.hendp.com}")
    public void setShowpoint(String showpoint) {
        this.showpoint = showpoint;
    }

    @Value("${s3.bucket:phptest}")
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    private static void getClient() {

        if (null != client) {
            return;
        }
        AWSCredentials credential = new BasicAWSCredentials("0PABSWI1SB07D8TLLTFA", "NY4Rnqm4UE4sb9CnKsyNnzyAraCOur1A9ksDJyIB");
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTP);
        client = new AmazonS3Client(credential, clientConfig);
        client.setEndpoint(endpoint);


        log.debug("s3上传地址endpoint:{}  ak:{} sk:{}", endpoint, accessKey, secretKey);
        client.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(true));
    }

    public static String upload (File file) throws Exception {
        String url;
        FileInputStream fis = null;
        try {
            String fileName = file.getName();
            String key = getKey(fileName);
            fis = new FileInputStream(file);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentEncoding("UTF-8");
            metadata.setContentLength(file.length());
            metadata.setContentType(new MimetypesFileTypeMap().getContentType(file));
            url = request(fis, key, metadata);
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error("UploadS3Util fis 关流异常！");
                }
            }
        }

        return url;
    }

    public static String upload (InputStream fis, String fileName, long contentLength, String contentType) throws Exception {
        String url;
        try {
            String key = getKey(fileName);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentEncoding("UTF-8");
            metadata.setContentLength(contentLength);
            metadata.setContentType(contentType);
            url = request(fis, key, metadata);
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error("UploadS3Util fis 关流异常！");
                }
            }
        }
        return url;
    }

    public static String upload (MultipartFile file) throws Exception {

        String url;
        InputStream is = null;
        try {
            String name = file.getOriginalFilename();
            String key = getKey(name);
            is = new ByteArrayInputStream(file.getBytes());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentEncoding("UTF-8");
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            url = request(is, key, metadata);
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("UploadS3Util is 关流异常！");
                }
            }
        }

        return url;
    }




    private static<T extends InputStream> String request (T is, String key, ObjectMetadata metadata) throws Exception {

        getClient();
        log.debug("s3上传成功bucket:{} key:{}", bucket, key);
        PutObjectRequest mall = new PutObjectRequest(bucket, key, is, metadata).withCannedAcl(CannedAccessControlList.AuthenticatedRead);
        client.putObject(mall);
        String url = new StringBuffer("http://").append(showpoint).append("/").append(bucket).append("/").append(key).toString();
        log.info("s3上传成功，返回文件url: {}", url);
        return url;
    }

    private static String getKey(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf("."), fileName.length());
        String uuid = new Random().ints().toString();
        return uuid + ext;
    }
    /**
     * 下载文件
     *
     * @param remoteFileName 文件名
     * @param path     下载成功保存路径
     */
    public static boolean downFromS3(String remoteFileName, String path) throws Exception {
        //定义一个URL对象，就是你想下载的图片的URL地址
        URL url = new URL(remoteFileName);
        //打开连接
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //设置请求方式为"GET"
        conn.setRequestMethod("GET");
        //超时响应时间为10秒
        conn.setConnectTimeout(60 * 1000);
        //通过输入流获取图片数据
        InputStream is = conn.getInputStream();
        //得到图片的二进制数据，以二进制封装得到数据，具有通用性
        byte[] data = readInputStream(is);
        //创建一个文件对象用来保存图片，默认保存当前工程根目录，起名叫Copy.jpg
        File imageFile = new File(path);
        //创建输出流
        FileOutputStream outStream = new FileOutputStream(imageFile);
        //写入数据
        outStream.write(data);
        //关闭输出流，释放资源
        outStream.close();
        return true;
    }

    /**
     * 测试用例
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        /*//定义一个URL对象，就是你想下载的图片的URL地址
        URL url = null;
        try {
            url = new URL("http://testrgw.hengchang6.com/hetongxitong/077c10632881419e82657f02aee4abae.jpg");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        //打开连接
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //设置请求方式为"GET"
        try {
            conn.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        //超时响应时间为10秒
        conn.setConnectTimeout(10 * 1000);
        //通过输入流获取图片数据
        InputStream is = null;
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //得到图片的二进制数据，以二进制封装得到数据，具有通用性
        byte[] data = new byte[0];
        try {
            data = readInputStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //创建一个文件对象用来保存图片，默认保存当前工程根目录，起名叫Copy.jpg
        File imageFile = new File("E:\\fd\\suopic\\copy\\66.jpg");
        //创建输出流
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(imageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //写入数据
        try {
            outStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //关闭输出流，释放资源
        try {
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//*/
//        String url = "http://testrgw.hengchang6.com/hetongxitong/09f8c866e6ca44508bb333910628ce21.png";
//        //String url = "http://testrgw.hengchang6.com/hetongxitong/2b99a140a47a4bed9a76276fb3974dd7.jpg";
//        //获取文件名
//        String urlFileName = url.substring(url.lastIndexOf("/") + 1);
//        System.out.println("文件全称"+urlFileName);
//        String[] urlFileParts = urlFileName.split("\\.");
//        System.out.println("文件名是"+urlFileParts[0]);
//        System.out.println("文件名扩展"+urlFileParts[1]);
//        //下载图片
//        try {
//            UploadS3Util.downFromS3(url,"E:\\fd\\suopic\\copy\\"+urlFileName);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        //
//        //生成缩略图
//        File fromPic = new File("E:\\fd\\suopic\\copy\\"+urlFileName);
//        ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();

//        MultipartFile file = null;//上传的要压缩的图片
//        try {
//            byte[] bigContent =file.getBytes();//获取文件的字节数组
//        } catch (IOException e) {
//            e.printStackTrace();
////        }
//        System.out.println("原地址是：E:\\fd\\suopic\\copy\\"+urlFileName);
//        File toPic =new File("E:\\fd\\suopic\\copy\\"+urlFileParts[0]+"_small."+urlFileParts[1]);
//        System.out.println("缩略地址是：E:\\fd\\suopic\\copy\\"+urlFileParts[0]+"_small."+urlFileParts[1]);
//        try {
//            //使用文件输入流 FileInputStream 读取图片
//           // FileInputStream fileInputStream = new FileInputStream(fromPic);
//
////            Thumbnails.of(fromPic).scale(0.8f).outputQuality(0.1f).toFile(toPic);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        //将缩略图上传
        // File upfile = new File("E:\\fd\\suopic\\copy\\"+urlFileParts[0]+"_small."+urlFileParts[1]);
        File file = new File("M:\\files1\\5.mp3");
        upload(file);
    }

    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        //创建一个Buffer字符串
        byte[] buffer = new byte[6024];
        //每次读取的字符串长度，如果为-1，代表全部读取完毕
        int len;
        //使用一个输入流从buffer里把数据读取出来
        while ((len = inStream.read(buffer)) != -1) {
            //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
            outStream.write(buffer, 0, len);
        }
        //关闭输入流
        inStream.close();
        //把outStream里的数据写入内存
        return outStream.toByteArray();
    }

}


