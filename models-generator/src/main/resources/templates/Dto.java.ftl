package ${packageName};

<#list imports as imp>
<#if imp != "lombok.Data" && imp != "lombok.EqualsAndHashCode">
import ${imp};
</#if>
</#list>

<#if annotations?has_content>
<#list annotations as ann>
<#if ann != "@Data" && ann != "@EqualsAndHashCode(callSuper = true)">
${ann}
</#if>
</#list>
</#if>
public class ${className}<#if extendsClass??> extends ${extendsClass}</#if> {

<#list attributes as name, attr>
    <#if attr.annotations?has_content>
    <#list attr.annotations as ann>
    ${ann}
    </#list>
    </#if>
    private ${attr.type} ${name};
</#list>

<#list attributes as name, attr>
    public ${attr.type} get${name?cap_first}() {
        return this.${name};
    }

    public void set${name?cap_first}(${attr.type} ${name}) {
        this.${name} = ${name};
    }
</#list>
}
