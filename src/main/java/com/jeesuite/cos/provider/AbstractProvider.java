package com.jeesuite.cos.provider;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.jeesuite.common.http.HttpUtils;
import com.jeesuite.cos.COSProvider;
import com.jeesuite.cos.CosProviderConfig;

/**
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2017年1月7日
 */
public abstract class AbstractProvider implements COSProvider{

	protected static final String URL_PREFIX_PATTERN = "(http).*\\.(com|cn)\\/";
	protected static final String HTTP_PREFIX = "http://";
	protected static final String HTTPS_PREFIX = "https://";
	protected static final String DIR_SPLITER = "/";
	private static final Map<String, String> bucketUrlPrefixMappings = new HashMap<String, String>();

	
	protected CosProviderConfig conf;
	
	
	public AbstractProvider(CosProviderConfig conf) {
		Validate.notBlank(conf.getAccessKey(), "[accessKey] not defined");
		Validate.notBlank(conf.getSecretKey(), "[secretKey] not defined");
		this.conf = conf;
		if(StringUtils.isNotBlank(conf.getUrlPrefix()) && !conf.getUrlPrefix().endsWith("/")) {
			this.conf.setUrlPrefix(conf.getUrlPrefix() + "/");
		}
	}


	protected String getFullPath(String bucketName,String file) {
		if(file.startsWith(HTTP_PREFIX) || file.startsWith(HTTPS_PREFIX)){
			return file;
		}
		return getBucketUrlPrefix(bucketName) + file;
	}
	
	protected String resolveFileKey(String bucketName,String fileUrl) {
		if(!fileUrl.startsWith(HTTP_PREFIX) && !fileUrl.startsWith(HTTPS_PREFIX)){
			return fileUrl;
		}
		String urlprefix = getBucketUrlPrefix(bucketName);
		return fileUrl.replace(urlprefix, StringUtils.EMPTY);
	}

	@Override
	public String downloadAndSaveAs(String bucketName,String file, String localSaveDir) {
		return HttpUtils.downloadFile(getDownloadUrl(bucketName,file,300), localSaveDir);
	}
	
	@Override
	public String getDownloadUrl(String bucketName,String fileKey, int expireInSeconds) {
		String url;
		if(conf.isPrivate()){
			fileKey = resolveFileKey(bucketName, fileKey);
			String presignedUrl = generatePresignedUrl(bucketName,fileKey,expireInSeconds);
			String urlprefix = getBucketUrlPrefix(bucketName);
			url = presignedUrl.replaceFirst(URL_PREFIX_PATTERN, urlprefix);
		}else{
			url = getFullPath(bucketName,fileKey);
		}
		return url;
	}


	/**
	 * @param bucketName
	 * @return
	 */
	protected String getBucketUrlPrefix(String bucketName) {
		if(bucketUrlPrefixMappings.containsKey(bucketName)){
			return bucketUrlPrefixMappings.get(bucketName);
		}
		synchronized (bucketUrlPrefixMappings) {
			if(bucketUrlPrefixMappings.containsKey(bucketName)){
				return bucketUrlPrefixMappings.get(bucketName);
			}
			
			String urlPrefix;
			if(StringUtils.isNotBlank(conf.getUrlPrefix())) {
				urlPrefix = conf.getUrlPrefix().replace("{bucketName}", bucketName);
			}else {
				urlPrefix = buildBucketUrlPrefix(bucketName);
			}
			bucketUrlPrefixMappings.put(bucketName, StringUtils.trimToEmpty(urlPrefix));
		}
		return bucketUrlPrefixMappings.get(bucketName);
	}
	
	protected String currentBucketName(String bucketName) {
		if(StringUtils.isBlank(bucketName)) {
			bucketName = conf.getDefaultBucketName();
		}
		if(StringUtils.isBlank(bucketName)){
			throw new IllegalArgumentException("[bucketName] not defined");
		}
		return bucketName;
	}
	
	
	protected abstract String buildBucketUrlPrefix(String bucketName);
	protected abstract String generatePresignedUrl(String bucketName,String fileKey, int expireInSeconds);

	
}
