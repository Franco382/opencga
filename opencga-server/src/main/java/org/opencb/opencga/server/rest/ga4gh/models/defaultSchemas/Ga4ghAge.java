/*
 * GA4GH Beacon API Specification
 * Schemas to be used as default by the Beacon.
 *
 * The version of the OpenAPI document: 1.0
 * Contact: beacon@ga4gh.org
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.opencb.opencga.server.rest.ga4gh.models.defaultSchemas;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Definition of age of individual at baseline as age and/or age group. 
 */
@ApiModel(description = "Definition of age of individual at baseline as age and/or age group. ")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2020-03-24T09:26:11.315Z[Europe/London]")
public class Ga4ghAge {
  public static final String SERIALIZED_NAME_AGE = "age";
  @SerializedName(SERIALIZED_NAME_AGE)
  private String age;

  public static final String SERIALIZED_NAME_AGE_GROUP = "ageGroup";
  @SerializedName(SERIALIZED_NAME_AGE_GROUP)
  private String ageGroup;


  public Ga4ghAge age(String age) {
    
    this.age = age;
    return this;
  }

   /**
   * Value indicating the age of the individual at the time of collection in the ISO8601 duration format P[n]Y[n]M[n]DT[n]H[n]M[n]S 
   * @return age
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "P32Y6M1D", value = "Value indicating the age of the individual at the time of collection in the ISO8601 duration format P[n]Y[n]M[n]DT[n]H[n]M[n]S ")

  public String getAge() {
    return age;
  }


  public void setAge(String age) {
    this.age = age;
  }


  public Ga4ghAge ageGroup(String ageGroup) {
    
    this.ageGroup = ageGroup;
    return this;
  }

   /**
   * Categorical value from NCIT Age Group ontology classifying the individuals in age groups. 
   * @return ageGroup
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "NCIT:C17600", value = "Categorical value from NCIT Age Group ontology classifying the individuals in age groups. ")

  public String getAgeGroup() {
    return ageGroup;
  }


  public void setAgeGroup(String ageGroup) {
    this.ageGroup = ageGroup;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Ga4ghAge age = (Ga4ghAge) o;
    return Objects.equals(this.age, age.age) &&
        Objects.equals(this.ageGroup, age.ageGroup);
  }

  @Override
  public int hashCode() {
    return Objects.hash(age, ageGroup);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Ga4ghAge {\n");
    sb.append("    age: ").append(toIndentedString(age)).append("\n");
    sb.append("    ageGroup: ").append(toIndentedString(ageGroup)).append("\n");
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

