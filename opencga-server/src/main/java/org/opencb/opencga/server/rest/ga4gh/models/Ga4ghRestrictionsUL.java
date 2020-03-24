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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets RestrictionsUL
 */
public enum Ga4ghRestrictionsUL {
  
  UNRESTRICTED("UNRESTRICTED"),
  
  LIMITED("LIMITED");

  private String value;

  Ga4ghRestrictionsUL(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static Ga4ghRestrictionsUL fromValue(String value) {
    for (Ga4ghRestrictionsUL b : Ga4ghRestrictionsUL.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

