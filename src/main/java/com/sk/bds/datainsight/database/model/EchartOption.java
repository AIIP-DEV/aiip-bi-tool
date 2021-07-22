package com.sk.bds.datainsight.database.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Arrays;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class EchartOption {

    private final String emptyValue = "";

    @Getter
    @AllArgsConstructor
    public enum ChartType {
        TABLE(0, "TABLE"),
        TABLE_SUM(1, "TABLE_SUM"),
        BAR(2, "BAR"),
        COLUMN(3, "COLUMN"),
        LINE(4, "LINE"),
        AREA(5, "AREA"),
        PIE(6, "PIE"),
        FUNNEL(7, "FUNNEL"),
        RADAR(8, "RADAR"),
        POLAR(9, "POLAR");

        private final int type;
        private final String name;
    }




//    // TODO define chart type
    public static final int TABLE = 0;
    public static final int TABLE_SUM = 1;
    public static final int BAR = 2;
    public static final int COLUMN = 3;
    public static final int LINE = 4;
    public static final int AREA = 5;
    public static final int PIE = 6;
    public static final int FUNNEL = 7;
    public static final int RADAR = 8;
    public static final int POLAR = 9;

    public EchartOption() {
    }

    public Object getChartOption(int index) {

        switch (index) {
            case TABLE:
                return new EchartOption.TableChartOption();
            case TABLE_SUM:
                return new EchartOption.TableSumChartOption();
            case BAR:
                return new EchartOption.BarChartOption();
            case COLUMN:
                return new EchartOption.ColumnChartOption();
            case LINE:
                return new EchartOption.LineChartOption();
            case AREA:
                return new EchartOption.AreaChartOption();
            case PIE:
                return new EchartOption.PieChartOption();
            case FUNNEL:
                return new EchartOption.FunnelChartOption();
            case RADAR:
                return new EchartOption.RadarChartOption();
            case POLAR:
                return new EchartOption.PolarChartOption();
        }
        return null;
    }

    // common
    @Data
    public class CommonSection {
        public ChartTextStyle textStyle = new ChartTextStyle("12");
        public ChartTitle title = new ChartTitle();
        public BackgroundColorOption backgroundColor = new BackgroundColorOption("string", "colorpicker", "#FFFFFFFF", "#FFFFFFFF");
        public ColorWithValueOption color = new ColorWithValueOption("Array", "input", Arrays.asList("#5470C6", "#91CC75", "#FAC858", "#EE6666", "#73C0DE", "#3BA2720", "#FC8452", "#9A60B4", "#EA7CCC"), Arrays.asList("#5470C6", "#91CC75", "#FAC858", "#EE6666", "#73C0DE", "#3BA2720", "#FC8452", "#9A60B4", "#EA7CCC"));
        public LegendOption legend = new LegendOption();
        public TooltipOption tooltip = new TooltipOption();
        //    "value": ["axis", "value", "group", "line"] // 모든 차트에 공통으로 들어감.
        public List<String> value = Arrays.asList("axis", "value", "group", "line");

    }

    // for chart options
    @Data
    public class TableChartOption extends CommonSection {
        public RecordsOption records = new RecordsOption();
        public ReverseOuterOption reverse = new ReverseOuterOption();
    }

    @Data
    public class TableSumChartOption extends CommonSection {
        public OrderByOuterOption orderby = new OrderByOuterOption();
        public ReverseOuterOption reverse = new ReverseOuterOption();
    }

    @Data
    public class BarChartOption extends CommonSection {
        public GridOption grid = new GridOption();
        @JsonProperty("xAxis")
        public XAxisOption xaxis = new XAxisOption();
        @JsonProperty("yAxis")
        public YAxisOption yaxis = new YAxisOption("category");
        public SeriesFroBarOpiton series = new SeriesFroBarOpiton();
    }

    @Data
    public class LineChartOption extends CommonSection {
        public GridOption grid = new GridOption();
        @JsonProperty("xAxis")
        public XAxisOption xaxis = new XAxisOption();
        @JsonProperty("yAxis")
        public YAxisOption yaxis = new YAxisOption("value");
        public SeriesOpiton series = new SeriesOpiton();
    }

    @Data
    public class ColumnChartOption extends CommonSection {
        public GridOption grid = new GridOption();
        @JsonProperty("xAxis")
        public XAxisOption xaxis = new XAxisOption();
        @JsonProperty("yAxis")
        public YAxisOption yaxis = new YAxisOption("value");
    }

    @Data
    public class AreaChartOption extends CommonSection {
        public SeriesForAreaOpiton series = new SeriesForAreaOpiton();
    }

    @Data
    public class PieChartOption extends CommonSection {
        public SeriesForPieOption series = new SeriesForPieOption();
    }

    @Data
    public class FunnelChartOption extends CommonSection {
        public SeriesForFunnelOption series = new SeriesForFunnelOption();
    }

    @Data
    public class RadarChartOption extends CommonSection {
        public SeriesForRadaarOption series = new SeriesForRadaarOption();
        public RadarOption radar = new RadarOption();
    }

    @Data
    public class PolarChartOption extends CommonSection {
        //        public PolarOption polar = new PolarOption();
        public AngleAxisOption angleAxis = new AngleAxisOption();
        public RadiusAxisOption radiusAxis = new RadiusAxisOption();
//        public SeriesForPolarOpiton series = new SeriesForPolarOpiton();
    }


///////////////////////////////////////////////////////
// chart sub option
///////////////////////////////////////////////////////

    @Data
    public class ChartTextStyle {
        @JsonIgnore
        String defaultFontSize;
        ColorOption color;
        FontSizeOption fontSize;
        FontStyleOption fontStyle;
        AlignOption align;
        FontWeightOption fontWeight;
        //        FontFamilyOption fontFamily;
        OverflowOption overflow;


        public ChartTextStyle(String defaultFontSize) {
            this.defaultFontSize = defaultFontSize;
            color = new ColorOption("string", "colorpicker", emptyValue, "#333333FF");
            fontSize = new FontSizeOption("number", "input", "12", this.defaultFontSize);
            fontStyle = new FontStyleOption("string", "radio", Arrays.asList("normal", "italic", "oblique"), "normal");
            align = new AlignOption("string", "radio", Arrays.asList("left", "center", "right"), "left");
            fontWeight = new FontWeightOption("string", "input", emptyValue, "normal");
//            fontFamily = new FontFamilyOption("string", "input", emptyValue, "normal");
            overflow = new OverflowOption("string", "fixed", "none", "none");
        }


    }

    @Data
    public class ParamOption {
        String dataFormats;
        String htmlElements;
        List<String> values;
        Object defaultValue;

        ParamOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            this.dataFormats = dataFormats;
            this.htmlElements = htmlElements;
            this.values = values;
            this.defaultValue = defaultValue;
        }

        ParamOption(String dataFormats, String htmlElements, List<String> values, List<String> defaultValue) {
            this.dataFormats = dataFormats;
            this.htmlElements = htmlElements;
            this.values = values;
            this.defaultValue = defaultValue;
        }
    }

    @Data
    public class ParamStringOption {
        String dataFormats;
        String htmlElements;
        String value;
        Object defaultValue;

        ParamStringOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            this.dataFormats = dataFormats;
            this.htmlElements = htmlElements;
            this.value = value;
            this.defaultValue = defaultValue;
        }

        ParamStringOption(String dataFormats, String htmlElements, String value, boolean defaultValue) {
            this.dataFormats = dataFormats;
            this.htmlElements = htmlElements;
            this.value = value;
            this.defaultValue = defaultValue;
        }
    }

    public class ColorOption extends ParamStringOption {
        ColorOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }


    public class FontStyleOption extends ParamOption {
        FontStyleOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class FontWeightOption extends ParamStringOption {
        FontWeightOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class FontFamilyOption extends ParamStringOption {
        FontFamilyOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class FontSizeOption extends ParamStringOption {
        FontSizeOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    @Data
    public class ChartTitle {
        public LeftOption left = new LeftOption("string", "input", emptyValue, "auto");
        public TopOption top = new TopOption("string", "input", emptyValue, "auto");
        public RightOption right = new RightOption("string", "input", emptyValue, "auto");
        public BottomOption bottom = new BottomOption("string", "input", emptyValue, "auto");
        public SubTextOption subtext = new SubTextOption("string", "input", emptyValue, "sub title");
        public TextAlignOption textAlign = new TextAlignOption("string", "dropdown", Arrays.asList("auto", "left", "right", "center"), "auto");
        public ShowOption show = new ShowOption("boolean", "checkbox", emptyValue, false);
        public SubTargetOption subtarget = new SubTargetOption("string", "radio", Arrays.asList("self", "blank"), "self");
        public TextOption text = new TextOption("string", "input", emptyValue, "Untitle");
        public PaddingOption padding = new PaddingOption("number", "input", emptyValue, "10");
        public ChartTextStyle textStyle = new ChartTextStyle("30");
        public SubTextStyle subtextStyle = new SubTextStyle("");
        public TargetOption target = new TargetOption("string", "radio", Arrays.asList("self", "blank"), "self");

//        public LinkOption link = new LinkOption("string", "input", emptyValue, emptyValue);
//        public SubLinkOption sublink = new SubLinkOption("string", "input", emptyValue, emptyValue);
    }

    public class ShowOption extends ParamStringOption {
        ShowOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }

        ShowOption(String dataFormats, String htmlElements, String value, boolean defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class TextOption extends ParamStringOption {
        TextOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class LinkOption extends ParamStringOption {
        LinkOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class TargetOption extends ParamOption {
        TargetOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class SubTextOption extends ParamStringOption {
        SubTextOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class SubLinkOption extends ParamStringOption {
        SubLinkOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class SubTargetOption extends ParamOption {
        SubTargetOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class SubTextStyle {
        String defaultColor = "#333333FF";

        public SubTextStyle(String defaultColor) {
            this.defaultColor = defaultColor;
        }

        public ColorOption color = new ColorOption("string", "colorpicker", emptyValue, this.defaultColor);
        public FontStyleOption fontStyle = new FontStyleOption("string", "radio", Arrays.asList("normal", "italic", "oblique"), "normal");
        public FontWeightOption fontWeight = new FontWeightOption("string", "input", emptyValue, "normal");
        //        public FontFamilyOption fontFamily = new FontFamilyOption("string", "input", emptyValue, "normal");
        public FontSizeOption fontSize = new FontSizeOption("number", "input", "12", "12");
        public AlignOption align = new AlignOption("string", "radio", Arrays.asList("left", "center", "right"), "left");
    }

    public class AlignOption extends ParamOption {
        AlignOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class TextAlignOption extends ParamOption {
        TextAlignOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class BackgroundColorOption extends ParamStringOption {
        BackgroundColorOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class LegendOption {
        public TypeForSeriesOption type = new TypeForSeriesOption("string", "fixed", "plain", "plain");
        public ItemGapOption itemGap = new ItemGapOption("number", "input", emptyValue, "10");
        public OrientOption orient = new OrientOption("string", "radio", Arrays.asList("horizontal", "vertical"), "horizontal");
        public SubTextStyle subTextStyle = new SubTextStyle("#333333FF");
        public BottomOption bottom = new BottomOption("string", "input", emptyValue, "true");
        public ItemHeightOption itemHeight = new ItemHeightOption("number", "input", emptyValue, "14");
        public ShowOption show = new ShowOption("boolean", "checkbox", emptyValue, false);
        public IconOption icon = new IconOption("string", "dropdown", Arrays.asList("circle", "rect", "roundRect", "triangle", "diamond", "pin", "arrow", "none"), "circle");
        public RightOption right = new RightOption("string", "input", emptyValue, "0");
        public AlignOption align = new AlignOption("string", "radio", Arrays.asList("auto", "left", "right"), "auto");
        public TopOption top = new TopOption("string", "input", emptyValue, "auto");
        public LeftOption left = new LeftOption("string", "input", emptyValue, "auto");
        public FormatterOption formatter = new FormatterOption("string", "input", emptyValue, "{name}");
        public SelectedModeOption selectedMode = new SelectedModeOption("string", "checkbox", emptyValue, true);
        public WidthOption width = new WidthOption("string", "input", emptyValue, "200");
        public ItemWidthOption itemWidth = new ItemWidthOption("number", "input", emptyValue, "15");
        public HeightOption height = new HeightOption("string", "input", emptyValue, "200");
    }

    public class LeftOption extends ParamStringOption {
        LeftOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class TopOption extends ParamStringOption {
        TopOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class RightOption extends ParamStringOption {
        RightOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class BottomOption extends ParamStringOption {
        BottomOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class WidthOption extends ParamStringOption {
        WidthOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class HeightOption extends ParamStringOption {
        HeightOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class OrientOption extends ParamOption {
        OrientOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class ItemGapOption extends ParamStringOption {
        ItemGapOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class ItemWidthOption extends ParamStringOption {
        ItemWidthOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class ItemHeightOption extends ParamStringOption {
        ItemHeightOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class IconOption extends ParamOption {
        IconOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class TooltipOption {
        public FormatterOption formatter = new FormatterOption("string", "input", emptyValue, "{b0}: {c0}");
        public PaddingOption padding = new PaddingOption("number", "input", emptyValue, "10");
        public BackgroundColorOption backgroundColor = new BackgroundColorOption("string", "colorpicker", "#FFFFFFFF", "#FFBAF50F");
        public BoardColorOption borderColor = new BoardColorOption("string", "colorpicker", emptyValue, "#333333FF");
        public TriggerOnOption triggerOn = new TriggerOnOption("boolean", "dropdown", Arrays.asList("mousemove", "click", "mousemove", "click", "none"), "mousemove");
        public ShowContentOption showContent = new ShowContentOption("boolean", "checkbox", emptyValue, "true");
        public BoardWidthOption borderWidth = new BoardWidthOption("number", "input", emptyValue, "0");
        public ShowOption show = new ShowOption("boolean", "checkbox", emptyValue, false);
        public TriggerOption trigger = new TriggerOption("string", "radio", Arrays.asList("item", "axis", "none"), "item");
        public ChartTextStyle textStyle = new ChartTextStyle("12");
        public OrderOption order = new OrderOption("string", "dropdown", Arrays.asList("seriesAsc", "seriesDesc", "valueAsc", "valueDesc"), "seriesAsc");
    }

    public class TriggerOption extends ParamOption {
        TriggerOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class ShowContentOption extends ParamStringOption {
        ShowContentOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class TriggerOnOption extends ParamOption {
        TriggerOnOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class FormatterOption extends ParamStringOption {
        FormatterOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class BoardColorOption extends ParamStringOption {
        BoardColorOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class BoardWidthOption extends ParamStringOption {
        BoardWidthOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class PaddingOption extends ParamStringOption {
        PaddingOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class RecordsOption {
        public LimitCountOption limitCount = new LimitCountOption("number", "input", emptyValue, emptyValue);
    }

    public class LimitCountOption extends ParamStringOption {
        LimitCountOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class ReverseOuterOption {
        public ReverseOption reverse = new ReverseOption("boolean", "checkbox", emptyValue, emptyValue);
    }

    public class ReverseOption extends ParamStringOption {
        ReverseOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class OrderByOuterOption {
        public OrderByOption orderby = new OrderByOption("string", "dropdown", emptyValue, emptyValue);
    }

    public class OrderByOption extends ParamStringOption {
        OrderByOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class GridOption {
        public ShowOption show = new ShowOption("boolean", "checkbox", emptyValue, false);
        public LeftOption left = new LeftOption("string", "input", emptyValue, emptyValue);
        public TopOption top = new TopOption("string", "input", emptyValue, emptyValue);
        public RightOption right = new RightOption("string", "input", emptyValue, emptyValue);
        public BottomOption bottom = new BottomOption("string", "input", emptyValue, "true");
        public WidthOption width = new WidthOption("string", "input", emptyValue, emptyValue);
        public HeightOption height = new HeightOption("string", "input", emptyValue, emptyValue);
        public BackgroundColorOption backgroundColor = new BackgroundColorOption("string", "colorpicker", "#FFFFFFFF", "#FFFFFFFF");
        public BoardColorOption borderColor = new BoardColorOption("string", "colorpicker", emptyValue, emptyValue);
        public BoardWidthOption borderWidth = new BoardWidthOption("number", "input", emptyValue, emptyValue);
    }

    public class XAxisOption {
        public ShowOption show = new ShowOption("boolean", "checkbox", emptyValue, false);
        public PositionOption position = new PositionOption("string", "radio", Arrays.asList("top", "bottom"), "top");
        public OffsetOption offset = new OffsetOption("number", "input", emptyValue, emptyValue);
        public TypeOption type = new TypeOption("string", "dropdown", Arrays.asList("value", "category", "time", "log"), "category");
        public NameOption name = new NameOption("string", "input", emptyValue, emptyValue);
        public NameLocationOption nameLocation = new NameLocationOption("string", "radio", Arrays.asList("start", "center", "end"), "start");
        public NameTextStyleOption nameTextStyle = new NameTextStyleOption();
        public NameGapOption nameGap = new NameGapOption("number", "input", emptyValue, emptyValue);
        public NameRotateOption nameRotate = new NameRotateOption("number", "input", emptyValue, emptyValue);
        public MinOption min = new MinOption("number", "input", emptyValue, emptyValue);
        public MaxOption max = new MaxOption("number", "input", emptyValue, emptyValue);
        public SplitNumberOption splitNumber = new SplitNumberOption("number", "input", emptyValue, emptyValue);
        public LogBaseOption logBase = new LogBaseOption("number", "input", emptyValue, emptyValue);
        public AxisLineOption axisLine = new AxisLineOption();
        public AxisLabelOption axisLabel = new AxisLabelOption();
    }

    public class PositionOption extends ParamOption {
        PositionOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class OffsetOption extends ParamStringOption {
        OffsetOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class TypeOption extends ParamOption {
        TypeOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class NameOption extends ParamStringOption {
        NameOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class NameLocationOption extends ParamOption {
        NameLocationOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class NameTextStyleOption {
        public ColorOption color = new ColorOption("string", "colorpicker", emptyValue, "#5470C6");
        public FontStyleOption fontStyle = new FontStyleOption("string", "radio", Arrays.asList("normal", "italic", "oblique"), "normal");
        public FontWeightOption fontWeight = new FontWeightOption("string", "input", emptyValue, "normal");
        //        public FontFamilyOption fontFamily = new FontFamilyOption("string", "input", emptyValue, "normal");
        public FontSizeOption fontSize = new FontSizeOption("number", "input", "12", "12");
    }

    public class NameGapOption extends ParamStringOption {
        NameGapOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class NameRotateOption extends ParamStringOption {
        NameRotateOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class MinOption extends ParamStringOption {
        MinOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class MaxOption extends ParamStringOption {
        MaxOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class SplitNumberOption extends ParamStringOption {
        SplitNumberOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class LogBaseOption extends ParamStringOption {
        LogBaseOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class AxisLineOption {
        public ShowOption show = new ShowOption("boolean", "checkbox", emptyValue, false);
        public LineStyleOption lineStyle = new LineStyleOption();
    }

    public class LineStyleOption {
        public ColorOption color = new ColorOption("string", "colorpicker", emptyValue, "#5470C6");
        public WidthOption width = new WidthOption("number", "input", emptyValue, emptyValue);
        public TypeOption type = new TypeOption("string", "dropdown", Arrays.asList("solid", "dashed", "dotted"), "solid");
        public OpacityOption opacity = new OpacityOption("number", "input", emptyValue, emptyValue);
    }

    public class OpacityOption extends ParamStringOption {
        OpacityOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class AxisLabelOption {
        public ShowOption show = new ShowOption("boolean", "checkbox", emptyValue, false);
        public InsideOption inside = new InsideOption("boolean", "checkbox", emptyValue, emptyValue);
        public RotateOption rotate = new RotateOption("number", "input", emptyValue, emptyValue);
        public ColorOption color = new ColorOption("string", "colorpicker", emptyValue, "#5470C6");
        public MarginOption margin = new MarginOption("number", "input", emptyValue, emptyValue);
        public FontStyleOption fontStyle = new FontStyleOption("string", "radio", Arrays.asList("normal", "italic", "oblique"), "normal");
        public FontWeightOption fontWeight = new FontWeightOption("string", "input", emptyValue, emptyValue);
        //        public FontFamilyOption fontFamily = new FontFamilyOption("string", "input", emptyValue, emptyValue);
        public FontSizeOption fontSize = new FontSizeOption("number", "input", "12", "12");
        public AlignOption align = new AlignOption("string", "radio", Arrays.asList("left", "center", "right"), "left");
    }

    public class InsideOption extends ParamStringOption {
        InsideOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class RotateOption extends ParamStringOption {
        RotateOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class MarginOption extends ParamStringOption {
        MarginOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class YAxisOption {
        @JsonIgnore
        String yAxisType;

        public ShowOption show;
        public PositionOption position;
        public OffsetOption offset;
        public TypeOption type;
        public NameOption name;
        public NameLocationOption nameLocation;
        public NameTextStyleOption nameTextStyle;
        public NameGapOption nameGap;
        public NameRotateOption nameRotate;
        public MinOption min;
        public MaxOption max;
        public SplitNumberOption splitNumber;
        public LogBaseOption logBase;
        public AxisLineOption axisLine;
        public AxisLabelOption axisLabel;

        public YAxisOption(String yAxisType) {
            this.yAxisType = yAxisType;
            show = new ShowOption("boolean", "checkbox", emptyValue, "true");
            position = new PositionOption("string", "radio", Arrays.asList("left", "right"), "left");
            offset = new OffsetOption("number", "input", emptyValue, emptyValue);
            type = new TypeOption("string", "dropdown", Arrays.asList("value", "category", "time", "log"), this.yAxisType);
            name = new NameOption("string", "input", emptyValue, emptyValue);
            nameLocation = new NameLocationOption("string", "radio", Arrays.asList("start", "center", "end"), "start");
            nameTextStyle = new NameTextStyleOption();
            nameGap = new NameGapOption("number", "input", emptyValue, emptyValue);
            nameRotate = new NameRotateOption("number", "input", emptyValue, emptyValue);
            min = new MinOption("number", "input", emptyValue, emptyValue);
            max = new MaxOption("number", "input", emptyValue, emptyValue);
            splitNumber = new SplitNumberOption("number", "input", emptyValue, emptyValue);
            logBase = new LogBaseOption("number", "input", emptyValue, emptyValue);
            axisLine = new AxisLineOption();
            axisLabel = new AxisLabelOption();
        }
    }


    public class SeriesOpiton {
        public TypeForSeriesOption type = new TypeForSeriesOption("string", "fixed", "line", "line");
        public ShowSymbolOpion showSymbol = new ShowSymbolOpion("boolean", "checkbox", emptyValue, emptyValue);
        public SymbolOption symbol = new SymbolOption("string", "dropdown", Arrays.asList("circle", "rect", "roundRect", "triangle", "diamond", "pin", "arrow", "none"), "circle");
        public SymbolSizeOption symbolSize = new SymbolSizeOption("number", "input", emptyValue, emptyValue);
        public StackOption stack = new StackOption("string", "input", emptyValue, emptyValue);
        public StepOption step = new StepOption("boolean", "checkbox", emptyValue, emptyValue);
        public LabelOption label = new LabelOption();
        public LineStyleWioutOpacityOption lineStyle = new LineStyleWioutOpacityOption();
        public SmoothOption smooth = new SmoothOption("boolean", "checkbox", emptyValue, emptyValue);
    }

    public class ShowSymbolOpion extends ParamStringOption {
        ShowSymbolOpion(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class SymbolOption extends ParamOption {
        SymbolOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class SymbolSizeOption extends ParamStringOption {
        SymbolSizeOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class StackOption extends ParamStringOption {
        StackOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class LabelOption {
        public ShowOption show = new ShowOption("boolean", "checkbox", emptyValue, false);
        public PositionOption position = new PositionOption("string", "dropdown", Arrays.asList("top", "left", "right", "bottom", "inside", "insideLeft", "insideRight", "insideTop", "insideBottom", "insideTopLeft", "insideBottomLeft", "insideTopRight", "insideBottomRight"), "top");
        public RotateOption rotate = new RotateOption("number", "input", emptyValue, "#5470C6");
        public ColorOption color = new ColorOption("string", "colorpicker", emptyValue, emptyValue);
        public FontStyleOption fontStyle = new FontStyleOption("string", "radio", Arrays.asList("normal", "italic", "oblique"), "normal");
        public FontWeightOption fontWeight = new FontWeightOption("string", "input", emptyValue, emptyValue);
        //        public FontFamilyOption fontFamily = new FontFamilyOption("string", "input", emptyValue, emptyValue);
        public FontSizeOption fontSize = new FontSizeOption("number", "input", emptyValue, emptyValue);
        public AlignOption align = new AlignOption("string", "radio", Arrays.asList("left", "center", "right"), "left");
    }

    public class LineStyleWioutOpacityOption {
        public ColorOption color = new ColorOption("string", "colorpicker", emptyValue, "#5470C6");
        public WidthOption width = new WidthOption("number", "input", emptyValue, emptyValue);
        public TypeOption type = new TypeOption("string", "dropdown", Arrays.asList("solid", "dashed", "dotted"), "dashed");
    }

    public class SmoothOption extends ParamStringOption {
        SmoothOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class SeriesForAreaOpiton extends SeriesOpiton {
        public TypeForSeriesOption type = new TypeForSeriesOption("string", "fixed", "line", "line");
        public ShowSymbolOpion showSymbol = new ShowSymbolOpion("boolean", "checkbox", emptyValue, emptyValue);
        public SymbolOption symbol = new SymbolOption("string", "dropdown", Arrays.asList("circle", "rect", "roundRect", "triangle", "diamond", "pin", "arrow", "none"), "circle");
        public SymbolSizeOption symbolSize = new SymbolSizeOption("number", "input", emptyValue, emptyValue);
        public StackOption stack = new StackOption("string", "input", emptyValue, emptyValue);
        public StepOption step = new StepOption("boolean", "checkbox", emptyValue, emptyValue);
        public LabelOption label = new LabelOption();
        public LineStyleWioutOpacityOption lineStyle = new LineStyleWioutOpacityOption();
        public SmoothOption smooth = new SmoothOption("boolean", "checkbox", emptyValue, emptyValue);
        public AreaStyleOption areaStyle = new AreaStyleOption();
    }

    public class StepOption extends ParamStringOption {
        StepOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class AreaStyleOption {
        public ColorOption color = new ColorOption("string", "colorpicker", emptyValue, "#5470C6");
        public OpacityOption opacity = new OpacityOption("number", "input", emptyValue, emptyValue);
    }


    public class SeriesFroBarOpiton {
        public TypeForSeriesOption type = new TypeForSeriesOption("string", "fixed", "bar", "bar");
        public ShowBackgroundOption showBackground = new ShowBackgroundOption("boolean", "input", emptyValue, emptyValue);
        public BackgroundStyleOption backgroundStyle = new BackgroundStyleOption();
        public LabelOption label = new LabelOption();
        public BarWidthOption barWidth = new BarWidthOption("string", "input", Arrays.asList("50px", "50%"), "50%");
        public BarGapOpion barGap = new BarGapOpion("string", "input", "40%", "");
        public StackOption stack = new StackOption("string", "input", emptyValue, emptyValue);
        public ItemStyleOption itemStyle = new ItemStyleOption();

    }

    public class ShowBackgroundOption extends ParamStringOption {
        ShowBackgroundOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class BackgroundStyleOption {
        public ColorOption color = new ColorOption("string", "colorpicker", emptyValue, "#5470C6");
        public OpacityOption opacity = new OpacityOption("number", "input", emptyValue, emptyValue);
    }

    public class BarWidthOption extends ParamOption {
        BarWidthOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class BarGapOpion extends ParamStringOption {
        BarGapOpion(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class ItemStyleOption {
        public ColorOption color = new ColorOption("string", "colorpicker", emptyValue, "#5470C6");
        public BoardColorOption borderColor = new BoardColorOption("string", "colorpicker", emptyValue, emptyValue);
        public BoardWidthOption borderWidth = new BoardWidthOption("number", "input", emptyValue, emptyValue);
        public BoaderTypeOption borderType = new BoaderTypeOption("string", "dropdown", Arrays.asList("solid", "dashed", "dotted"), "solid");
        public BorderRadiusOption borderRadius = new BorderRadiusOption("number", "input", emptyValue, emptyValue);
        public OpacityOption opacity = new OpacityOption("number", "input", emptyValue, emptyValue);
    }

    public class BoaderTypeOption extends ParamOption {
        BoaderTypeOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class BorderRadiusOption extends ParamStringOption {
        BorderRadiusOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }


    public class SeriesForPieOption {
        public TypeForSeriesOption type = new TypeForSeriesOption("string", "fixed", "pie", "pie");
        public ClockwiseOption clockwise = new ClockwiseOption("boolean", "checkbox", emptyValue, emptyValue);
        public StartAngleOption startAngle = new StartAngleOption("number", "input", emptyValue, emptyValue);
        public RoseTypeOption roseType = new RoseTypeOption("boolean", "checkbox", emptyValue, emptyValue);
        public LeftOption left = new LeftOption("string", "input", emptyValue, emptyValue);
        public TopOption top = new TopOption("string", "input", emptyValue, emptyValue);
        public RightOption right = new RightOption("string", "input", emptyValue, emptyValue);
        public BottomOption bottom = new BottomOption("string", "input", emptyValue, emptyValue);
        public WidthOption width = new WidthOption("string", "input", emptyValue, emptyValue);
        public HeightOption height = new HeightOption("string", "input", emptyValue, emptyValue);
        public LabelForPieOption label = new LabelForPieOption();
        public LabelLineOption labelLine = new LabelLineOption();
        public ItemStyleOption itemStyle = new ItemStyleOption();
        public RadiusOption radius = new RadiusOption();
    }


    public class ClockwiseOption extends ParamStringOption {
        ClockwiseOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class StartAngleOption extends ParamStringOption {
        StartAngleOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class RoseTypeOption extends ParamStringOption {
        RoseTypeOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class LabelForPieOption {
        public ShowOption show = new ShowOption("boolean", "checkbox", emptyValue, false);
        public PositionOption position = new PositionOption("string", "dropdown", Arrays.asList("outside", "inside", "center"), "outside");
        public RotateOption rotate = new RotateOption("number", "input", emptyValue, emptyValue);
        public ColorOption color = new ColorOption("string", "colorpicker", emptyValue, "#5470C6");
        public FontStyleOption fontStyle = new FontStyleOption("string", "radio", Arrays.asList("normal", "italic", "oblique"), "normal");
        public FontWeightOption fontWeight = new FontWeightOption("string", "input", emptyValue, "normal");
        //        public FontFamilyOption fontFamily = new FontFamilyOption("string", "input", emptyValue, emptyValue);
        public FontSizeOption fontSize = new FontSizeOption("number", "input", emptyValue, emptyValue);
        public AlignOption align = new AlignOption("string", "radio", Arrays.asList("left", "center", "right"), "left");
        public DistanceToLabelLineOption distanceToLabelLine = new DistanceToLabelLineOption("number", "input", emptyValue, emptyValue);
        public EdgeDistanceOption edgeDistance = new EdgeDistanceOption("string", "input", emptyValue, emptyValue);
    }

    public class DistanceToLabelLineOption extends ParamStringOption {
        DistanceToLabelLineOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class EdgeDistanceOption extends ParamStringOption {
        EdgeDistanceOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class LabelLineOption {
        public ShowOption show = new ShowOption("boolean", "checkbox", emptyValue, false);
        public LengthOption length = new LengthOption("number", "input", emptyValue, emptyValue);
        public SmoothOption smooth = new SmoothOption("boolean", "checkbox", emptyValue, emptyValue);
        public LineStyleOption lineStyle = new LineStyleOption();
    }

    public class LengthOption extends ParamStringOption {
        LengthOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class RadiusOption {
        public SizeOption size = new SizeOption("boolean", "checkbox", Arrays.asList("40%", "100%"), "100%");
        public InnerSizeOption innerSize = new InnerSizeOption("boolean", "checkbox", Arrays.asList("40%", "100%"), "100%");
    }

    public class SizeOption extends ParamOption {
        SizeOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class InnerSizeOption extends ParamOption {
        InnerSizeOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }


    public class SeriesForFunnelOption {
        public MinOption min = new MinOption("number", "input", emptyValue, emptyValue);
        public MaxOption max = new MaxOption("number", "input", emptyValue, emptyValue);
        public MinSizeOption minSize = new MinSizeOption("string", "input", "0%", "");
        public MaxSizeOption maxSize = new MaxSizeOption("string", "input", "100%", "");
        public OrientOption orient = new OrientOption("string", "dropdown", Arrays.asList("horizontal", "vertical"), "horizontal");
        public SortOption sort = new SortOption("string", "dropdown", Arrays.asList("ascending", "descending", "none"), "ascending");
        public GapOption gap = new GapOption("number", "input", emptyValue, emptyValue);
        public LegendHoverLinkOption legendHoverLink = new LegendHoverLinkOption("boolean", "checkbox", emptyValue, emptyValue);
        public FunnelAlingOption funnelAling = new FunnelAlingOption("string", "dropdown", Arrays.asList("left", "right", "center"), "left");
        public LabelForFunnelOption label = new LabelForFunnelOption();
        public LabelLineForFunnelOption labelLine = new LabelLineForFunnelOption();
        public ItemStyleForFunnelOption itemStyle = new ItemStyleForFunnelOption();
        public LeftOption left = new LeftOption("number", "input", emptyValue, emptyValue);
        public TopOption top = new TopOption("number", "input", emptyValue, emptyValue);
        public RightOption right = new RightOption("number", "input", emptyValue, emptyValue);
        public BottomOption bottom = new BottomOption("number", "input", emptyValue, emptyValue);
        public WidthOption width = new WidthOption("string", "input", "auto", "");
        public HeightOption height = new HeightOption("string", "input", "auto", "");
        public SeriesLayoutByOption seriesLayoutBy = new SeriesLayoutByOption("string", "dropdown", Arrays.asList("column", "row"), "column");
        public DatasetIndexOption datasetIndex = new DatasetIndexOption("number", "input", "0", "");
        public AnimationOption animation = new AnimationOption("boolean", "checkbox", "1", "");
    }

    public class ItemStyleForFunnelOption {
        public ColorOption color = new ColorOption("string", "colorpicker", emptyValue, "#5470C6");
        public BoardColorOption borderColor = new BoardColorOption("string", "colorpicker", emptyValue, emptyValue);
        public OpacityOption opacity = new OpacityOption("number", "input", emptyValue, emptyValue);
    }

    public class MinSizeOption extends ParamStringOption {
        MinSizeOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class MaxSizeOption extends ParamStringOption {
        MaxSizeOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class SortOption extends ParamOption {
        SortOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class GapOption extends ParamStringOption {
        GapOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class LegendHoverLinkOption extends ParamStringOption {
        LegendHoverLinkOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class FunnelAlingOption extends ParamOption {
        FunnelAlingOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class LabelForFunnelOption {
        public ShowOption show = new ShowOption("boolean", "checkbox", emptyValue, false);
        public ColorOption color = new ColorOption("string", "colorpicker", emptyValue, "#5470C6");
        public PositionOption position = new PositionOption("string", "dropdown", Arrays.asList("top", "left", "right", "bottom", "inside", "center", "inner"), "top");
        public FontSizeOption fontSize = new FontSizeOption("number", "input", "12", "");
        public AlignOption align = new AlignOption("string", "dropdown", Arrays.asList("left", "center", "right"), "left");
        public VerticalAlignOption verticalAlign = new VerticalAlignOption("string", "dropdown", Arrays.asList("top", "middle", "bottom"), "top");
        public EllipsisOption ellipsis = new EllipsisOption("string", "fixed", "overflow", "overflow");
    }

    public class VerticalAlignOption extends ParamOption {
        VerticalAlignOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class EllipsisOption extends ParamStringOption {
        EllipsisOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class LabelLineForFunnelOption {
        public ShowOption show = new ShowOption("boolean", "checkbox", emptyValue, false);
        public LengthOption length = new LengthOption("number", "input", emptyValue, emptyValue);
    }

    public class SeriesLayoutByOption extends ParamOption {
        SeriesLayoutByOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class DatasetIndexOption extends ParamStringOption {
        DatasetIndexOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class AnimationOption extends ParamStringOption {
        AnimationOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }


    public class SeriesForRadaarOption {
        public SymbolOption symbol = new SymbolOption("string", "dropdown", Arrays.asList("circle", "rect", "roundRect", "triangle", "diamond", "pin", "arrow", "none"), "circle");
        public SymbolSizeOption symbolSize = new SymbolSizeOption("number", "input", "4", "");
        public SymbolRotateOption symbolRotate = new SymbolRotateOption("number", "input", emptyValue, emptyValue);
        public SymbolOffsetOption symbolOffset = new SymbolOffsetOption("Array", "input", Arrays.asList("0%", "50%"), "0%");
        public LabelForFunnelOption label = new LabelForFunnelOption();
        public ItemStyleForFunnelOption itemStyle = new ItemStyleForFunnelOption();
        public EmphasisOption emphasis = new EmphasisOption("object", "dropdown", Arrays.asList("none", "self", "series"), "self");
        public ZlevelOption zlevel = new ZlevelOption("number", "input", "0", "");
        public ZOption z = new ZOption("number", "input", "2", "");
        public AnimationOption animation = new AnimationOption("boolean", "checkbox", "1", "");
    }

    public class SymbolRotateOption extends ParamStringOption {
        SymbolRotateOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class SymbolOffsetOption extends ParamOption {
        SymbolOffsetOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class EmphasisOption extends ParamOption {
        EmphasisOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }


    public class ZlevelOption extends ParamStringOption {
        ZlevelOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class ZOption extends ParamStringOption {
        ZOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class RadarOption {
        //        public IndicatiorOption indicator = new IndicatiorOption();
public List<IndicatiorOption> indicator = Arrays.asList(new IndicatiorOption());
        public CenterOption center = new CenterOption("Array", "fixed", Arrays.asList("50%", "50%"), Arrays.asList("50%", "50%"));
        public RadiusForPolarOption radius = new RadiusForPolarOption("string", "input", "0", "75%");
        public ShapeOption shape = new ShapeOption("string", "dropdown", Arrays.asList("polygon", "circle"), "polygon");
    }

    public class IndicatiorOption {
        public NameOption name = new NameOption("string", "input", emptyValue, emptyValue);
        public MinOption min = new MinOption("number", "input", emptyValue, emptyValue);
        public MaxOption max = new MaxOption("number", "input", emptyValue, emptyValue);
        public ColorOption color = new ColorOption("string", "colorpicker", emptyValue, "#5470C6");
    }

    public class SeriesForPolarOpiton {
        public SymbolOption symbol = new SymbolOption("string", "dropdown", Arrays.asList("circle", "rect", "roundRect", "triangle", "diamond", "pin", "arrow", "none"), "circle");
        public SymbolSizeOption symbolSize = new SymbolSizeOption("number", "input", "4", "");
        public SymbolRotateOption symbolRotate = new SymbolRotateOption("number", "input", emptyValue, emptyValue);
        public SymbolOffsetOption symbolOffset = new SymbolOffsetOption("Array", "input", Arrays.asList("0%", "50%"), "0%");
        public LabelForFunnelOption label = new LabelForFunnelOption();
        public ItemStyleForFunnelOption itemStyle = new ItemStyleForFunnelOption();
        public EmphasisOption emphasis = new EmphasisOption("object", "dropdown", Arrays.asList("none", "self", "series"), "self");
        public AnimationOption animation = new AnimationOption("boolean", "checkbox", "1", "");
    }

    public class PolarOption {
        public CenterOption center = new CenterOption("Array", "input", Arrays.asList("50%", "50%"), "50%");
        public RadiusForPolarOption radius = new RadiusForPolarOption("string", "input", "0", "");
    }

    public class CenterOption extends ParamOption {
        CenterOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
        CenterOption(String dataFormats, String htmlElements, List<String> values, List<String> defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    public class RadiusForPolarOption extends ParamStringOption {
        RadiusForPolarOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class AngleAxisOption {
        public TypeOption type = new TypeOption("string", "dropdown", Arrays.asList("value", "category", "time", "log"), "category");
        public DataOption data = new DataOption("Array", "input", emptyValue, emptyValue);
        public BoundaryGapoption boundaryGap = new BoundaryGapoption("boolean", "checkbox", emptyValue, emptyValue);
        public SplitLineOption splitLine = new SplitLineOption();
        public AxisLineForPolarOption axisLine = new AxisLineForPolarOption();
    }

    public class AxisLineForPolarOption {
        public ShowOption show = new ShowOption("object", "checkbox", emptyValue, "true");
    }

    public class DataOption extends ParamStringOption {
        DataOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class BoundaryGapoption extends ParamStringOption {
        BoundaryGapoption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class SplitLineOption {
        public ShowOption show = new ShowOption("object", "checkbox", emptyValue, "true");
    }

    public class RadiusAxisOption {
        public TypeOption type = new TypeOption("string", "dropdown", Arrays.asList("value", "category", "time", "log"), "category");
        public DataOption data = new DataOption("Array", "input", emptyValue, emptyValue);
        public AxisLineForPolarOption axisLine = new AxisLineForPolarOption();
        public AxisLabelForPolarOption axisLabel = new AxisLabelForPolarOption();
    }

    public class AxisLabelForPolarOption {
        public RotateOption rotate = new RotateOption("object", "input", "45", "");
    }

    public class TypeForSeriesOption extends ParamStringOption {
        TypeForSeriesOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    public class ColorWithValueOption extends ParamOption {
        ColorWithValueOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }

        ColorWithValueOption(String dataFormats, String htmlElements, List<String> values, List<String> defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }


    private class ShapeOption extends ParamOption {
        ShapeOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }

    private class SelectedModeOption extends ParamStringOption {
        SelectedModeOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }

        SelectedModeOption(String dataFormats, String htmlElements, String value, Boolean defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    private class OverflowOption extends ParamStringOption {
        OverflowOption(String dataFormats, String htmlElements, String value, String defaultValue) {
            super(dataFormats, htmlElements, value, defaultValue);
        }
    }

    private class OrderOption extends ParamOption {
        OrderOption(String dataFormats, String htmlElements, List<String> values, String defaultValue) {
            super(dataFormats, htmlElements, values, defaultValue);
        }
    }
}
