package com.ytfs.service.servlet.user;

import com.ytfs.service.dao.ObjectAccessor;
import com.ytfs.service.dao.ObjectMeta;
import com.ytfs.service.dao.User;
import com.ytfs.service.packet.DownloadObjectInitReq;
import com.ytfs.service.packet.DownloadObjectInitResp;
import com.ytfs.service.servlet.Handler;
import org.apache.log4j.Logger;

public class DownloadObjectInitHandler extends Handler<DownloadObjectInitReq> {

    private static final Logger LOG = Logger.getLogger(DownloadObjectInitHandler.class);

    @Override
    public Object handle() throws Throwable {
        User user = this.getUser();
        int userid = user.getUserID();
        ObjectMeta meta = ObjectAccessor.getObject(userid, request.getVHW());
        LOG.info("Download object:" + userid + "/" + meta.getVNU());
        DownloadObjectInitResp resp = new DownloadObjectInitResp();
        resp.setRefers(meta.getBlocks());
        resp.setLength(meta.getLength());
        return resp;
    }

}
