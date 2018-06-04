package cn.com.zoesoft.rhip.esb.observer;

import cn.com.zoesoft.rhip.esb.util.ServiceBusUtils;
import com.zoe.phip.ssp.model.synapse.SynapseFaultException;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.ParameterObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.ProxyService;

public class ProxyServiceParameterObserver
        implements ParameterObserver {
    private static final Log log = LogFactory.getLog(ProxyServiceParameterObserver.class);
    private static String[] skipParams = {"wso2statistics.service.response.time", "org.apache.axis2.context.externalize.AxisServiceName", "lastUsedTime"};
    private AxisService service;
    private ConfigurationContext configurationContext;

    public ProxyServiceParameterObserver(ConfigurationContext configurationContext, AxisService service) {
        this.configurationContext = configurationContext;
        this.service = service;
    }

    public void parameterChanged(String name, Object value) {
        SynapseConfiguration config = ((SynapseEnvironment) this.service.getAxisConfiguration().getParameter(SynapseConstants.SYNAPSE_ENV).getValue()).getSynapseConfiguration();

        ProxyService proxy = config.getProxyService(this.service.getName());
        /*
        if ("passwordCallbackRef".equals(name)) {
          proxy.setModuleEngaged(true);
        }
        */
        if (proxy != null) {
            if (this.service.getParameter(name) != null) {
                proxy.addParameter(name, value);
            } else {
                proxy.getParameterMap().remove(name);
            }
            if (!isSkipPersistenceForParam(name)) {
                try {
                    ServiceBusUtils.persistProxyService(this.configurationContext, proxy);
                } catch (SynapseFaultException e) {
                    log.error("Error While persisting proxy information", e);
                }
            }
        } else {
            log.error("Proxy Service " + name + " does not exist ");
        }
    }

    private boolean isSkipPersistenceForParam(String name) {
        for (String param : skipParams) {
            if (param.equals(name)) {
                return true;
            }
        }
        return false;
    }
}
