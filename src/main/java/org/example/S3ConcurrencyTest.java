package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import javax.activation.MimetypesFileTypeMap;

public class S3ConcurrencyTest {

    private static final String accessKey = "0PABSWI1SB07D8TLLTFA";
    private static final String secretKey = "NY4Rnqm4UE4sb9CnKsyNnzyAraCOur1A9ksDJyIB";
    private static final String bucket = "phptest";
    private static final String key = "S3_test_";
    private static final String endpoint = "http://s3-zone2-yj-pbs.hendp.com";
    private static final String localFolderPath = "M:\\files1\\";

    private static final int threadCount = 300;

    public static void main(String[] args) {
        File directory = new File(localFolderPath);

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            for (File file : files) {
                if (file.isFile()) {

                    Runnable worker = new S3Uploader(file);
                    executor.execute(worker);
                }
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            System.out.println("All tasks finished.");
        }
    }

    private static class S3Uploader implements Runnable {
        private File file;

        public S3Uploader(File file) {
            this.file = file;
        }

        public void run() {
            try {
                upload(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void upload(File file) throws Exception {
            String url;
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentEncoding("UTF-8");
                metadata.setContentLength(file.length());
                metadata.setContentType(new MimetypesFileTypeMap().getContentType(file));
                url = request(fis, key + file.getName(), metadata);
                System.out.println("Uploaded: " + file.getName());
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private String request(InputStream is, String key, ObjectMetadata metadata) throws Exception {
            AWSCredentials credential = new BasicAWSCredentials(accessKey, secretKey);
            AmazonS3 client = new AmazonS3Client(credential);
            client.setEndpoint(endpoint);
            client.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(true));

            PutObjectRequest request = new PutObjectRequest(bucket, key, is, metadata)
                    .withCannedAcl(CannedAccessControlList.AuthenticatedRead);
            client.putObject(request);

            return "上传成功";
        }
    }
}