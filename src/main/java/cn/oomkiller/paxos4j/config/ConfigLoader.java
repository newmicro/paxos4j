package cn.oomkiller.paxos4j.config;

import cn.oomkiller.paxos4j.utils.YamlUtil;
import java.util.List;

public class ConfigLoader {
  public void load(String confFile) {
    YamlUtil.loadAs(confFile, Config.class);

    NodeInfo myNode;
    if (parse_ipport(argv[1], myNode) != 0) {
      printf("parse myip:myport fail\n");
      return -1;
    }

    List<NodeInfo> nodeInfoList;
    if (parse_ipport_list(argv[2], nodeInfoList) != 0) {
      printf("parse ip/port list fail\n");
      return -1;
    }

  }

  public Options populate() {
      Options oOptions;

      int ret = MakeLogStoragePath(oOptions.sLogStoragePath);

      //this groupcount means run paxos group count.
      //every paxos group is independent, there are no any communicate between any 2 paxos group.
      oOptions.groupCount = 1;

      oOptions.oMyNode = m_oMyNode;
      oOptions.vecNodeInfoList = m_vecNodeList;

      GroupSMInfo oSMInfo;
      oSMInfo.iGroupIdx = 0;
      //one paxos group can have multi state machine.
      oSMInfo.vecSMList.push_back(&m_oEchoSM);
      oOptions.vecGroupSMInfoList.push_back(oSMInfo);

      //use logger_google to print log
      LogFunc pLogFunc;
      ret = LoggerGoogle :: GetLogger("phxecho", "./log", 3, pLogFunc);
      if (ret != 0)
      {
          printf("get logger_google fail, ret %d\n", ret);
          return ret;
      }

      //set logger
      oOptions.pLogFunc = pLogFunc;

  }
}
