package com.Demo;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration.Transition;
import com.amazonaws.services.s3.model.StorageClass;

import org.junit.Test;

import java.util.Arrays;
public class BucketLifecycleSample {
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
     * 设置生命周期配置
     */
    @Test
    public void testBucketLifecycle(){
        // Create a rule to archive objects with the "glacierobjects/" prefix to Glacier immediately.
        BucketLifecycleConfiguration.Rule rule1 = new BucketLifecycleConfiguration.Rule()
                .withId("Archive immediately rule")
                .withPrefix("documents/")
                .addTransition(new Transition().withDays(0).withStorageClass(StorageClass.Glacier))
                .withStatus(BucketLifecycleConfiguration.ENABLED);

        // Create a rule to transition objects to the Standard-Infrequent Access storage class
        // after 30 days, then to Glacier after 365 days. will delete the objects after 3650 days.
        // The rule applies to all objects with the tag "archive" set to "true".
        BucketLifecycleConfiguration.Rule rule2 = new BucketLifecycleConfiguration.Rule()
                .withId("Archive and then delete rule")
                .withPrefix("documents/")
                .addTransition(new Transition().withDays(30).withStorageClass(StorageClass.StandardInfrequentAccess))
                .addTransition(new Transition().withDays(365).withStorageClass(StorageClass.Glacier))
                .withExpirationInDays(3650)
                .withStatus(BucketLifecycleConfiguration.ENABLED);

        // Add the rules to a new BucketLifecycleConfiguration.
        BucketLifecycleConfiguration configuration = new BucketLifecycleConfiguration()
                .withRules(Arrays.asList(rule1, rule2));

        try {
            // Save the configuration.
            s3.setBucketLifecycleConfiguration(bucketName, configuration);

            // Retrieve the configuration.
            configuration = s3.getBucketLifecycleConfiguration(bucketName);

            // Add a new rule with both a prefix predicate and a tag predicate.
            configuration.getRules().add(new BucketLifecycleConfiguration.Rule().withId("NewRule")
                    .withExpirationInDays(3650)
                    .withStatus(BucketLifecycleConfiguration.ENABLED));

            // Save the configuration.
            s3.setBucketLifecycleConfiguration(bucketName, configuration);

            // Verify that the configuration now has three rules.
            configuration = s3.getBucketLifecycleConfiguration(bucketName);
            System.out.println("Expected # of rules = 3; found: " + configuration.getRules().size());

            // Delete the configuration.
            s3.deleteBucketLifecycleConfiguration(bucketName);

            // Verify that the configuration has been deleted by attempting to retrieve it.
            configuration = s3.getBucketLifecycleConfiguration(bucketName);
            String s = (configuration == null) ? "No configuration found." : "Configuration found.";
            System.out.println(s);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
        System.out.println("Test bucket lifecycle done!");

    }
}
