package poisontrigger.kteams.util;

public final class Base62 {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = ALPHABET.length();

    /** Hex (e.g. "9199eaf59d0c") -> base62 (letters+digits). */
    public static String hexToBase62(String hex) {
        if (hex == null || hex.isEmpty()) return String.valueOf(ALPHABET.charAt(0));
        hex = hex.startsWith("0x") || hex.startsWith("0X") ? hex.substring(2) : hex;
        // BigInteger handles arbitrary length
        java.math.BigInteger n = new java.math.BigInteger(hex, 16);
        if (n.signum() == 0) return String.valueOf(ALPHABET.charAt(0));
        StringBuilder sb = new StringBuilder();
        while (n.signum() > 0) {
            java.math.BigInteger[] dr = n.divideAndRemainder(java.math.BigInteger.valueOf(BASE));
            n = dr[0];
            int rem = dr[1].intValue();
            sb.append(ALPHABET.charAt(rem));
        }
        return sb.reverse().toString();
    }

    public static String base62ToHex(String b62) {
        if (b62 == null || b62.isEmpty()) return "0";
        java.math.BigInteger n = java.math.BigInteger.ZERO;
        for (int i = 0; i < b62.length(); i++) {
            int idx = ALPHABET.indexOf(b62.charAt(i));
            if (idx < 0) throw new IllegalArgumentException("Invalid base62 char: " + b62.charAt(i));
            n = n.multiply(java.math.BigInteger.valueOf(BASE)).add(java.math.BigInteger.valueOf(idx));
        }
        return n.toString(16);
    }
}
