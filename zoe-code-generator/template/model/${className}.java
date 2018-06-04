<#include "/java_copyright.include">
<#assign className=table.className>
<#assign classNameLower=className?uncap_first>
        package ${basepackage}.model;

        import javax.persistence.*;
        import java.util.Date;
        import java.io.Serializable;



/**
 * @author ${author}
 * @version 1.0
 * @date ${now?string("yyyy-MM-dd")}
 */
@Table(name = "${table.sqlName}")
public class ${className}  implements Serializable{
<#list table.columns as field>
<#assign type=field.javaType/>
<#if field.javaType=="java.sql.Date">
<#assign type="Date"/>
</#if>
<#if field.javaType=="java.sql.Clob">
<#assign type="String"/>
</#if>
/**
 * ${field.remarks}
 */
@Column(name = "${field.sqlName}")
private ${type} ${field.entityName?uncap_first};

</#list>

<#list table.columns as field>
<#assign type=field.javaType/>
<#if field.javaType=="java.sql.Date">
   <#assign type="Date"/>
</#if>
<#if field.javaType=="java.sql.Clob">
<#assign type="String"/>
</#if>

public ${type} get${field.entityName?cap_first}(){
        return this.${field.entityName?uncap_first};
        }

public void set${field.entityName?cap_first}(${type} ${field.entityName?uncap_first}){
        this.${field.entityName?uncap_first}=${field.entityName?uncap_first};
        }

</#list>
        }
