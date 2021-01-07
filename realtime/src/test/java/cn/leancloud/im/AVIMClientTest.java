package cn.leancloud.im;

import cn.leancloud.*;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMConversationsQuery;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.im.v2.conversation.AVIMConversationMemberInfo;
import cn.leancloud.im.v2.messages.AVIMAudioMessage;
import cn.leancloud.session.AVConnectionManager;
import cn.leancloud.utils.StringUtil;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

public class AVIMClientTest extends TestCase {
  private CountDownLatch countDownLatch = null;
  private boolean opersationSucceed = false;
  private static final String userName = "JavaSDKUnitUser007";
  private static final String userEmail = "JavaSDKUnitUser007@lean.cloud";
  private static final String userPassword = "UnitTest#Password";

  public AVIMClientTest(String name) {
    super(name);
    Configure.initialize();
  }

  @Override
  protected void setUp() throws Exception {
    this.countDownLatch = new CountDownLatch(1);
    AVConnectionManager manager = AVConnectionManager.getInstance();
    manager.autoConnection();
    opersationSucceed = false;
  }

  @Override
  protected void tearDown() throws Exception {
    this.countDownLatch = null;
  }

  public void testOpenClient() throws Exception {
    AVIMClient client = AVIMClient.getInstance("testUser1");
    Thread.sleep(4000);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null != e) {
          System.out.println("failed open client.");
          e.printStackTrace();
        } else {
          System.out.println("succeed open client.");
          client.close(null);
          opersationSucceed = true;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

//  public void testCreateNewUser() throws Exception {
//    AVUser user = new AVUser();
//    user.setEmail(userEmail);
//    user.setUsername(userName);
//    user.setPassword(userPassword);
//    user.signUp();
//  }

  public void testOpenClientThroughAVUser() throws Exception {
    {
      try {
        AVUser usr = new AVUser();
        usr.setEmail(userEmail);
        usr.setUsername(userName);
        usr.setPassword(userPassword);
        usr.signUp();
      } catch (Exception e) {

      }

    }
    AVUser currentUser = AVUser.logIn(userName, userPassword).blockingFirst();
    final AVIMClient client = AVIMClient.getInstance(currentUser);
    Thread.sleep(4000);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null != e) {
          System.out.println("failed to open client.");
          e.printStackTrace();
        } else {
          System.out.println("succeed to open client.");
          opersationSucceed = true;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
    client.close(null);
  }

  public void testCloseClient() throws Exception {
    final AVIMClient client = AVIMClient.getInstance("testUser1");
    Thread.sleep(4000);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null != e) {
          System.out.println("failed open client.");
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          System.out.println("succeed open client.");
          client.close(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient client, AVIMException e) {
              if (null != e) {
                System.out.println("failed close client");
                e.printStackTrace();
              } else {
                System.out.println("succeed close client.");
                opersationSucceed = true;
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testOnlineQuery() throws Exception {
    final AVIMClient client = AVIMClient.getInstance("testUser1");
    Thread.sleep(4000);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(final AVIMClient client, AVIMException e) {
        if (null != e) {
          System.out.println("failed open client.");
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          System.out.println("succeed open client.");
          List<String> clients = Arrays.asList("Tom", "Jerry", "William");
          client.getOnlineClients(clients, new AVIMOnlineClientsCallback() {
            @Override
            public void done(List<String> object, AVIMException e) {
              if (null != e) {
                System.out.println("failed getOnlineClients");
                e.printStackTrace();
              } else {
                System.out.println("succeed getOnlineClients. result=" + object.toString());
                opersationSucceed = true;
              }
              client.close(null);
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testGetClientStatus() throws Exception {
    final AVIMClient client = AVIMClient.getInstance("testUser1");
    Thread.sleep(4000);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(final AVIMClient client, final AVIMException e) {
        if (null != e) {
          System.out.println("failed open client.");
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          System.out.println("succeed open client.");
          client.getClientStatus(new AVIMClientStatusCallback() {
            @Override
            public void done(AVIMClient.AVIMClientStatus status) {
              if (null != e) {
                System.out.println("failed getOnlineClients");
                e.printStackTrace();
              } else {
                System.out.println("succeed getOnlineClients.");
                opersationSucceed = true;
              }
              client.close(null);
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationQuery() throws Exception {
    final AVIMClient client = AVIMClient.getInstance("testUser1");
    Thread.sleep(4000);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null != e) {
          System.out.println("failed open client.");
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          System.out.println("succeed open client.");
          AVIMConversationsQuery query = client.getConversationsQuery();
          query.setQueryPolicy(AVQuery.CachePolicy.NETWORK_ONLY);
          query.whereContains("m", "Tom").addAscendingOrder("createdAt")
                  .findInBackground(new AVIMConversationQueryCallback() {
            @Override
            public void done(List<AVIMConversation> conversations, AVIMException ex) {
              if (null != ex) {
                System.out.println("failed to query convs");
                ex.printStackTrace();
              } else {
                System.out.println("succeed to query convs. results=" + conversations.toString());
                opersationSucceed = true;
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testCreateConversation() throws Exception {
    final AVIMClient client = AVIMClient.getInstance("testUser1");
    Thread.sleep(4000);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null != e) {
          System.out.println("failed open client.");
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          System.out.println("succeed open client.");
          client.createConversation(Arrays.asList("testUser2"), "user1&user2", null, false, true,
                  new AVIMConversationCreatedCallback() {
            @Override
            public void done(AVIMConversation conversation, AVIMException ex) {
              if (null != ex) {
                System.out.println("failed to create Conv");
                ex.printStackTrace();
                countDownLatch.countDown();
              } else {
                String uniqueId = conversation.getUniqueId();
                List<String> members = conversation.getMembers();
                System.out.println("succeed to create Conv. uniqueId=" + uniqueId + ", members=" + StringUtil.join(",", members));
                conversation.getAllMemberInfo(0, 10, new AVIMConversationMemberQueryCallback() {
                  @Override
                  public void done(List<AVIMConversationMemberInfo> memberInfoList, AVIMException e3) {
                    if (null != e3) {
                      System.out.println("failed to query member info");
                      e3.printStackTrace();
                    } else {
                      System.out.println("succeed to query member info, result=" + memberInfoList);
                      opersationSucceed = true;
                    }
                    countDownLatch.countDown();
                  }
                });
              }
            }
          });
        }
      }
    });
    countDownLatch.await();
    client.close(null);
    assertTrue(opersationSucceed);
  }

  public void testCreateChatRoom() throws Exception {
    final AVIMClient client = AVIMClient.getInstance("testUser1");
    Thread.sleep(4000);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null != e) {
          System.out.println("failed open client.");
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          System.out.println("succeed open client.");
          client.createChatRoom(Arrays.asList("testUser2"), null, null, true,
                  new AVIMConversationCreatedCallback() {
                    @Override
                    public void done(AVIMConversation conversation, AVIMException ex) {
                      if (null != ex) {
                        System.out.println("failed to create ChatRoom Conv");
                        ex.printStackTrace();
                        countDownLatch.countDown();
                      } else {
                        System.out.println("succeed to create ChatRoom Conv");
                        conversation.getAllMemberInfo(0, 10, new AVIMConversationMemberQueryCallback() {
                          @Override
                          public void done(List<AVIMConversationMemberInfo> memberInfoList, AVIMException e3) {
                            if (null != e3) {
                              System.out.println("failed to query member info");
                              e3.printStackTrace();
                            } else {
                              System.out.println("succeed to query member info, result=" + memberInfoList);
                              opersationSucceed = true;
                            }
                            countDownLatch.countDown();
                          }
                        });
                      }
                    }
                  });
        }
      }
    });
    countDownLatch.await();
    client.close(null);
    assertTrue(opersationSucceed);
  }

  public void testCreateTempConv() throws Exception {
    final AVIMClient client = AVIMClient.getInstance("testUser1");
    Thread.sleep(4000);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null != e) {
          System.out.println("failed open client.");
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          System.out.println("succeed open client.");
          client.createTemporaryConversation(Arrays.asList("testUser2"),
                  new AVIMConversationCreatedCallback() {
                    @Override
                    public void done(AVIMConversation conversation, AVIMException ex) {
                      if (null != ex) {
                        System.out.println("failed to create temp Conv");
                        ex.printStackTrace();
                        countDownLatch.countDown();
                      } else {
                        System.out.println("succeed to create temp Conv");
                        conversation.getAllMemberInfo(0, 10, new AVIMConversationMemberQueryCallback() {
                          @Override
                          public void done(List<AVIMConversationMemberInfo> memberInfoList, AVIMException e3) {
                            if (null != e3) {
                              System.out.println("failed to query member info");
                              e3.printStackTrace();
                            } else {
                              System.out.println("succeed to query member info, result=" + memberInfoList);
                            }
                            opersationSucceed = true;
                            countDownLatch.countDown();
                          }
                        });
                      }
                    }
                  });
        }
      }
    });
    countDownLatch.await();
    client.close(null);
    assertTrue(opersationSucceed);
  }

  public void testSendAudioMessageInTempConv() throws Exception {
    final AVIMClient client = AVIMClient.getInstance("testUser1");
    Thread.sleep(4000);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null != e) {
          System.out.println("failed open client.");
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          System.out.println("succeed open client.");
          client.createTemporaryConversation(Arrays.asList("testUser2"),
                  new AVIMConversationCreatedCallback() {
                    @Override
                    public void done(AVIMConversation conversation, AVIMException ex) {
                      if (null != ex) {
                        System.out.println("failed to create temp Conv");
                        ex.printStackTrace();
                        countDownLatch.countDown();
                      } else {
                        System.out.println("succeed to create temp Conv");
                        AVFile file = new AVFile("apple.acc", "https://some.website.com/apple.acc", null);
                        AVIMAudioMessage m = new AVIMAudioMessage(file);
                        m.setText("来自苹果发布会现场的录音");
                        conversation.sendMessage(m, new AVIMConversationCallback() {
                          @Override
                          public void done(AVIMException e3) {
                            if (null != e3) {
                              System.out.println("failed to send audio message.");
                              e3.printStackTrace();
                            } else {
                              System.out.println("succeed to send audio message.");
                              opersationSucceed = true;
                            }
                            countDownLatch.countDown();
                          }
                        });
                      }
                    }
                  });
        }
      }
    });
    countDownLatch.await();
    client.close(null);
    assertTrue(opersationSucceed);
  }

  public void testGetConversations() throws Exception {
    final AVIMClient client = AVIMClient.getInstance("testUser1");
    Thread.sleep(4000);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null != e) {
          System.out.println("failed open client.");
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          System.out.println("succeed open client.");
          client.getChatRoom("chatroom");
          client.getServiceConversation("serviceaccount");
          client.getTemporaryConversation("temporaryconversation");
          countDownLatch.countDown();
        }
      }
    });
    countDownLatch.await();
    client.close(null);
  }

  private String getFirstKey(ConcurrentMap<String, Object> container) {
    if (null == container || container.size() < 1) {
      return "";
    }
    return container.keySet().iterator().next();
  }

  public void testHashMapKeyOrder() throws Exception {
    ConcurrentMap<String, Object> container = new ConcurrentHashMap<>();
    String firstKey = getFirstKey(container);
    System.out.println("firstKey at launch: " + firstKey);
    for (int i = 0; i < 20;i++) {
      String tmpKey = StringUtil.getRandomString(10);
      container.put(tmpKey, System.currentTimeMillis());
      firstKey = getFirstKey(container);
      System.out.println("firstKey: " + firstKey + ", after add tmp key: " + tmpKey);
    }
  }
}
