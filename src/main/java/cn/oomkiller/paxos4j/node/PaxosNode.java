package cn.oomkiller.paxos4j.node;

import cn.oomkiller.paxos4j.algorithm.Instance;
import cn.oomkiller.paxos4j.config.Config;
import cn.oomkiller.paxos4j.config.Options;
import cn.oomkiller.paxos4j.log.FileLogStore;
import cn.oomkiller.paxos4j.log.LogStorage;
import cn.oomkiller.paxos4j.transport.Communicate;
import cn.oomkiller.paxos4j.transport.DefaultMessageHandler;
import cn.oomkiller.paxos4j.transport.MsgTransport;
import cn.oomkiller.paxos4j.transport.network.DefaultNetwork;
import cn.oomkiller.paxos4j.transport.network.Network;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class PaxosNode implements Node {
  private LogStorage logStorage;
  private Network network;
  private long myNodeId;

  private MsgTransport communicate;
  private Config config;
  private Instance instance;

  //  public PaxosNode() {
  ////    this.logStorage = new FileLogStore(options.getLogDir());
  ////    this.messageHandler = new DefaultMessageHandler(instanceMap);
  ////    this.network = new DefaultNetwork(options.getIp(), options.getPort(),
  // options.getIoThreadCount(), messageHandler);
  ////    this.communicate = new Communicate(this.config, this.network);
  ////    this.instance = new Instance(this.config, this.communicate, this.logStorage);
  ////    this.instanceMap.put(myNodeId, instance);
  //  }

  @Override
  public void runNode(Options options) {
    init(options);
    // step1 set node to network
    // very important, let network on recieve callback can work.
    network.setMessageHandler(new DefaultMessageHandler(this.instance));

    // step2 run network.
    // start recieve message from network, so all must init before this step.
    // must be the last step.
    network.runNetwork();
  }

  @Override
  public void stopNode() {
    //    //1.step: must stop master(app) first.
    //    for (auto & poMaster : m_vecMasterList)
    //    {
    //      poMaster->StopMaster();
    //    }
    //
    //    //2.step: stop proposebatch
    //    for (auto & poProposeBatch : m_vecProposeBatch)
    //    {
    //      poProposeBatch->Stop();
    //    }
    //
    //    //3.step: stop group
    //    for (auto & poGroup : m_vecGroupList)
    //    {
    //      poGroup->Stop();
    //    }

    // 4. step: stop network.
    this.network.stopNetwork();
  }

  public void init(Options options) {
    checkOptions(options);

    this.myNodeId = options.getMyNode().getNodeId();

    // step1 init logstorage
    initLogStorage(options);

    // step2 init network
    initNetWork(options);

    // step3 build masterlist
    //    for (int iGroupIdx = 0; iGroupIdx < options.iGroupCount; iGroupIdx++)
    //    {
    //      MasterMgr poMaster = new MasterMgr(this, iGroupIdx, poLogStorage,
    // options.pMasterChangeCallback);
    //      assert(poMaster != nullptr);
    //      m_vecMasterList.push_back(poMaster);
    //
    //      poMaster.init();
    //    }

    // step4 build grouplist
    //    for (int iGroupIdx = 0; iGroupIdx < oOptions.iGroupCount; iGroupIdx++)
    //    {
    //      Group poGroup = new Group(poLogStorage, poNetWork,
    // m_vecMasterList[iGroupIdx]->GetMasterSM(), iGroupIdx, oOptions);
    //      assert(poGroup != nullptr);
    //      m_vecGroupList.push_back(poGroup);
    //    }
    this.config = new Config(
            this.logStorage,
            options.isLogSync(),
            options.getSyncInterval(),
            options.isUseMembership(),
            options.getMyNode(),
            options.getNodeInfoList());
    this.communicate = new Communicate(this.config, this.network);
    this.instance = new Instance(this.config, this.communicate, this.logStorage);

    // step5 build batchpropose
    //    if (options.useBatchPropose)
    //    {
    //      for (int groupIdx = 0; groupIdx < options.getGroupCount(); groupIdx++)
    //      {
    //        ProposeBatch poProposeBatch = new ProposeBatch(groupIdx, this, &m_oNotifierPool);
    //        assert(poProposeBatch != nullptr);
    //        m_vecProposeBatch.push_back(poProposeBatch);
    //      }
    //    }

    // step6 init statemachine
    initStateMachine(options);

    // step7 parallel init group
    //    for (Group poGroup : m_vecGroupList)
    //    {
    //      poGroup.startInit();
    //    }
    instance.init();

    // last step. must init ok, then should start threads.
    // because that stop threads is slower, if init fail, we need much time to stop many threads.
    // so we put start threads in the last step.
    instance.start();
    //    runMaster(options);
    //    runProposeBatch();
  }

  private void initStateMachine(Options options) {
    //    for (auto & oGroupSMInfo : oOptions.vecGroupSMInfoList)
    //    {
    //      for (auto & poSM : oGroupSMInfo.vecSMList)
    //      {
    //        AddStateMachine(oGroupSMInfo.iGroupIdx, poSM);
    //      }
    //    }
  }

  private void initNetWork(Options options) {
    if (options.getNetwork() != null) {
      this.network = options.getNetwork();
      log.info("OK, use user network");
      return;
    }

    this.network =
        new DefaultNetwork(options.getMyNode().getIp(), options.getMyNode().getPort(), options.getIoThreadCount());

    log.info("OK, use default network");
  }

  private void initLogStorage(Options options) {
    if (options.getLogStorage() != null) {
      this.logStorage = options.getLogStorage();
      log.info("OK, use user logstorage");
      return;
    }

    if (StringUtils.isEmpty(options.getLogStoragePath())) {
      log.error("LogStorage Path is null");
      throw new IllegalArgumentException();
    }

    this.logStorage = new FileLogStore(options.getLogStoragePath());
//    .init(options.getLogStoragePath(), options.getGroupCount());
    log.info("OK, use default logstorage");
  }

  private void checkOptions(Options options) {
    if (options.getLogStorage() == null && StringUtils.isEmpty(options.getLogStoragePath())) {
      log.error("no logpath and logstorage is null");
      throw new IllegalArgumentException();
    }

    if (options.getUdpMaxSize() > 64 * 1024) {
      log.error("udp max size %zu is too large", options.getUdpMaxSize());
      throw new IllegalArgumentException();
    }

    if (options.getGroupCount() > 200) {
      log.error("group count %d is too large", options.getGroupCount());
      throw new IllegalArgumentException();
    }

    if (options.getGroupCount() <= 0) {
      log.error("group count %d is small than zero or equal to zero", options.getGroupCount());
      throw new IllegalArgumentException();
    }
  }

  @Override
  public boolean propose(byte[] value) {
    instance.commitNewValue(value);
    //    return m_vecGroupList[iGroupIdx]->GetCommitter()->NewValueGetID(sValue, llInstanceID,
    // poSMCtx);
    return false;
  }

  @Override
  public long getMyNodeId() {
    return this.myNodeId;
  }
}
