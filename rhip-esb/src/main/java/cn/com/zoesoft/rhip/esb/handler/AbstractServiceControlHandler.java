package cn.com.zoesoft.rhip.esb.handler;

import com.zoe.phip.ssp.model.synapse.SynapseFaultException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ServerConfigurationInformation;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by qiuyungen on 2016/5/23.
 */
public abstract class AbstractServiceControlHandler {

    private static final String SYNAPSE_CONFIG_LOCK = "synapse.config.lock";

    private final Log log = LogFactory.getLog(AbstractServiceControlHandler.class);

    private ConfigurationContext context;

    protected ConfigurationContext getContext() {

        if (this.context != null)
            return this.context;

        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        if (messageContext != null) {
            this.context = messageContext.getConfigurationContext();
        }
        return this.context;
    }

    protected AxisConfiguration getAxisConfiguration() {
        return getContext().getAxisConfiguration();
    }

    protected SynapseConfiguration getSynapseConfiguration() {
        return (SynapseConfiguration) getAxisConfiguration().getParameter(SynapseConstants.SYNAPSE_CONFIG).getValue();
    }

    protected SynapseEnvironment getSynapseEnvironment() {
        return (SynapseEnvironment) getAxisConfiguration().getParameter(SynapseConstants.SYNAPSE_ENV).getValue();
    }

    protected ServerConfigurationInformation getServerConfigurationInformation(){
        return (ServerConfigurationInformation) getAxisConfiguration().getParameter(SynapseConstants.SYNAPSE_SERVER_CONFIG_INFO).getValue();
    }

    protected Lock getLock() {
        Parameter p = getAxisConfiguration().getParameter(SYNAPSE_CONFIG_LOCK);
        if (p != null) {
            return (Lock) p.getValue();
        }
        log.warn("synapse.config.lock is null, Recreating a new lock");
        Lock lock = new ReentrantLock();
        try {
            getAxisConfiguration().addParameter(SYNAPSE_CONFIG_LOCK, lock);
            return lock;
        } catch (AxisFault axisFault) {
            log.error("Error while setting synapse.config.lock");
        }
        return null;
    }

    protected void handleException(Log log, String message, Exception e)
            throws SynapseFaultException {
        if (e == null) {
            SynapseFaultException paf = new SynapseFaultException(message);
            log.error(message, paf);
            throw paf;
        }
        message = message + " :: " + e.getMessage();
        log.error(message, e);
        throw new SynapseFaultException(message, e);
    }
}