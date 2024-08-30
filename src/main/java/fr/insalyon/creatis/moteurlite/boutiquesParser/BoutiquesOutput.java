// Source code is decompiled from a .class file using FernFlower decompiler.
package fr.insalyon.creatis.moteurlite.boutiquesParser;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BoutiquesOutput {
   private String commandLineFlag;
   private String commandLineFlagSeparator;
   private List<String> conditionalPathTemplate;
   private String description;
   private List<String> fileTemplate;
   private String id;
   private Boolean isList;
   private String name;
   private Boolean optional;
   private String pathTemplate;
   private List<String> pathTemplateStrippedExtensions;
   private Boolean usesAbsolutePath;
   private String valueKey;

   public BoutiquesOutput() {
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

   @JsonProperty("conditional-path-template")
   public List<String> getConditionalPathTemplate() {
      return this.conditionalPathTemplate;
   }

   public void setConditionalPathTemplate(List<String> conditionalPathTemplate) {
      //System.out.println("conditional-path-template:" + conditionalPathTemplate);
      this.conditionalPathTemplate = conditionalPathTemplate;
   }

   @JsonProperty("description")
   public String getDescription() {
      return this.description;
   }

   public void setDescription(String description) {
      //System.out.println("description:" + description);
      this.description = description;
   }

   @JsonProperty("file-template")
   public List<String> getFileTemplate() {
      return this.fileTemplate;
   }

   public void setFileTemplate(List<String> fileTemplate) {
      //System.out.println("file-template:" + fileTemplate);
      this.fileTemplate = fileTemplate;
   }

   @JsonProperty("id")
   public String getId() {
      return this.id;
   }

   public void setId(String id) {
      //System.out.println("id:" + id);
      this.id = id;
   }

   @JsonProperty("list")
   public Boolean getIsList() {
      return this.isList;
   }

   public void setIsList(Boolean isList) {
      //System.out.println("isList:" + isList);
      this.isList = isList;
   }

   @JsonProperty("name")
   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      //System.out.println("name:" + name);
      this.name = name;
   }

   @JsonProperty("optional")
   public Boolean getOptional() {
      return this.optional;
   }

   public void setOptional(Boolean optional) {
      //System.out.println("optional:" + optional);
      this.optional = optional;
   }

   @JsonProperty("path-template")
   public String getPathTemplate() {
      return this.pathTemplate;
   }

   public void setPathTemplate(String pathTemplate) {
      //System.out.println("path-template:" + pathTemplate);
      this.pathTemplate = pathTemplate;
   }

   @JsonProperty("path-template-stripped-extensions")
   public List<String> getPathTemplateStrippedExtensions() {
      return this.pathTemplateStrippedExtensions;
   }

   public void setPathTemplateStrippedExtensions(List<String> pathTemplateStrippedExtensions) {
      //System.out.println("path-template-stripped-extensions:" + pathTemplateStrippedExtensions);
      this.pathTemplateStrippedExtensions = pathTemplateStrippedExtensions;
   }

   @JsonProperty("uses-absolute-path")
   public Boolean getUsesAbsolutePath() {
      return this.usesAbsolutePath;
   }

   public void setUsesAbsolutePath(Boolean usesAbsolutePath) {
      //System.out.println("uses-absolute-path:" + usesAbsolutePath);
      this.usesAbsolutePath = usesAbsolutePath;
   }

   @JsonProperty("value-key")
   public String getValueKey() {
      return this.valueKey;
   }

   public void setValueKey(String valueKey) {
      //System.out.println("value-key:" + valueKey);
      this.valueKey = valueKey;
   }
}
