package org.example;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.*;
import java.util.concurrent.TimeUnit;

public class S3ReadTest {

    private static final String accessKey = "0PABSWI1SB07D8TLLTFA";
    private static final String secretKey = "NY4Rnqm4UE4sb9CnKsyNnzyAraCOur1A9ksDJyIB";
    private static final String bucket = "phptest";
    private static final String keyPrefix = "S3_test_";
    private static final String endpoint = "http://s3-zone2-yj-pbs.hendp.com";
    private static final String localFolderPath = "M:\\files2\\";
    private static final int threadCount = 200;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        AmazonS3 s3Client = getS3Client();

        ObjectListing objectListing = s3Client.listObjects(bucket);
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            Runnable worker = new S3Reader(s3Client, objectSummary);
            executor.execute(worker);
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("All tasks finished.");
    }

    private static AmazonS3 getS3Client() {
        AWSCredentials credential = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 client = new AmazonS3Client(credential);
        client.setEndpoint(endpoint);
        client.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(true));
        return client;
    }

    private static class S3Reader implements Runnable {
        private AmazonS3 s3Client;
        private S3ObjectSummary objectSummary;

        public S3Reader(AmazonS3 s3Client, S3ObjectSummary objectSummary) {
            this.s3Client = s3Client;
            this.objectSummary = objectSummary;
        }

        public void run() {
            try {
                readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void readObject() {
            try {
                S3Object object = s3Client.getObject(bucket, objectSummary.getKey());
                try (InputStream is = object.getObjectContent()) {
                    String fileName = localFolderPath + objectSummary.getKey().substring(keyPrefix.length());
                    try (FileOutputStream fos = new FileOutputStream(fileName)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                        System.out.println("Downloaded: " + fileName);
                    }
                }
            } catch (IOException | AmazonS3Exception e) {
                System.err.println("Error while reading object: " + e.getMessage());
            }
        }
    }
}
