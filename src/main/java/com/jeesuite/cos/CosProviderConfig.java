package com.jeesuite.cos;

/**
 * 
 * <br>
 * Class Name   : CosProviderConfig
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2020年1月3日
 */
public class CosProviderConfig {

	private String appId;
	private String accessKey;
	private String secretKey;
	private String regionName;
	private String defaultBucketName;
	private boolean isPrivate;
	private String urlPrefix;
	private long maxAllowdSingleFileSize = 5 * 1024L * 1024L * 1024L;
	private int maxConnectionsCount = 100;

	
	/**
	 * @return the appId
	 */
	public String getAppId() {
		return appId;
	}
	/**
	 * @param appId the appId to set
	 */
	public void setAppId(String appId) {
		this.appId = appId;
	}
	/**
	 * @return the accessKey
	 */
	public String getAccessKey() {
		return accessKey;
	}
	/**
	 * @param accessKey the accessKey to set
	 */
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	/**
	 * @return the secretKey
	 */
	public String getSecretKey() {
		return secretKey;
	}
	/**
	 * @param secretKey the secretKey to set
	 */
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	/**
	 * @return the regionName
	 */
	public String getRegionName() {
		return regionName;
	}
	/**
	 * @param regionName the regionName to set
	 */
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	
	public String getDefaultBucketName() {
		return defaultBucketName;
	}
	public void setDefaultBucketName(String defaultBucketName) {
		this.defaultBucketName = defaultBucketName;
	}
	/**
	 * @return the isPrivate
	 */
	public boolean isPrivate() {
		return isPrivate;
	}
	/**
	 * @param isPrivate the isPrivate to set
	 */
	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}
	
	public String getUrlPrefix() {
		return urlPrefix;
	}
	public void setUrlPrefix(String urlPrefix) {
		this.urlPrefix = urlPrefix;
	}
	/**
	 * @return the maxAllowdSingleFileSize
	 */
	public long getMaxAllowdSingleFileSize() {
		return maxAllowdSingleFileSize;
	}
	/**
	 * @param maxAllowdSingleFileSize the maxAllowdSingleFileSize to set
	 */
	public void setMaxAllowdSingleFileSize(long maxAllowdSingleFileSize) {
		this.maxAllowdSingleFileSize = maxAllowdSingleFileSize;
	}
	/**
	 * @return the maxConnectionsCount
	 */
	public int getMaxConnectionsCount() {
		return maxConnectionsCount;
	}
	/**
	 * @param maxConnectionsCount the maxConnectionsCount to set
	 */
	public void setMaxConnectionsCount(int maxConnectionsCount) {
		this.maxConnectionsCount = maxConnectionsCount;
	}
	
}
