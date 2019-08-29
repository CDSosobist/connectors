package CDSOsobist.connectors.rest.ones.ZUP2;

import com.ibm.icu.text.Transliterator;

import java.util.List;

@SuppressWarnings("unused")
class Translit {

    private static final String CYR_TO_LAT = "Cyrillic-Latin";
    public static final String LAT_TO_CYR = "Latin-Cyrillic";

    public static String transliterate(String args) {
        Transliterator toLatinTrans = Transliterator.getInstance(CYR_TO_LAT);
        String result = toLatinTrans.transliterate(String.valueOf(args));
        System.out.println(result);
        return result;
    }

    public static void transliterate(List<String[]> args) {
        Transliterator toLatinTrans = Transliterator.getInstance(CYR_TO_LAT);
        String result = toLatinTrans.transliterate(String.valueOf(args));
        System.out.println(result);
    }

}
