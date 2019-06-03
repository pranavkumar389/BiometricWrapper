/**
 * @author CE032
 *
 */
 
package cordova.plugin.biometric;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "Opts")
public class Opts {

    public Opts() {}

    @Attribute(name = "fCount", required = false)
    public String fCount;

    @Attribute(name = "fType", required = false)
    public String fType;

    @Attribute(name = "iCount", required = false)
    public String iCount;

    @Attribute(name = "iType", required = false)
    public String iType;

    @Attribute(name = "pCount", required = false)
    public String pCount;

    @Attribute(name = "pType", required = false)
    public String pType;

    @Attribute(name = "format", required = false)
    public String format;

    @Attribute(name = "pidVer", required = false)
    public String pidVer;

    @Attribute(name = "timeout", required = false)
    public String timeout;

    @Attribute(name = "otp", required = false)
    public String otp;

    @Attribute(name = "wadh", required = false)
    public String wadh;

    @Attribute(name = "posh", required = false)
    public String posh;

    @Attribute(name = "env", required = false)
    public String env; // S == Stage, P == Prod, PP == PreProd

}
