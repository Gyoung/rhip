package cn.com.zoesoft.rhip.esb.handler.support;

import cn.com.zoesoft.rhip.esb.handler.AbstractServiceControlHandler;
import cn.com.zoesoft.rhip.esb.handler.ServiceCacheStore;
import cn.com.zoesoft.rhip.esb.handler.ServiceControlHandler;
import cn.com.zoesoft.rhip.esb.util.ServiceBusUtils;
import com.zoe.phip.ssp.model.synapse.*;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.ProxyServiceFactory;
import org.apache.synapse.config.xml.SequenceMediatorSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.core.axis2.ProxyService;

import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Created by qiuyungen on 2016/5/25.
 */
public final class ProxyServiceControlHandler extends AbstractServiceControlHandler implements ServiceControlHandler {

    private final Log log = LogFactory.getLog(ProxyServiceControlHandler.class);

    void addProxyService(OMElement proxyServiceElement, String fileName, boolean updateMode)
            throws SynapseFaultException {
        try {
            if (proxyServiceElement.getQName().getLocalPart().equals(XMLConfigConstants.PROXY_ELT.getLocalPart())) {
                String proxyId = proxyServiceElement.getAttributeValue(new QName("id"));
                String proxyName = proxyServiceElement.getAttributeValue(new QName("name"));

                ServiceCacheStore.instance().addProxy(proxyId, proxyName);
                if ((getSynapseConfiguration().getProxyService(proxyName) != null) || (getSynapseConfiguration().getAxisConfiguration().getService(proxyName) != null)) {
                    //handleException(log, "A service named " + proxyName + " already exists", null);
                    delete(proxyId);
                }
                {
                    ProxyService proxy = ProxyServiceFactory.createProxy(proxyServiceElement, getSynapseConfiguration().getProperties());
                    if (updateMode) {
                        proxy.setFileName(fileName);
                    } else if (fileName != null) {
                        proxy.setFileName(fileName);
                    } else {
                        proxy.setFileName(ServiceBusUtils.generateFileName(proxy.getName()));
                    }
                    try {
                        getSynapseConfiguration().addProxyService(proxy.getName(), proxy);

                        proxy.buildAxisService(getSynapseConfiguration(), getAxisConfiguration());
                        //addParameterObserver(proxy.getName());
                        if (log.isDebugEnabled()) {
                            log.debug("Added proxy service : " + proxyName);
                        }
                        if (!proxy.isStartOnLoad()) {
                            proxy.stop(getSynapseConfiguration());
                        }
                        if (proxy.getTargetInLineInSequence() != null) {
                            proxy.getTargetInLineInSequence().init(getSynapseEnvironment());
                        }
                        if (proxy.getTargetInLineOutSequence() != null) {
                            proxy.getTargetInLineOutSequence().init(getSynapseEnvironment());
                        }
                        if (proxy.getTargetInLineFaultSequence() != null) {
                            proxy.getTargetInLineFaultSequence().init(getSynapseEnvironment());
                        }
                        if (proxy.getTargetInLineEndpoint() != null) {
                            proxy.getTargetInLineEndpoint().init(getSynapseEnvironment());
                        }
                        persistProxyService(proxy);
                    } catch (Exception e) {
                        getSynapseConfiguration().removeProxyService(proxyName);
                        try {
                            if (getAxisConfiguration().getService(proxy.getName()) != null) {
                                getAxisConfiguration().removeService(proxy.getName());
                            }
                        } catch (Exception ignore) {
                        }
                        handleException(log, "Error trying to add the proxy service to the ESB configuration : " + proxy.getName(), e);
                    }
                }
            } else {
                handleException(log, "Invalid proxy service definition", null);
            }
        } catch (AxisFault af) {
            handleException(log, "Invalid proxy service definition", af);
        }
    }

    void persistProxyService(ProxyService proxy)
            throws SynapseFaultException {
        ServiceBusUtils.persistProxyService(getContext(), proxy);
    }

    @Override
    public String getSource(String id) throws SynapseFaultException {
        IServiceEntity entity = getService(id);
        if (entity != null)
            return entity.retrieveOM().toString();
        return null;
    }

    @Override
    public IServiceEntity getService(String id) throws SynapseFaultException {
        Lock lock = getLock();
        try {
            lock.lock();
            ProxyService ps = proxyForName(id);
            return generateProxyDataFor(ps);
        } finally {
            lock.unlock();
        }
    }

    ProxyService proxyForName(String proxyId)
            throws SynapseFaultException {
        final String proxyName = ServiceCacheStore.instance().getProxyName(proxyId);
        try {
            ProxyService ps = getSynapseConfiguration().getProxyService(proxyName);
            if (ps != null) {
                return ps;
            }
            handleException(log, "A proxy service named : " + proxyName + " does not exist", null);
        } catch (Exception af) {
            handleException(log, "Unable to get the proxy service definition for : " + proxyName, af);
        }
        return null;
    }

    private ProxyServiceEntity generateProxyDataFor(ProxyService proxy)
            throws SynapseFaultException {
        ProxyServiceEntity entity = new ProxyServiceEntity();
        entity.setBasis(new ServiceEntity());
        entity.getBasis().setName(proxy.getName());

        entity.setRunning(proxy.isRunning());
        if ((proxy.getAspectConfiguration() != null) && (proxy.getAspectConfiguration().isStatisticsEnable())) {
            entity.setEnableStatistics(true);
        } else {
            entity.setEnableStatistics(false);
        }
        if (proxy.getTraceState() == 1) {
            entity.setEnableTracing(true);
        } else if (proxy.getTraceState() == 0) {
            entity.setEnableTracing(false);
        }
        if (proxy.isStartOnLoad()) {
            entity.setStartOnLoad(true);
        } else {
            entity.setStartOnLoad(false);
        }
        List list;
        if (((list = proxy.getTransports()) != null) && (!list.isEmpty())) {
            String[] arr = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = ((String) list.get(i));
            }
            entity.setTransports(arr);
        }
        if (proxy.getServiceGroup() != null) {
            entity.setServiceGroup(proxy.getServiceGroup());
        }
        if (proxy.getDescription() != null) {
            entity.setDescription(proxy.getDescription());
        }

        LinkedList<ServiceSequence> sequenceList = new LinkedList<>();
        SequenceMediatorSerializer seqMedSerializer = new SequenceMediatorSerializer();
        if (proxy.getTargetInSequence() != null) {
            entity.setInSequence(proxy.getTargetInSequence());
        } else if (proxy.getTargetInLineInSequence() != null) {
            OMElement inSeq = seqMedSerializer.serializeAnonymousSequence(null, proxy.getTargetInLineInSequence());
            inSeq.setLocalName("inSequence");
            ServiceSequence in = new ServiceSequence();
            in.buildOM(inSeq);
            entity.setInSequence(in.getId());
            sequenceList.add(in);
        }
        if (proxy.getTargetOutSequence() != null) {
            entity.setOutSequence(proxy.getTargetOutSequence());
        } else if (proxy.getTargetInLineOutSequence() != null) {
            OMElement outSeq = seqMedSerializer.serializeAnonymousSequence(null, proxy.getTargetInLineOutSequence());
            outSeq.setLocalName("outSequence");

            ServiceSequence out = new ServiceSequence();
            out.buildOM(outSeq);
            entity.setOutSequence(out.getId());
            sequenceList.add(out);
        }
        if (proxy.getTargetFaultSequence() != null) {
            entity.setFaultSequence(proxy.getTargetFaultSequence());
        } else if (proxy.getTargetInLineFaultSequence() != null) {
            OMElement faultSeq = seqMedSerializer.serializeAnonymousSequence(null, proxy.getTargetInLineFaultSequence());
            faultSeq.setLocalName("faultSequence");

            ServiceSequence fault = new ServiceSequence();
            fault.buildOM(faultSeq);
            entity.setFaultSequence(fault.getId());
            sequenceList.add(fault);
        }
        if (sequenceList.size() != 0) {
            ServiceSequence[] sequences = new ServiceSequence[sequenceList.size()];
            entity.getBasis().setSequences(sequenceList.toArray(sequences));
        }
        sequenceList.clear();
        sequenceList = null;

        if (proxy.getTargetInLineEndpoint() != null) {
            entity.setWsdlUri(proxy.getTargetInLineEndpoint().toString());
        }
        if (proxy.getWsdlURI() != null) {
            entity.setPublishWSDL(proxy.getWsdlURI().toString());
        }
        Map<String, Object> map;
        if (((map = proxy.getParameterMap()) != null) && (!map.isEmpty())) {
            Entry[] entries = new Entry[map.size()];
            int i = 0;
            for (Map.Entry<String, Object> key : map.entrySet()) {
                Object o = key.getValue();
                if ((o instanceof String)) {
                    entries[i] = new Entry((String) key.getKey(), (String) o);
                    i++;
                } else if ((o instanceof OMElement)) {
                    entries[i] = new Entry((String) key.getKey(), o.toString());
                    i++;
                }
            }
            entity.setServiceParams(entries);
        }
        return entity;
    }

    @Override
    public boolean add(IServiceEntity entity) throws SynapseFaultException {
        Lock lock = getLock();
        try {
            lock.lock();
            addProxyService(entity.retrieveOM(), null, false);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean start(String proxyId) throws SynapseFaultException {
        final String proxyName = ServiceCacheStore.instance().getProxyName(proxyId);

        log.debug("Starting/Re-starting proxy service : " + proxyName);
        Lock lock = getLock();
        try {
            lock.lock();

            ProxyService proxy = getSynapseConfiguration().getProxyService(proxyName);
            List pinnedServers = proxy.getPinnedServers();
            if ((pinnedServers.isEmpty()) || (pinnedServers.contains(getServerConfigurationInformation().getServerName()))) {
                proxy.start(getSynapseConfiguration());
            }
            if (log.isDebugEnabled()) {
                log.debug("Started/Re-started proxy service : " + proxyName);
            }
            return true;
        } catch (Exception af) {
            handleException(log, "Unable to start/re-start proxy service: " + proxyName, af);
        } finally {
            lock.unlock();
        }
        return false;
    }

    @Override
    public boolean stop(String proxyId) throws SynapseFaultException {
        final String proxyName = ServiceCacheStore.instance().getProxyName(proxyId);

        log.debug("Stopping proxy service : " + proxyName);
        Lock lock = getLock();
        try {
            lock.lock();

            ProxyService proxy = getSynapseConfiguration().getProxyService(proxyName);
            List pinnedServers = proxy.getPinnedServers();
            if ((pinnedServers.isEmpty()) || (pinnedServers.contains(getServerConfigurationInformation().getSynapseXMLLocation()))) {
                proxy.stop(getSynapseConfiguration());
            }
            if (log.isDebugEnabled()) {
                log.debug("Stopped proxy service : " + proxyName);
            }
            return true;
        } catch (Exception af) {
            handleException(log, "Unable to stop proxy service : " + proxyName, af);
        } finally {
            lock.unlock();
        }
        return false;
    }

    @Override
    public boolean delete(String proxyId) throws SynapseFaultException {
        final String proxyName = ServiceCacheStore.instance().getProxyName(proxyId);

        Lock lock = getLock();
        try {
            lock.lock();
            if (log.isDebugEnabled()) {
                log.debug("Deleting proxy service : " + proxyName);
            }
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            ProxyService proxy = synapseConfiguration.getProxyService(proxyName);

            if (proxy != null) {
                synapseConfiguration.removeProxyService(proxyName);
                ServiceBusUtils.deleteProxyService(getServerConfigurationInformation(), proxy);
                if (log.isDebugEnabled()) {
                    log.debug("Proxy service : " + proxyName + " deleted");
                }
                return true;
            }
            log.warn("No proxy service exists by the name : " + proxyName);
            return true;
        } catch (Exception e) {
            handleException(log, "Unable to delete proxy service : " + proxyName, e);
        } finally {
            lock.unlock();

            ServiceCacheStore.instance().removeProxy(proxyId);
        }
        return false;
    }
}
