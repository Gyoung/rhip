package cn.com.zoesoft.rhip.esb.handler;

import com.zoe.phip.ssp.model.synapse.IServiceEntity;
import com.zoe.phip.ssp.model.synapse.SynapseFaultException;

/**
 * Created by qiuyungen on 2016/5/25.
 */
public interface ServiceControlHandler {

    String getSource(String id) throws SynapseFaultException;

    IServiceEntity getService(String id) throws SynapseFaultException;

    boolean add(IServiceEntity entity) throws SynapseFaultException;

    boolean start(String id) throws SynapseFaultException;

    boolean stop(String id) throws SynapseFaultException;

    boolean delete(String id) throws SynapseFaultException;
}
