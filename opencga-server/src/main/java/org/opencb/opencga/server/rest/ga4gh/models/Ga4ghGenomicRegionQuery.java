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
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fields available in a region query. 
 */
@ApiModel(description = "Fields available in a region query. ")
@JsonPropertyOrder({
  Ga4ghGenomicRegionQuery.JSON_PROPERTY_VARIANT,
  Ga4ghGenomicRegionQuery.JSON_PROPERTY_DATASETS,
  Ga4ghGenomicRegionQuery.JSON_PROPERTY_FILTERS,
  Ga4ghGenomicRegionQuery.JSON_PROPERTY_CUSTOM_FILTERS
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2020-03-24T15:12:46.170Z[Europe/London]")
public class Ga4ghGenomicRegionQuery   {
  public static final String JSON_PROPERTY_VARIANT = "variant";
  @JsonProperty(JSON_PROPERTY_VARIANT)
  private Ga4ghGenomicRegionFields variant;

  public static final String JSON_PROPERTY_DATASETS = "datasets";
  @JsonProperty(JSON_PROPERTY_DATASETS)
  private Ga4ghRequestDatasets datasets;

  public static final String JSON_PROPERTY_FILTERS = "filters";
  @JsonProperty(JSON_PROPERTY_FILTERS)
  private List<String> filters = null;

  public static final String JSON_PROPERTY_CUSTOM_FILTERS = "customFilters";
  @JsonProperty(JSON_PROPERTY_CUSTOM_FILTERS)
  private List<String> customFilters = null;

  public Ga4ghGenomicRegionQuery variant(Ga4ghGenomicRegionFields variant) {
    this.variant = variant;
    return this;
  }

  /**
   * Get variant
   * @return variant
   **/
  @JsonProperty("variant")
  @ApiModelProperty(required = true, value = "")
  @NotNull @Valid 
  public Ga4ghGenomicRegionFields getVariant() {
    return variant;
  }

  public void setVariant(Ga4ghGenomicRegionFields variant) {
    this.variant = variant;
  }

  public Ga4ghGenomicRegionQuery datasets(Ga4ghRequestDatasets datasets) {
    this.datasets = datasets;
    return this;
  }

  /**
   * Get datasets
   * @return datasets
   **/
  @JsonProperty("datasets")
  @ApiModelProperty(value = "")
  @Valid 
  public Ga4ghRequestDatasets getDatasets() {
    return datasets;
  }

  public void setDatasets(Ga4ghRequestDatasets datasets) {
    this.datasets = datasets;
  }

  public Ga4ghGenomicRegionQuery filters(List<String> filters) {
    this.filters = filters;
    return this;
  }

  public Ga4ghGenomicRegionQuery addFiltersItem(String filtersItem) {
    if (this.filters == null) {
      this.filters = new ArrayList<String>();
    }
    this.filters.add(filtersItem);
    return this;
  }

  /**
   * Description pending 
   * @return filters
   **/
  @JsonProperty("filters")
  @ApiModelProperty(example = "BTO:0000199", value = "Description pending ")
  
  public List<String> getFilters() {
    return filters;
  }

  public void setFilters(List<String> filters) {
    this.filters = filters;
  }

  public Ga4ghGenomicRegionQuery customFilters(List<String> customFilters) {
    this.customFilters = customFilters;
    return this;
  }

  public Ga4ghGenomicRegionQuery addCustomFiltersItem(String customFiltersItem) {
    if (this.customFilters == null) {
      this.customFilters = new ArrayList<String>();
    }
    this.customFilters.add(customFiltersItem);
    return this;
  }

  /**
   * Description pending 
   * @return customFilters
   **/
  @JsonProperty("customFilters")
  @ApiModelProperty(example = "mydict.aterm:avalue,mydict.aterm2:avalue2", value = "Description pending ")
  
  public List<String> getCustomFilters() {
    return customFilters;
  }

  public void setCustomFilters(List<String> customFilters) {
    this.customFilters = customFilters;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Ga4ghGenomicRegionQuery genomicRegionQuery = (Ga4ghGenomicRegionQuery) o;
    return Objects.equals(this.variant, genomicRegionQuery.variant) &&
        Objects.equals(this.datasets, genomicRegionQuery.datasets) &&
        Objects.equals(this.filters, genomicRegionQuery.filters) &&
        Objects.equals(this.customFilters, genomicRegionQuery.customFilters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(variant, datasets, filters, customFilters);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Ga4ghGenomicRegionQuery {\n");

    sb.append("    variant: ").append(toIndentedString(variant)).append("\n");
    sb.append("    datasets: ").append(toIndentedString(datasets)).append("\n");
    sb.append("    filters: ").append(toIndentedString(filters)).append("\n");
    sb.append("    customFilters: ").append(toIndentedString(customFilters)).append("\n");
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

