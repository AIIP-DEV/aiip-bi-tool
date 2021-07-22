package com.sk.bds.datainsight.echart;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.sk.bds.datainsight.database.model.Chart;
import com.sk.bds.datainsight.service.ChartService;
import com.sk.bds.datainsight.util.Constants;
import lombok.Data;
import lombok.Getter;

import java.text.SimpleDateFormat;
import java.util.*;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PolarTwoValueAxes {
    private String title;
    private Map<String, Object> options;
    private List<Map<String, Object>> rawData; // db data

    public PolarTwoValueAxes(Map<String, Object> reqOption, List<Map<String, Object>> rawData) {
        this.options = reqOption;
        this.rawData = rawData;
    };

    public Object getChartOption() {
        return new ChartDrawInfo();
    }


    @Data
    private class ChartDrawInfo {
        private LegendObject legend = new LegendObject(options);
        private PolarObject polar = new PolarObject(options);
        private TooltipObject tooltip = new TooltipObject(options);
        private ArrayList<SeriesOption> series = new SeriesSection().getSeriesOptions();
        private AngleAxisObject angleAxis = new AngleAxisObject(options);
        private RadiusAxisObject radiusAxis = new RadiusAxisObject(options);
        private AnimationDurationOption animationDuration = new AnimationDurationOption(options);
//        private TitleObject title = new TitleObject(options);



    }

    @Getter
    public class SeriesSection {
        ArrayList<SeriesOption> seriesOptions = new ArrayList<>();

        public SeriesSection() {
            SeriesOption seriesOption = new SeriesOption(rawData);

            this.seriesOptions.add(seriesOption);
        }

        public ArrayList<SeriesOption> getSeriesOptions() {
            return seriesOptions;
        }
    }


    @Getter
    public class TitleObject {
        /*title: {
            text: 'title'
        }*/
        public String text = "";

        public TitleObject(Map<String, Object> options) {
            Map<String, Object> titleJson = (Map<String, Object>) options.get("title");
            this.text = (String) titleJson.get("text");
        }
    }

    @Getter
    public class LegendObject {
        /*legend: {
            data: ['line']
        }*/
        List<String> data = new ArrayList<>();

        public LegendObject(Map<String, Object> options) {
            Map<String, Object> objectMap = (Map<String, Object>) options.get("legend");
            data.add("line"); //  현재 프런트에서 legend.data가 넘어오지 않음 기본값으로 설정
        }
    }

    @Getter
    public class PolarObject {
        List<String> center = new ArrayList<>();
        public PolarObject(Map<String, Object> options) {
            Map<String, Object> objectMap = getObjectMapFrom(options, "legend");
            center.add("50%");
            center.add("50%");
        }
    }

    private Map<String, Object> getObjectMapFrom(Map<String, Object> options, String key) {
        return (Map<String, Object>) options.get(key);
    }

    @Getter
    public class TooltipObject {
        /*tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross'
            }
        }*/
        String trigger = "axis";
        AxisPointerObject axisPointer;
        public TooltipObject(Map<String, Object> options) {
            Map<String, Object> objectMap = (Map<String, Object>) options.get("tooltip");
            this.axisPointer = new AxisPointerObject(options);
        }
    }

    private class AxisPointerObject {
        // type
        public String type = "cross";

        public AxisPointerObject(Map<String, Object> options) {
            Map<String, Object> objectMap = (Map<String, Object>) options.get("tooltip");
            this.type = "cross";
        }
    }

    @Getter
    public class AngleAxisObject {
        /*angleAxis: {
            type: 'value',
            startAngle: 0
        }*/
        private String type = "value";
        private int startAngle = 0;
        public AngleAxisObject(Map<String, Object> options) {
            Map<String, Object> objectMap = (Map<String, Object>) options.get("angleAxis");
        }
    }

    @Getter
    public class RadiusAxisObject {
        /*radiusAxis: {
            min: 0
        }*/
        private int min = 0;
        public RadiusAxisObject(Map<String, Object> options) {
            Map<String, Object> objectMap = (Map<String, Object>) options.get("radiusAxis");
        }
    }

    @Getter
    public class SeriesOption {
        /*series: [{
            coordinateSystem: 'polar',
            name: 'line',
            type: 'line',
            showSymbol: false,
            data: data
        }]*/
        String coordinateSystem = "polar";
        String name = "line";
        String type = "line";
        boolean showSymbol = false;
        List<List<Object>> data;

        public SeriesOption(List<Map<String, Object>> rawData) {
            this.data = makeData(rawData);
        }
    }

    private List<List<Object>> makeData(List<Map<String, Object>> rawData) {
        List<List<Object>> data = new ArrayList<>();
        for (Map<String, Object> item: rawData) {
            List<Object> child = new ArrayList<>();
            child.add(item.get("category"));
            child.add(item.get("count"));
            data.add(child);

        }
        return data;
    }

    @Getter
    public class AnimationDurationOption {
        int animationDuration = 2000;
        public AnimationDurationOption(Map<String, Object> options) {

        }
    }
}
