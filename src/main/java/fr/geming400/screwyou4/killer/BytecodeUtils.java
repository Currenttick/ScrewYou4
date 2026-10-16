package fr.geming400.screwyou4.killer;

public class BytecodeUtils {

    public static short getParametersSize(String desc, boolean longDoubleAdd) {
        short parametersSize = 0;
        boolean inParameterType = false;
        boolean inArray = false;
        for (char c : desc.toCharArray()) {
            if (c == ';') {
                inParameterType = false;
                inArray = false;
            }
            if (c == '[') {
                inArray = true;
                continue;
            }
            if (inParameterType) {
                continue;
            }
            if ("BCDFIJSZ".contains(String.valueOf(c))) {
                parametersSize++;
                if (longDoubleAdd && !inArray && (c == 'D' || c == 'J')) {
                    parametersSize++;
                }
                inArray = false;
            }
            if (c == 'L') {
                parametersSize++;
                inParameterType = true;
            }
        }
        return parametersSize;
    }

}
