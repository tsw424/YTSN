package com.ytfs.service.dao;

import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.ytfs.common.ServiceException;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

import static com.ytfs.common.ServiceErrorCode.OBJECT_ALREADY_EXISTS;

public class FileAccessor {

    private static final Logger LOG = Logger.getLogger(FileAccessor.class);
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
    public static Map<String,byte[]> listObjectByBucket(ObjectId bucketId) {
        LOG.info("bucketId======"+bucketId);
        Map<String,byte[]> map = new HashMap<>();
        Bson bson = Filters.eq("bucketId", bucketId);
        Document fields = new Document("fileName", 1);
        fields.append("meta",1);
        FindIterable<Document> it = MongoSource.getFileCollection().find(bson).projection(fields);
        for (Document doc : it) {
            map.put(doc.getString("fileName"),((Binary) doc.get("meta")).getData());
        }
        return map;
    }

    /**
     *
     * @param meta
     * @throws ServiceException
     *   Delete object by bucket and fileName
     */
    public static void deleteFileMeta(FileMeta meta) throws ServiceException {
        LOG.info("fileName=====deleteFileMeta() ======"+meta.getFileName());
        LOG.info("bucketId=====deleteFileMeta() ======"+meta.getBucketId());
        Bson bson1 = Filters.eq("bucketId", meta.getBucketId());
        Bson bson2 = Filters.eq("fileName", meta.getFileName());
        Bson bson = Filters.and(bson1, bson2);
        MongoSource.getFileCollection().deleteOne(bson);
    }
}
