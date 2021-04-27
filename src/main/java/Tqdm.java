import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/*
    代码参考https://github.com/weiyinfu/tqdm
* */
public class Tqdm<T>  implements  Iterable<T>{
    // Input
    int total; // 总个数
    int progress = -1; // 当前进度
    int nCols; // 控制台进度条宽度
    String desc; // 进度条描述
    long printIntervalInMilli = 1000; // 每隔多长时间打印一次
    Iterator<T> data; // 可迭代对象
    //State
    long lastPrintTime = 0;
    long lastProgress;
    long beginTime;
    char unitChar = '█';
    char[] chars;

    //Default
    final static int DEFAULT_NCOLS = 70;

    Tqdm(Iterator<T> a, int total, String desc, int nCols, boolean gui) {
        this.total = total;
        this.data = a;
        this.desc = desc;
        this.nCols = nCols == -1 ? DEFAULT_NCOLS : nCols;
        this.beginTime = this.lastPrintTime = System.currentTimeMillis();
        this.chars = new char[nCols];
    }

    public static <T> Tqdm<T> tqdm(List<T> a) {
        return new Tqdm<>(a.iterator(), a.size(), "", DEFAULT_NCOLS, false);
    }
    public static Tqdm<Object> tqdm(int total, String desc) {
        return new Tqdm<>(null, total, desc, DEFAULT_NCOLS, false);
    }

    public static Tqdm<Object> tqdm(int total, String desc, boolean gui) {
        return new Tqdm<>(null, total, desc, DEFAULT_NCOLS, gui);
    }

    public static <T> Tqdm<T> tqdm(Iterator<T> a, int total, String desc) {
        return new Tqdm<>(a, total, desc, DEFAULT_NCOLS, false);
    }

    public static <T> Tqdm<T> tqdm(List<T> a, String desc) {
        return new Tqdm<>(a.iterator(), a.size(), desc, DEFAULT_NCOLS, false);
    }

    public static <T> Tqdm<T> tqdm(List<T> a, boolean gui) {
        return new Tqdm<>(a.iterator(), a.size(), "", DEFAULT_NCOLS, gui);
    }

    public static <T> Tqdm<T> tqdm(List<T> a, int width) {
        return new Tqdm<>(a.iterator(), a.size(), "", DEFAULT_NCOLS, false);
    }

    public void update(int delta) {
        progress += delta;
        if (System.currentTimeMillis() - lastPrintTime > printIntervalInMilli) {
            String speedString = formatSpeed(progress, lastProgress, System.currentTimeMillis() - lastPrintTime);
            double percent = 1.0 * progress / total * 100;
            String percentString = String.format("%2d%%", (int)(percent));
            long usedTime = System.currentTimeMillis() - beginTime;
            long leftTime = (long) (usedTime * 1.0 / progress * (total - progress));
            String timeString = String.format("[%s<%s, %s]", formatTime(usedTime), formatTime(leftTime), speedString);

            StringBuilder builder = new StringBuilder("\r");
            if (desc != null) {
                builder.append(desc).append(":");
            }
            builder.append(percentString);
            int charCount = Math.min(progress * nCols / total, chars.length);
            Arrays.fill(chars, 0, charCount, unitChar);
            Arrays.fill(chars, charCount, nCols, ' ');
            builder.append('|').append(chars).append ('|');

            builder.append(String.format("%d/%d", progress, total));
            builder.append(timeString);
            System.out.print(builder.toString());
            lastPrintTime = System.currentTimeMillis();
            lastProgress = progress;
        }
    }
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                if (data == null) throw new RuntimeException("tqdm need set data");
                return data.hasNext();
            }

            @Override
            public T next() {
                update(1);
                return data.next();
            }
        };
    }

    String formatTime(long duration) {
        final int SECOND = 1000;
        final int MINUTE = 60 * SECOND;
        final int HOUR = 60 * MINUTE;
        final int DAY = 24 * HOUR;
        long day = duration / DAY;
        duration %= DAY;
        long hour = duration / HOUR;
        duration %= HOUR;
        long minute = duration / MINUTE;
        duration %= MINUTE;
        long second = duration / SECOND;
        StringBuilder builder = new StringBuilder();
        if (day > 0) {
            builder.append(String.format("%dd", day));
        }
        if (hour > 0) {
            builder.append(String.format("%dH", hour));
        }
        if (minute > 0) {
            builder.append(String.format("%dm", minute));
        }
        if (second > 0) {
            builder.append(String.format("%ds", second));
        }
        return builder.toString();
    }
    String formatSpeed(long currentProgress, long lastProgress, long duration) {
        long progress = currentProgress - lastProgress;
        if (progress == 1) {
            return String.format("%s/iter", formatTime(duration));
        } else {
            return String.format("%s iter/s", progress);
        }
    }
}
