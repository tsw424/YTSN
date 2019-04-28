package com.ytfs.service.dao;

import com.mongodb.client.model.Filters;
import static com.ytfs.service.ServerConfig.REDIS_BLOCK_EXPIRE;
import com.ytfs.service.utils.LogConfigurator;
import io.jafka.jeos.util.Base58;
import io.jafka.jeos.util.KeyUtil;
import java.security.MessageDigest;
import org.apache.commons.codec.binary.Hex;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import redis.clients.jedis.BasicCommands;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.BinaryJedisCluster;

public class Test {

    public static void main(String[] a) throws Exception {
        LogConfigurator.configPath();


        //testRedis();
        //testSeq();     
        testUser();
        //testObject();
    }

 

    private static void testSeq() throws Exception {
        //int uid = Sequence.getSequence(1);
        //System.out.println(Integer.toHexString(uid));

        long l = Sequence.generateBlockID(10);
        System.out.println(Long.toHexString(l));
        System.out.println(Long.toHexString(l + 1));
        System.out.println(Long.toHexString(l + 2));
        System.out.println(Long.toHexString(l + 3));
        System.out.println(Long.toHexString(l + 4));
        System.out.println(Long.toHexString(l + 5));
        System.out.println(Long.toHexString(l + 6));
        System.out.println(Long.toHexString(l + 7));
        System.out.println(Long.toHexString(l + 8));
        System.out.println(Long.toHexString(l + 9));
        System.out.println("********************************");
        l = Sequence.generateBlockID(10);
        System.out.println(Long.toHexString(l));
        System.out.println(Long.toHexString(l + 1));
        System.out.println(Long.toHexString(l + 2));
        System.out.println(Long.toHexString(l + 3));
        System.out.println(Long.toHexString(l + 4));
        System.out.println(Long.toHexString(l + 5));
        System.out.println(Long.toHexString(l + 6));
        System.out.println(Long.toHexString(l + 7));
        System.out.println(Long.toHexString(l + 8));
        System.out.println(Long.toHexString(l + 9));
    }

    private static void testUser() throws Exception {
        User usr = new User(Sequence.generateUserID());
        //Invalid public key:25nsgBoxHrhgZ3xk7eQLqgU36SPomd92dgxeYxXUXndWV
        //25nsgBoxHrhgZ3xk7eQLqgU36SPomd92dgxeYxXUXndWV
        byte[] kuep = Base58.decode("GZsJqUv51pw4c5HnBHiStK3jwJKXZjdtxVwkEShR9Ljb7ZUN1T");//公钥
        byte[] kusp = Base58.decode("5KQKydL7TuRwjzaFSK4ezH9RUXWuYHW1yYDp5CmQfsfTuu9MBLZ");//si钥    
        String ss=KeyUtil.toPublicKey("5KQKydL7TuRwjzaFSK4ezH9RUXWuYHW1yYDp5CmQfsfTuu9MBLZ");
        
        System.out.println(ss);
        kuep=Base58.decode(ss.substring(3));
        
        
        usr.setKUEp(kuep);
        usr.setKUSp(kusp);
        usr.setEosName("username1234");
        usr.setTotalBaseCost(2);
        usr.setUsedSpace(465);
        UserAccessor.addUser(usr);

        usr = UserAccessor.getUser(usr.getUserID());
        System.out.println(usr.getUserID());
        System.out.println(new String(usr.getKUEp()));
        System.out.println(new String(usr.getKUSp()));
        System.out.println(new String(usr.getEosName()));
        System.out.println(usr.getTotalBaseCost());
        System.out.println(usr.getUsedSpace());

    }

    private static void testObject() throws Exception {
        ObjectMeta meta = new ObjectMeta(1, "sssssssssssgewggwgwg".getBytes());
        meta.setBlocks("fsdgdth".getBytes());
        meta.setNLINK(1);
        meta.setVNU(new ObjectId());
        //ObjectDAO.addObject(meta);

        ObjectAccessor.isObjectExists(meta);
        System.out.println("nlink:" + meta.getNLINK());
        System.out.println("VNU:" + meta.getVNU());

        //ObjectDAO.incObjectNLINK(meta);
        meta = ObjectAccessor.getObject(meta.getUserID(), meta.getVHW());
        System.out.println(meta.getUserID());
        System.out.println(new String(meta.getVHW()));
        System.out.println(new String(meta.getBlocks()));
        System.out.println(meta.getNLINK());
    }
}
