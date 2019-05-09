package com.ytfs.service.servlet;

import com.ytfs.service.node.NodeCache;
import com.ytfs.service.packet.NodeRegReq;
import com.ytfs.service.utils.SerializationUtil;
import com.ytfs.service.utils.ServiceErrorCode;
import com.ytfs.service.utils.ServiceException;
import com.ytfs.service.packet.StatusRepReq;
import com.ytfs.service.packet.UploadShardResp;
import io.yottachain.nodemgmt.core.exception.NodeMgmtException;
import io.yottachain.p2phost.interfaces.NodeCallback;
import org.apache.log4j.Logger;

public class FromNodeMsgDispatcher implements NodeCallback {

    private static final Logger LOG = Logger.getLogger(FromNodeMsgDispatcher.class);

    @Override
    public byte[] onMessageFromNode(byte[] data, String nodekey) {
        Object response = null;
        try {
            Object message = SerializationUtil.deserialize(data);
            if (message instanceof UploadShardResp) {
                int nodeId = NodeCache.getNodeId(nodekey);
                response = UploadShardHandler.uploadShardResp((UploadShardResp) message, nodeId);
            } else if (message instanceof NodeRegReq) {
                response = NodeMessageHandler.reg((NodeRegReq) message, nodekey);
            } else if (message instanceof StatusRepReq) {
                int nodeId = NodeCache.getNodeId(nodekey);
                response = NodeMessageHandler.statusRep((StatusRepReq) message, nodeId);
            }
            return SerializationUtil.serialize(response);
        } catch (ServiceException s) {
            LOG.error("", s);
            return SerializationUtil.serialize(s);
        } catch (NodeMgmtException ne) {
            LOG.error("", ne);
            ServiceException se = new ServiceException(ServiceErrorCode.INVALID_NODE_ID, ne.getMessage());
            return SerializationUtil.serialize(se);
        } catch (Throwable r) {
            LOG.error("", r);
            ServiceException se = new ServiceException(ServiceErrorCode.SERVER_ERROR, r.getMessage());
            return SerializationUtil.serialize(se);
        }
    }

}
