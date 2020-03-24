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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The Variant Annotation schema is common to both an Evidence and a Data Beacon. 
 */
@ApiModel(description = "The Variant Annotation schema is common to both an Evidence and a Data Beacon. ")
@JsonPropertyOrder({
  Ga4ghVariantAnnotations.JSON_PROPERTY_GENOMIC_H_G_V_S_ID,
  Ga4ghVariantAnnotations.JSON_PROPERTY_PROTEIN_H_G_V_S_IDS,
  Ga4ghVariantAnnotations.JSON_PROPERTY_MOLECULAR_CONSEQUENCE,
  Ga4ghVariantAnnotations.JSON_PROPERTY_GENE_IDS,
  Ga4ghVariantAnnotations.JSON_PROPERTY_TRANSCRIPT_IDS,
  Ga4ghVariantAnnotations.JSON_PROPERTY_VARIANT_GENE_RELATIONSHIP,
  Ga4ghVariantAnnotations.JSON_PROPERTY_CLINICAL_RELEVANCE,
  Ga4ghVariantAnnotations.JSON_PROPERTY_ALTERNATIVE_IDS,
  Ga4ghVariantAnnotations.JSON_PROPERTY_INFO
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2020-03-24T15:12:46.170Z[Europe/London]")
public class Ga4ghVariantAnnotations   {
  public static final String JSON_PROPERTY_GENOMIC_H_G_V_S_ID = "genomicHGVSId";
  @JsonProperty(JSON_PROPERTY_GENOMIC_H_G_V_S_ID)
  private String genomicHGVSId;

  public static final String JSON_PROPERTY_PROTEIN_H_G_V_S_IDS = "proteinHGVSIds";
  @JsonProperty(JSON_PROPERTY_PROTEIN_H_G_V_S_IDS)
  private List<String> proteinHGVSIds = null;

  public static final String JSON_PROPERTY_MOLECULAR_CONSEQUENCE = "molecularConsequence";
  @JsonProperty(JSON_PROPERTY_MOLECULAR_CONSEQUENCE)
  private String molecularConsequence;

  public static final String JSON_PROPERTY_GENE_IDS = "geneIds";
  @JsonProperty(JSON_PROPERTY_GENE_IDS)
  private List<String> geneIds = null;

  public static final String JSON_PROPERTY_TRANSCRIPT_IDS = "transcriptIds";
  @JsonProperty(JSON_PROPERTY_TRANSCRIPT_IDS)
  private List<String> transcriptIds = null;

  public static final String JSON_PROPERTY_VARIANT_GENE_RELATIONSHIP = "variantGeneRelationship";
  @JsonProperty(JSON_PROPERTY_VARIANT_GENE_RELATIONSHIP)
  private String variantGeneRelationship;

  public static final String JSON_PROPERTY_CLINICAL_RELEVANCE = "clinicalRelevance";
  @JsonProperty(JSON_PROPERTY_CLINICAL_RELEVANCE)
  private List<Ga4ghClinicalRelevance> clinicalRelevance = null;

  public static final String JSON_PROPERTY_ALTERNATIVE_IDS = "alternativeIds";
  @JsonProperty(JSON_PROPERTY_ALTERNATIVE_IDS)
  private List<String> alternativeIds = null;

  public static final String JSON_PROPERTY_INFO = "info";
  @JsonProperty(JSON_PROPERTY_INFO)
  private Object info;

  public Ga4ghVariantAnnotations genomicHGVSId(String genomicHGVSId) {
    this.genomicHGVSId = genomicHGVSId;
    return this;
  }

  /**
   * HGVSId descriptor at genomic level (recommended, referred to genome assembly defined in Variant Representation. schema) 
   * @return genomicHGVSId
   **/
  @JsonProperty("genomicHGVSId")
  @ApiModelProperty(example = "NC_000011.9:g.134086816T>C", value = "HGVSId descriptor at genomic level (recommended, referred to genome assembly defined in Variant Representation. schema) ")
  
  public String getGenomicHGVSId() {
    return genomicHGVSId;
  }

  public void setGenomicHGVSId(String genomicHGVSId) {
    this.genomicHGVSId = genomicHGVSId;
  }

  public Ga4ghVariantAnnotations proteinHGVSIds(List<String> proteinHGVSIds) {
    this.proteinHGVSIds = proteinHGVSIds;
    return this;
  }

  public Ga4ghVariantAnnotations addProteinHGVSIdsItem(String proteinHGVSIdsItem) {
    if (this.proteinHGVSIds == null) {
      this.proteinHGVSIds = new ArrayList<String>();
    }
    this.proteinHGVSIds.add(proteinHGVSIdsItem);
    return this;
  }

  /**
   * List of HGVSId descriptor(s) at protein level (for protein-altering variants). 
   * @return proteinHGVSIds
   **/
  @JsonProperty("proteinHGVSIds")
  @ApiModelProperty(example = "[\"NP_009225.1:p.Glu1817Ter\"]", value = "List of HGVSId descriptor(s) at protein level (for protein-altering variants). ")
  
  public List<String> getProteinHGVSIds() {
    return proteinHGVSIds;
  }

  public void setProteinHGVSIds(List<String> proteinHGVSIds) {
    this.proteinHGVSIds = proteinHGVSIds;
  }

  public Ga4ghVariantAnnotations molecularConsequence(String molecularConsequence) {
    this.molecularConsequence = molecularConsequence;
    return this;
  }

  /**
   * Categorical value from Sequence Variant ontology (SO:0001060) describing the molecular consequence of the variant such as missense variant and frameshift truncation variant for protein-altering variants. 
   * @return molecularConsequence
   **/
  @JsonProperty("molecularConsequence")
  @ApiModelProperty(example = "SO:0001583", value = "Categorical value from Sequence Variant ontology (SO:0001060) describing the molecular consequence of the variant such as missense variant and frameshift truncation variant for protein-altering variants. ")
  
  public String getMolecularConsequence() {
    return molecularConsequence;
  }

  public void setMolecularConsequence(String molecularConsequence) {
    this.molecularConsequence = molecularConsequence;
  }

  public Ga4ghVariantAnnotations geneIds(List<String> geneIds) {
    this.geneIds = geneIds;
    return this;
  }

  public Ga4ghVariantAnnotations addGeneIdsItem(String geneIdsItem) {
    if (this.geneIds == null) {
      this.geneIds = new ArrayList<String>();
    }
    this.geneIds.add(geneIdsItem);
    return this;
  }

  /**
   * List of HGNC ID(s) for gene(s) a↵ected by the variant. 
   * @return geneIds
   **/
  @JsonProperty("geneIds")
  @ApiModelProperty(example = "[\"HGNC:8157\"]", value = "List of HGNC ID(s) for gene(s) a↵ected by the variant. ")
  
  public List<String> getGeneIds() {
    return geneIds;
  }

  public void setGeneIds(List<String> geneIds) {
    this.geneIds = geneIds;
  }

  public Ga4ghVariantAnnotations transcriptIds(List<String> transcriptIds) {
    this.transcriptIds = transcriptIds;
    return this;
  }

  public Ga4ghVariantAnnotations addTranscriptIdsItem(String transcriptIdsItem) {
    if (this.transcriptIds == null) {
      this.transcriptIds = new ArrayList<String>();
    }
    this.transcriptIds.add(transcriptIdsItem);
    return this;
  }

  /**
   * List of ENSMEBL ID(s) for transcript(s) a↵ected by the variant. 
   * @return transcriptIds
   **/
  @JsonProperty("transcriptIds")
  @ApiModelProperty(example = "[\"ENST00000277010.9\"]", value = "List of ENSMEBL ID(s) for transcript(s) a↵ected by the variant. ")
  
  public List<String> getTranscriptIds() {
    return transcriptIds;
  }

  public void setTranscriptIds(List<String> transcriptIds) {
    this.transcriptIds = transcriptIds;
  }

  public Ga4ghVariantAnnotations variantGeneRelationship(String variantGeneRelationship) {
    this.variantGeneRelationship = variantGeneRelationship;
    return this;
  }

  /**
   * Categorical value classifying the variant according to the broadness of the variant e↵ect in terms of genes: intergenic, 5UTR, 3UTR, single-gene (exonic, intronic), in overlapping genes (exonic, intronic), spanning multiple genes, multiple genes 
   * @return variantGeneRelationship
   **/
  @JsonProperty("variantGeneRelationship")
  @ApiModelProperty(example = "single gene", value = "Categorical value classifying the variant according to the broadness of the variant e↵ect in terms of genes: intergenic, 5UTR, 3UTR, single-gene (exonic, intronic), in overlapping genes (exonic, intronic), spanning multiple genes, multiple genes ")
  
  public String getVariantGeneRelationship() {
    return variantGeneRelationship;
  }

  public void setVariantGeneRelationship(String variantGeneRelationship) {
    this.variantGeneRelationship = variantGeneRelationship;
  }

  public Ga4ghVariantAnnotations clinicalRelevance(List<Ga4ghClinicalRelevance> clinicalRelevance) {
    this.clinicalRelevance = clinicalRelevance;
    return this;
  }

  public Ga4ghVariantAnnotations addClinicalRelevanceItem(Ga4ghClinicalRelevance clinicalRelevanceItem) {
    if (this.clinicalRelevance == null) {
      this.clinicalRelevance = new ArrayList<Ga4ghClinicalRelevance>();
    }
    this.clinicalRelevance.add(clinicalRelevanceItem);
    return this;
  }

  /**
   * Get clinicalRelevance
   * @return clinicalRelevance
   **/
  @JsonProperty("clinicalRelevance")
  @ApiModelProperty(value = "")
  @Valid 
  public List<Ga4ghClinicalRelevance> getClinicalRelevance() {
    return clinicalRelevance;
  }

  public void setClinicalRelevance(List<Ga4ghClinicalRelevance> clinicalRelevance) {
    this.clinicalRelevance = clinicalRelevance;
  }

  public Ga4ghVariantAnnotations alternativeIds(List<String> alternativeIds) {
    this.alternativeIds = alternativeIds;
    return this;
  }

  public Ga4ghVariantAnnotations addAlternativeIdsItem(String alternativeIdsItem) {
    if (this.alternativeIds == null) {
      this.alternativeIds = new ArrayList<String>();
    }
    this.alternativeIds.add(alternativeIdsItem);
    return this;
  }

  /**
   * List of Cross-referencing ID(s) (CURIE) for previously described variants (e.g. clinVarId, ClinGen, COSMIC). 
   * @return alternativeIds
   **/
  @JsonProperty("alternativeIds")
  @ApiModelProperty(example = "[\"rs80356868\",\"CA003602\",\"VCV000055583.1\"]", value = "List of Cross-referencing ID(s) (CURIE) for previously described variants (e.g. clinVarId, ClinGen, COSMIC). ")
  
  public List<String> getAlternativeIds() {
    return alternativeIds;
  }

  public void setAlternativeIds(List<String> alternativeIds) {
    this.alternativeIds = alternativeIds;
  }

  public Ga4ghVariantAnnotations info(Object info) {
    this.info = info;
    return this;
  }

  /**
   * Additional unspecified metadata about the dataset response or its  content. 
   * @return info
   **/
  @JsonProperty("info")
  @ApiModelProperty(example = "{\"additionalInfoKey1\":[\"additionalInfoValue1\",\"additionalInfoValue2\"],\"additionalInfoKey2\":\"additionalInfoValue3\"}", value = "Additional unspecified metadata about the dataset response or its  content. ")
  @Valid 
  public Object getInfo() {
    return info;
  }

  public void setInfo(Object info) {
    this.info = info;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Ga4ghVariantAnnotations variantAnnotations = (Ga4ghVariantAnnotations) o;
    return Objects.equals(this.genomicHGVSId, variantAnnotations.genomicHGVSId) &&
        Objects.equals(this.proteinHGVSIds, variantAnnotations.proteinHGVSIds) &&
        Objects.equals(this.molecularConsequence, variantAnnotations.molecularConsequence) &&
        Objects.equals(this.geneIds, variantAnnotations.geneIds) &&
        Objects.equals(this.transcriptIds, variantAnnotations.transcriptIds) &&
        Objects.equals(this.variantGeneRelationship, variantAnnotations.variantGeneRelationship) &&
        Objects.equals(this.clinicalRelevance, variantAnnotations.clinicalRelevance) &&
        Objects.equals(this.alternativeIds, variantAnnotations.alternativeIds) &&
        Objects.equals(this.info, variantAnnotations.info);
  }

  @Override
  public int hashCode() {
    return Objects.hash(genomicHGVSId, proteinHGVSIds, molecularConsequence, geneIds, transcriptIds, variantGeneRelationship, clinicalRelevance, alternativeIds, info);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Ga4ghVariantAnnotations {\n");

    sb.append("    genomicHGVSId: ").append(toIndentedString(genomicHGVSId)).append("\n");
    sb.append("    proteinHGVSIds: ").append(toIndentedString(proteinHGVSIds)).append("\n");
    sb.append("    molecularConsequence: ").append(toIndentedString(molecularConsequence)).append("\n");
    sb.append("    geneIds: ").append(toIndentedString(geneIds)).append("\n");
    sb.append("    transcriptIds: ").append(toIndentedString(transcriptIds)).append("\n");
    sb.append("    variantGeneRelationship: ").append(toIndentedString(variantGeneRelationship)).append("\n");
    sb.append("    clinicalRelevance: ").append(toIndentedString(clinicalRelevance)).append("\n");
    sb.append("    alternativeIds: ").append(toIndentedString(alternativeIds)).append("\n");
    sb.append("    info: ").append(toIndentedString(info)).append("\n");
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

