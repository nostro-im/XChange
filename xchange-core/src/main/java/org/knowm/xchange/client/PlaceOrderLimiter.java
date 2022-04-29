package org.knowm.xchange.client;

import org.knowm.xchange.exceptions.RateLimitExceededException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

public class PlaceOrderLimiter {
    public static final String LIMIT_KEY = "placeOrderLimit";
    public static final String SLEEP_MILLIS_KEY = "placeOrderSleep";
    public static final String MAX_SLEEP_MILLIS_KEY = "placeOrderMaxSleep";
    
    private static final long SLEEP_MARGIN_MILLIS = 5;

    private final int limit;
    private final long sleepMillis;
    private final long maxSleepMillis;
    
    private final ReentrantLock lock = new ReentrantLock(true); 
    private final List<Long> timestamps;

    public PlaceOrderLimiter(int limit, long sleepMillis, long maxSleepMillis) {
        this.limit = limit;
        this.sleepMillis = sleepMillis;
        this.maxSleepMillis = maxSleepMillis;
        
        timestamps = new ArrayList<>();
        for(int i = 0; i < limit; i++) {
            timestamps.add(0L);
        }
    }

    public String executePlace(Callable<String> callable) throws IOException {
        // if limit is not set, call place order directly
        if (limit <= 0) {
            try {
                return callable.call();
            } catch (Exception e) {
                return handleException(e);
            }
        }
        
        // otherwise execute according to the limit 
        long t0 = System.currentTimeMillis();
        lock.lock();
        try {
            long t1 = System.currentTimeMillis();
            if (t1 - t0 > maxSleepMillis) {
                throw new RateLimitExceededException("Exceeded waiting time " + maxSleepMillis + "ms");
            }
            
            long sleepTime = sleepMillis + SLEEP_MARGIN_MILLIS - (t1 - timestamps.get(0));
            if (sleepTime > 0) {
                Thread.sleep(sleepTime);
            }
            String result = callable.call();
            
            timestamps.add(System.currentTimeMillis());
            timestamps.remove(0);
            
            return result;
        } catch (Exception e) {
            return handleException(e);
        } finally {
            lock.unlock();
        }
    }

    private String handleException(Exception e) throws IOException {
        if (e instanceof IOException) {
            throw (IOException) e;
        }
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        throw new RuntimeException("Error placing order", e);
    }

    @Override
    public String toString() {
        return "PlaceOrderLimiter{" +
                "limit=" + limit +
                ", sleepMillis=" + sleepMillis +
                ", maxSleepMillis=" + maxSleepMillis +
                '}';
    }

    public static PlaceOrderLimiter fromSpecificParams(Map<String, Object> params, int deftLimit, long defSleepMillis, long defMaxSleepMillis) {
        int limit = Optional.ofNullable(params.get(LIMIT_KEY)).map(o -> Integer.valueOf((String) o)).orElse(deftLimit);
        long sleepMillis = Optional.ofNullable(params.get(SLEEP_MILLIS_KEY)).map(o -> Long.valueOf((String) o)).orElse(defSleepMillis);
        long maxSleepMillis = Optional.ofNullable(params.get(MAX_SLEEP_MILLIS_KEY)).map(o -> Long.valueOf((String) o)).orElse(defMaxSleepMillis);
        
        return new PlaceOrderLimiter(limit, sleepMillis, maxSleepMillis);
    }
}
