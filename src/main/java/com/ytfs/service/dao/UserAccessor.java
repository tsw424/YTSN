package com.ytfs.service.dao;

import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

public class UserAccessor {

    public static void updateUser(int uid, long usedSpace, long fileTotal, long spaceTotal) {
        Bson bson = Filters.eq("_id", uid);
        Document doc = new Document("usedspace", usedSpace);
        doc.append("fileTotal", fileTotal);
        doc.append("spaceTotal", spaceTotal);
        Document update = new Document("$inc", doc);
        MongoSource.getUserCollection().updateOne(bson, update);
    }

    public static User getUser(int uid) {
        Bson bson = Filters.eq("_id", uid);
        Document document = MongoSource.getUserCollection().find(bson).first();
        if (document == null) {
            return null;
        } else {
            return new User(document);
        }
    }

    public static User getUser(byte[] KUEp) {
        Bson bson = Filters.eq("KUEp", new Binary(KUEp));
        Document document = MongoSource.getUserCollection().find(bson).first();
        if (document == null) {
            return null;
        } else {
            return new User(document);
        }
    }

    public static void addUser(User user) {
        MongoSource.getUserCollection().insertOne(user.toDocument());
    }

}
