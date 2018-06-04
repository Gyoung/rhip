package cn.com.zoesoft.rhip.esb.handler.support;

import cn.com.zoesoft.rhip.esb.handler.AbstractServiceControlHandler;
import cn.com.zoesoft.rhip.esb.handler.ServiceCacheStore;
import cn.com.zoesoft.rhip.esb.handler.ServiceControlHandler;
import cn.com.zoesoft.rhip.esb.util.ServiceBusUtils;
import com.zoe.phip.ssp.model.synapse.*;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.SequenceMediatorSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.rest.APIFactory;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.Resource;

import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;

/**
 * Created by qiuyungen on 2016/5/25.
 */
public final class RestfulServiceControlHandler extends AbstractServiceControlHandler implements ServiceControlHandler {

    private final Log log = LogFactory.getLog(RestfulServiceControlHandler.class);

    void addRestfulService(OMElement serviceElement, String fileName, boolean updateMode)
            throws SynapseFaultException {

        if (serviceElement.getQName().getLocalPart().equals(XMLConfigConstants.API_ELT.getLocalPart())) {
            String apiId = serviceElement.getAttributeValue(new QName("id"));
            String apiName = serviceElement.getAttributeValue(new QName("name"));

            ServiceCacheStore.instance().addRestful(apiId, apiName);
            if ((getSynapseConfiguration().getAPI(apiName) != null)) {
                //handleException(log, "A service named " + apiName + " already exists", null);
                delete(apiId);
            }
            {
                API api = APIFactory.createAPI(serviceElement);
                if (updateMode) {
                    api.setFileName(fileName);
                } else if (fileName != null) {
                    api.setFileName(fileName);
                } else {
                    api.setFileName(ServiceBusUtils.generateFileName(api.getName()));
                }
                try {
                    getSynapseConfiguration().addAPI(api.getName(), api);

                    api.init(this.getSynapseEnvironment());

                    persistRestfulService(api);
                } catch (Exception e) {
                    try {
                        delete(apiName);
                    } catch (Exception ignore) {
                    }
                    handleException(log, "Error trying to add the proxy service to the ESB configuration : " + api.getName(), e);
                }
            }
        } else {
            handleException(log, "Invalid proxy service definition", null);
        }
    }

    void persistRestfulService(API api)
            throws SynapseFaultException {
        ServiceBusUtils.persistRestfulService(getContext(), api);
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
            API ps = proxyForName(id);
            return generateRestfulDataFor(ps);
        } finally {
            lock.unlock();
        }
    }

    API proxyForName(String apiId)
            throws SynapseFaultException {
        final String name = ServiceCacheStore.instance().getRestful(apiId);
        try {
            API ps = getSynapseConfiguration().getAPI(name);
            if (ps != null) {
                return ps;
            }
            handleException(log, "A restful service named : " + name + " does not exist", null);
        } catch (Exception af) {
            handleException(log, "Unable to get the restful service definition for : " + name, af);
        }
        return null;
    }

    private RestfulServiceEntity generateRestfulDataFor(API api)
            throws SynapseFaultException {
        RestfulServiceEntity entity = new RestfulServiceEntity();
        entity.setBasis(new ServiceEntity());
        entity.getBasis().setName(api.getName());
        entity.setContext(api.getContext());

        Resource[] resources = api.getResources();
        if (resources != null && resources.length > 0) {

            RestfulResourceEntity[] restfulResources = new RestfulResourceEntity[resources.length];

            LinkedList<ServiceSequence> sequenceList = new LinkedList<>();
            for (int i = 0; i < resources.length; i++) {
                restfulResources[i] = new RestfulResourceEntity();

                Resource resource = resources[i];

                SequenceMediatorSerializer seqMedSerializer = new SequenceMediatorSerializer();
                if (resource.getInSequenceKey() != null) {
                    restfulResources[i].setInSequence(resource.getInSequenceKey());
                } else if (resource.getInSequence() != null) {
                    OMElement inSeq = seqMedSerializer.serializeAnonymousSequence(null, resource.getInSequence());
                    inSeq.setLocalName("inSequence");
                    ServiceSequence in = new ServiceSequence();
                    in.buildOM(inSeq);
                    restfulResources[i].setInSequence(in.getId());
                    sequenceList.add(in);
                }
                if (resource.getOutSequenceKey() != null) {
                    restfulResources[i].setOutSequence(resource.getOutSequenceKey());
                } else if (resource.getOutSequence() != null) {
                    OMElement outSeq = seqMedSerializer.serializeAnonymousSequence(null, resource.getOutSequence());
                    outSeq.setLocalName("outSequence");

                    ServiceSequence out = new ServiceSequence();
                    out.buildOM(outSeq);
                    restfulResources[i].setOutSequence(out.getId());
                    sequenceList.add(out);
                }
                if (resource.getFaultSequenceKey() != null) {
                    restfulResources[i].setFaultSequence(resource.getFaultSequenceKey());
                } else if (resource.getFaultSequence() != null) {
                    OMElement faultSeq = seqMedSerializer.serializeAnonymousSequence(null, resource.getFaultSequence());
                    faultSeq.setLocalName("faultSequence");

                    ServiceSequence fault = new ServiceSequence();
                    fault.buildOM(faultSeq);
                    restfulResources[i].setFaultSequence(fault.getId());
                    sequenceList.add(fault);
                }
            }
            entity.setResources(restfulResources);
            if (sequenceList.size() != 0) {
                ServiceSequence[] sequences = new ServiceSequence[sequenceList.size()];
                entity.getBasis().setSequences(sequenceList.toArray(sequences));
            }
            sequenceList.clear();
            sequenceList = null;
        }

        return entity;
    }

    @Override
    public boolean add(IServiceEntity entity) throws SynapseFaultException {
        Lock lock = getLock();
        try {
            lock.lock();

            OMElement element = entity.retrieveOM();

            // check resource element;
            OMElement resource = element.getFirstChildWithName(new QName(ServiceEntity.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "resource"));
            if (resource == null)
                return false;

            addRestfulService(element, null, false);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean start(String id) throws SynapseFaultException {
        return true;
    }

    @Override
    public boolean stop(String id) throws SynapseFaultException {
        return true;
    }

    @Override
    public boolean delete(String apiId) throws SynapseFaultException {
        final String name = ServiceCacheStore.instance().getRestful(apiId);
        Lock lock = getLock();
        try {
            lock.lock();
            if (log.isDebugEnabled()) {
                log.debug("Deleting restful service : " + name);
            }
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            API api = synapseConfiguration.getAPI(name);

            if (api != null) {
                synapseConfiguration.removeAPI(name);
                ServiceBusUtils.deleteRestfulService(getServerConfigurationInformation(), api);
                if (log.isDebugEnabled()) {
                    log.debug("restful service : " + name + " deleted");
                }
                return true;
            }
            log.warn("No restful service exists by the name : " + name);
            return true;
        } catch (Exception e) {
            handleException(log, "Unable to delete restful service : " + name, e);
        } finally {
            lock.unlock();

            ServiceCacheStore.instance().removeRestful(apiId);
        }
        return false;
    }
}
