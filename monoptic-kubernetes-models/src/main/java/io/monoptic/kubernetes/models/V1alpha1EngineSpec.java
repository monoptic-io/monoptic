/*
 * Kubernetes
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: v1.21.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package io.monoptic.kubernetes.models;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;

/**
 * Engine spec.
 */
@ApiModel(description = "Engine spec.")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-02-15T21:48:13.764Z[Etc/UTC]")
public class V1alpha1EngineSpec {
  public static final String SERIALIZED_NAME_SECRET = "secret";
  @SerializedName(SERIALIZED_NAME_SECRET)
  private String secret;

  public static final String SERIALIZED_NAME_URL = "url";
  @SerializedName(SERIALIZED_NAME_URL)
  private String url;


  public V1alpha1EngineSpec secret(String secret) {
    
    this.secret = secret;
    return this;
  }

   /**
   * Secret containing username, password, etc.
   * @return secret
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "Secret containing username, password, etc.")

  public String getSecret() {
    return secret;
  }


  public void setSecret(String secret) {
    this.secret = secret;
  }


  public V1alpha1EngineSpec url(String url) {
    
    this.url = url;
    return this;
  }

   /**
   * JDBC connection URL
   * @return url
  **/
  @ApiModelProperty(required = true, value = "JDBC connection URL")

  public String getUrl() {
    return url;
  }


  public void setUrl(String url) {
    this.url = url;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1alpha1EngineSpec v1alpha1EngineSpec = (V1alpha1EngineSpec) o;
    return Objects.equals(this.secret, v1alpha1EngineSpec.secret) &&
        Objects.equals(this.url, v1alpha1EngineSpec.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(secret, url);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1alpha1EngineSpec {\n");
    sb.append("    secret: ").append(toIndentedString(secret)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

