package bupt.wspn.cache.Utils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.ZipfDistribution;

import java.util.Random;

@Slf4j
public class RequestUtils {
    public static double DEFAULT_ZIPF_LAMDA = Double.valueOf(PropertyUtils.getProperty("slave.DEFAULT_ZIPF_LAMDA"));
    public static int DEFAULT_RESOURCE_AMOUNT = Integer.valueOf(PropertyUtils.getProperty("slave.resourceAmount"));

    public static String getRequestId(@NonNull double lamda){
        return getRequestId(lamda,false);
    }

    public static String getRequestId(@NonNull double lamda, boolean transform){
        final Random random = new Random();
        final int pivot = transform ? random.nextInt(DEFAULT_RESOURCE_AMOUNT) + 1 : 1;
        final ZipfDistribution zipfDistribution = new ZipfDistribution(DEFAULT_RESOURCE_AMOUNT,lamda);
        int sample = zipfDistribution.sample();
        if(sample < pivot){
            sample = pivot - sample + 1;
        }
        return FilenameConvertor.toStringName(sample);
    }
}
