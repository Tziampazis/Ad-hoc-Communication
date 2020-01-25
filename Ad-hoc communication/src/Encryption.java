
/**
 * This applies a simple XOR encryption
 * @author Yannis
 *
 */

public class Encryption {
    static final String key = "isdhfoiehsfoewdjfoewjfoewisjweofweksfewksdferkgorekfotkdflkgdl";
    
    public static int[] convertToIntArray(byte[] input) {
        int[] ret = new int[input.length];
        for (int i = 0; i < input.length; i++)
        {
            ret[i] = input[i] & 0xff; // Range 0 to 255, not -128 to 127
        }
        return ret;
    }

    public static byte[] encrypt(String str) {
        byte[] output = new byte[str.length()];
        for(int i = 0; i < str.length(); i++) {
            Integer o = ((Integer.valueOf(str.charAt(i)) ^ Integer.valueOf(key.charAt(i % (key.length() - 1)))) + '0');
            byte b = o.byteValue();
            output[i] = b;
        }
        return output;        
    }

    public static String decrypt(byte[] encrypted) {
        String output = ""; 
        
        int[] intEncrypted = convertToIntArray(encrypted);
        
        for(int i = 0; i < intEncrypted.length; i++) {
            output += (char) ((intEncrypted[i] - 48) ^ (int) key.charAt(i % (key.length() - 1)));
        }
        return output;
    }
	
}
