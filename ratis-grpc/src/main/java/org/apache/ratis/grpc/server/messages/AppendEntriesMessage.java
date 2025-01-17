package org.apache.ratis.grpc.server.messages;

import java.util.Base64;

import org.apache.ratis.grpc.server.GrpcLogAppender;
import org.apache.ratis.grpc.server.GrpcLogAppender.AppendEntriesRequest;
import org.apache.ratis.proto.RaftProtos.AppendEntriesRequestProto;
import org.apache.ratis.proto.RaftProtos.LogEntryProto;
import org.apache.ratis.server.fuzzer.comm.GsonHelper;
import org.apache.ratis.server.fuzzer.comm.JsonMessage;
import org.apache.ratis.server.fuzzer.events.LogUpdateEvent;
import org.apache.ratis.server.fuzzer.messages.Message;
import org.apache.ratis.server.protocol.TermIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class AppendEntriesMessage extends Message {

    public static final Logger LOG = LoggerFactory.getLogger(AppendEntriesMessage.class);
    private AppendEntriesRequestProto proto;
    private AppendEntriesRequest request;
    private GrpcLogAppender logAppender;

    public AppendEntriesMessage(AppendEntriesRequestProto proto, AppendEntriesRequest request, GrpcLogAppender logAppender, String invokeServerId) {
        this.type = "append_entries_request";
        this.messageId = fuzzerClient.generateId();
        this.proto = proto;
        this.request = request;
        this.logAppender = logAppender;
        this.invokeServerId = invokeServerId;

        // fuzzerClient.sendEvent(new LogUpdateEvent(this.proto.getServerRequest().getRequestorId().toStringUtf8(), this.proto.getEntriesList()));

        LOG.info("Intercepted append entries from server " + this.proto.getServerRequest().getRequestorId().toStringUtf8() + " to " + this.proto.getServerRequest().getReplyId().toStringUtf8());
        
        if (!fuzzerClient.isControlledExecution())
            invoke();
    }

    @Override
    public void invoke() {
        if (fuzzerClient.shouldShutdown())
            return;
        LOG.info("Invoking append entries on server: " + invokeServerId);
        try {
            logAppender.appendLog_(proto, request, false);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public String toJsonString() {
        String to = proto.getServerRequest().getReplyId().toStringUtf8();
        String from = proto.getServerRequest().getRequestorId().toStringUtf8();

        JsonObject json = new JsonObject();
        TermIndex prevLog = request.getPreviousLog();
        long prev_log_idx = prevLog == null ? 0 : prevLog.getIndex();
        long prev_log_term = prevLog == null ? 0 : prevLog.getTerm();

        json.addProperty("type", type);
        json.addProperty("leader_id", proto.getServerRequest().getRequestorId().toStringUtf8());
        json.addProperty("term", (double) proto.getLeaderTerm());
        json.addProperty("prev_log_idx", (double) prev_log_idx);
        json.addProperty("prev_log_term", (double) prev_log_term);
        json.addProperty("leader_commit", (double) proto.getLeaderCommit());
        json.addProperty("msg_id", (double) -1);

        JsonObject entries = new JsonObject();
        int i = 0;
        String entryStr;
        for(LogEntryProto entry : proto.getEntriesList()) {
            if (entry.hasStateMachineLogEntry()) {
                JsonObject entryJson = new JsonObject();
                entryJson.addProperty("term", (double) entry.getTerm());
                entryJson.addProperty("id", (int) entry.getIndex());
                entryJson.addProperty("session", ""); // Not applicable for Ratis
                entryJson.addProperty("type", "str"); // Not applicable for Ratis
                entryStr = Base64.getEncoder().encodeToString(entry.getStateMachineLogEntry().getLogData().toByteArray());
                entryJson.addProperty("data_len", entryStr.length());
                if (entryStr.length() > 0) {
                    entryJson.addProperty("data", entryStr);
                } else {
                    entryJson.addProperty("data", "");
                }
                entries.add(Integer.toString(i), entryJson);
                i++;
            }
        }
        json.add("entries", entries);

        Gson gson = GsonHelper.gson;

        JsonMessage msg = new JsonMessage(to, type, gson.toJson(json).getBytes());
        msg.setFrom(from);
        msg.setId(this.getId());

        return gson.toJson(msg);
    }
    
}
