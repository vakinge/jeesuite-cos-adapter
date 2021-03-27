package com.jeesuite.cos;

import java.io.InputStream;
import java.util.Map;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.jeesuite.common.JeesuiteBaseException;
import com.jeesuite.cos.provider.aliyun.AliyunProvider;
import com.jeesuite.cos.provider.qcloud.QcloudProvider;
import com.jeesuite.cos.provider.qiniu.QiniuProvider;

/**
 * 统一服务门面
 * 
 * <br>
 * Class Name   : CosProviderServiceFacade
 *
 * @author jiangwei
 * @version 1.0.0
 * @date Mar 27, 2021
 */
public class CosProviderServiceFacade implements InitializingBean,DisposableBean {

	private String type;
	private CosProvider provider;
	private CosProviderConfig config;
	

	public CosProvider getProvider() {
		return provider;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setConfig(CosProviderConfig config) {
		this.config = config;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if(AliyunProvider.NAME.equals(type)) {
			provider = new AliyunProvider(config);
		}else if(QcloudProvider.NAME.equals(type)) {
			provider = new QcloudProvider(config);
		}else if(QiniuProvider.NAME.equals(type)) {
			provider = new QiniuProvider(config);
		}else {
			throw new JeesuiteBaseException("cos["+type+"] not support");
		}
	}
	
	@Override
	public void destroy() throws Exception {
		if(provider != null) {
			provider.close();
		}
	}
	
	public boolean existsBucket(String bucketName) {
		return provider.existsBucket(bucketName);
	}

	public void createBucket(String bucketName) {
		provider.createBucket(bucketName);
	}

	public void deleteBucket(String bucketName) {
		provider.deleteBucket(bucketName);
	}

	public CUploadResult upload(CUploadObject object) {
		return provider.upload(object);
	}

	public String getDownloadUrl(String bucketName, String fileKey, int expireInSeconds) {
		return provider.getDownloadUrl(bucketName, fileKey, expireInSeconds);
	}

	public boolean exists(String bucketName, String fileKey) {
		return provider.exists(bucketName, fileKey);
	}

	public boolean delete(String bucketName, String fileKey) {
		return provider.delete(bucketName, fileKey);
	}

	public byte[] getObjectBytes(String bucketName, String fileKey) {
		return provider.getObjectBytes(bucketName, fileKey);
	}

	public InputStream getObjectInputStream(String bucketName, String fileKey) {
		return provider.getObjectInputStream(bucketName, fileKey);
	}

	public String downloadAndSaveAs(String bucketName, String fileKey, String localSaveDir) {
		return provider.downloadAndSaveAs(bucketName, fileKey, localSaveDir);
	}

	public Map<String, Object> createUploadToken(UploadTokenParam param) {
		return provider.createUploadToken(param);
	}

	public CObjectMetadata getObjectMetadata(String bucketName, String fileKey) {
		return provider.getObjectMetadata(bucketName, fileKey);
	}

}
