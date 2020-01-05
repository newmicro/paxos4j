package cn.oomkiller.paxos4j.config;

import cn.oomkiller.paxos4j.log.LogStorage;
import cn.oomkiller.paxos4j.transport.network.Network;
import lombok.Data;

import java.util.List;

@Data
public class Options {
    //optional
    //User-specified paxoslog storage.
    //Default is null.
    private LogStorage logStorage;

    //optional
    //If poLogStorage == nullptr, sLogStoragePath is required.
    private String logStoragePath;

    //optional
    //If true, the write will be flushed from the operating system
    //buffer cache before the write is considered complete.
    //If this flag is true, writes will be slower.
    //
    //If this flag is false, and the machine crashes, some recent
    //writes may be lost. Note that if it is just the process that
    //crashes (i.e., the machine does not reboot), no writes will be
    //lost even if logSync==false. Because of the data lost, we not guarantee consistence.
    //
    //Default is true.
    private boolean logSync = true;

    //optional
    //Default is 0.
    //This means the write will skip flush at most iSyncInterval times.
    //That also means you will lost at most iSyncInterval count's paxos log.
    private int syncInterval;

    //optional
    //User-specified network.
    private Network network;

    //optional
    //Our default network use udp and tcp combination, a message we use udp or tcp to send decide by a threshold.
    //Message size under iUDPMaxSize we use udp to send.
    //Default is 4096.
     private int udpMaxSize = 4096;

    //optional
    //Our default network io thread count.
    //Default is 1.
    private int ioThreadCount = 1;

    //optional
    //We support to run multi phxpaxos on one process.
    //One paxos group here means one independent phxpaxos. Any two phxpaxos(paxos group) only share network, no other.
    //There is no communication between any two paxos group.
    //Default is 1.
    private int groupCount = 1;

    //required
    //Self node's ip/port.
    private NodeInfo myNode;

    //required
    //All nodes's ip/port with a paxos set(usually three or five nodes).
    private List<NodeInfo> nodeInfoList;

    //optional
    //Only bUseMembership == true, we use option's nodeinfolist to init paxos membership,
    //after that, paxos will remember all nodeinfos, so second time you can run paxos without vecNodeList,
    //and you can only change membership by use function in node.h.
    //
    //Default is false.
    //if bUseMembership == false, that means every time you run paxos will use vecNodeList to build a new membership.
    //when you change membership by a new vecNodeList, we don't guarantee consistence.
    //
    //For test, you can set false.
    //But when you use it to real services, remember to set true.
    private boolean useMembership = false;

    //While membership change, phxpaxos will call this function.
    //Default is nullptr.
    private MembershipChangeCallback membershipChangeCallback;

    //While master change, phxpaxos will call this function.
    //Default is nullptr.
    private MasterChangeCallback masterChangeCallback;

    //optional
    //One phxpaxos can mounting multi state machines.
    //This vector include different phxpaxos's state machines list.
    private List<GroupSMInfo> vecGroupSMInfoList;

    //optional
    //If use this mode, that means you propose large value(maybe large than 5M means large) much more.
    //Large value means long latency, long timeout, this mode will fit it.
    //Default is false
    private boolean largeValueMode = false;

    //optional
    //All followers's ip/port, and follow to node's ip/port.
    //Follower only learn but not participation paxos algorithmic process.
    //Default is empty.
    private List<FollowerNodeInfo> followerNodeInfoList;

    //optional
    //If you use checkpoint replayer feature, set as true.
    //Default is false;
    private boolean useCheckpointReplayer = false;

    //optional
    //Only bUseBatchPropose is true can use API BatchPropose in node.h
    //Default is false;
    private boolean useBatchPropose = false;

    //optional
    //Only bOpenChangeValueBeforePropose is true, that will callback sm's function(BeforePropose).
    //Default is false;
    private boolean openChangeValueBeforePropose = false;
}
