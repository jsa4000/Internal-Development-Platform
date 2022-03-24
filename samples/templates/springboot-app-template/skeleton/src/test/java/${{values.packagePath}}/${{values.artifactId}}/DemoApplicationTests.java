package ${{ values.packageName }}.${{ values.artifactId }};

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ${{ values.artifactId }}ApplicationTests {

    @Test
    void contextLoads() {
    }

}
