package bupt.wspn.cache.Utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.ZipfDistribution;

@Slf4j
public class RequestUtils {
    public static double DEFAULT_ZIPF_LAMDA = Double.valueOf(PropertyUtil.getProperty("slave.DEFAULT_ZIPF_LAMDA"));
    public static int DEFAULT_RESOURCE_AMOUNT = Integer.valueOf(PropertyUtil.getProperty("slave.resourceAmount"));

    public static String getRequestId(double lamda){
        ZipfDistribution zipfDistribution = new ZipfDistribution(DEFAULT_RESOURCE_AMOUNT,lamda);
        return FilenameConvertor.toStringName(zipfDistribution.sample());
    }
}
