package com.jeesuite.cos;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jeesuite.common.util.TokenGenerator;

/**
 * 
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2017年1月5日
 */
public class CUploadObject {

	private String bucketName;
	private String fileName;
	private String fileKey;
	private String mimeType;
	private String folderPath;
	private byte[] bytes;
	private File file;
	private long fileSize;
	private InputStream inputStream;
	private Map<String, Object> metadata = new HashMap<String, Object>();

	public CUploadObject(String filePath) {
		this.file = new File(filePath);
		this.fileName = file.getName();
		this.fileSize = file.length();
	}

	public CUploadObject(File file) {
		this.file = file;
		this.fileName = file.getName();
		this.fileSize = file.length();
	}

	public CUploadObject(String fileKey, File file) {
		this.fileKey = fileKey;
		this.file = file;
		this.fileSize = file.length();
	}

	public CUploadObject(String fileKey, InputStream inputStream,long fileSize, String mimeType) {
		this.fileKey = fileKey;
		this.inputStream = inputStream;
		this.mimeType = mimeType;
		this.fileSize = fileSize;
	}

	public CUploadObject(String fileKey, byte[] bytes, String mimeType) {
		this.fileKey = fileKey;
		this.bytes = bytes;
		this.mimeType = mimeType;
		this.fileSize = bytes.length;
	}
	

	/**
	 * @return the bucketName
	 */
	public String getBucketName() {
		return bucketName;
	}

	public String getFileKey() {
		return fileKey;
	}

	/**
	 * @return the fileSize
	 */
	public long getFileSize() {
		return fileSize;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public File getFile() {
		return file;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setString(String mimeType) {
		this.mimeType = mimeType;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public CUploadObject addMetaData(String key, Object value) {
		metadata.put(key, value);
		return this;
	}
	
	public String getMimeType(){
		return mimeType;
	}

	public CUploadObject folderPath(String folderPath) {
		this.folderPath = folderPath;
		return this;
	}
	
	public CUploadObject bucketName(String bucketName) {
		this.bucketName = bucketName;
		return this;
	}
	
	public String buildFileKey() {
		StringBuilder builder = new StringBuilder();
		if(StringUtils.isNotBlank(this.folderPath)){
			builder.append(FilePathHelper.formatDirectoryPath(folderPath));
		}
		if(StringUtils.isNotBlank(fileKey)){
			if(this.fileKey.startsWith(FilePathHelper.DIR_SPLITER)){
				builder.append(this.fileKey.substring(1));
			}else{
				builder.append(this.fileKey);
			}
		}else{
			builder.append(TokenGenerator.generate());
			String extension = null;
			if(StringUtils.isNotBlank(fileName) && fileName.contains(FilePathHelper.DOT)){
				extension = fileName.substring(fileName.lastIndexOf(FilePathHelper.DOT));
			}
			if(extension == null && mimeType != null){
				extension = MimeTypeFileExtensionConvert.getFileExtension(mimeType);
			}
			
			if(extension != null){
				builder.append(extension);
			}
		}
		
		return builder.toString();
	}

}
