/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.kix;

import ec.tstoolkit.utilities.INameValidator;

/**
 *
 * @author Thomas Witthohn
 */
public class KIXNameValidator implements INameValidator {

    private final CharSequence ex;
    private final String msg;
    private boolean failed;

    public KIXNameValidator(CharSequence ex) {
        this.ex = ex;
        StringBuilder builder = new StringBuilder();
        builder.append("The name can't be empty or contain '").append(ex).append('\'');
        msg = builder.toString();
    }

    @Override
    public boolean accept(String name) {
        if(name.isEmpty()){
            failed = true;
            return !failed;
        }
        for (int i = 0; i < ex.length(); i++) {
            if (name.contains(ex.subSequence(i, i + 1))) {
                failed = true;
                return !failed;
            }
        }
        failed = false;
        return !failed;
    }

    @Override
    public String getLastError() {

        return failed ? msg : null;
    }

}
