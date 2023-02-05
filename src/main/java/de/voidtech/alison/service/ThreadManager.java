package main.java.de.voidtech.alison.service;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ThreadManager
{
    private HashMap<String, ExecutorService> threadMap;
    
    public ThreadManager() {
        this.threadMap = new HashMap<>();
    }
    
    private ExecutorService findOrSpawnThread(final String threadID) {
        if (!threadMap.containsKey(threadID)) {
            BasicThreadFactory factory = new BasicThreadFactory.Builder()
                    .namingPattern(threadID + "-%d")
                    .daemon(true)
                    .priority(Thread.NORM_PRIORITY)
                    .build();
            threadMap.put(threadID, Executors.newCachedThreadPool(factory));
        }
        return threadMap.get(threadID);
    }
    
    public ExecutorService getThreadByName(final String name) {
        return this.findOrSpawnThread(name);
    }
}