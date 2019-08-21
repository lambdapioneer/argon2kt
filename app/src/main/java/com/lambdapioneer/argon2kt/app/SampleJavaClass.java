package com.lambdapioneer.argon2kt.app;

import androidx.annotation.Keep;
import com.lambdapioneer.argon2kt.Argon2Kt;
import com.lambdapioneer.argon2kt.Argon2KtResult;
import com.lambdapioneer.argon2kt.Argon2Mode;

@Keep
public class SampleJavaClass {

    @Keep
    public void usingArgon2KtFromJava(String password, String salt) {
        final Argon2Kt argon2Kt = new Argon2Kt();

        final Argon2KtResult hashResult = argon2Kt.hash(Argon2Mode.ARGON2_I, password.getBytes(), salt.getBytes());
        final String encodedOutput = hashResult.encodedOutputAsString();

        final boolean verificationResult = argon2Kt.verify(Argon2Mode.ARGON2_I, encodedOutput, password.getBytes());
        System.out.println("verification result: " + verificationResult);
    }

}
