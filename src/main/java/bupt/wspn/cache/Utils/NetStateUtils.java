package bupt.wspn.cache.Utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NetStateUtils wraps PING to check the network availability between two hosts.
 */
@Slf4j
public class NetStateUtils {
    private final static int TIME_OUT_MS = 2000;

    public static double ping(String ipv4Addr) throws IOException {
        final String ip = ipv4Addr;
        try {
            if (!InetAddress.getByName(ip).isReachable(TIME_OUT_MS)) {
                log.info("Host "+ip+" is not reachable");
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
        final Runtime runtime = Runtime.getRuntime();
        final Process process = runtime.exec("ping " + ip + " -c 3");
        if (Objects.isNull(process)) return -1;
        final InputStream is = process.getInputStream();
        final InputStreamReader isr = new InputStreamReader(is);
        final BufferedReader br = new BufferedReader(isr);
        String line;
        double res = Double.MAX_VALUE;
        while ((line = br.readLine()) != null) {
            double tmp = getResult(line);
            if (tmp > 0) {
                //return the smallest result
                res = tmp < res ? tmp : res;
            }
        }
        res = (res == Double.MAX_VALUE) ? -1 : res;
        log.info("Ping host " + ip + " delay:"+String.valueOf(res));
        return res;
    }

    /**
     * Get delay value from ping result lines.
     *
     * @param line
     * @return
     */
    public static double getResult(String line) {
//        final String pattern = "(\\d+ms)(\\s+)(TTL=\\d+)";
        final String pattern = "(time=)([1-9]\\d*\\.?\\d*)";
        final Pattern r = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
        final Matcher matcher = r.matcher(line);
        if(matcher.find()){
            return Double.valueOf(matcher.group(2));
        } else {
            return -1;
        }
    }
}
