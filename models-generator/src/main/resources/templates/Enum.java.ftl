package ${packageName};

/**
 * Generated Enum for ${enumName}.
 *
 * @author Yusuf F. Adiputera
 */
public enum ${enumName} {
<#list values as val>
    ${val}<#if val_has_next>,<#else>;</#if>
</#list>
}
