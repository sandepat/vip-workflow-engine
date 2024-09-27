package fr.insalyon.creatis.moteurlite;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 
 * @author Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class CreateInvocation {

    public static String convertMapToJson(Map<String, String> map1, Map<String, String> map2) throws URISyntaxException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();

        for (Map.Entry<String, String> entry : map1.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String type = map2.get(key);

            if ("Integer".equals(type) || "Number".equals(type)) {
                jsonNode.put(key, Integer.parseInt(value));
            } else if ("Flag".equals(type)) {
                jsonNode.put(key, Boolean.parseBoolean(value));
            } else if ("File".equals(type)) {
                value = new File(System.getProperty("user.dir")).toURI().relativize(new File(value).toURI()).getPath();
                URI uri = new URI(value);
                String basename = Paths.get(uri.getPath()).getFileName().toString();
                jsonNode.put(key, basename);
            }
            else {
                URI uri = new URI(value);
                String basename = Paths.get(uri.getPath()).getFileName().toString();
                jsonNode.put(key, basename);
            }
        }
        return jsonNode.toString();
    }
}
