package ${packageName};

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ${modelPackage}.${modelClassName};
import ${dtoPackage}.${dtoClassName};

import org.mapstruct.ReportingPolicy;
<#if decorator??>
import org.mapstruct.DecoratedWith;
</#if>
<#if mappings??>
import org.mapstruct.Mapping;
</#if>
/**
 * Auto-generated MapStruct Mapper for ${modelClassName} -> ${dtoClassName}.
 * 
 * @author Yusuf F. Adiputera
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE<#if uses??>, uses = { <#list uses as use>${use}.class<#if use_has_next>, </#if></#list> }</#if>)
<#if decorator??>
@DecoratedWith(${decorator}.class)
</#if>
public interface ${mapperName} {
    ${mapperName} INSTANCE = Mappers.getMapper(${mapperName}.class);

    <#if mappings??>
    <#list mappings as m>
    @Mapping(target = "${m.target}"<#if m.source??>, source = "${m.source}"</#if><#if m.expression??>, expression = "${m.expression?j_string}"</#if><#if m.ignore??>, ignore = ${m.ignore?c}</#if>)
    </#list>
    </#if>
    ${dtoClassName} toDto(${modelClassName} entity);
}
