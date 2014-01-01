package info.ralab.uxdf.instance;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class IdMakerTest {

    @Test
    public void testNextId() throws InterruptedException {
        final int n = 100;
        int t = 10;
        final int idSize = t * n;
        ExecutorService executorService = Executors.newFixedThreadPool(t);
        Set<String> idSet = Sets.newConcurrentHashSet();

        while (t > 0) {
            t--;
            executorService.execute(() -> {
                int i = 0;
                while (i < n) {
                    i++;

                    idSet.add(IdMaker.next());
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);

        Assert.assertFalse(idSet.isEmpty());
        Assert.assertEquals(idSize, idSet.size());

        System.out.println(idSet.iterator().next());

        AtomicBoolean effective = new AtomicBoolean(true);
        idSet.forEach(id -> effective.compareAndSet(!IdMaker.effective(id), false));

        Assert.assertTrue(effective.get());
    }

    public static void main(String[] args) {
        System.out.println(IdMaker.effective("000000000000000000000008c"));
    }
}
