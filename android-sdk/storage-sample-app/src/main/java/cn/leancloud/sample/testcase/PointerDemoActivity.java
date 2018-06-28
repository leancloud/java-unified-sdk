package cn.leancloud.sample.testcase;

import java.util.List;

import cn.leancloud.AVException;
import cn.leancloud.AVQuery;
import cn.leancloud.sample.DemoBaseActivity;
import cn.leancloud.sample.Post;
import cn.leancloud.sample.Student;

/**
 * Created by fengjunwen on 2018/5/10.
 */

public class PointerDemoActivity extends DemoBaseActivity {
  public void testRelateObject() throws AVException {
    Student student = getFirstStudent();

    Post post = new Post();
    post.setContent("每个 iOS 程序员必备的 8 个开发工具");
    post.setAuthor(student);
    post.save();
    log("把 Student 对象绑定到了 Post 对象的 author 字段！post: " + prettyJSON(post));
  }

  public void testObjectArray() throws AVException {
    List<Student> students = findStudents();
    Post post = new Post();
    post.setContent("每个 iOS 程序员必备的 8 个开发工具");
    post.addAll(Post.LIKES, students);
    post.save();
    log("用了 Object Array 来保存 Post 的点赞列表！Post : " + prettyJSON(post));
  }

  public void testNotIncludeObject() throws AVException {
    AVQuery<Post> query = AVQuery.getQuery(Post.class);
    query.whereExists(Post.LIKES);
    log("将不包含 likes 字段的具体数据");
    Post first = query.getFirst();
    if (first != null) {
      log("Post =" + prettyJSON(first));
    } else {
      log("请先运行例子 testObjectArray");
    }
  }

  public void testIncludeObject() throws AVException {
    AVQuery<Post> query = AVQuery.getQuery(Post.class);
    query.whereExists(Post.LIKES);
    log("让返回结果包含了 likes 字段的具体数据，不单单是赞的人的 objectId");
    query.include(Post.LIKES);
    Post first = query.getFirst();
    if (first != null) {
      log("Post =" + prettyJSON(first));
    } else {
      log("请先运行例子 testObjectArray");
    }
  }
}
