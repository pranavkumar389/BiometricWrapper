/**
 * @author CE032
 *
 */
 
package cordova.plugin.biometric;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by SW11 on 3/4/2017.
 */
@Root(name = "Param")
public class Param {

    public Param() {}

    @Attribute(name = "name", required = false)
    public String name;

    @Attribute(name = "value", required = false)
    public String value;
}
