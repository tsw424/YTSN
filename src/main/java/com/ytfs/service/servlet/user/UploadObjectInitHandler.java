package com.ytfs.service.servlet.user;

import com.ytfs.common.conf.ServerConfig;
import com.ytfs.service.dao.ObjectAccessor;
import com.ytfs.service.dao.ObjectMeta;
import com.ytfs.service.dao.User;
import com.ytfs.common.node.SuperNodeList;
import com.ytfs.service.servlet.Handler;
import com.ytfs.common.ServiceErrorCode;
import com.ytfs.common.ServiceException;
import com.ytfs.common.eos.EOSClient;
import com.ytfs.service.packet.ObjectRefer;
import com.ytfs.service.packet.UploadObjectInitReq;
import com.ytfs.service.packet.UploadObjectInitResp;
import io.yottachain.nodemgmt.core.vo.SuperNode;
import java.util.List;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

public class UploadObjectInitHandler extends Handler<UploadObjectInitReq> {

    private static final Logger LOG = Logger.getLogger(UploadObjectInitHandler.class);

    @Override
    public Object handle() throws Throwable {
        User user = this.getUser();
        LOG.info("Upload object init " + user.getUserID());
        int userid = user.getUserID();
        SuperNode n = SuperNodeList.getUserSuperNode(userid);
        if (n.getId() != ServerConfig.superNodeID) {
            throw new ServiceException(ServiceErrorCode.INVALID_USER_ID);
        }
        if (request.getVHW() == null || request.getVHW().length != 32) {
            throw new ServiceException(ServiceErrorCode.INVALID_VHW);
        }
        ObjectMeta meta = new ObjectMeta(userid, request.getVHW());
        boolean exists = ObjectAccessor.isObjectExists(meta);
        UploadObjectInitResp resp = new UploadObjectInitResp(false);
        if (exists) {
            resp.setVNU(meta.getVNU());
            int nlink = meta.getNLINK();
            if (nlink == 0) {//正在上传               
                List<ObjectRefer> refers = ObjectRefer.parse(meta.getBlocks());
                short[] blocks = new short[refers.size()];
                for (int ii = 0; ii < blocks.length; ii++) {
                    blocks[ii] = refers.get(ii).getId();
                }
                resp.setBlocks(blocks);
            } else {
                ObjectAccessor.incObjectNLINK(meta);
                resp.setRepeat(true);
                return resp;
            }
        } else {
            meta.setVNU(new ObjectId());
            resp.setVNU(meta.getVNU());
        }
        boolean has= EOSClient.hasSpace(request.getLength(),user.getUsername());      
        if (has) {           
            meta.setLength(request.getLength());
            meta.setNLINK(0);
            ObjectAccessor.insertOrUpdate(meta);
        } else {
            throw new ServiceException(ServiceErrorCode.NOT_ENOUGH_DHH);
        }
        return resp;
    }

}
