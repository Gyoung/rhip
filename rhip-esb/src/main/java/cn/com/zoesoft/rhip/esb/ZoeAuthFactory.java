package cn.com.zoesoft.rhip.esb;

import cn.com.zoe.crypto.CryptoUtil;
import cn.com.zoesoft.util.JsonUtil;
import cn.com.zoesoft.util.StringUtil;
import com.zoe.phip.ssp.model.synapse.ZoeAuthToken;

import java.util.Base64;
import java.util.Date;

/**
 * Created by evinl on 2016-09-08.
 */
public class ZoeAuthFactory {
    public static void main(String... args) throws Exception {
        final String publicKey = "MIGdMA0GCSqGSIb3DQEBAQUAA4GLADCBhwKBgQChl8ye7mSd+0dvJ5Xp5BFqEub9577o50FAuMP8BA1uvS8qfKrpB26MsJizTpcBIqMAcHzaI4BVC0bQChc5GPVSVbZIQTJv9PXjTTeer3tH/j4y8UQrcS6QVb8z0hyrHdvZBHYF0lN14xUgGRBXqgTQJGtV5/USrFBNXli1EkIs7wIBEQ==";
        final byte[] publicKeyBuffer = Base64.getDecoder().decode(publicKey);
        ZoeAuthToken token = new ZoeAuthToken();
        token.setCreatetime(new Date().getTime());
        token.setDecryptkey(null);
        token.setLicence(args[0]);
        final String tokenSource = JsonUtil.object2Json(token);
        final String cryptoTarget = CryptoUtil.CryptDataWithRSA(tokenSource, publicKeyBuffer);
        System.out.println(cryptoTarget);
    }
}