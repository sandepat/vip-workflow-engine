package fr.insalyon.creatis.moteurlite.boutiquesParser;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BoutiquesObjectParser {

    public BoutiquesObjectParser(List<Map<String, Object>> inputs, Class BoutiquesClass)
    {
                    ObjectMapper mapper = new ObjectMapper();
            try {
                String jsonArray = mapper.writeValueAsString(inputs);
                jsonArray = jsonArray.substring(1, jsonArray.length()-1);
                //jsonArray = jsonArray.replaceAll("\\[", "").replaceAll("\\]","");

                //JavaType javaType = mapper.getTypeFactory().constructParametricType(JsonResponse.class, User.class);
                mapper.readValue(jsonArray, BoutiquesClass); 

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } 
            catch (Exception e) {
                e.printStackTrace();
            }
    }
}