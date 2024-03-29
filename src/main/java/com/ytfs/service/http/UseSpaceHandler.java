package com.ytfs.service.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ytfs.service.dao.UserAccessor;
import io.yottachain.nodemgmt.YottaNodeMgmt;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

public class UseSpaceHandler extends HttpHandler {

    static final String REQ_TOTAL_PATH = "/total";
    static final String REQ_ACTIVE_NODES_PATH = "/active_nodes";
    static final String REQ_STAT_PATH = "/statistics";

    @Override
    public void service(Request rqst, Response rspns) throws Exception {
        try {
            rspns.setContentType("text/json");
            String path = rqst.getContextPath();
            if (path.equalsIgnoreCase(REQ_TOTAL_PATH)) {
                String json = gettotal();
                rspns.getWriter().write(json);
            } else if (path.equalsIgnoreCase(REQ_ACTIVE_NODES_PATH)) {
                List<Map<String, String>> ls = YottaNodeMgmt.activeNodesList();
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(ls);
                rspns.getWriter().write(json);
            } else if (path.equalsIgnoreCase(REQ_STAT_PATH)) {
                Map<String, Long> map = YottaNodeMgmt.statistics();
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(map);
                rspns.getWriter().write(json);
            } else {
                rspns.setContentType("text/html");
                InputStream is = this.getClass().getResourceAsStream("/statapi.html");
                byte[] bs = new byte[1024];
                int len = 0;
                while ((len = is.read(bs)) != -1) {
                    rspns.getOutputStream().write(bs, 0, len);
                }
            }
            rspns.flush();
        } catch (Exception e) {
            String message = e.getMessage();
            rspns.sendError(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode(), message);
        }
    }

    private String gettotal() throws Exception {
        Document doc = UserAccessor.total();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(doc);
    }

    public static String getusertotal(String id) throws Exception {
        int userid = 0;
        try {
            userid = Integer.parseInt(id);
        } catch (Exception r) {
            throw new Exception("Invalid userid");
        }
        Document doc = UserAccessor.userTotal(userid);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(doc);
    }

}
