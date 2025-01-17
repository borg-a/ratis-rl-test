package org.apache.ratis.examples.common;

import org.apache.ratis.RaftConfigKeys;
import org.apache.ratis.conf.Parameters;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcConfigKeys;
import org.apache.ratis.grpc.server.GrpcService;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.rpc.SupportedRpcType;
import org.apache.ratis.server.impl.MiniRaftCluster;
import org.apache.ratis.util.NetUtils;

import java.util.Optional;

/**
 * A {@link MiniRaftCluster} with {{@link SupportedRpcType#GRPC}} and data stream disabled.
 */
public class MiniRaftClusterWithGrpc extends MiniRaftCluster.RpcBase {
  public static final Factory<MiniRaftClusterWithGrpc> FACTORY
      = new Factory<MiniRaftClusterWithGrpc>() {
    @Override
    public MiniRaftClusterWithGrpc newCluster(String[] ids, String[] listenerIds, RaftProperties prop) {
      RaftConfigKeys.Rpc.setType(prop, SupportedRpcType.GRPC);
      return new MiniRaftClusterWithGrpc(ids, listenerIds, prop, null);
    }
  };

  public interface FactoryGet extends Factory.Get<MiniRaftClusterWithGrpc> {
    @Override
    default Factory<MiniRaftClusterWithGrpc> getFactory() {
      return FACTORY;
    }
  }

//   public static final DelayLocalExecutionInjection sendServerRequestInjection =
    //   new DelayLocalExecutionInjection(GrpcService.GRPC_SEND_SERVER_REQUEST);

  protected MiniRaftClusterWithGrpc(String[] ids, String[] listenerIds, RaftProperties properties, Parameters parameters) {
    super(ids, listenerIds, properties, parameters);
  }

  @Override
  protected Parameters setPropertiesAndInitParameters(RaftPeerId id, RaftGroup group, RaftProperties properties) {
    GrpcConfigKeys.Server.setPort(properties, getPort(id, group));
    Optional.ofNullable(getAddress(id, group, RaftPeer::getClientAddress)).ifPresent(address ->
        GrpcConfigKeys.Client.setPort(properties, NetUtils.createSocketAddr(address).getPort()));
    Optional.ofNullable(getAddress(id, group, RaftPeer::getAdminAddress)).ifPresent(address ->
        GrpcConfigKeys.Admin.setPort(properties, NetUtils.createSocketAddr(address).getPort()));
    return parameters;
  }

  @Override
  protected void blockQueueAndSetDelay(String leaderId, int delayMs)
      throws InterruptedException {
    // RaftTestUtil.blockQueueAndSetDelay(getServers(), sendServerRequestInjection,
    //     leaderId, delayMs, getTimeoutMax());
    throw new UnsupportedOperationException("Unimplemented method 'blockQueueAndSetDelay'");
  }

  @Override
  public void setBlockRequestsFrom(String src, boolean block) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setBlockRequestsFrom'");
  }
}

