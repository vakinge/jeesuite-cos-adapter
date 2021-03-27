package com.jeesuite.cos;

import com.jeesuite.common.JeesuiteBaseException;
import com.jeesuite.common.util.ResourceUtils;
import com.jeesuite.cos.provider.aliyun.AliyunProvider;
import com.jeesuite.cos.provider.qcloud.QcloudProvider;
import com.jeesuite.cos.provider.qiniu.QiniuProvider;

/**
 * 默认客户端
 * 
 * <br>
 * Class Name   : CosDefaultClientBuilder/
 *
 * @author jiangwei
 * @version 1.0.0
 * @date Mar 27, 2021
 */
public class CosDefaultClientBuilder {

	private static CosProvider  provider;

	public static CosProvider getProvider() {
		if(provider != null)return provider;
		synchronized (CosDefaultClientBuilder.class) {
			if(provider != null)return provider;
			//
			String type = ResourceUtils.getAndValidateProperty("jeesuite.cos.adapter.type");
			
			CosProviderConfig conf = new CosProviderConfig();
			conf.setAccessKey(ResourceUtils.getProperty("jeesuite.cos.adapter.accessKey"));
			conf.setSecretKey(ResourceUtils.getProperty("jeesuite.cos.adapter.secretKey"));
			conf.setAppId(ResourceUtils.getProperty("jeesuite.cos.adapter.appId"));
			conf.setDefaultBucketName(ResourceUtils.getProperty("jeesuite.cos.adapter.defaultBucketName"));
			conf.setMaxAllowdSingleFileSize(ResourceUtils.getLong("jeesuite.cos.adapter.maxFileSize", 0));
			conf.setMaxConnectionsCount(ResourceUtils.getInt("jeesuite.cos.adapter.maxConnections", 100));
			conf.setPrivate(ResourceUtils.getBoolean("jeesuite.cos.adapter.isPrivate", false));
			conf.setRegionName(ResourceUtils.getProperty("jeesuite.cos.adapter.regionName"));
			conf.setUrlPrefix(ResourceUtils.getProperty("jeesuite.cos.adapter.urlPrefix"));
			
			
			if(AliyunProvider.NAME.equals(type)) {
				provider = new AliyunProvider(conf);
			}else if(QcloudProvider.NAME.equals(type)) {
				provider = new QcloudProvider(conf);
			}else if(QiniuProvider.NAME.equals(type)) {
				provider = new QiniuProvider(conf);
			}else {
				throw new JeesuiteBaseException("cos["+type+"] not support");
			}
			
		}
		
		return provider;
	}
	
	
	
}
