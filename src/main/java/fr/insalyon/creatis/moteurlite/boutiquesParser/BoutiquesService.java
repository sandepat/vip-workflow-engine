package fr.insalyon.creatis.moteurlite.boutiquesParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insalyon.creatis.gasw.GaswException;

/**
 * 
 * Author: Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class BoutiquesService {
    public BoutiquesService() {
    }

    public BoutiquesEntities parseFile(String boutiquesDescriptorString) throws FileNotFoundException, IOException,
            InvalidBoutiquesDescriptorException, GaswException, ParseException {
        Object object = new JSONParser().parse(new FileReader(boutiquesDescriptorString));
        String objeString = String.valueOf(object);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(objeString, BoutiquesEntities.class);
    }

    public String getNameOfBoutiquesFile(BoutiquesEntities boutiquesEntities) {
        return boutiquesEntities.getName() + ".json";
    }

    public String getApplicationName(BoutiquesEntities boutiquesEntities) {
        String nameOfBoutiquesFile = getNameOfBoutiquesFile(boutiquesEntities);
        return nameOfBoutiquesFile.substring(0, nameOfBoutiquesFile.lastIndexOf('.'));
    }

    public HashMap<Integer, String> getInputIdOfBoutiquesFile(BoutiquesEntities boutiquesEntities) {
        return boutiquesEntities.getInputId();
    }

    public HashMap<Integer, String> getOutputIdOfBoutiquesFile(BoutiquesEntities boutiquesEntities) {
        return boutiquesEntities.getOutputId();
    }

    public HashMap<String, String> getInputTypeOfBoutiquesFile(BoutiquesEntities boutiquesEntities) {
        return boutiquesEntities.getInputTypes();
    }

    public HashMap<String, String> getInputValueKeyOfBoutiquesFile(BoutiquesEntities boutiquesEntities) {
        return boutiquesEntities.getInputValueKey();
    }

    public Set<String> getInputOptionalOfBoutiquesFile(BoutiquesEntities boutiquesEntities) {
        return boutiquesEntities.getInputOptional();
    }

    public HashMap<String, String> getOutputPathTemplateOfBoutiquesFile(BoutiquesEntities boutiquesEntities) {
        return boutiquesEntities.getOutputPathTemplateList();
    }

    public Set<String> getCrossMap(BoutiquesEntities boutiquesEntities) {
        return boutiquesEntities.getCrossMap();
    }

    public Set<String> getDotMap(BoutiquesEntities boutiquesEntities) {
        return boutiquesEntities.getDotMap();
    }
}