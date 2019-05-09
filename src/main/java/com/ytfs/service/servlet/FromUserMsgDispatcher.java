package com.ytfs.service.servlet;

import com.ytfs.service.dao.User;
import com.ytfs.service.dao.UserCache;
import com.ytfs.service.node.NodeManager;
import com.ytfs.service.packet.s3.CreateBucketReq;
import com.ytfs.service.packet.DownloadObjectInitReq;
import com.ytfs.service.packet.DownloadBlockInitReq;
import com.ytfs.service.packet.s3.DownloadFileReq;
import com.ytfs.service.packet.GetBalanceReq;
import com.ytfs.service.packet.s3.ListBucketReq;
import com.ytfs.service.packet.ListSuperNodeReq;
import com.ytfs.service.packet.ListSuperNodeResp;
import com.ytfs.service.packet.SubBalanceReq;
import com.ytfs.service.utils.SerializationUtil;
import com.ytfs.service.utils.ServiceErrorCode;
import com.ytfs.service.utils.ServiceException;
import com.ytfs.service.packet.UploadBlockDBReq;
import com.ytfs.service.packet.UploadBlockDupReq;
import com.ytfs.service.packet.UploadBlockEndReq;
import com.ytfs.service.packet.UploadBlockInitReq;
import com.ytfs.service.packet.UploadBlockSubReq;
import com.ytfs.service.packet.s3.UploadFileReq;
import com.ytfs.service.packet.UploadObjectEndReq;
import com.ytfs.service.packet.UploadObjectInitReq;
import io.jafka.jeos.util.Base58;
import io.yottachain.p2phost.interfaces.UserCallback;
import org.apache.log4j.Logger;

public class FromUserMsgDispatcher implements UserCallback {

    private static final Logger LOG = Logger.getLogger(FromUserMsgDispatcher.class);

    @Override
    public byte[] onMessageFromUser(byte[] data, String userkey) {
        User user = UserCache.getUser(Base58.decode(userkey));
        if (user == null) {
            LOG.warn("Invalid public key:" + userkey);
            ServiceException se = new ServiceException(ServiceErrorCode.INVALID_USER_ID);
            return SerializationUtil.serialize(se);
        }
        Object message = SerializationUtil.deserialize(data);
        Object response = null;
        try {
            if (message instanceof ListSuperNodeReq) {
                ListSuperNodeResp resp = new ListSuperNodeResp();
                LOG.info("List super node...");
                resp.setSuperList(NodeManager.getSuperNode());
                response = resp;
            } else if (message instanceof UploadObjectInitReq) {
                response = UploadObjectHandler.init((UploadObjectInitReq) message, user);
            } else if (message instanceof GetBalanceReq) {
                response = UploadObjectHandler.getBalanceReq((GetBalanceReq) message, user);
            } else if (message instanceof SubBalanceReq) {
                response = UploadObjectHandler.subBalanceReq((SubBalanceReq) message, user);
            } else if (message instanceof UploadBlockInitReq) {
                response = UploadBlockHandler.init((UploadBlockInitReq) message, user);
            } else if (message instanceof UploadBlockDupReq) {
                response = UploadBlockHandler.repeat((UploadBlockDupReq) message, user);
            } else if (message instanceof UploadBlockDBReq) {
                response = UploadBlockHandler.saveToDB((UploadBlockDBReq) message, user);
            } else if (message instanceof UploadBlockSubReq) {
                response = UploadBlockHandler.subUpload((UploadBlockSubReq) message, user);
            } else if (message instanceof UploadBlockEndReq) {
                response = UploadBlockHandler.complete((UploadBlockEndReq) message, user);
            } else if (message instanceof UploadObjectEndReq) {
                response = UploadObjectHandler.complete((UploadObjectEndReq) message, user);
            } else if (message instanceof DownloadObjectInitReq) {
                response = DownloadHandler.init((DownloadObjectInitReq) message, user);
            } else if (message instanceof DownloadBlockInitReq) {
                response = DownloadHandler.getBlockMeta((DownloadBlockInitReq) message, user);
            } else if (message instanceof UploadFileReq) {
                response = FileMetaHandler.writeFileMeta((UploadFileReq) message, user);
            } else if (message instanceof DownloadFileReq) {
                response = FileMetaHandler.readFileMeta((DownloadFileReq) message, user);
            } else if (message instanceof CreateBucketReq) {
                response = FileMetaHandler.createBucket((CreateBucketReq) message, user);
            } else if (message instanceof ListBucketReq) {
                response = FileMetaHandler.listBucket((ListBucketReq) message, user);
            }
            return SerializationUtil.serialize(response);
        } catch (ServiceException s) {
            LOG.error("", s);
            return SerializationUtil.serialize(s);
        } catch (Throwable r) {
            LOG.error("", r);
            ServiceException se = new ServiceException(ServiceErrorCode.SERVER_ERROR, r.getMessage());
            return SerializationUtil.serialize(se);
        }
    }

}
