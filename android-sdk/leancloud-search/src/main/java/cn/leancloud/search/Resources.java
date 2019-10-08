package cn.leancloud.search;

import android.content.Context;

public class Resources {
  protected static String getPackageName(Context context) {
    return context.getApplicationContext().getPackageName();
  }

  protected static int getResourceId(Context context, String resourceId, String type) {
    return context.getResources().getIdentifier(resourceId, type, getPackageName(context));
  }

  public static class layout {
    public static int search_activity(Context context) {
      return getResourceId(context, "leancloud_activity_search", "layout");
    }

    public static int search_result_item(Context context) {
      return getResourceId(context, "leancloud_search_result_item", "layout");
    }

    public static int search_actionbar(Context context) {
      return getResourceId(context, "leancloud_search_actionbar", "layout");
    }

    public static int search_loading(Context context) {
      return getResourceId(context, "leancloud_search_loading", "layout");
    }

  }

  public static class drawable {

  }

  public static class id {

    public static int search_result_listview(Context context) {
      return getResourceId(context, "leancloud_search_result_listview", "id");
    }

    public static int search_result_title(Context context) {
      return getResourceId(context, "leancloud_search_result_title", "id");
    }

    public static int search_result_description(Context context) {
      return getResourceId(context, "leancloud_search_result_description", "id");
    }

    public static int search_result_open_app(Context context) {
      return getResourceId(context, "leancloud_search_result_open_app", "id");
    }

    public static int search_actionbar_back(Context context) {
      return getResourceId(context, "leancloud_search_actionbar_back", "id");
    }

    public static int search_emtpy_result(Context context) {
      return getResourceId(context, "leancloud_search_emtpy_result", "id");
    }
  }


  public static class string {}
}
