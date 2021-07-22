package com.sk.bds.datainsight.database.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Option {
    public static final int COLUMN = 0;
    public static final int PIE = 1;
    public static final int LINE = 2;
    public static final int RADAR = 3;
    public static final int FUNNEL = 4;
    public static final int TABLE = 5;
    public static final int WORD_CLOUD = 6;

    public Option() {}

    public Object getOption(int index) {
        switch (index) {
            case COLUMN:
                return new Column();
            case PIE:
                return new Pie();
            case LINE:
                return new Line();
            case RADAR:
                return new Radar();
            case FUNNEL:
                return new Funnel();
            case TABLE:
                return new Table();
            case WORD_CLOUD:
                return new WordCloud();
        }
        return null;
    }

    @Data
    public class Column {
        private String type = "serial";
        private String path = "common-module/ext_libs/amcharts/";
        private String fontFamily = "맑은 고딕";
        private boolean handDrawn = false;
        private int handDrawScatter = 2;
        private int handDrawThickness = 1;
        private boolean rotate = false;
        private boolean sequencedAnimation = true;
        private float startDuration = 0.6f;
        private String startEffect = "easeInSine";
        private int percentPrecision = 2;
        private int precision = -1;
        private Responsive responsive = new Responsive();
        private Legend legend = new Legend();
        private Balloon balloon = new Balloon();
        private List<Title> titles;
        private Enabled chartCursor = new Enabled(true);
        private Enabled chartScrollbar = new Enabled(false);
        private Enabled valueScrollbar = new Enabled(false);
        private List<ValueAxe> valueAxes;
        private CategoryAxis categoryAxis = new CategoryAxis();
        private Export export = new Export(new Enabled(true));

        public Column() {
            titles = new ArrayList<>();
            titles.add(new Title());
            valueAxes = new ArrayList<>();
            valueAxes.add(new ValueAxe());
        }
    }

    @Data
    public class Pie {
        private String type = "pie";
        private String path = "common-module/ext_libs/amcharts/";
        private String innerRadius = "0%";
        private float hoverAlpha = 0.5f;
        private int labelTickAlpha = 1;
        private int outlineThickness = 0;
        private boolean usePrefixes = true;
        private boolean sequencedAnimation = true;
        private String color = "#000000";
        private String fontFamily = "맑은 고딕";
        private int fontSize = 11;
        private boolean handDrawn = false;
        private int handDrawScatter = 2;
        private int handDrawThickness = 1;
        private boolean labelsEnabled = true;
        private float startDuration = 0.6f;
        private String startEffect = "easeInSine";
        private int percentPrecision = 2;
        private int precision = -1;
        private Responsive responsive = new Responsive(true, 450, false);
        private Balloon balloon = new Balloon(1, false, null);
        private String radius = "30%";
        private List<Title> titles;
        private int hideLabelsPercent = 3;
        private int maxLabelWidth = 150;
        private Export export = new Export(new Enabled(true));
        private Legend legend = new Legend(true, true, 100, "center", true, "circle");
        private int angle = 0;
        private int depth3D = 0;

        public Pie(){
            titles = new ArrayList<>();
            titles.add(new Title(true, "#000000", null, 15, ""));
        }

    }

    @Data
    public class Line {
        private String type = "line";
        private String path = "common-module/ext_libs/amcharts/";
        private String fontFamily = "맑은 고딕";
        private boolean handDrawn = false;
        private int handDrawScatter = 2;
        private int handDrawThickness = 1;
        private boolean rotate = false;
        private boolean sequencedAnimation = true;
        private float startDuration = 0.6f;
        private String startEffect = "easeInSine";
        private int percentPrecision = 2;
        private int precision = -1;
        private Responsive responsive = new Responsive(true, 200, false);
        private Legend legend = new Legend(true, true, 100, "center", true, "circle");
        private Balloon balloon = new Balloon(1, false, 8);
        private List<Title> titles;
        private Enabled chartCursor = new Enabled(true);
        private Enabled chartScrollbar = new Enabled(false);
        private Enabled valueScrollbar = new Enabled(false);
        private List<ValueAxe> valueAxes;
        private CategoryAxis categoryAxis = new CategoryAxis("start", "start", "#EFEFEF", 1, "#EEE", 9, 1, 10, 20, true, "", false);
        private Export export = new Export(new Enabled(true));

        public Line() {
            titles = new ArrayList<>();
            titles.add(new Title(true, "#000000", null, 15, ""));
            valueAxes = new ArrayList<>();
            valueAxes.add(new ValueAxe("ValueAxis-1", 0, 0, 0, "", 9, 0, 0, 9));
        }
    }

    @Data
    public class Funnel {
        private String type = "funnel";
        private String path = "common-module/ext_libs/amcharts/";
        private String fontFamily = "맑은 고딕";
        private boolean handDrawn = false;
        private int handDrawScatter = 2;
        private int handDrawThickness = 1;
        private String baseWidth = "100%";
        private String labelPosition = "right";
        private String neckHeight = "50%";
        private String neckWidth = "50%";
        private String valueRepresents = "Height";
        private boolean sequencedAnimation = true;
        private float startDuration = 0.6f;
        private String startEffect = "easeInSine";
        private String percentPrecision = "2";
        private int precision = -1;
        private Balloon balloon = new Balloon(1, false, 8);
        private List<Title> titles;
        private int depth3D = 100;
        private Export export = new Export(new Enabled(true));
        private int marginLeft = 15;
        private int marginRight = 160;

        public Funnel() {
            titles = new ArrayList<>();
            titles.add(new Title(true, "#000000", null, 15, ""));
        }
    }

    @Data
    public class Radar {
        private String type = "radar";
        private String path = "common-module/ext_libs/amcharts/";
        private String fontFamily = "맑은 고딕";
        private boolean handDrawn = false;
        private int handDrawScatter = 2;
        private int handDrawThickness = 1;
        private String radius = "35%";
        private boolean sequencedAnimation = true;
        private float startDuration = 0.6f;
        private String startEffect = "easeInSine";
        private int percentPrecision = 2;
        private int precisionAnInt= -1;
        private Balloon balloon = new Balloon(1, false, 8);
        private List<Title> titles;
        private List<ValueAxe> valueAxes;
        private Export export = new Export(new Enabled(true));

        public Radar() {
            titles = new ArrayList<>();
            titles.add(new Title(true, "#000000", null, 15, ""));
            valueAxes = new ArrayList<>();
            valueAxes.add(new ValueAxe(20, "ValueAxis-1", 0, 0.1f, 3, 9, "polygons", 9));
        }
    }

    @Data
    public class Table {
        private String type = "table";
        private int limitCnt = 100;
    }

    @Data
    public class WordCloud {
        private String type = "WordCloudSeries";
        private List<Series> series = new ArrayList<>();
        private String color = "#000000";

        public WordCloud() {
            series.add(new Series());
        }
    }

    @Data
    class Series {
        private String type = "WordCloudSeries";
        private int maxCount = 10;
        private int minValue = 1;
        private int minWordLength = 1;
        private int minFontSize = 2;
        private int maxFontSize = 20;
        private Labels labels = new Labels();
        private int accuracy = 5;
        private float randomness = 0.2f;
        private DataFields dataFields = new DataFields();
        private Colors colors = new Colors();
        private LabelsContainer labelsContainer = new LabelsContainer();
    }

    @Data
    class LabelsContainer {
        private int rotation = 0;
    }

    @Data
    class Colors {
        private String type = "ColorSet";
        private HashMap<String, Object> passOptions = new HashMap<>();
    }

    @Data
    class DataFields {
        private String word = "tag";
        private String value = "weight";
    }

    @Data
    class Labels {
        private String tooltipText = "{word}:\n[bold]{value}[/]";
        private String fill = "#000000";
    }

    @Data
    class CategoryAxis {
        private String gridPosition = "start";
        private String tickPosition = "start";
        private String axisColor = "#EFEFEF";
        private int gridAlpha = 1;
        private String gridColor = "#EEE";
        private int fontSize = 9;
        private int labelFrequency = 1;
        private int autoRotateCount = 10;
        private int autoRotateAngle = 20;
        private boolean centerRotatedLabels = true;
        private String title = "";
        private boolean reverse = false;

        CategoryAxis() {}

        CategoryAxis(String gridPosition, String tickPosition, String axisColor, int gridAlpha, String gridColor, int fontSize, int labelFrequency,
                     int autoRotateCount, int autoRotateAngle, boolean centerRotatedLabels, String title, boolean reverse) {
            this.gridPosition = gridPosition;
            this.tickPosition = tickPosition;
            this.axisColor = axisColor;
            this.gridAlpha = gridAlpha;
            this.gridColor = gridColor;
            this.fontSize = fontSize;
            this.labelFrequency = labelFrequency;
            this.autoRotateCount = autoRotateCount;
            this.autoRotateAngle = autoRotateAngle;
            this.centerRotatedLabels = centerRotatedLabels;
            this.title = title;
            this.reverse = reverse;
        }
    }

    @Data
    class ValueAxe {
        private String id = "ValueAxis-1";
        private Integer zeroGridAlpha = 0;
        private Integer axisThickness = 0;
        private Integer gridThickness = 0;
        private String title = "";
        private Integer fontSize = 9;
        private String gridType = null;
        private Integer axisTitleOffset = null;
        private Integer minimum = null;
        private Integer maximum = null;
        private Float axisAlpha = null;
        private Integer dashLength = null;
        private Integer titleFontSize = 9;
        private boolean integersOnly = false;
        public ValueAxe() {}

        public ValueAxe(String id, Integer zeroGridAlpha, Integer axisThickness, Integer gridThickness, String title, Integer fontSize, Integer minimum, Integer maximum, Integer titleFontSize) {
            this.id = id;
            this.zeroGridAlpha = zeroGridAlpha;
            this.axisThickness = axisThickness;
            this.gridThickness = gridThickness;
            this.title = title;
            this.fontSize = fontSize;
            this.minimum = minimum;
            this.maximum = maximum;
            this.titleFontSize = titleFontSize;
        }

        public ValueAxe(Integer axisTitleOffset, String id, Integer minimum, Float axisAlpha, Integer dashLength, Integer fontSize, String gridType, Integer titleFontSize) {
            this. axisTitleOffset = axisTitleOffset;
            this.id = id;
            this.minimum = minimum;
            this.axisAlpha = axisAlpha;
            this.dashLength = dashLength;
            this.fontSize = fontSize;
            this.gridType = gridType;
            this.zeroGridAlpha = null;
            this.axisThickness = null;
            this.gridThickness = null;
            this.title = null;
            this.titleFontSize = titleFontSize;
        }
    }

    @Data
    class Enabled {
        private boolean enabled = true;

        public Enabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    @Data
    class Title {
        private boolean bold = true;
        private String color = "#000000";
        private String id = null;
        private int size = 15;
        private String text = "";

        public Title() {}

        public Title(boolean bold, String color, String id, int size, String text) {
            this.bold = bold;
            this.color = color;
            this.id = id;
            this.size = size;
            this.text = text;
        }
    }

    @Data
    class Balloon {
        private int fadeOutDuration = 1;
        private boolean fixedPosition = false;
        private Integer fontSize = 8;

        public Balloon() {}

        public Balloon(int fadeOutDuration, boolean fixedPosition, Integer fontSize) {
            this.fadeOutDuration = fadeOutDuration;
            this.fixedPosition = fixedPosition;
            this.fontSize = fontSize;
        }
    }

    @Data
    class Legend {
        private boolean enabled = true;
        private boolean equalWidths = true;
        private int maxColumns = 100;
        private String align = "center";
        private boolean autoMargins = true;
        private String markerType = "circle";

        public Legend() {}

        public Legend(boolean enabled, boolean equalWidths, int maxColumns, String align, boolean autoMargins, String markerType) {
            this.enabled = enabled;
            this.equalWidths = equalWidths;
            this.maxColumns = maxColumns;
            this.align = align;
            this.autoMargins = autoMargins;
            this.markerType = markerType;
        }
    }

    @Data
    class Responsive {
        private boolean enabled = true;
        private List<Rule> rules;

        public Responsive() {
            rules = new ArrayList<Rule>();
            rules.add(new Rule());
        }

        public Responsive(boolean enabled, int maxHeight, boolean overrides) {
            this.enabled = enabled;
            rules = new ArrayList<Rule>();
            rules.add(new Rule(maxHeight, overrides));
        }

        @Data
        class Rule {
            private int maxHeight = 200;
            private Override overrides = new Override();

            public Rule() {}

            public Rule(int maxHeight, boolean overrides) {
                this.maxHeight = maxHeight;
                this.overrides = new Override(new Enabled(overrides));
            }

            @Data
            class Override {
                private Enabled legend = new Enabled(false);

                public Override() {}

                public Override(Enabled legend) {
                    this.legend = legend;
                }
            }
        }
    }

    @Data
    class Export {
        private Enabled enabled = new Enabled(true);
        private ArrayList menu = new ArrayList();

        public Export() {}

        public Export(Enabled enabled) {
            this.enabled = enabled;
        }
    }
}
