package PutApi;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.TransferManagerConfiguration;
import com.amazonaws.services.s3.transfer.Upload;

import common.RGWClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

/**
 * 
 *  Class Name: MultPartUpload.java
 *  Function: 
 *  The Multipart upload API enables you to upload large objects in parts. You can use this API to upload new large objects 
 *  PART_SIZE was set to 5M.  if object.size < 10M , upload object as a whole , else  The object is divided into 5M pieces 
 *  
 *     Modifications:   
 *  
 *  @author chen  DateTime 2018年1月17日 上午10:24
 *  @version 1.0
 */
public class MultPartUpload {


	static final String ACCESS_KEY = "*********";
	static final String SECRET_KEY = "*********";
	static final String endpoint = "*********"; 
	
	
	public static void main(String[] args) throws FileNotFoundException {
		
		final String USAGE = "\n" +
	            "To run this example, supply the name of an S3 bucket and a file to\n" +
	            "upload to it.\n" +
	            "\n" +
	            "Ex: MultPartUpload <bucketname> <filename>\n";

        if (args.length < 2) {
	       System.out.println(USAGE);
	       System.exit(1);
        }
    	String bucket = args[0];
	    String file_path = args[1];
	    String key = Paths.get(file_path).getFileName().toString();

		
		long UPLOAD_PART_SIZE = 5* 1024 * 1024;
    	long multipartUploadThreshold = 10*1024*1024;
 
    	File file=new File(file_path);
		
    	RGWClient	client = new RGWClient(ACCESS_KEY, SECRET_KEY, endpoint);
    	AmazonS3 conn = client.createConnect();

        //Configure about the TransferManger to control your upload.
        TransferManager tm = TransferManagerBuilder.standard()
    	        .withExecutorFactory(() -> Executors.newFixedThreadPool(100))
    	        .withMinimumUploadPartSize(UPLOAD_PART_SIZE)
    	        .withMultipartUploadThreshold( (long) multipartUploadThreshold)
    	        .withS3Client(conn)
    	        .build();
    
        System.out.println("Hello");
        long startTime=System.nanoTime();   //获取开始时间  
        InputStream inputStream = new FileInputStream(file);
        ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(file.length());

        PutObjectRequest putObjectrequest = new PutObjectRequest(
 			   bucket, key, inputStream, objectMetadata);
        
        Upload upload = tm.upload(putObjectrequest.withCannedAcl(CannedAccessControlList.PublicRead));
        
        System.out.println("Hello2");
 
        try {
            // Or you can block and wait for the upload to finish
            upload.waitForCompletion();
            System.out.println("Upload complete.");

            long endTime=System.nanoTime(); //获取结束时间  

            System.out.println("程序运行时间： "+(endTime-startTime)+"ns");  
        } catch (AmazonClientException amazonClientException) {
            System.out.println("Unable to upload file, upload was aborted.");
            amazonClientException.printStackTrace();
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        finally
        {
             tm.shutdownNow();
        }
    }
	
	
}


