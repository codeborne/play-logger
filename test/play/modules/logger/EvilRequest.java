package play.modules.logger;

import play.mvc.results.Error;

/**
 * @author <a href="mailto:fivesmallq@gmail.com">fivesmallq</a>
 * @version Revision: 1.0
 * @date 2017/2/16 5:37
 */
public class EvilRequest extends Error{
    public EvilRequest(String reason) {
        super(reason);
    }

    public EvilRequest(int status, String reason) {
        super(status, reason);
    }
}
