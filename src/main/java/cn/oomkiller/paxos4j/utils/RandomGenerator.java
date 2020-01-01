package cn.oomkiller.paxos4j.utils;

public interface RandomGenerator {
    default int fastRand() {
        if (!seed_thread_safe.init)
        {
            InitFastRandomSeed();
        }

        return rand_r(&seed_thread_safe.seed);
    }

    static void InitFastRandomSeed()
    {
        static pthread_once_t once = PTHREAD_ONCE_INIT;
        pthread_once(&once, InitFastRandomSeedAtFork);

        ResetFastRandomSeed();
    }
}
