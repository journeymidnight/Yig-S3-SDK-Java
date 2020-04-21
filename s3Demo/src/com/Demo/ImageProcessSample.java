package com.Demo;

import com.amazonaws.*;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class ImageProcessSample {
    private final static String ACCESS_KEY = "hehehehe";
    private final static String SECRET_KEY = "hehehehe";
    private final static String END_POINT = "oss-cn-north-1.unicloudsrv.com";
    static AmazonS3Client s3 = getAmazonS3Client(ACCESS_KEY, SECRET_KEY, END_POINT);

    static String objectName = "L.jpg";

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
     * 公共读图片处理
     */
    @Test
    public void testResizeImage() {
        String style = "x-oss-process=image/resize,m_fixed,w_100,h_100";  // 缩放

        try {
            String path = "http://" + END_POINT + "/" + objectName + "?" + style;

            URL newUrl = new URL(path);

            //download
            URLConnection con = newUrl.openConnection();
            con.setConnectTimeout(5 * 1000);
            InputStream s3is = con.getInputStream();

            FileOutputStream fos = new FileOutputStream(new File("f://L.jpeg"));
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
            s3is.close();
            fos.close();

        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        System.out.println("Test resize image done!");
    }

    @Test
    public void testRotateImage() {
        String style = "x-oss-process=image/rotate,90"; // 旋转

        try {
            String path = "http://" + END_POINT + "/" + objectName + "?" + style;

            URL newUrl = new URL(path);

            //download
            URLConnection con = newUrl.openConnection();
            con.setConnectTimeout(5 * 1000);
            InputStream s3is = con.getInputStream();

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
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        System.out.println("Test rotate image done!");
    }

    @Test
    public void testWatermarkImage() {
        String style = "x-oss-process=image/watermark,text_SGVsbG8g5Zu-54mH5pyN5YqhIQ,x_0,y_0"; // 文字水印

        try {
            String path = "http://" + END_POINT + "/" + objectName + "?" + style;

            URL newUrl = new URL(path);

            //download
            URLConnection con = newUrl.openConnection();
            con.setConnectTimeout(5 * 1000);
            InputStream s3is = con.getInputStream();

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
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        System.out.println("Test watermark image done!");
    }

    /**
     * 私有图片处理
     */
    @Test
    public void testPrivateResizeImage() {
        String style = "x-oss-process=image/resize,m_fixed,w_100,h_100";  // 缩放

        try {
            // Set the presigned URL to expire after one hour.
            java.util.Date expiration = new java.util.Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60 * 60 * 60;
            expiration.setTime(expTimeMillis);

            // Generate the presigned URL.
            System.out.println("Generating pre-signed URL.");
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest("", "")
                            .withKey(objectName)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);
            URL url = s3.generatePresignedUrl(generatePresignedUrlRequest);
            System.out.println("Pre-Signed URL: " + url.toString());

            //add process style
            String path = url.toString();
            String[] pathParts = path.split("\\?");

            StringBuilder newPath = new StringBuilder(pathParts[0] + "?" + style + "&");
            for (int i = 1; i < pathParts.length; i++) {
                newPath.append(pathParts[i]);
            }
            System.out.println("newUrl: " + newPath.toString());
            URL newUrl = new URL(newPath.toString());

            //download
            URLConnection con = newUrl.openConnection();
            con.setConnectTimeout(5 * 1000);
            InputStream s3is = con.getInputStream();

            FileOutputStream fos = new FileOutputStream(new File("f://L.jpeg"));
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
            s3is.close();
            fos.close();

        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Test resize image done!");
    }
}