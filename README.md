## 介绍
阿里云、七牛云、腾讯云等云存储适配


## 文档
具体使用请查看[使用文档](http://docs.jeesuite.com/docments/jeesuite-cos-adapter.html)

## 实例代码
```java
public void test() {
		
		CosProvider provider = CosDefaultClientBuilder.getProvider();
		
		String bucketName = "jeesuite";
		//创建bucket
		provider.createBucket(bucketName);
		
		CUploadObject uploadObject = new CUploadObject(new File("/Users/jiangwei/Desktop/1.txt")).bucketName(bucketName).folderPath("2020/01/13");
		//上传
		CUploadResult result = provider.upload(uploadObject);
		//是否存在
		boolean exists = provider.exists("jeesuite", result.getFileKey());
		//元信息
		CObjectMetadata metadata = provider.getObjectMetadata(bucketName, result.getFileKey());
		//删除
		provider.delete(null, result.getFileKey());
		
		provider.close();
	}
```