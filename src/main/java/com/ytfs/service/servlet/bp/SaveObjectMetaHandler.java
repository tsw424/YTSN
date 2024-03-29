package com.ytfs.service.servlet.bp;

import static com.ytfs.common.ServiceErrorCode.INVALID_UPLOAD_ID;
import com.ytfs.common.conf.ServerConfig;
import com.ytfs.service.dao.ObjectAccessor;
import com.ytfs.common.net.P2PUtils;
import com.ytfs.common.node.SuperNodeList;
import com.ytfs.service.servlet.Handler;
import com.ytfs.common.ServiceException;
import com.ytfs.service.packet.ObjectRefer;
import com.ytfs.service.packet.SaveObjectMetaReq;
import com.ytfs.service.packet.SaveObjectMetaResp;
import com.ytfs.service.servlet.ReferCache;
import io.yottachain.nodemgmt.core.vo.SuperNode;
import java.util.List;
import org.apache.log4j.Logger;

public class SaveObjectMetaHandler extends Handler<SaveObjectMetaReq> {

    private static final Logger LOG = Logger.getLogger(SaveObjectMetaHandler.class);

    /**
     * 保存上传进度至BPU
     *
     * @param req
     * @return
     * @throws ServiceException
     */
    public static SaveObjectMetaResp saveObjectMetaCall(SaveObjectMetaReq req) throws ServiceException {
        SuperNode node = SuperNodeList.getUserSuperNode(req.getUserID());
        if (node.getId() == ServerConfig.superNodeID) {
            return saveObjectMeta(req);
        } else {
            return (SaveObjectMetaResp) P2PUtils.requestBP(req, node);
        }
    }

    @Override
    public Object handle() throws Throwable {
        return saveObjectMeta(request);
    }

    private static SaveObjectMetaResp saveObjectMeta(SaveObjectMetaReq request) throws ServiceException {
        LOG.info("Save object meta:" + request.getUserID() + "/" + request.getVNU()
                + "/" + request.getRefer().getId() + "/" + request.getRefer().getVBI());
        if (request.getRefer().getVBI() == 0) {
            throw new ServiceException(INVALID_UPLOAD_ID);
        }
        SaveObjectMetaResp resp = new SaveObjectMetaResp();
        List<ObjectRefer> refers = ReferCache.getRefersCache(request.getVNU(), request.getUserID());
        resp.setExists(false);
        synchronized (refers) {
            for (ObjectRefer refer : refers) {
                if (refer.getId() == request.getRefer().getId()) {
                    resp.setExists(true);
                    break;
                }
            }
            if (!resp.isExists()) {
                long usedspace = sumUsedSpace(request.getRefer().getRealSize(), request.getNlink());
                refers.add(request.getRefer());
                byte[] bs = ObjectRefer.merge(refers);
                ObjectAccessor.updateObject(request.getVNU(), bs, usedspace);
            }
        }
        return resp;
    }

    private static long sumUsedSpace(long space, long nlink) {
        return space / nlink + 1;
    }

}
