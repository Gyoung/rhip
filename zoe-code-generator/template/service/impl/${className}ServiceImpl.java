<#include "/java_copyright.include">
<#assign className=table.className>
<#assign classNameLower=className?uncap_first>
        package ${basepackage}.service.impl;
        import org.springframework.stereotype.Repository;
        import com.alibaba.dubbo.config.annotation.Service;
        import com.zoe.phip.module.service.impl.in.DubboGenericService;


/**
 * @author ${author}
 * @version 1.0
 * @date ${now?string("yyyy-MM-dd")}
 */
@Repository("${className?uncap_first}Service")
@Service(interfaceClass = I${className}Service.class, proxy = "sdpf",protocol = {"dubbo"},  dynamic = true)
public class ${className}ServiceImpl extends DubboGenericService{

        }