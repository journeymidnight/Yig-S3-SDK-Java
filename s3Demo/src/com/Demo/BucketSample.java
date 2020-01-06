package com.Demo;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.Grant;
import org.junit.Test;

import java.util.List;

public class BucketSample {
    private final static String ACCESS_KEY = "DrjUiXNcQXih3R5n";
    private final static String SECRET_KEY = "dqcLTLYnczosouL1v7ZEC7buM8hm1b";
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
        System.out.println("Test bucket delete done!");
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
}
