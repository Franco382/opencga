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
 * Default schema for variant annotation 
 */
@ApiModel(description = "Default schema for variant annotation ")
@JsonPropertyOrder({
  Ga4ghVariantAnnotationDefault.JSON_PROPERTY_VERSION,
  Ga4ghVariantAnnotationDefault.JSON_PROPERTY_VALUE
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2020-03-24T15:12:46.170Z[Europe/London]")
public class Ga4ghVariantAnnotationDefault   {
  public static final String JSON_PROPERTY_VERSION = "version";
  @JsonProperty(JSON_PROPERTY_VERSION)
  private String version;

  public static final String JSON_PROPERTY_VALUE = "value";
  @JsonProperty(JSON_PROPERTY_VALUE)
  private Ga4ghVariantAnnotations value;

  public Ga4ghVariantAnnotationDefault version(String version) {
    this.version = version;
    return this;
  }

  /**
   * Version of the schema 
   * @return version
   **/
  @JsonProperty("version")
  @ApiModelProperty(example = "beacon-variant-annotation-v0.1", value = "Version of the schema ")
  
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Ga4ghVariantAnnotationDefault value(Ga4ghVariantAnnotations value) {
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
  public Ga4ghVariantAnnotations getValue() {
    return value;
  }

  public void setValue(Ga4ghVariantAnnotations value) {
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
    Ga4ghVariantAnnotationDefault variantAnnotationDefault = (Ga4ghVariantAnnotationDefault) o;
    return Objects.equals(this.version, variantAnnotationDefault.version) &&
        Objects.equals(this.value, variantAnnotationDefault.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, value);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Ga4ghVariantAnnotationDefault {\n");

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

