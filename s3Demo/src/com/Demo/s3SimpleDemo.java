package com.Demo;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.*;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class s3SimpleDemo extends TestCase {


    private final static String ACCESS_KEY = "hehehehe";
    private final static String SECRET_KEY = "hehehehe";
    private final static String END_POINT = "oss-cn-north-1.unicloudsrv.com";
    static AmazonS3Client s3 = getAmazonS3Client(ACCESS_KEY, SECRET_KEY, END_POINT);

    static String bucketName = "javatest";

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
     * 创建桶
     */
    @Test
    public void testCreateBucket() {
        Bucket b = null;
        if (s3.doesBucketExist(bucketName)) {
            System.out.format("Bucket %s already exists.\n", bucketName);
        } else {
            try {
                s3.createBucket(bucketName);
            } catch (AmazonS3Exception e) {
                System.err.println(e.getErrorMessage());
            }
        }
        System.out.println("Test create bucket done!");
    }

    /**
     * 删除桶
     */
    @Test
    public void testDeleteBucket() {
        try{
            s3.deleteBucket(bucketName);
        }catch (Exception e){
            System.out.format("Error: %s \n", e);
        }
        System.out.println("Test delete bucket done!");
    }

    /**
     * 查看桶列表
     */
    @Test
    public void testBucketList() {
        try {
            List<Bucket> buckets = s3.listBuckets();
            for (Bucket b : buckets) {
                System.out.println("* " + b.getName());
            }
        }catch (AmazonS3Exception e) {
            System.err.println(e.getErrorMessage());
        }
        System.out.println("Test bucket list done!");
    }

    /**
     * 上传文件
     */
    @Test
    public void testPutObject() {
        String filePath = "F:\\optional.docx";
        String keyName = Paths.get(filePath).getFileName().toString();
        try {
            s3.putObject(bucketName, keyName, new File(filePath));
            s3.setObjectAcl(bucketName, "optional.docx", CannedAccessControlList.PublicRead);
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
            S3Object o = s3.getObject(bucketName, "123456/key/qq.json");
            System.out.println(o);
            S3ObjectInputStream s3is = o.getObjectContent();
            FileOutputStream fos = new FileOutputStream(new File("G://qq.json"));
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
     * 查看桶的acl
     */
    @Test
    public void testGetBucketAcl() {
        try {
            AccessControlList acl = s3.getBucketAcl(bucketName);
            List<Grant> grants = acl.getGrantsAsList();
            for (Grant grant : grants) {
                System.out.format("%s\n",grant.getPermission().toString());
                System.out.format("%s\n",s3.getBucketPolicy(bucketName).getPolicyText());
            }
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Test get bucket acl done!");
    }

    /**
     * 设置桶的acl
     */
    @Test
    public void testSetBucketAcl() {
        //公共读通过bucketPolicy来设置
        String bucketPolicy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
        String bucketAcl = "PublicRead";
        try {
            if ("PublicRead".equals(bucketAcl)) {
                s3.setBucketPolicy(bucketName, bucketPolicy);
            } else {
                s3.deleteBucketPolicy(bucketName);
            }
        }catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
        System.out.println("Test set bucket acl done!");
    }


    /**
     * 查看文件的acl
     */
    @Test
    public void testGetObjectAcl() {
        try {
            AccessControlList acl = s3.getObjectAcl(bucketName, "optional.docx");
            List<Grant> grants = acl.getGrantsAsList();
            for (Grant grant : grants) {
                System.out.format("%s\n", grant.getPermission().toString());
            }
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Test get object acl done!");
    }

    /**
     * 设置文件的acl
     */
    @Test
    public void testSetObjectAcl() {
        try {
            s3.setObjectAcl(bucketName, "optional.docx", CannedAccessControlList.Private);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Test set object acl done!");
    }
}