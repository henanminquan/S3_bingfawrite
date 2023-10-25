package org.example;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.util.StringUtils.*;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class S3PerformanceTest1 {
    private static final String accessKey = "0PABSWI1SB07D8TLLTFA";
    private static final String secretKey = "NY4Rnqm4UE4sb9CnKsyNnzyAraCOur1A9ksDJyIB";
    private static final String bucket = "phptest";
    private static final String key = "S3_test_";
//    private static final String FILE_PATH = "M:\\files1\\75.mp3";
private static final String FILE_PATH ="";
    private static AmazonS3 client;
    private static String endpoint="http://s3-zone2-yj-pbs.hendp.com";
    //鹏博士应急公网

    public static void main(String[] args) throws Exception {
        //遍历文件夹下的
        File directory = new File("M:\\\\files1\\\\"); // 将路径替换为你要遍历的文件夹路径
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
//                    System.out.println("M:\\\\files1\\\\"+file.getName());
                    System.out.println("zybbigdata");
                    upload(new File("M:\\\\files1\\\\"+file.getName()));
                }
            }
        }

//        File file = new File(FILE_PATH);
//        File file = new File("M:\\\\files1\\\\2.doc");
//          upload(file);
        }

    private static void getClient() {
        if (null != client) {
            return;
        }
        AWSCredentials credential = new BasicAWSCredentials(accessKey, secretKey);
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTP);
        client = new AmazonS3Client(credential, clientConfig);
        client.setEndpoint(endpoint);
//        log.debug("s3上传地址endpoint:{}  ak:{} sk:{}", endpoint, accessKey, secretKey);
        client.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(true));
    }
    public static String upload (File file) throws Exception {
        String url;
        FileInputStream fis = null;
        try {
            String fileName = file.getName();
            fis = new FileInputStream(file);
//            String key="zyb_";
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentEncoding("UTF-8");
            metadata.setContentLength(file.length());
            metadata.setContentType(new MimetypesFileTypeMap().getContentType(file));
            url = request(fis, key+file.getName(), metadata);
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return url;
    }
    private static<T extends InputStream> String request (T is, String key, ObjectMetadata metadata) throws Exception{
        getClient();
//        log.debug("s3上传成功bucket:{} key:{}", bucket, key);
        com.amazonaws.services.s3.model.PutObjectRequest mall = new com.amazonaws.services.s3.model.PutObjectRequest(bucket, key, is, metadata).withCannedAcl(CannedAccessControlList.AuthenticatedRead);
        client.putObject(mall);
        return "上传成功";
    }
}

