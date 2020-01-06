package com.Demo;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MultiPartUploadSample {
    private final static String ACCESS_KEY = "DrjUiXNcQXih3R5n";
    private final static String SECRET_KEY = "dqcLTLYnczosouL1v7ZEC7buM8hm1b";
    private final static String END_POINT = "oss-cn-north-1.unicloudsrv.com";
    static AmazonS3Client s3 = getAmazonS3Client(ACCESS_KEY, SECRET_KEY, END_POINT);

    static String bucketName = "javatest";
    static String objectName = "java-sdk-key";
    static String filePath = "s3Demo/src/L.jpeg";
    public static AmazonS3Client getAmazonS3Client(String accessKey, String secretKey, String endPoint) {
        System.setProperty(SDKGlobalConfiguration.DISABLE_CERT_CHECKING_SYSTEM_PROPERTY, "true");
        BasicAWSCredentials cred = new BasicAWSCredentials(accessKey, secretKey);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setProtocol(Protocol.HTTP);
        AmazonS3Client s3 = new AmazonS3Client(cred, clientConfiguration);
        S3ClientOptions options = S3ClientOptions.builder().setPathStyleAccess(true).setPayloadSigningEnabled(true).disableChunkedEncoding().build();
        s3.setS3ClientOptions(options);
        s3.setEndpoint(endPoint);
        return s3;
    }

    /**
     * 上传文件
     */
    @Test
    public void testMutiPartUpload(){
        File file = new File(filePath);
        long contentLength = file.length();
        long partSize = 5 * 1024 * 1024; // Set part size to 5 MB.

        try {
            List<PartETag> partETags = new ArrayList<PartETag>();

            // Initiate the multipart upload.
            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, objectName);
            InitiateMultipartUploadResult initResponse = s3.initiateMultipartUpload(initRequest);

            // Upload the file parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Because the last part could be less than 5 MB, adjust the part size as needed.
                partSize = Math.min(partSize, (contentLength - filePosition));

                // Create the request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(bucketName)
                        .withKey(objectName)
                        .withUploadId(initResponse.getUploadId())
                        .withPartNumber(i)
                        .withFileOffset(filePosition)
                        .withFile(file)
                        .withPartSize(partSize);

                // Upload the part and add the response's ETag to our list.
                UploadPartResult uploadResult = s3.uploadPart(uploadRequest);
                partETags.add(uploadResult.getPartETag());

                filePosition += partSize;
            }

            // Complete the multipart upload.
            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucketName, objectName,
                    initResponse.getUploadId(), partETags);
            s3.completeMultipartUpload(compRequest);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
        System.out.println("Test mutipart upload done!");
    }

    /**
     * 终止分段上传
     */
    @Test
    public void testAbortMutiPartUpload() {
        try {
            // Find all in-progress multipart uploads.
            ListMultipartUploadsRequest allMultipartUploadsRequest = new ListMultipartUploadsRequest(bucketName);
            MultipartUploadListing multipartUploadListing = s3.listMultipartUploads(allMultipartUploadsRequest);

            List<MultipartUpload> uploads = multipartUploadListing.getMultipartUploads();
            System.out.println("Before deletions, " + uploads.size() + " multipart uploads in progress.");

            // Abort each upload.
            for (MultipartUpload u : uploads) {
                System.out.println("Upload in progress: Key = \"" + u.getKey() + "\", id = " + u.getUploadId());
                s3.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, u.getKey(), u.getUploadId()));
                System.out.println("Upload deleted: Key = \"" + u.getKey() + "\", id = " + u.getUploadId());
            }

            // Verify that all in-progress multipart uploads have been aborted.
            multipartUploadListing = s3.listMultipartUploads(allMultipartUploadsRequest);
            uploads = multipartUploadListing.getMultipartUploads();
            System.out.println("After aborting uploads, " + uploads.size() + " multipart uploads in progress.");
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
        System.out.println("Test abort mutipart upload done!");
    }

    /**
     * 列出分段上传
     */
    @Test
    public void testListMutiPartUpload() {
        try {
            // Retrieve a list of all in-progress multipart uploads.
            ListMultipartUploadsRequest allMultipartUploadsRequest = new ListMultipartUploadsRequest(bucketName);
            MultipartUploadListing multipartUploadListing = s3.listMultipartUploads(allMultipartUploadsRequest);
            List<MultipartUpload> uploads = multipartUploadListing.getMultipartUploads();

            // Display information about all in-progress multipart uploads.
            System.out.println(uploads.size() + " multipart upload(s) in progress.");
            for (MultipartUpload u : uploads) {
                System.out.println("Upload in progress: Key = \"" + u.getKey() + "\", id = " + u.getUploadId());
            }
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
        System.out.println("Test list mutipart upload done!");
    }

}
