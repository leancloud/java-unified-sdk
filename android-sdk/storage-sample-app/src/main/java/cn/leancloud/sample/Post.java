package cn.leancloud.sample;

import java.util.List;

import cn.leancloud.AVObject;
import cn.leancloud.AVRelation;
import cn.leancloud.annotation.AVClassName;

/**
 * Created by fengjunwen on 2018/5/10.
 */

@AVClassName("Post")
public class Post extends AVObject {

  public static final String CONTENT = "content";
  public static final String AUTHOR = "author";
  public static final String LIKES = "likes";
  public static final String REWARDS = "rewards"; // 打赏

  public Post() {

  }

  public Student getAuthor() {
    return getAVObject(AUTHOR);
  }

  public void setAuthor(Student author) {
    put(AUTHOR, author);
  }

  public String getContent() {
    return getString(CONTENT);
  }

  public void setContent(String content) {
    put(CONTENT, content);
  }

  public List<Student> getLikes() {
    return getList(LIKES);
  }

  public void setLikes(List<Student> likes) {
    put(LIKES, likes);
  }

  public AVRelation<Student> getRewardStudents() {
    return getRelation(REWARDS);
  }

  public void setRewardStudents(AVRelation<Student> rewardStudents) {
    put(REWARDS, rewardStudents);
  }
}

