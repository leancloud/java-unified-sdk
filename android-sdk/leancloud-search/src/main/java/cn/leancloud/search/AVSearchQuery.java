package cn.leancloud.search;

import java.util.List;

import cn.leancloud.AVObject;

public class AVSearchQuery<T extends AVObject> {
    private String sid;
    private int limit = 100;
    private int skip = 0;
    private String hightlights;
    private static final String URL = "search/select";
    private List<String> fields;
    private String queryString;
    private String titleAttribute;
    private String className;
    private int hits;
    private String order;
    private AVSearchSortBuilder sortBuilder;
    private List<String> include;
    Class<T> clazz;

}
