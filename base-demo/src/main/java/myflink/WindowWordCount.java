package myflink;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class WindowWordCount {
    public static void main(String[] args) throws Exception {
        // 获取上下文
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        /**
         * 数据源为本地socket，可通过以下命令启动
         * nc -lk 9999
         */
        DataStreamSource<String> streamSource = env
                .socketTextStream("localhost", 9999);
        DataStream<Tuple2<String, Integer>> dataStream = (DataStream<Tuple2<String, Integer>>) streamSource
                .flatMap(new Splitter())
                // 按第一个词分组
                .keyBy(0)
                // 窗口为5s
                .timeWindow(Time.seconds(5))
                // 统计
                .sum(1);

        dataStream.print();
        // 任务执行
        env.execute();
    }

    public static class Splitter implements FlatMapFunction<String, Tuple2<String, Integer>> {
        @Override
        public void flatMap(String sentence, Collector<Tuple2<String, Integer>> out) throws Exception {
            for (String word: sentence.split(" ")) {
                out.collect(new Tuple2<>(word, 1));
            }
        }
    }

}
