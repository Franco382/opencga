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
import java.util.Objects;

/**
 * Default schema for variant 
 */
@ApiModel(description = "Default schema for variant ")
@JsonPropertyOrder({
  Ga4ghVariantDefault.JSON_PROPERTY_VERSION,
  Ga4ghVariantDefault.JSON_PROPERTY_VALUE
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2020-03-24T15:12:46.170Z[Europe/London]")
public class Ga4ghVariantDefault   {
  public static final String JSON_PROPERTY_VERSION = "version";
  @JsonProperty(JSON_PROPERTY_VERSION)
  private String version;

  public static final String JSON_PROPERTY_VALUE = "value";
  @JsonProperty(JSON_PROPERTY_VALUE)
  private Ga4ghVariant2 value;

  public Ga4ghVariantDefault version(String version) {
    this.version = version;
    return this;
  }

  /**
   * Version of the schema 
   * @return version
   **/
  @JsonProperty("version")
  @ApiModelProperty(example = "beacon-variant-v0.1", value = "Version of the schema ")
  
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Ga4ghVariantDefault value(Ga4ghVariant2 value) {
    this.value = value;
    return this;
  }

  /**
   * Get value
   * @return value
   **/
  @JsonProperty("value")
  @ApiModelProperty(value = "")
  @Valid 
  public Ga4ghVariant2 getValue() {
    return value;
  }

  public void setValue(Ga4ghVariant2 value) {
    this.value = value;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Ga4ghVariantDefault variantDefault = (Ga4ghVariantDefault) o;
    return Objects.equals(this.version, variantDefault.version) &&
        Objects.equals(this.value, variantDefault.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, value);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Ga4ghVariantDefault {\n");

    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

