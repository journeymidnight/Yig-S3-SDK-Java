package com.Demo;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.*;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class ObjectSample {
    private final static String ACCESS_KEY = "DrjUiXNcQXih3R5n";
    private final static String SECRET_KEY = "dqcLTLYnczosouL1v7ZEC7buM8hm1b";
    private final static String END_POINT = "oss-cn-north-1.unicloudsrv.com";
    static AmazonS3Client s3 = getAmazonS3Client(ACCESS_KEY, SECRET_KEY, END_POINT);

    static String bucketName = "javatest";
    static String objectName = "java-sdk-key";
    static String sourceKey = "java-sdk-key";
    static String destinationKey = "java-sdk-key1";
    static String filePath = "s3Demo/src/L.jpeg";

    public static AmazonS3Client getAmazonS3Client(String accessKey, String secretKey, String endPoint) {
        System.setProperty(SDKGlobalConfiguration.DISABLE_CERT_CHECKING_SYSTEM_PROPERTY, "true");
        BasicAWSCredentials cred = new BasicAWSCredentials(accessKey, secretKey);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setProtocol(Protocol.HTTP);
        AmazonS3Client s3 = new AmazonS3Client(cred,clientConfiguration);
        S3ClientOptions options = S3ClientOptions.builder().setPathStyleAccess(true).setPayloadSigningEnabled(true).disableChunkedEncoding().build();
        s3.setS3ClientOptions(options);
        s3.setEndpoint(endPoint);
        return s3;
    }

    /**
     * 上传文件
     */
    @Test
    public void testPutObject() {
        try {
            s3.putObject(bucketName, objectName, new File("s3Demo/src/L.jpeg"));
            s3.setObjectAcl(bucketName, objectName, CannedAccessControlList.PublicRead);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Test put object done!");
    }

    /**
     * 下载文件
     */
    @Test
    public void testGetObject() {
        try {
            S3Object o = s3.getObject(bucketName, objectName);
            System.out.println(o);
            S3ObjectInputStream s3is = o.getObjectContent();
            FileOutputStream fos = new FileOutputStream(new File("s3Demo/src/L.jpeg"));
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
            s3is.close();
            fos.close();
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Test get object done!");
    }

    /**
     * 下载文件分段
     */
    @Test
    public void testGetObjectWithRange() {
        try {
            // Get a range of bytes from an object and print the bytes.
            GetObjectRequest rangeObjectRequest = new GetObjectRequest(bucketName, objectName)
                    .withRange(0, 9);
            S3Object o = s3.getObject(rangeObjectRequest);
            System.out.println(o);

        } catch (AmazonServiceException e){
            System.err.println(e.getErrorMessage());
        }
        System.out.println("Test get object with range done!");

    }

    /**
     * 桶下文件列表
     */
    @Test
    public void testListObjects() {
        try{
            ListObjectsV2Result result = s3.listObjectsV2(bucketName);
            List<S3ObjectSummary> objects = result.getObjectSummaries();
            for (S3ObjectSummary os: objects) {
                System.out.println("* " + os.getKey());
            }
        }catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Test list object done!");
    }
    /**
     * 删除桶下文件
     */
    @Test
    public void testDeleteObject(String bucketName,String objectName) {
        try{
            s3.deleteObject(new DeleteObjectRequest(bucketName, objectName));
        }catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Test delete object done!");
    }
    /**
     * 查看文件的acl
     */
    @Test
    public void testGetObjectAcl() {
        try {
            AccessControlList acl = s3.getObjectAcl(bucketName, objectName);
            List<Grant> grants = acl.getGrantsAsList();
            for (Grant grant : grants) {
                System.out.format("%s\n", grant.getPermission().toString());
            }
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Test get object acl Done!");
    }

    /**
     * 设置文件的acl
     */
    @Test
    public void testSetObjectAcl() {
        try {
            s3.setObjectAcl(bucketName, objectName, CannedAccessControlList.Private);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("test set object acl done!");
    }

    /**
     * 复制对象
     */
    @Test
    public void testCopyObject() {
        try {
            // Copy the object into a new object in the same bucket.
            CopyObjectRequest copyObjRequest = new CopyObjectRequest(bucketName, sourceKey, bucketName, destinationKey);
            s3.copyObject(copyObjRequest);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Test copy object done!");
    }

    /**
     * 复制对象
     */
    @Test
    public void testObjectMeta() {
        try {
            // Upload a text string as a new object.
            s3.putObject(bucketName, objectName, "Uploaded String Object");

            // Upload a file as a new object with ContentType and title specified.
            PutObjectRequest request = new PutObjectRequest(bucketName, objectName, new File(filePath));
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("plain/text");
            metadata.addUserMetadata("x-amz-meta-title", "someTitle");
            request.setMetadata(metadata);
            s3.putObject(request);
        }
        catch(AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
        System.out.println("Test object meta done!");
    }

}
