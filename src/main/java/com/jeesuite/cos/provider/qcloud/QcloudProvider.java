package com.jeesuite.cos.provider.qcloud;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;

import com.jeesuite.common.JeesuiteBaseException;
import com.jeesuite.common.async.StandardThreadExecutor;
import com.jeesuite.common.async.StandardThreadExecutor.StandardThreadFactory;
import com.jeesuite.cos.CObjectMetadata;
import com.jeesuite.cos.CUploadObject;
import com.jeesuite.cos.CUploadResult;
import com.jeesuite.cos.CosProviderConfig;
import com.jeesuite.cos.FilePathHelper;
import com.jeesuite.cos.UploadTokenParam;
import com.jeesuite.cos.provider.AbstractProvider;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;

/**
 * 
 * <br>
 * Class Name   : QcloudProvider
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2020年1月2日
 */
public class QcloudProvider extends AbstractProvider {

	public static final String NAME = "qcloud";
	
	private COSClient cosclient;
	private TransferManager transferManager;
	private StandardThreadExecutor transferExecutor;
	
	//private Pattern bucketWithAppId = Pattern.compile(".*-[0-9]{3,}$");
	
	/**
	 * @param conf
	 */
	public QcloudProvider(CosProviderConfig conf) {
		super(conf);
		Validate.notBlank(conf.getAppId(), "[appId] not defined");
		//设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
		if(StringUtils.isBlank(conf.getRegionName())){
			conf.setRegionName("ap-guangzhou");
		}

		COSCredentials cred = new BasicCOSCredentials(conf.getAccessKey(), conf.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(conf.getRegionName()));
        clientConfig.setMaxConnectionsCount(conf.getMaxConnectionsCount());
        //生成cos客户端
        cosclient = new COSClient(cred, clientConfig);
        //
        transferExecutor = new StandardThreadExecutor(1, 5,0, TimeUnit.SECONDS, 1,new StandardThreadFactory("cos-transfer-executor"));
        transferManager = new TransferManager(cosclient, transferExecutor);
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void createBucket(String bucketName) {
		bucketName = currentBucketName(bucketName);
		if(cosclient.doesBucketExist(bucketName)){
			throw new JeesuiteBaseException(406, "bucketName["+bucketName+"]已存在");
		}
		cosclient.createBucket(bucketName);
	}

	@Override
	public void deleteBucket(String bucketName) {
		bucketName = currentBucketName(bucketName);
		cosclient.deleteBucket(bucketName);
	}
	
	@Override
	public boolean exists(String bucketName,String fileKey) {
		fileKey = resolveFileKey(bucketName, fileKey);
		bucketName = currentBucketName(bucketName);
		return cosclient.doesObjectExist(bucketName, fileKey);
	}
	
	@Override
	public CUploadResult upload(CUploadObject object) {
		PutObjectRequest request;
		String fileKey = object.buildFileKey();
		String bucketName = currentBucketName(object.getBucketName());
		if(object.getFile() != null){
			request = new PutObjectRequest(bucketName, fileKey, object.getFile());
		}else if(object.getBytes() != null){
			ByteArrayInputStream inputStream = new ByteArrayInputStream(object.getBytes());
			ObjectMetadata objectMetadata = new ObjectMetadata();
	        objectMetadata.setContentLength(object.getFileSize());
			request = new PutObjectRequest(bucketName, fileKey, inputStream, objectMetadata);
		}else if(object.getInputStream() != null){
			ObjectMetadata objectMetadata = new ObjectMetadata();
	        objectMetadata.setContentLength(object.getFileSize());
			request = new PutObjectRequest(bucketName, fileKey, object.getInputStream(), objectMetadata);
		}else{
			throw new IllegalArgumentException("upload object is NULL");
		}
		
		try {
			if(object.getFileSize() > conf.getMaxAllowdSingleFileSize()){
				Upload upload = transferManager.upload(request);
				com.qcloud.cos.model.UploadResult result = upload.waitForUploadResult();
				return new CUploadResult(result.getRequestId(),fileKey, getFullPath(object.getBucketName(),fileKey), null); 
			}else{
				PutObjectResult result = cosclient.putObject(request);
				return new CUploadResult(result.getRequestId(), fileKey,getFullPath(object.getBucketName(),fileKey), result.getContentMd5());
			}
		} catch (Exception e) {
			throw new JeesuiteBaseException(500, buildMessage(bucketName,e));
		}
	}

	@Override
	protected String generatePresignedUrl(String bucketName,String fileKey, int expireInSeconds) {
		bucketName = currentBucketName(bucketName);
		try {
			URL url = cosclient.generatePresignedUrl(bucketName, fileKey, DateUtils.addSeconds(new Date(), expireInSeconds));
			return url.toString();
		} catch (Exception e) {
			throw new JeesuiteBaseException(500, buildMessage(bucketName,e));
		}
	}

	@Override
	public boolean delete(String bucketName,String fileKey) {
		try {
			bucketName = currentBucketName(bucketName);
			cosclient.deleteObject(bucketName, fileKey);
		} catch (Exception e) {
			throw new JeesuiteBaseException(500, buildMessage(bucketName,e));
		} 
		return true;
	}
	
	@Override
	public byte[] getObjectBytes(String bucketName, String fileKey) {
		try {
			InputStream inputStream = getObjectInputStream(bucketName, fileKey);
			return IOUtils.toByteArray(inputStream);
		} catch (IOException e) {
			throw new JeesuiteBaseException(e.getMessage());
		}
	}

	@Override
	public InputStream getObjectInputStream(String bucketName, String fileKey) {
		try {
			String _bucketName = currentBucketName(bucketName);
			String _fileKey = resolveFileKey(bucketName, fileKey);
			COSObject cosObject = cosclient.getObject(_bucketName, _fileKey);
			return cosObject.getObjectContent();
		} catch (Exception e) {
			throw new JeesuiteBaseException(500, buildMessage(bucketName,e));
		}
	}

	@Override
	public CObjectMetadata getObjectMetadata(String bucketName, String fileKey) {
		try {
			String _bucketName = currentBucketName(bucketName);
			String _fileKey = resolveFileKey(bucketName, fileKey);
			ObjectMetadata metadata = cosclient.getObjectMetadata(_bucketName, _fileKey);
			CObjectMetadata objectMetadata = new CObjectMetadata();
			objectMetadata.setCreateTime(metadata.getLastModified());
			objectMetadata.setMimeType(metadata.getContentType());
			objectMetadata.setFilesize(metadata.getContentLength());
			objectMetadata.setHash(metadata.getContentMD5());
			objectMetadata.setExpirationTime(metadata.getExpirationTime());
			objectMetadata.setCustomMetadatas(metadata.getUserMetadata());
			return objectMetadata;
		} catch (Exception e) {
			throw new JeesuiteBaseException(500, buildMessage(bucketName,e));
		}
	}

	//https://github.com/tencentyun/qcloud-cos-sts-sdk/tree/master/java
	@Override
	public Map<String, Object> createUploadToken(UploadTokenParam param) {
		Map<String, Object> config = new TreeMap<String, Object>();

		// 替换为您的 SecretId
		config.put("SecretId", "AKIDHTVVaVR6e3");
		// 替换为您的 SecretKey
		config.put("SecretKey", "PdkhT9e2rZCfy6");

		// 临时密钥有效时长，单位是秒，默认1800秒，最长可设定有效期为7200秒
		config.put("durationSeconds", 1800);

		// 换成您的 bucket
		config.put("bucket", "examplebucket-1250000000");
		// 换成 bucket 所在地区
		config.put("region", "ap-guangzhou");
		config.put("allowPrefix", "a.jpg");

		// 密钥的权限列表。简单上传、表单上传和分片上传需要以下的权限，其他权限列表请看
		// https://cloud.tencent.com/document/product/436/31923
		String[] allowActions = new String[] {
				// 简单上传
				"name/cos:PutObject",
				// 表单上传、小程序上传
				"name/cos:PostObject",
				// 分片上传
				"name/cos:InitiateMultipartUpload", "name/cos:ListMultipartUploads", "name/cos:ListParts",
				"name/cos:UploadPart", "name/cos:CompleteMultipartUpload" };
		config.put("allowActions", allowActions);

		//JSONObject credential = CosStsClient.getCredential(config);
		// 成功返回临时密钥信息，如下打印密钥信息
		return null;
	}


	@Override
	public void close() {
		cosclient.shutdown();
		transferExecutor.shutdown();
	}
	
	protected String currentBucketName(String bucketName){
		bucketName = super.currentBucketName(bucketName);
		if(bucketName.endsWith(conf.getAppId()))return bucketName;
		return new StringBuilder(bucketName).append(FilePathHelper.MID_LINE).append(conf.getAppId()).toString();
	}

	@Override
	protected String buildBucketUrlPrefix(String bucketName) {
		//http://qietitoolstest-1252877917.cos.ap-guangzhou.myqcloud.com/
		StringBuilder urlBuilder = new StringBuilder().append("http://").append(bucketName).append("-").append(conf.getAppId()).append(".cos.").append(conf.getRegionName()).append(".myqcloud.com/");
		return urlBuilder.toString();
	}
	
	private static String buildMessage(String bucketName,Exception e){
		if(e instanceof CosServiceException){
			if("NoSuchBucket".equals(((CosServiceException)e).getErrorCode())){
				throw new JeesuiteBaseException(404, "bucketName["+bucketName+"]不存在"); 
			}else if("AccessDenied".equals(((CosServiceException)e).getErrorCode())){
				throw new JeesuiteBaseException(403, "appId与bucketName["+bucketName+"]不匹配"); 
			}else if("InvalidAccessKeyId".equals(((CosServiceException)e).getErrorCode())){
				throw new JeesuiteBaseException(40, "AccessKey配置错误"); 
			}
			return ((CosServiceException)e).getErrorMessage();
		}else{
			return e.getMessage();
		}
	}

}
