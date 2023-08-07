package youyihj.probezs.render;

import net.minecraft.util.Util;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import youyihj.probezs.ProbeZS;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author youyihj
 */
@Mod.EventBusSubscriber
public class RenderTaskDispatcher {
    private static final Queue<FutureTask<?>> tasks = new ArrayDeque<>();

    public static <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<>(callable);
        synchronized (tasks) {
            tasks.add(task);
            return task;
        }
    }

    @SubscribeEvent
    public static void onRenderTickStart(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            synchronized (tasks) {
                while (!tasks.isEmpty()) {
                    Util.runTask(tasks.poll(), ProbeZS.logger);
                }
            }
        }
    }
}
