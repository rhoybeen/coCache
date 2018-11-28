package bupt.wspn.cache.Utils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.ZipfDistribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class RequestUtils {
    public static int DEFAULT_RESOURCE_AMOUNT = Integer.valueOf(PropertyUtils.getProperty("slave.resourceAmount"));
    public static int DEFAULT_REQUEST_AMOUNT = Integer.valueOf(PropertyUtils.getProperty("slave.default_request_number"));

    public List<String> getRequestId(double lamda){
        return getRequestId(lamda,false);
    }

    public static List<String> getRequestId(double lamda, boolean transform){
        return getRequestId(lamda,transform,DEFAULT_REQUEST_AMOUNT);
    }

    public static List<String> getRequestId(double lamda, boolean transform, int count){
        final List<String> res = new ArrayList<String>();
        final Random random = new Random();
        final int pivot = transform ? random.nextInt(DEFAULT_RESOURCE_AMOUNT) + 1 : 1;
        final ZipfDistribution zipfDistribution = new ZipfDistribution(DEFAULT_RESOURCE_AMOUNT,lamda);
        for(int i=0;i<count;i++){
            int sample = zipfDistribution.sample();
            if(sample <= pivot){
                sample = pivot - sample + 1;
            }
            res.add(FilenameConvertor.toStringName(sample));
        }
        return res;
    }
}
