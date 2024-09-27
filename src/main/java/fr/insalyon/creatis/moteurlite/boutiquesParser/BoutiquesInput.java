// Source code is decompiled from a .class file using FernFlower decompiler.
package fr.insalyon.creatis.moteurlite.boutiquesParser;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * Author: Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class BoutiquesInput {
   private String commandLineFlag;
   private String commandLineFlagSeparator;
   private Object defaultValue;
   private String description;
   private List<String> disablesInputs;
   private Boolean exclusiveMaximum;
   private Boolean exclusiveMinimum;
   private String id;
   private Boolean isInteger;
   private Boolean isList;
   private String listSeparator;
   private Number maxListEntries;
   private Number maximum;
   private Number minListEntries;
   private Number minimum;
   private String name;
   private Boolean optional;
   private List<String> requiresInputs;
   private String type;
   private Boolean usesAbsolutePath;
   private List<Object> valueChoices;
   private Map<String, Object> valueDisables;
   private String valueKey;
   private Map<String, Object> valueRequires;

   public BoutiquesInput() {
   }

   @JsonProperty("command-line-flag")
   public String getCommandLineFlag() {
      return this.commandLineFlag;
   }

   public void setCommandLineFlag(String commandLineFlag) {
      //System.out.println("command-line-flag:" + commandLineFlag);
      this.commandLineFlag = commandLineFlag;
   }

   @JsonProperty("command-line-flag-separator")
   public String getCommandLineFlagSeparator() {
      return this.commandLineFlagSeparator;
   }

   public void setCommandLineFlagSeparator(String commandLineFlagSeparator) {
      //System.out.println("command-line-flag-separator:" + commandLineFlagSeparator);
      this.commandLineFlagSeparator = commandLineFlagSeparator;
   }

   @JsonProperty("default-value")
   public Object getDefaultValue() {
      return this.defaultValue;
   }

   public void setDefaultValue(Object defaultValue) {
      this.defaultValue = defaultValue;
   }

   @JsonProperty("description")
   public String getDescription() {
      return this.description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   @JsonProperty("disables-inputs")
   public List<String> getDisablesInputs() {
      return this.disablesInputs;
   }

   public void setDisablesInputs(List<String> disablesInputs) {
      this.disablesInputs = disablesInputs;
   }

   @JsonProperty("exclusive-maximum")
   public Boolean getExclusiveMaximum() {
      return this.exclusiveMaximum;
   }

   public void setExclusiveMaximum(Boolean exclusiveMaximum) {
      this.exclusiveMaximum = exclusiveMaximum;
   }

   @JsonProperty("exclusive-minimum")
   public Boolean getExclusiveMinimum() {
      return this.exclusiveMinimum;
   }

   public void setExclusiveMinimum(Boolean exclusiveMinimum) {
      this.exclusiveMinimum = exclusiveMinimum;
   }

   @JsonProperty("id")
   public String getId() {
      return this.id;
   }

   public void setId(String id) {
      this.id = id;
   }

   @JsonProperty("integer")
   public Boolean getIsInteger() {
      return this.isInteger;
   }

   public void setIsInteger(Boolean isInteger) {
      this.isInteger = isInteger;
   }

   @JsonProperty("list")
   public Boolean getIsList() {
      return this.isList;
   }

   public void setIsList(Boolean isList) {
      this.isList = isList;
   }

   @JsonProperty("list-separator")
   public String getListSeparator() {
      return this.listSeparator;
   }

   public void setListSeparator(String listSeparator) {
      this.listSeparator = listSeparator;
   }

   @JsonProperty("max-list-entries")
   public Number getMaxListEntries() {
      return this.maxListEntries;
   }

   public void setMaxListEntries(Number maxListEntries) {
      this.maxListEntries = maxListEntries;
   }

   @JsonProperty("maximum")
   public Number getMaximum() {
      return this.maximum;
   }

   public void setMaximum(Number maximum) {
      this.maximum = maximum;
   }

   @JsonProperty("min-list-entries")
   public Number getMinListEntries() {
      return this.minListEntries;
   }

   public void setMinListEntries(Number minListEntries) {
      this.minListEntries = minListEntries;
   }

   @JsonProperty("minimum")
   public Number getMinimum() {
      return this.minimum;
   }

   public void setMinimum(Number minimum) {
      this.minimum = minimum;
   }

   @JsonProperty("name")
   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   @JsonProperty("optional")
   public Boolean getOptional() {
      return this.optional;
   }

   public void setOptional(Boolean optional) {
      this.optional = optional;
   }

   @JsonProperty("requires-inputs")
   public List<String> getRequiresInputs() {
      return this.requiresInputs;
   }

   public void setRequiresInputs(List<String> requiresInputs) {
      this.requiresInputs = requiresInputs;
   }

   @JsonProperty("type")
   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   @JsonProperty("uses-absolute-path")
   public Boolean getUsesAbsolutePath() {
      return this.usesAbsolutePath;
   }

   public void setUsesAbsolutePath(Boolean usesAbsolutePath) {
      this.usesAbsolutePath = usesAbsolutePath;
   }

   @JsonProperty("value-choices")
   public List<Object> getValueChoices() {
      return this.valueChoices;
   }

   public void setValueChoices(List<Object> valueChoices) {
      this.valueChoices = valueChoices;
   }

   @JsonProperty("value-disables")
   public Map<String, Object> getValueDisables() {
      return this.valueDisables;
   }

   public void setValueDisables(Map<String, Object> valueDisables) {
      this.valueDisables = valueDisables;
   }

   @JsonProperty("value-key")
   public String getValueKey() {
      return this.valueKey;
   }

   public void setValueKey(String valueKey) {
      this.valueKey = valueKey;
   }

   @JsonProperty("value-requires")
   public Map<String, Object> getValueRequires() {
      return this.valueRequires;
   }

   public void setValueRequires(Map<String, Object> valueRequires) {
      this.valueRequires = valueRequires;
   }
}