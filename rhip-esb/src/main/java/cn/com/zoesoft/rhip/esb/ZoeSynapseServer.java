package cn.com.zoesoft.rhip.esb;

import cn.com.zoesoft.rhip.esb.services.SspControlService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.*;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

/**
 * Created by qiuyungen on 2016/5/23.
 */
public final class ZoeSynapseServer {

    private static final String REMOTE_SERVICE_URL_FORMAT = "http://%s/com.zoe.phip.ssp.service.service.webservice.ISspControlService";

    private static final Log log = LogFactory.getLog(SynapseServer.class);
    private static final String USAGE_TXT = "Usage: SynapseServer <axis2_repository> <axis2_xml> <synapse_home> <synapse_xml> <resolve_root> <deployment mode>\n Opts: -? this message";
    private static ServerManager serverManager;

    public ZoeSynapseServer() {
    }

    public static void printUsage() {
        System.out.println(USAGE_TXT);
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {
        if (args.length <= 0 || args.length == 2 || args.length == 3 || args.length >= 8) {
            printUsage();
        }

        SspControlService.build(String.format(REMOTE_SERVICE_URL_FORMAT, args[args.length - 1]));

        // remove args last index
        args = Arrays.copyOf(args, args.length - 1);

        log.info("Starting Apache Synapse...");

        ServerConfigurationInformation configurationInformation = ServerConfigurationInformationFactory.createServerConfigurationInformation(args);
        configurationInformation.setServerControllerProvider(ZoeAxis2SynapseController.class.getName());

        cleanLocalService(configurationInformation);

        serverManager = new ServerManager();
        serverManager.init(configurationInformation, null);

        try {
            serverManager.start();

            addShutdownHook();
            log.info("Apache Synapse started successfully");

            // load service
            loadRemoteService(configurationInformation);

            (new CountDownLatch(1)).await();
        } catch (SynapseException var3) {
            log.error("Error starting Apache Synapse, trying a clean shutdown...", var3);
            serverManager.shutdown();
        }
    }

    private static void addShutdownHook() {
        Thread shutdownHook = new Thread() {
            public void run() {
                ZoeSynapseServer.log.info("Shutting down Apache Synapse...");

                try {
                    ZoeSynapseServer.serverManager.shutdown();
                    ZoeSynapseServer.log.info("Apache Synapse shutdown complete");
                    ZoeSynapseServer.log.info("Halting JVM");
                } catch (Exception var2) {
                    ZoeSynapseServer.log.error("Error occurred while shutting down Apache Synapse, it may not be a clean shutdown", var2);
                }

            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private static void removeAllFiles(File folder) {
        if (!folder.exists())
            return;

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".xml"));
        if (files != null && files.length > 0) {
            for (File file : files) {

                if (file.getName().toLowerCase().endsWith(".local.xml"))
                    continue;

                file.delete();
            }
        }
    }

    private static void cleanLocalService(ServerConfigurationInformation configuration) {
        // clean local proxy-service、api
        final File apiFolder = new File(configuration.getSynapseXMLLocation() + "/api");
        removeAllFiles(apiFolder);
        final File proxyServiceFolder = new File(configuration.getSynapseXMLLocation() + "/proxy-services");
        removeAllFiles(proxyServiceFolder);
    }

    private static void loadRemoteService(ServerConfigurationInformation configuration) {
        Thread thread = new Thread(() -> {
            try {
                // apply ssp about synapse setting
                SspControlService.instance().applySynapseSetting();

                // load remote services
                SspControlService.instance().publishService();
            } catch (Exception axisFault) {
                log.error(axisFault);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}