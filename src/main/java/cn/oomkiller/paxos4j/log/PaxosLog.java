package cn.oomkiller.paxos4j.log;

import cn.oomkiller.paxos4j.algorithm.Acceptor;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PaxosLog {
    private LogStorage logStorage;

    public void writeLog(long instanceId, byte[] value) {
        AcceptorStateData oState;
        oState.set_instanceid(llInstanceID);
        oState.set_acceptedvalue(sValue);
        oState.set_promiseid(0);
        oState.set_promisenodeid(nullnode);
        oState.set_acceptedid(0);
        oState.set_acceptednodeid(nullnode);

        int ret = writeState(oWriteOptions, iGroupIdx, llInstanceID, oState);
        if (ret != 0)
        {
            PLG1Err("WriteState to db fail, groupidx %d instanceid %lu ret %d", iGroupIdx, llInstanceID, ret);
            return ret;
        }
    }

    public byte[] readLog(long instanceId) {

    }

    public long getMaxInstanceIdFromLog() {
        return 0L;
    }

    public void writeState(long instanceId, Acceptor.State state) {
    }

    public Acceptor.State readState(long instanceId) {
        return null;
    }
}
