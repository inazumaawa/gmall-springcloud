package obs.service.impl;

import com.obs.services.ObsClient;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectResult;
import obs.config.ObsConfig;
import obs.service.ObsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ObsServiceImpl implements ObsService {

    @Autowired
    private ObsClient obsClient;
    @Autowired
    private ObsConfig obsConfig;

    @Override
    public String upload(String bucketKey, String objectKey, InputStream inputStream, String contentType) {
        String bucketName = obsConfig.getBucketName(bucketKey);
        if (obsClient.doesObjectExist(bucketName, objectKey)) {
            throw new RuntimeException("文件已存在: " + objectKey);
        }
        obsClient.putObject(bucketName, objectKey, inputStream);
        return buildUrl(bucketKey, objectKey);
    }

    @Override
    public String getUrl(String bucketKey, String objectKey) {
        return buildUrl(bucketKey, objectKey);
    }

    @Override
    public void delete(String bucketKey, String objectKey) {
        String bucketName = obsConfig.getBucketName(bucketKey);
        obsClient.deleteObject(bucketName, objectKey);
    }

    @Override
    public String replace(String bucketKey, String objectKey, InputStream inputStream, String contentType) {
        String bucketName = obsConfig.getBucketName(bucketKey);
        obsClient.putObject(bucketName, objectKey, inputStream);
        return buildUrl(bucketKey, objectKey);
    }

    @Override
    public List<String> list(String bucketKey, String prefix) {
        String bucketName = obsConfig.getBucketName(bucketKey);
        ListObjectsRequest request = new ListObjectsRequest(bucketName);
        request.setPrefix(prefix);
        request.setMaxKeys(100);
        ObjectListing result = obsClient.listObjects(request);
        List<String> urls = new ArrayList<>();
        for (ObsObject obj : result.getObjects()) {
            urls.add(buildUrl(bucketKey, obj.getObjectKey()));
        }
        return urls;
    }

    private String buildUrl(String bucketKey, String objectKey) {
        String baseUrl = obsConfig.getBaseUrl(bucketKey);
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (!objectKey.startsWith("/")) {
            objectKey = "/" + objectKey;
        }
        return baseUrl + objectKey;
    }
}
