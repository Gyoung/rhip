<#include "/java_copyright.include">
<#assign className=table.className>
<#assign classNameLower=className?uncap_first>

        package ${basepackage}.dao;


/**
 * @author ${author}
 * @version 1.0
 * @date ${now?string("yyyy-MM-dd")}
 */
public interface I${className}Dao {

     /**
      * 新增
      * @param ${className?uncap_first}
      * @return
      */
     int add(${className} ${className?uncap_first});
     /**
      * 更新
      * @param ${className?uncap_first}
      * @return
      */
     int update(${className} ${className?uncap_first});
 }