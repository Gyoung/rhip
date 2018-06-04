package cn.com.zoesoft.rhip.code;

import com.zoe.aop.generator.GeneratorFacade;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zengjiyang on 2016/2/1.
 */
public class GeneratorTest {
    @Test
    public void test() throws Exception {
        GeneratorFacade facade = new GeneratorFacade();
        facade.getGenerator().addTemplateRootDir(new File("template"));

        facade.deleteOutRootDir();

        System.setProperty("oracle.jdbc.remarksReporting","true");

        List<String> tableNames = Arrays.asList(
                "EMR_QC_SCORE_SICK_RECORD"
        );

        tableNames.forEach(v -> {
            if (v == null || v.trim().length() == 0)
                return;
            try {
                facade.generateByTable(v);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}