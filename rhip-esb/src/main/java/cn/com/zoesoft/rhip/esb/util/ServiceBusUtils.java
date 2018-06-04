package cn.com.zoesoft.rhip.esb.util;

import com.zoe.phip.ssp.model.synapse.SynapseFaultException;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ServerConfigurationInformation;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.deployers.APIDeployer;
import org.apache.synapse.deployers.ProxyServiceDeployer;
import org.apache.synapse.rest.API;

import java.io.File;

/**
 * Created by qiuyungen on 2016/5/23.
 */
public final class ServiceBusUtils {
    private static final Log log = LogFactory.getLog(ServiceBusUtils.class);

    public static String generateFileName(String name) {
        return name.replaceAll("[\\/?*|:<> ]", "_") + ".xml";
    }

    public static void persistProxyService(ConfigurationContext configurationContext, ProxyService proxy)
            throws SynapseFaultException {
        ProxyServiceDeployer deployer = new ProxyServiceDeployer();
        deployer.init(configurationContext);
        deployer.restoreSynapseArtifact(proxy.getName());
        deployer = null;
    }

    public static void persistRestfulService(ConfigurationContext configurationContext, API api)
            throws SynapseFaultException {
        APIDeployer deployer = new APIDeployer();
        deployer.init(configurationContext);
        deployer.restoreSynapseArtifact(api.getName());
        deployer = null;
    }

    public static void deleteProxyService(ServerConfigurationInformation configurationInformation, ProxyService proxy) {
        String fileName = configurationInformation.getSynapseXMLLocation() + File.separator + MultiXMLConfigurationBuilder.PROXY_SERVICES_DIR + File.separator + proxy.getFileName();
        File file = new File(fileName);
        if (file.exists())
            file.delete();
    }

    public static void deleteRestfulService(ServerConfigurationInformation configurationInformation, API api) {
        String fileName = configurationInformation.getSynapseXMLLocation() + File.separator + MultiXMLConfigurationBuilder.REST_API_DIR + File.separator + api.getFileName();
        File file = new File(fileName);
        if (file.exists())
            file.delete();
    }
}
