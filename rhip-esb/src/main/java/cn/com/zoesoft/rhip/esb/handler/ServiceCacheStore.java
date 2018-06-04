package cn.com.zoesoft.rhip.esb.handler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qiuyungen on 2016/8/18.
 */
public final class ServiceCacheStore {

    private final Map<String, String> proxyMap = new HashMap<>();
    private final Map<String, String> restfulMap = new HashMap<>();
    private final Map<String, String> complexMap = new HashMap<>();


    private ServiceCacheStore() {
    }

    public static ServiceCacheStore instance() {
        return Holder.instance();
    }

    public String getProxyName(String id) {
        return proxyMap.getOrDefault(id, "");
    }

    public void addProxy(String id, String name) {
        proxyMap.put(id, name);
    }

    public void removeProxy(String id) {
        proxyMap.remove(id);
    }

    public String getRestful(String id) {
        return restfulMap.getOrDefault(id, "");
    }

    public void addRestful(String id, String name) {
        restfulMap.put(id, name);
    }

    public void removeRestful(String id) {
        restfulMap.remove(id);
    }

    public String getComplex(String id) {
        return complexMap.getOrDefault(id, "");
    }

    public void addComplex(String id, String name) {
        complexMap.put(id, name);
    }

    public void removeComplex(String id) {
        complexMap.remove(id);
    }

    private static class Holder {
        private static final ServiceCacheStore SERVICE_CACHE_STORE = new ServiceCacheStore();

        public static ServiceCacheStore instance() {
            return SERVICE_CACHE_STORE;
        }
    }
}
