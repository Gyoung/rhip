package cn.com.zoesoft.rhip.esb;


import cn.com.zoesoft.rhip.esb.services.SynapseMonitorImpl;
import cn.com.zoesoft.rhip.esb.services.SynapseServiceControllerImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.ServiceBuilder;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.Axis2SynapseController;
import org.apache.synapse.ServerConfigurationInformation;
import org.apache.synapse.ServerContextInformation;

import java.io.InputStream;

/**
 * Created by qiuyungen on 2016/5/23.
 */
public final class ZoeAxis2SynapseController extends Axis2SynapseController {

    private static final String SERVICE_GROUP_NAME = "ZoeAxis2SynapseController.ServiceGroupName";

    private static Class[] SERVICE_CLASSES = new Class[]{
            SynapseServiceControllerImpl.class,
            SynapseMonitorImpl.class
    };

    private static void addService(ConfigurationContext context, AxisServiceGroup group, Class clazz) {
        AxisService service = new AxisService();
        try {
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("services/" + clazz.getSimpleName() + ".xml");
            ServiceBuilder builder = new ServiceBuilder(stream, context, service);
            builder.populateService(builder.buildOM());

            group.addService(service);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(ServerConfigurationInformation serverConfigurationInformation, ServerContextInformation serverContextInformation) {
        super.init(serverConfigurationInformation, serverContextInformation);

        ConfigurationContext context = (ConfigurationContext) getContext();

        AxisConfiguration configuration = context.getAxisConfiguration();
        AxisServiceGroup serviceGroup = new AxisServiceGroup(configuration);
        serviceGroup.setServiceGroupName(SERVICE_GROUP_NAME);

        addServiceGroupParameter(serviceGroup);

        for (Class clazz : SERVICE_CLASSES) {
            addService(context, serviceGroup, clazz);
        }
        try {
            configuration.addServiceGroup(serviceGroup);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

    private void addServiceGroupParameter(AxisServiceGroup group) {
        try {
            group.addParameter(new Parameter("hiddenService", "true"));
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }
}