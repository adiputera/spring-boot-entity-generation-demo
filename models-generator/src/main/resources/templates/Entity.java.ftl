package ${model.package!"id.adiputera.demo.cms.models"};

<#list globalImports as imp>
<#if imp != "lombok.Data" && imp != "lombok.EqualsAndHashCode">
import ${imp};
</#if>
</#list>
<#if model.imports??>
<#list model.imports as imp>
<#if imp != "lombok.Data" && imp != "lombok.EqualsAndHashCode">
import ${imp};
</#if>
</#list>
</#if>

/**
 * Generated Entity for ${modelName}.
 *
 * @author Yusuf F. Adiputera
 */
<#if model.annotations??>
<#list model.annotations as ann>
<#if ann != "@Data" && ann != "@EqualsAndHashCode(callSuper = true)">
${ann}
</#if>
</#list>
</#if>
<#if model.table??>
@Table(name = "${model.table}"<#if model.indexes??>, indexes = {
    <#list model.indexes as idx>
    @Index(name = "${idx.name}", columnList = "<#list idx.columns as col>${col}<#if col_has_next>, </#if></#list>"<#if idx.unique?? && idx.unique>, unique = true</#if>)<#if idx_has_next>,</#if>
    </#list>
}</#if>)
</#if>
<#if (!model.isAbstract?? || !model.isAbstract) && (!model.annotations?? || !model.annotations?seq_contains("@MappedSuperclass"))>
@Entity
</#if>
public <#if model.isAbstract?? && model.isAbstract>abstract </#if>class ${modelName} <#if model.extends??>extends ${model.extends} </#if>{

<#if model.attributes??>
<#list model.attributes as attrName, attrDef>
    <#if attrDef.isPrimaryKey?? && attrDef.isPrimaryKey>
    @Id
    <#if attrDef.generator?? && attrDef.generator == "UUID">
    @GeneratedValue(strategy = GenerationType.UUID)
    <#else>
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    </#if>
    </#if>
    <#if attrDef.annotations??>
    <#list attrDef.annotations as ann>
    ${ann}
    </#list>
    </#if>
    <#if attrDef.relation??>
    @${attrDef.relation.type}<#if attrDef.relation.mappedBy??>(mappedBy = "${attrDef.relation.mappedBy}"<#if attrDef.relation.cascade??>, cascade = CascadeType.${attrDef.relation.cascade}</#if>)<#else><#if attrDef.relation.cascade??>(cascade = CascadeType.${attrDef.relation.cascade})</#if></#if>
    <#if attrDef.relation.joinColumn??>
    @JoinColumn(name = "${attrDef.relation.joinColumn}")
    </#if>
    <#if attrDef.relation.joinTable??>
    @JoinTable(name = "${attrDef.relation.joinTable}",
        joinColumns = @JoinColumn(name = "${attrDef.relation.joinColumn}"),
        inverseJoinColumns = @JoinColumn(name = "${attrDef.relation.inverseJoinColumn}")
    )
    </#if>
    </#if>
    <#if attrDef.columnDefinition?? || attrDef.unique?? || attrDef.nullable??>
    @Column(<#assign comma=false><#if attrDef.columnDefinition??>columnDefinition = "${attrDef.columnDefinition}"<#assign comma=true></#if><#if attrDef.unique??><#if comma>, </#if>unique = ${attrDef.unique?c}<#assign comma=true></#if><#if attrDef.nullable??><#if comma>, </#if>nullable = ${attrDef.nullable?c}</#if>)
    </#if>
    private ${attrDef.type} ${attrName}<#if attrDef.defaultValue??> = ${attrDef.defaultValue}</#if>;

</#list>

    // --- GETTERS AND SETTERS ---
<#list model.attributes as attrName, attrDef>

    /**
     * Gets the ${attrName} field.
     *
     * @return The ${attrName} value.
     */
    public ${attrDef.type} get${attrName?cap_first}() {
        return this.${attrName};
    }

    /**
     * Sets the ${attrName} field.
     *
     * @param ${attrName} The ${attrName} to set.
     */
    public void set${attrName?cap_first}(${attrDef.type} ${attrName}) {
        this.${attrName} = ${attrName};
    }
</#list>
</#if>
}
