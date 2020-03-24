/*
 * GA4GH Beacon API Specification
 * A Beacon is a web service for genetic data sharing that can be queried for  information about variants, individuals or samples.
 *
 * The version of the OpenAPI document: 2.0
 * Contact: beacon@ga4gh.org
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.opencb.opencga.server.rest.ga4gh.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.Objects;

/**
 * Blabla 
 */
@ApiModel(description = "Blabla ")
@JsonPropertyOrder({
  Ga4ghVariantDetails.JSON_PROPERTY_CHROMOSOME,
  Ga4ghVariantDetails.JSON_PROPERTY_REFERENCE_BASES,
  Ga4ghVariantDetails.JSON_PROPERTY_ALTERNATE_BASES,
  Ga4ghVariantDetails.JSON_PROPERTY_VARIANT_TYPE,
  Ga4ghVariantDetails.JSON_PROPERTY_START,
  Ga4ghVariantDetails.JSON_PROPERTY_END,
  Ga4ghVariantDetails.JSON_PROPERTY_ASSEMBLY_ID
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2020-03-24T15:12:46.170Z[Europe/London]")
public class Ga4ghVariantDetails   {
  public static final String JSON_PROPERTY_CHROMOSOME = "chromosome";
  @JsonProperty(JSON_PROPERTY_CHROMOSOME)
  private Ga4ghChromosome2 chromosome;

  public static final String JSON_PROPERTY_REFERENCE_BASES = "referenceBases";
  @JsonProperty(JSON_PROPERTY_REFERENCE_BASES)
  private String referenceBases;

  public static final String JSON_PROPERTY_ALTERNATE_BASES = "alternateBases";
  @JsonProperty(JSON_PROPERTY_ALTERNATE_BASES)
  private String alternateBases;

  public static final String JSON_PROPERTY_VARIANT_TYPE = "variantType";
  @JsonProperty(JSON_PROPERTY_VARIANT_TYPE)
  private String variantType;

  public static final String JSON_PROPERTY_START = "start";
  @JsonProperty(JSON_PROPERTY_START)
  private Long start;

  public static final String JSON_PROPERTY_END = "end";
  @JsonProperty(JSON_PROPERTY_END)
  private Long end;

  public static final String JSON_PROPERTY_ASSEMBLY_ID = "assemblyId";
  @JsonProperty(JSON_PROPERTY_ASSEMBLY_ID)
  private String assemblyId;

  public Ga4ghVariantDetails chromosome(Ga4ghChromosome2 chromosome) {
    this.chromosome = chromosome;
    return this;
  }

  /**
   * Get chromosome
   * @return chromosome
   **/
  @JsonProperty("chromosome")
  @ApiModelProperty(value = "")
  @Valid 
  public Ga4ghChromosome2 getChromosome() {
    return chromosome;
  }

  public void setChromosome(Ga4ghChromosome2 chromosome) {
    this.chromosome = chromosome;
  }

  public Ga4ghVariantDetails referenceBases(String referenceBases) {
    this.referenceBases = referenceBases;
    return this;
  }

  /**
   * Reference bases for this variant (starting from &#x60;start&#x60;). 
   * @return referenceBases
   **/
  @JsonProperty("referenceBases")
  @ApiModelProperty(value = "Reference bases for this variant (starting from `start`). ")
   @Pattern(regexp="^([ACGTN]+)$")
  public String getReferenceBases() {
    return referenceBases;
  }

  public void setReferenceBases(String referenceBases) {
    this.referenceBases = referenceBases;
  }

  public Ga4ghVariantDetails alternateBases(String alternateBases) {
    this.alternateBases = alternateBases;
    return this;
  }

  /**
   * The bases that appear instead of the reference bases. 
   * @return alternateBases
   **/
  @JsonProperty("alternateBases")
  @ApiModelProperty(value = "The bases that appear instead of the reference bases. ")
   @Pattern(regexp="^([ACGTN]+)$")
  public String getAlternateBases() {
    return alternateBases;
  }

  public void setAlternateBases(String alternateBases) {
    this.alternateBases = alternateBases;
  }

  public Ga4ghVariantDetails variantType(String variantType) {
    this.variantType = variantType;
    return this;
  }

  /**
   * The &#x60;variantType&#x60; is used to denote e.g. structural variants. 
   * @return variantType
   **/
  @JsonProperty("variantType")
  @ApiModelProperty(value = "The `variantType` is used to denote e.g. structural variants. ")
  
  public String getVariantType() {
    return variantType;
  }

  public void setVariantType(String variantType) {
    this.variantType = variantType;
  }

  public Ga4ghVariantDetails start(Long start) {
    this.start = start;
    return this;
  }

  /**
   * Blabla 
   * minimum: 0
   * @return start
   **/
  @JsonProperty("start")
  @ApiModelProperty(value = "Blabla ")
   @Min(0L)
  public Long getStart() {
    return start;
  }

  public void setStart(Long start) {
    this.start = start;
  }

  public Ga4ghVariantDetails end(Long end) {
    this.end = end;
    return this;
  }

  /**
   * Blabla 
   * minimum: 0
   * @return end
   **/
  @JsonProperty("end")
  @ApiModelProperty(value = "Blabla ")
   @Min(0L)
  public Long getEnd() {
    return end;
  }

  public void setEnd(Long end) {
    this.end = end;
  }

  public Ga4ghVariantDetails assemblyId(String assemblyId) {
    this.assemblyId = assemblyId;
    return this;
  }

  /**
   * Assembly identifier (GRC notation, e.g. &#x60;GRCh37&#x60;). 
   * @return assemblyId
   **/
  @JsonProperty("assemblyId")
  @ApiModelProperty(example = "GRCh38", value = "Assembly identifier (GRC notation, e.g. `GRCh37`). ")
  
  public String getAssemblyId() {
    return assemblyId;
  }

  public void setAssemblyId(String assemblyId) {
    this.assemblyId = assemblyId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Ga4ghVariantDetails variantDetails = (Ga4ghVariantDetails) o;
    return Objects.equals(this.chromosome, variantDetails.chromosome) &&
        Objects.equals(this.referenceBases, variantDetails.referenceBases) &&
        Objects.equals(this.alternateBases, variantDetails.alternateBases) &&
        Objects.equals(this.variantType, variantDetails.variantType) &&
        Objects.equals(this.start, variantDetails.start) &&
        Objects.equals(this.end, variantDetails.end) &&
        Objects.equals(this.assemblyId, variantDetails.assemblyId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(chromosome, referenceBases, alternateBases, variantType, start, end, assemblyId);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Ga4ghVariantDetails {\n");

    sb.append("    chromosome: ").append(toIndentedString(chromosome)).append("\n");
    sb.append("    referenceBases: ").append(toIndentedString(referenceBases)).append("\n");
    sb.append("    alternateBases: ").append(toIndentedString(alternateBases)).append("\n");
    sb.append("    variantType: ").append(toIndentedString(variantType)).append("\n");
    sb.append("    start: ").append(toIndentedString(start)).append("\n");
    sb.append("    end: ").append(toIndentedString(end)).append("\n");
    sb.append("    assemblyId: ").append(toIndentedString(assemblyId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

