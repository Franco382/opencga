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
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * All the required fields to query any kind of variant (e.g. SNP, DUP,  etc.). 
 */
@ApiModel(description = "All the required fields to query any kind of variant (e.g. SNP, DUP,  etc.). ")
@JsonPropertyOrder({
  Ga4ghGenomicVariantFields.JSON_PROPERTY_ASSEMBLY_ID,
  Ga4ghGenomicVariantFields.JSON_PROPERTY_REFERENCE_NAME,
  Ga4ghGenomicVariantFields.JSON_PROPERTY_START,
  Ga4ghGenomicVariantFields.JSON_PROPERTY_END,
  Ga4ghGenomicVariantFields.JSON_PROPERTY_REFERENCE_BASES,
  Ga4ghGenomicVariantFields.JSON_PROPERTY_ALTERNATE_BASES,
  Ga4ghGenomicVariantFields.JSON_PROPERTY_VARIANT_TYPE,
  Ga4ghGenomicVariantFields.JSON_PROPERTY_MATE_NAME
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2020-03-24T15:12:46.170Z[Europe/London]")
public class Ga4ghGenomicVariantFields   {
  public static final String JSON_PROPERTY_ASSEMBLY_ID = "assemblyId";
  @JsonProperty(JSON_PROPERTY_ASSEMBLY_ID)
  private String assemblyId;

  public static final String JSON_PROPERTY_REFERENCE_NAME = "referenceName";
  @JsonProperty(JSON_PROPERTY_REFERENCE_NAME)
  private String referenceName;

  public static final String JSON_PROPERTY_START = "start";
  @JsonProperty(JSON_PROPERTY_START)
  private List<Long> start = new ArrayList<Long>();

  public static final String JSON_PROPERTY_END = "end";
  @JsonProperty(JSON_PROPERTY_END)
  private List<Long> end = null;

  public static final String JSON_PROPERTY_REFERENCE_BASES = "referenceBases";
  @JsonProperty(JSON_PROPERTY_REFERENCE_BASES)
  private String referenceBases;

  public static final String JSON_PROPERTY_ALTERNATE_BASES = "alternateBases";
  @JsonProperty(JSON_PROPERTY_ALTERNATE_BASES)
  private String alternateBases;

  public static final String JSON_PROPERTY_VARIANT_TYPE = "variantType";
  @JsonProperty(JSON_PROPERTY_VARIANT_TYPE)
  private String variantType;

  public static final String JSON_PROPERTY_MATE_NAME = "mateName";
  @JsonProperty(JSON_PROPERTY_MATE_NAME)
  private Ga4ghChromosome mateName;

  public Ga4ghGenomicVariantFields assemblyId(String assemblyId) {
    this.assemblyId = assemblyId;
    return this;
  }

  /**
   * Assembly identifier (GRC notation, e.g. GRCh37). 
   * @return assemblyId
   **/
  @JsonProperty("assemblyId")
  @ApiModelProperty(example = "GRCh38", required = true, value = "Assembly identifier (GRC notation, e.g. GRCh37). ")
  @NotNull 
  public String getAssemblyId() {
    return assemblyId;
  }

  public void setAssemblyId(String assemblyId) {
    this.assemblyId = assemblyId;
  }

  public Ga4ghGenomicVariantFields referenceName(String referenceName) {
    this.referenceName = referenceName;
    return this;
  }

  /**
   * Get referenceName
   * @return referenceName
   **/
  @JsonProperty("referenceName")
  @ApiModelProperty(required = true, value = "")
  @NotNull @Valid 
  public String getReferenceName() {
    return referenceName;
  }

  public void setReferenceName(String referenceName) {
    this.referenceName = referenceName;
  }

  public Ga4ghGenomicVariantFields start(List<Long> start) {
    this.start = start;
    return this;
  }

  public Ga4ghGenomicVariantFields addStartItem(Long startItem) {
    this.start.add(startItem);
    return this;
  }

  /**
   * Precise or fuzzy start coordinate position(s), allele locus  (0-based, inclusive). * start only:   - for single positions, e.g. the start of a specified sequence    alteration where the size is given through the specified    &#x60;alternateBases&#x60;   - typical use are queries for SNV and small InDels   - THIS IS NOT TRUE FOR RANGE QUERIES!!!! -&gt; the use of \&quot;start\&quot;    without an \&quot;end\&quot; parameter requires the use of \&quot;referenceBases\&quot; * &#x60;start&#x60; and &#x60;end&#x60;:   - special use case for exactly determined structural changes * use 2 values for querying imprecise positions (e.g. identifying  all structural variants starting anywhere between &#x60;start[0]&#x60; &lt;-&gt;  &#x60;start[1]&#x60;, and ending anywhere between &#x60;end[0]&#x60; &lt;-&gt; &#x60;end[1]&#x60;) * IS THIS NECESSARY???? -&gt; single or double sided precise matches  can be achieved by setting &#x60;start[0]&#x60; &#x3D; &#x60;start[1]&#x60; XOR &#x60;end[0]&#x60; &#x3D;  &#x60;end[1]&#x60; 
   * minimum: 0
   * @return start
   **/
  @JsonProperty("start")
  @ApiModelProperty(required = true, value = "Precise or fuzzy start coordinate position(s), allele locus  (0-based, inclusive). * start only:   - for single positions, e.g. the start of a specified sequence    alteration where the size is given through the specified    `alternateBases`   - typical use are queries for SNV and small InDels   - THIS IS NOT TRUE FOR RANGE QUERIES!!!! -> the use of \"start\"    without an \"end\" parameter requires the use of \"referenceBases\" * `start` and `end`:   - special use case for exactly determined structural changes * use 2 values for querying imprecise positions (e.g. identifying  all structural variants starting anywhere between `start[0]` <->  `start[1]`, and ending anywhere between `end[0]` <-> `end[1]`) * IS THIS NECESSARY???? -> single or double sided precise matches  can be achieved by setting `start[0]` = `start[1]` XOR `end[0]` =  `end[1]` ")
  @NotNull  @DecimalMin("0")
  public List<Long> getStart() {
    return start;
  }

  public void setStart(List<Long> start) {
    this.start = start;
  }

  public Ga4ghGenomicVariantFields end(List<Long> end) {
    this.end = end;
    return this;
  }

  public Ga4ghGenomicVariantFields addEndItem(Long endItem) {
    if (this.end == null) {
      this.end = new ArrayList<Long>();
    }
    this.end.add(endItem);
    return this;
  }

  /**
   * Precise or fuzzy end coordinate(s) (0-based, exclusive). See start.  For fuzzy matches, provide 2 values in the array (e.g. [111,222]). 
   * @return end
   **/
  @JsonProperty("end")
  @ApiModelProperty(value = "Precise or fuzzy end coordinate(s) (0-based, exclusive). See start.  For fuzzy matches, provide 2 values in the array (e.g. [111,222]). ")
  
  public List<Long> getEnd() {
    return end;
  }

  public void setEnd(List<Long> end) {
    this.end = end;
  }

  public Ga4ghGenomicVariantFields referenceBases(String referenceBases) {
    this.referenceBases = referenceBases;
    return this;
  }

  /**
   * Reference bases for this variant (starting from &#x60;start&#x60;).  Accepted values: [ACGTN]*. N is a wildcard, that denotes the  position of any base, and can be used as a standalone base of any  type or within a partially known sequence. For example a sequence  where the first and last bases are known, but the middle portion can  exhibit countless variations of [ACGT], or the bases are unknown:  ANNT the Ns can take take any form of [ACGT], which makes both ACCT  and ATGT (or any other combination) viable sequences. 
   * @return referenceBases
   **/
  @JsonProperty("referenceBases")
  @ApiModelProperty(value = "Reference bases for this variant (starting from `start`).  Accepted values: [ACGTN]*. N is a wildcard, that denotes the  position of any base, and can be used as a standalone base of any  type or within a partially known sequence. For example a sequence  where the first and last bases are known, but the middle portion can  exhibit countless variations of [ACGT], or the bases are unknown:  ANNT the Ns can take take any form of [ACGT], which makes both ACCT  and ATGT (or any other combination) viable sequences. ")
   @Pattern(regexp="^([ACGTN]+)$")
  public String getReferenceBases() {
    return referenceBases;
  }

  public void setReferenceBases(String referenceBases) {
    this.referenceBases = referenceBases;
  }

  public Ga4ghGenomicVariantFields alternateBases(String alternateBases) {
    this.alternateBases = alternateBases;
    return this;
  }

  /**
   * The bases that appear instead of the reference bases. Accepted  values: [ACGTN]*. N is a wildcard, that denotes the position of any  base, and can be used as a standalone base of any type or within a  partially known sequence. For example a sequence where the first and  last bases are known, but the middle portion can exhibit countless  variations of [ACGT], or the bases are unknown: ANNT the Ns can take  take any form of [ACGT], which makes both ACCT and ATGT (or any  other combination) viable sequences. Symbolic ALT alleles (DEL, INS, DUP, INV, CNV, DUP:TANDEM, DEL:ME, INS:ME) will be represented in &#x60;variantType&#x60;. Optional: either &#x60;alternateBases&#x60; or &#x60;variantType&#x60; is required. 
   * @return alternateBases
   **/
  @JsonProperty("alternateBases")
  @ApiModelProperty(value = "The bases that appear instead of the reference bases. Accepted  values: [ACGTN]*. N is a wildcard, that denotes the position of any  base, and can be used as a standalone base of any type or within a  partially known sequence. For example a sequence where the first and  last bases are known, but the middle portion can exhibit countless  variations of [ACGT], or the bases are unknown: ANNT the Ns can take  take any form of [ACGT], which makes both ACCT and ATGT (or any  other combination) viable sequences. Symbolic ALT alleles (DEL, INS, DUP, INV, CNV, DUP:TANDEM, DEL:ME, INS:ME) will be represented in `variantType`. Optional: either `alternateBases` or `variantType` is required. ")
   @Pattern(regexp="^([ACGTN]+)$")
  public String getAlternateBases() {
    return alternateBases;
  }

  public void setAlternateBases(String alternateBases) {
    this.alternateBases = alternateBases;
  }

  public Ga4ghGenomicVariantFields variantType(String variantType) {
    this.variantType = variantType;
    return this;
  }

  /**
   * The &#x60;variantType&#x60; is used to denote e.g. structural variants. Examples: * DUP: duplication of sequence following &#x60;start&#x60;; not necessarily in situ * DEL: deletion of sequence following &#x60;start&#x60; * BND: breakend, i.e. termination of the allele at position       &#x60;start&#x60; or in the &#x60;startMin&#x60; &#x3D;&gt; &#x60;startMax&#x60; interval, or fusion       of the sequence to distant partner Optional: either &#x60;alternateBases&#x60; or &#x60;variantType&#x60; is required. 
   * @return variantType
   **/
  @JsonProperty("variantType")
  @ApiModelProperty(value = "The `variantType` is used to denote e.g. structural variants. Examples: * DUP: duplication of sequence following `start`; not necessarily in situ * DEL: deletion of sequence following `start` * BND: breakend, i.e. termination of the allele at position       `start` or in the `startMin` => `startMax` interval, or fusion       of the sequence to distant partner Optional: either `alternateBases` or `variantType` is required. ")
  
  public String getVariantType() {
    return variantType;
  }

  public void setVariantType(String variantType) {
    this.variantType = variantType;
  }

  public Ga4ghGenomicVariantFields mateName(Ga4ghChromosome mateName) {
    this.mateName = mateName;
    return this;
  }

  /**
   * Get mateName
   * @return mateName
   **/
  @JsonProperty("mateName")
  @ApiModelProperty(value = "")
  @Valid 
  public Ga4ghChromosome getMateName() {
    return mateName;
  }

  public void setMateName(Ga4ghChromosome mateName) {
    this.mateName = mateName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Ga4ghGenomicVariantFields genomicVariantFields = (Ga4ghGenomicVariantFields) o;
    return Objects.equals(this.assemblyId, genomicVariantFields.assemblyId) &&
        Objects.equals(this.referenceName, genomicVariantFields.referenceName) &&
        Objects.equals(this.start, genomicVariantFields.start) &&
        Objects.equals(this.end, genomicVariantFields.end) &&
        Objects.equals(this.referenceBases, genomicVariantFields.referenceBases) &&
        Objects.equals(this.alternateBases, genomicVariantFields.alternateBases) &&
        Objects.equals(this.variantType, genomicVariantFields.variantType) &&
        Objects.equals(this.mateName, genomicVariantFields.mateName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(assemblyId, referenceName, start, end, referenceBases, alternateBases, variantType, mateName);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Ga4ghGenomicVariantFields {\n");

    sb.append("    assemblyId: ").append(toIndentedString(assemblyId)).append("\n");
    sb.append("    referenceName: ").append(toIndentedString(referenceName)).append("\n");
    sb.append("    start: ").append(toIndentedString(start)).append("\n");
    sb.append("    end: ").append(toIndentedString(end)).append("\n");
    sb.append("    referenceBases: ").append(toIndentedString(referenceBases)).append("\n");
    sb.append("    alternateBases: ").append(toIndentedString(alternateBases)).append("\n");
    sb.append("    variantType: ").append(toIndentedString(variantType)).append("\n");
    sb.append("    mateName: ").append(toIndentedString(mateName)).append("\n");
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

