package fr.insalyon.creatis.moteurlite.boutiquesParser;

/**
 * 
 * @author Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class InvalidBoutiquesDescriptorException extends Exception {
   public InvalidBoutiquesDescriptorException(String message) {
      super(message);
   }

   public InvalidBoutiquesDescriptorException(String message, Throwable parent) {
      super(message, parent);
   }
}
