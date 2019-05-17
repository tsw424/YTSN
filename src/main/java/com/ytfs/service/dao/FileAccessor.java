package com.ytfs.service.dao;

import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import static com.ytfs.common.ServiceErrorCode.OBJECT_ALREADY_EXISTS;
import com.ytfs.common.ServiceException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileAccessor {

    public static void saveFileMeta(FileMeta meta) throws ServiceException {
        try {
            MongoSource.getFileCollection().insertOne(meta.toDocument());
        } catch (MongoWriteException e) {
            if (e.getMessage().contains("dup key")) {
                throw new ServiceException(OBJECT_ALREADY_EXISTS);
            }
        }
    }

    public static FileMeta getFileMeta(ObjectId bucketid, String filename) {
        Bson bson1 = Filters.eq("bucketId", bucketid);
        Bson bson2 = Filters.eq("fileName", filename);
        Bson bson = Filters.and(bson1, bson2);
        Document doc = MongoSource.getFileCollection().find(bson).first();
        if (doc == null) {
            return null;
        } else {
            return new FileMeta(doc);
        }
    }

    //根据bucketname获取objectname
    public static Map<String,byte[]> listObjectByBucket(ObjectId bucketId, int userid) {
//        BucketMeta bucketMeta = BucketCache.getBucket(userid,bucketname);
//        List<String> ls = new ArrayList();
        Map<String,byte[]> map = new HashMap<>();
        Bson bson1 = Filters.eq("bucketId", bucketId);
        Bson bson2 = Filters.eq("userId", userid);
        Bson bson = Filters.and(bson1, bson2);
        Document fields = new Document("fileName", 1);
        fields.append("meta",1);
        FindIterable<Document> it = MongoSource.getFileCollection().find(bson).projection(fields);
        for (Document doc : it) {
            map.put(doc.getString("fileName"),((Binary) doc.get("meta")).getData());
        }
        return map;
    }
    
}
