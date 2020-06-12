package cn.leancloud.im;

import cn.leancloud.AVException;
import cn.leancloud.AVQuery;
import cn.leancloud.Configure;
import cn.leancloud.im.v2.*;
import cn.leancloud.im.v2.callback.*;
import cn.leancloud.im.v2.messages.AVIMRecalledMessage;
import cn.leancloud.im.v2.messages.AVIMTextMessage;
import cn.leancloud.session.AVConnectionManager;
import cn.leancloud.utils.StringUtil;
import cn.leancloud.json.JSON;
import cn.leancloud.json.JSONObject;
import junit.framework.TestCase;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class AVIMConversationTest extends TestCase {
  private CountDownLatch countDownLatch = null;
  private boolean opersationSucceed = false;
  private AVIMClient client = null;
  private List<String> memebers = Arrays.asList("User2", "User3");
  private String convName = "RealtimeUnitTest";
  private DummyConversationEventHandler conversationEventHandler =
          new DummyConversationEventHandler(0x00FFFF);
  String testConversationId = null;

  public AVIMConversationTest(String suiteName) {
    super(suiteName);
    Configure.initialize();
    AVIMClient.setClientEventHandler(new DummyClientEventHandler());
    AVIMMessageManager.setConversationEventHandler(conversationEventHandler);
    AVConnectionManager manager = AVConnectionManager.getInstance();
    manager.autoConnection();
    try {
      Thread.sleep(1000);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  protected void setUp() throws Exception {
    this.countDownLatch = new CountDownLatch(1);
    opersationSucceed = false;
    conversationEventHandler.resetAllCount();
    testConversationId = null;
  }

  @Override
  protected void tearDown() throws Exception {
    this.countDownLatch = null;
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    if (null != client) {
      client.close(new AVIMClientCallback() {
        @Override
        public void done(AVIMClient client, AVIMException e) {
          tmpCounter.countDown();
        }
      });
      tmpCounter.await();
    }
    client = null;
  }

  public void testSendTextMessage() throws Exception {
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client = AVIMClient.getInstance("testUser1");
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    client.createConversation(memebers, convName, null, false, true, new AVIMConversationCreatedCallback() {
      @Override
      public void done(AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          AVIMTextMessage msg = new AVIMTextMessage();
          msg.setText("test run @" + System.currentTimeMillis());
          conversation.sendMessage(msg, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException ex) {
              if (null != ex) {
                System.out.println("failed to send message");
                ex.printStackTrace();
              } else {
                System.out.println("succeed to send message");
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

  public void testSendAndReceiveBinaryMessage() throws Exception {
    AVIMMessageManager.registerDefaultMessageHandler(new DummyMessageHandler());
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    final CountDownLatch tmpCounter2 = new CountDownLatch(1);

    Thread firstThread = new Thread(new Runnable() {
      private AVIMConversation targetConversation = null;
      @Override
      public void run() {
        client = AVIMClient.getInstance("testUser1");
        client.open(new AVIMClientCallback() {
          @Override
          public void done(AVIMClient client, AVIMException e) {
            client.createConversation(memebers, convName, null, false, true, new AVIMConversationCreatedCallback() {
              @Override
              public void done(AVIMConversation conversation, AVIMException e) {
                if (null != e) {
                  e.printStackTrace();
                  tmpCounter.countDown();
                  countDownLatch.countDown();
                } else {
                  targetConversation = conversation;
                  tmpCounter.countDown();
                }
              }
            });
          }
        });
        try {
          tmpCounter2.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        AVIMBinaryMessage msg = new AVIMBinaryMessage();
        msg.setBytes(StringUtil.getRandomString(16).getBytes());
        targetConversation.sendMessage(msg, new AVIMConversationCallback() {
          @Override
          public void done(AVIMException ex) {
            if (null != ex) {
              System.out.println("failed to send binary message");
              ex.printStackTrace();
            } else {
              System.out.println("succeed to send binary message");
              opersationSucceed = true;
            }
            countDownLatch.countDown();
          }
        });
      }
    });
    Thread secondThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          tmpCounter.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        AVIMClient client2 = AVIMClient.getInstance("User2");
        client2.open(new AVIMClientCallback() {
          @Override
          public void done(AVIMClient client, AVIMException e) {
            if (null != e) {
              System.out.println("User2 open failed.");
            }
            tmpCounter2.countDown();
          }
        });
      }
    });

    firstThread.start();
    secondThread.start();
    countDownLatch.await();

    firstThread.join();
    secondThread.join();
    assertTrue(opersationSucceed);
  }

  public void testSendAndReceiveTextMessage() throws Exception {
    AVIMMessageManager.registerDefaultMessageHandler(new DummyMessageHandler());
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    final CountDownLatch tmpCounter2 = new CountDownLatch(1);
    client = AVIMClient.getInstance("testUser1");
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    client.createConversation(memebers, convName, null, false, true, new AVIMConversationCreatedCallback() {
      @Override
      public void done(AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          try {
            tmpCounter2.await();
          } catch (Exception ex) {
            ex.printStackTrace();
          }
          AVIMTextMessage msg = new AVIMTextMessage();
          msg.setText("test run @" + System.currentTimeMillis());
          conversation.sendMessage(msg, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException ex) {
              if (null != ex) {
                System.out.println("failed to send message");
                ex.printStackTrace();
              } else {
                System.out.println("succeed to send message");
                opersationSucceed = true;
              }
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    AVIMClient client2 = AVIMClient.getInstance("User2");
    client2.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null != e) {
          System.out.println("User2 open failed.");
        }
        tmpCounter2.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
    Thread.sleep(3000);
  }

  public void testRecallMessage() throws Exception {
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client = AVIMClient.getInstance("testUser1");
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    client.createConversation(memebers, convName, null, false, true, new AVIMConversationCreatedCallback() {
      @Override
      public void done(final AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          final AVIMTextMessage msg = new AVIMTextMessage();
          msg.setText("test run @" + System.currentTimeMillis());
          conversation.sendMessage(msg, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException ex) {
              if (null != ex) {
                System.out.println("failed to send message");
                ex.printStackTrace();
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to send message");
                conversation.recallMessage(msg, new AVIMMessageRecalledCallback() {
                  @Override
                  public void done(AVIMRecalledMessage recalledMessage, AVException e) {
                    if (null != e) {
                      System.out.println("failed to recall message");
                    } else {
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
    assertTrue(opersationSucceed);
  }

  public void testUpdateMessage() throws Exception {
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client = AVIMClient.getInstance("testUser1");
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    client.createConversation(memebers, convName, null, false, true, new AVIMConversationCreatedCallback() {
      @Override
      public void done(final AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          final AVIMTextMessage msg = new AVIMTextMessage();
          msg.setText("test run @" + System.currentTimeMillis());
          conversation.sendMessage(msg, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException ex) {
              if (null != ex) {
                System.out.println("failed to send message, cause:" + ex.getMessage());
                ex.printStackTrace();
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to send message. messageId:" + msg.getMessageId());
                AVIMTextMessage newMsg = new AVIMTextMessage();
                newMsg.setText("test updated @" + System.currentTimeMillis());
                conversation.updateMessage(msg, newMsg, new AVIMMessageUpdatedCallback() {
                  @Override
                  public void done(AVIMMessage curMessage, AVException e) {
                    if (null != e) {
                      System.out.println("failed to update message， cause:" + e.getMessage());
                    } else {
                      System.out.println("succeed to patch message");
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
    assertTrue(opersationSucceed);
  }

  public void testUpdateMessageNotification() throws Exception {
    final CountDownLatch firstStage = new CountDownLatch(1);
    final CountDownLatch secondStage = new CountDownLatch(1);
    final CountDownLatch endStage = new CountDownLatch(1);

    Thread senderThread = new Thread(new Runnable() {
      AVIMConversation targetConversation;
      AVIMTextMessage targetMessage;
      @Override
      public void run() {
        final CountDownLatch tmpCounter = new CountDownLatch(1);

        client = AVIMClient.getInstance("testUser1");
        client.open(new AVIMClientCallback() {
          @Override
          public void done(AVIMClient client, AVIMException e) {
            if (null != e) {
              System.out.println("☑️testUser1 loggin...");
            }
            tmpCounter.countDown();
          }
        });
        try {
          tmpCounter.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        client.createConversation(memebers, convName, null, false, true, new AVIMConversationCreatedCallback() {
          @Override
          public void done(final AVIMConversation conversation, AVIMException e) {
            if (null != e) {
              e.printStackTrace();
              firstStage.countDown();
            } else {
              System.out.println("☑️☑️testUser1 join conversation:" + conversation.getConversationId() + "...");
              testConversationId = conversation.getConversationId();
              final AVIMTextMessage msg = new AVIMTextMessage();
              msg.setText("test run @" + System.currentTimeMillis());
              conversation.sendMessage(msg, new AVIMConversationCallback() {
                @Override
                public void done(AVIMException ex) {
                  if (null != ex) {
                    System.out.println("❌️testUser1 failed to send message, cause:" + ex.getMessage());
                    ex.printStackTrace();
                    firstStage.countDown();
                  } else {
                    System.out.println("☑️☑️☑️testUser1 succeed to send message. messageId:" + msg.getMessageId());
                    targetConversation = conversation;
                    targetMessage = msg;
                    firstStage.countDown();
                  }
                }
              });
            }
          }
        });
        try {
          secondStage.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        final CountDownLatch updateLatch = new CountDownLatch(1);
        AVIMTextMessage newMsg = new AVIMTextMessage();
        newMsg.setText("test updated @" + System.currentTimeMillis());
        targetConversation.updateMessage(targetMessage, newMsg, new AVIMMessageUpdatedCallback() {
          @Override
          public void done(AVIMMessage curMessage, AVException e) {
            if (null != e) {
              System.out.println("❌️testUser1 failed to update message， cause:" + e.getMessage());
            } else {
              System.out.println("☑️☑️☑️☑️testUser1 succeed to patch message");
            }
            updateLatch.countDown();
          }
        });
        try {
          updateLatch.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        endStage.countDown();
        System.out.println("Sender Thread exited.");
      }
    });
    Thread receiverThread = new Thread(new Runnable() {
      AVIMClient currentClient = AVIMClient.getInstance("User2");
      @Override
      public void run() {
        final CountDownLatch loginLatch = new CountDownLatch(1);
        currentClient.open(new AVIMClientCallback() {
          @Override
          public void done(AVIMClient client, AVIMException e) {
            if (null == e) {
              System.out.println("☑️User2 loggin...");
            }
            loginLatch.countDown();
          }
        });
        try {
          loginLatch.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        try {
          firstStage.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }

        if (!StringUtil.isEmpty(testConversationId)) {
          final AVIMConversation targetConversation = currentClient.getConversation(testConversationId);
          final CountDownLatch joinLatch = new CountDownLatch(1);
          targetConversation.join(new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
              if (null != e) {
                e.printStackTrace();
              } else {
                System.out.println("☑️☑️User2 join conversation: " + testConversationId + "...");
              }
              AVIMMessage lastMessage = targetConversation.getLastMessage();
              joinLatch.countDown();
            }
          });
          try {
            joinLatch.await();
          } catch (Exception ex) {
            ;
          }
          secondStage.countDown();
          System.out.println("☑️☑️User2 notify sender to update message...");

          try {
            endStage.await();
          } catch (Exception ex) {
            ;
          }
          final CountDownLatch updateLatch = new CountDownLatch(1);
          System.out.println("☑️☑️️User2 got notification and try to query message...");
          targetConversation.queryMessages(1, new AVIMMessagesQueryCallback() {
            @Override
            public void done(List<AVIMMessage> messages, AVIMException e) {
              if (null != e || null == messages || messages.size() < 1) {
                System.out.println("User2 failed to query messages. cause:" + e.getMessage());
                updateLatch.countDown();
              } else {
                System.out.println("☑️☑️☑️User2 try to update message...");
                AVIMMessage targetMessage = messages.get(0);
                AVIMTextMessage newMsg = new AVIMTextMessage();
                newMsg.setText("test updated @" + System.currentTimeMillis());
                targetConversation.updateMessage(targetMessage, newMsg, new AVIMMessageUpdatedCallback() {
                  @Override
                  public void done(AVIMMessage message, AVException e) {
                    if (null != e) {
                      e.printStackTrace();
                    }
                    System.out.println("☑️☑️☑️☑️User2 update message result: " + (null != e));
                    opersationSucceed = true;
                    currentClient.close(new AVIMClientCallback() {
                      @Override
                      public void done(AVIMClient client, AVIMException e) {
                        updateLatch.countDown();
                      }
                    });
                  }
                });
              }
            }
          });
          try {
            updateLatch.await();
          } catch (Exception ex) {
            ;
          }
        } else {
          secondStage.countDown();
        }
        System.out.println("Receiver Thread Exit!");
      }
    });

    senderThread.start();
    receiverThread.start();
    senderThread.join(10000);
    receiverThread.join(10000);
    assertTrue(opersationSucceed);
  }

  public void testQueryMessagesWithUser2() throws Exception {
    final AVIMClient client = AVIMClient.getInstance("User2");
    final CountDownLatch loginLatch = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null == e) {
          System.out.println("☑️User2 loggin...");
        }
        loginLatch.countDown();
      }
    });
    try {
      loginLatch.await();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    testConversationId = "5ce23b8ec320f1ab6f53fd50";
    final AVIMConversation targetConversation = client.getConversation(testConversationId);
    final CountDownLatch joinLatch = new CountDownLatch(1);
    targetConversation.join(new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        if (null != e) {
          e.printStackTrace();
        } else {
          System.out.println("☑️☑️User2 join conversation: " + testConversationId + "...");
        }
        AVIMMessage lastMessage = targetConversation.getLastMessage();
        joinLatch.countDown();
      }
    });
    try {
      joinLatch.await();
    } catch (Exception ex) {
      ;
    }

    final CountDownLatch updateLatch = new CountDownLatch(1);
    System.out.println("☑️☑️️User2 got notification and try to query message...");
    targetConversation.queryMessages(1, new AVIMMessagesQueryCallback() {
      @Override
      public void done(List<AVIMMessage> messages, AVIMException e) {
        if (null != e ) {
          System.out.println("User2 failed to query messages. cause:" + e.getMessage());
          updateLatch.countDown();
        } else if (null == messages || messages.size() < 1) {
          System.out.println("User2 failed to query messages. result: null");
          updateLatch.countDown();
        } else {
          System.out.println("☑️☑️☑️User2 try to update message...");
          AVIMMessage targetMessage = messages.get(0);
          AVIMTextMessage newMsg = new AVIMTextMessage();
          newMsg.setText("test updated @" + System.currentTimeMillis());
          targetConversation.updateMessage(targetMessage, newMsg, new AVIMMessageUpdatedCallback() {
            @Override
            public void done(AVIMMessage message, AVException e) {
              if (null != e) {
                e.printStackTrace();
              }
              System.out.println("☑️☑️☑️☑️User2 update message result: " + (null != e));
              opersationSucceed = true;
              updateLatch.countDown();
            }
          });
        }
      }
    });
    updateLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testQueryMessages() throws Exception {
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client = AVIMClient.getInstance("testUser1");
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    client.createConversation(memebers, convName, null, false, true, new AVIMConversationCreatedCallback() {
      @Override
      public void done(AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          conversation.queryMessages(new AVIMMessagesQueryCallback() {
            @Override
            public void done(List<AVIMMessage> messages, AVIMException ex) {
              if (null != ex) {
                System.out.println("failed to query message");
                ex.printStackTrace();
              } else {
                System.out.println("succeed to query message: " + messages.size());
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

  public void testJoinedNotification() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    Map<String, Object> attr = new HashMap<>();
    attr.put("testTs", System.currentTimeMillis());
    attr.put("owner", "testUser1");
    client.createConversation(memebers, convName, attr, false, false, new AVIMConversationCreatedCallback() {
      @Override
      public void done(AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          AVIMTextMessage msg = new AVIMTextMessage();
          msg.setText("test run @" + System.currentTimeMillis());
          conversation.sendMessage(msg, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException ex) {
              if (null != ex) {
                System.out.println("failed to send message");
                ex.printStackTrace();
              } else {
                System.out.println("succeed to send message");
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
    Thread.sleep(2000);
  }

  public void testConversationQueryWithCache() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();

    AVIMConversationsQuery query = client.getConversationsQuery();
    query.containsMembers(Arrays.asList("testUser1"));
    query.addAscendingOrder("updatedAt");
    query.findInBackground(new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> conversations, AVIMException e) {
        if (null != e) {
          System.out.println("failed to query converstaion.");
        } else {
          System.out.println("succeed to query converstaion.");
          for (AVIMConversation conv: conversations) {
            System.out.println(conv);
          }
          // in core library, no db cache.
          opersationSucceed = true;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationQueryWithNetwork() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();

    AVIMConversationsQuery query = client.getConversationsQuery();
    query.containsMembers(Arrays.asList("testUser1"));
    query.addAscendingOrder("updatedAt");
    query.setQueryPolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    query.findInBackground(new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> conversations, AVIMException e) {
        if (null != e) {
          System.out.println("failed to query converstaion.");
        } else {
          System.out.println("succeed to query converstaion.");
          opersationSucceed = conversations.size() > 0;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testDummyConversationEventHandlerCounter() throws Exception {
    AVIMClient client = AVIMClient.getInstance("testUser");
    AVIMConversation conv = client.getConversation("conversationId");
    this.conversationEventHandler.onBlocked(client, conv, "nobody");
    this.conversationEventHandler.onInvited(client, conv, "nobody");
    this.conversationEventHandler.onMemberBlocked(client, conv, Arrays.asList("Tom", "Jerry"),"nobody");
    assertTrue(this.conversationEventHandler.getCount(0x00ffFF) == 3);
  }

  public void testCreateConversationWithAttributes() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    final String convName = "TestConv-" + System.currentTimeMillis();
    final Map<String, Object> attributes = new HashMap<>();
    attributes.put("type", 3);
    attributes.put("ts", System.currentTimeMillis());
    client.createConversation(Arrays.asList("testUser007"), convName, attributes, true, true, new AVIMConversationCreatedCallback() {
      @Override
      public void done(AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          System.out.println("failed to create conversation. cause:" + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to create conversation: " + conversation.toJSONString());
          String name = conversation.getName();
          int type = (int)conversation.getAttribute("type");
          System.out.println("created. name=" + name + ", type=" + type + ", uniqueId=" + conversation.getUniqueId());
          opersationSucceed = convName.equals(name) && 3 == type;
          countDownLatch.countDown();
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testCreateUniqueConversationWithAttributes() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    final String convName = "TestConv-" + System.currentTimeMillis();
    final Map<String, Object> attributes = new HashMap<>();
    attributes.put("type", 3);
    attributes.put("ts", System.currentTimeMillis());
    client.createConversation(Arrays.asList("testUser007"), convName, attributes, false, true, new AVIMConversationCreatedCallback() {
      @Override
      public void done(AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          System.out.println("failed to create conversation. cause:" + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to create conversation: " + conversation.toJSONString());
          String name = conversation.getName();
          int type = (int)conversation.getAttribute("type");
          System.out.println("created. name=" + name + ", type=" + type + ", uniqueId=" + conversation.getUniqueId());
          opersationSucceed = convName.equals(name) && 3 == type && conversation.isUnique();
          countDownLatch.countDown();
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationJoinAndQuit() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5dee02017c4cc935c85c93de";
    final AVIMConversation conversation = client.getConversation(conversationId, true, false);
    conversation.join(new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        if (null != e) {
          System.out.println("failed to join conversation. cause:" + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to join conversation.");
          try {
            Thread.sleep(1000);
          } catch (Exception ex) {
            ex.printStackTrace();
          }
          conversation.quit(new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
              if (null != e) {
                System.out.println("failed to quit conversation. cause:" + e.getMessage());
              } else {
                System.out.println("succeed to quit conversation.");
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

  public void testConversationRead() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5dee02017c4cc935c85c93de";
    final AVIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.join(new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        if (null != e) {
          System.out.println("failed to join conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to join conversation");
          conversation.read();
          try {
            Thread.sleep(1000);
          } catch (Exception ex) {
            ex.printStackTrace();
          }
          conversation.quit(new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
              if (null != e) {
                System.out.println("failed to quit conversation. cause:" + e.getMessage());
              } else {
                System.out.println("succeed to quit conversation.");
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

  public void testConversationFetch() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5dee02017c4cc935c85c93de";
    AVIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.fetchInfoInBackground(new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        if (null != e) {
          System.out.println("failed to fetch info. cause: " + e.getMessage());
        } else {
          System.out.println("succeed to fetch info.");
          opersationSucceed = true;
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationQueryMemberCount() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5dee02017c4cc935c85c93de";
    System.out.println("begin to fetch conversation:" + conversationId);
    final AVIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.fetchInfoInBackground(new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        if (null != e) {
          System.out.println("failed to refresh convesation.");
          e.printStackTrace();
          countDownLatch.countDown();
        } else {
          System.out.println("succeed fetch conversation:" + conversation.toJSONString());
          conversation.getMemberCount(new AVIMConversationMemberCountCallback() {
            @Override
            public void done(Integer memberCount, AVIMException e) {
              if (null != e) {
                e.printStackTrace();
              } else {
                System.out.println("get member count: " + memberCount);
              }
              opersationSucceed = e == null;
              countDownLatch.countDown();
            }
          });
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testModifyAndDeleteAttributes() throws Exception {
    client = AVIMClient.getInstance("William");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        System.out.println("client open finished.");
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5dee02017c4cc935c85c93de";
    final AVIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.fetchInfoInBackground(new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        if (null != e) {
          System.out.println("failed to fetch Conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          final String convName = conversation.getName();
          final String convAttrName = (String) conversation.getAttribute("name");
          System.out.println("convName=" + convName + ", convAttrName=" + convAttrName);
          final long now = System.currentTimeMillis();

          conversation.setName("TestConv" + now);
          conversation.setAttribute("ts", now);
          conversation.set("attr.type", 4);

          conversation.updateInfoInBackground(new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
              if (null == e) {
                boolean verifiedOnFirstUpdate = (int)conversation.get("attr.type") == 4
                        && conversation.getName().equals("TestConv" + now)
                        && (long)conversation.getAttribute("ts") == now;
                if (!verifiedOnFirstUpdate) {
                  System.out.println("failed to verify conversation attribute after first updated. conv=" + conversation.toJSONString());
                  countDownLatch.countDown();
                } else {
                  System.out.println("conversation attribute after first updated. conv=" + conversation.toJSONString());
                  conversation.remove("attr.ts");
                  conversation.remove("attr.type");
                  conversation.setName(convName);
                  conversation.updateInfoInBackground(new AVIMConversationCallback() {
                    @Override
                    public void done(AVIMException e) {
                      if (null == e) {
                        opersationSucceed = null == conversation.get("attr.ts")
                                && conversation.getName().equals(convName)
                                && null == conversation.get("attr.type");
                        if (!opersationSucceed) {
                          System.out.println("failed to verify conversation attribute after second updated. conv=" + conversation.toJSONString());
                        } else {
                          System.out.println("conversation attribute after second updated. conv=" + conversation.toJSONString());
                        }
                      }
                      countDownLatch.countDown();
                    }
                  });
                }
              } else {
                e.printStackTrace();
                countDownLatch.countDown();
              }

            }
          });
        }
      }
    });

    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationUpdateName() throws Exception {
    client = AVIMClient.getInstance("William");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        System.out.println("client open finished.");
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5dee02017c4cc935c85c93de";
    final AVIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.fetchInfoInBackground(new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        if (null != e) {
          System.out.println("failed to fetch Conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          String convName = conversation.getName();
          String convAttrName = (String) conversation.getAttribute("name");
          System.out.println("convName=" + convName + ", convAttrName=" + convAttrName);

          conversation.setName("TestConv" + System.currentTimeMillis());
          conversation.setAttribute("ts", System.currentTimeMillis());
          conversation.set("attr.type", 4);

          conversation.updateInfoInBackground(new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
              if (null == e) {
                opersationSucceed = true;
              } else {
                e.printStackTrace();
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

  public void testConversationFetchLastTime() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5805eefd8159ccabfc39bc1c";
    final AVIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.fetchReceiptTimestamps(new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        if (null != e) {
          System.out.println("failed to fetch ReceiptTimestamps. cause: " + e.getMessage());
          opersationSucceed = true;
        } else {
          System.out.println("succeed to fetch ReceiptTimestamps.");
          System.out.println("LastReadAt: " + conversation.getLastReadAt());
          System.out.println("LastDeliveredAt: " + conversation.getLastDeliveredAt());
        }
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testConversationFetchLastTimeWithMembership() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5dee02017c4cc935c85c93de";
    System.out.println("begin to join converseration: " + conversationId);
    final AVIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.join(new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        if (null != e) {
          System.out.println("failed to join conversation. cause:" + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to join to conversation: " + conversation.toJSONString());
          conversation.fetchReceiptTimestamps(new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
              if (null != e) {
                System.out.println("failed to fetch ReceiptTimestamps. cause: " + e.getMessage());
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to fetch ReceiptTimestamps." + conversation.toJSONString());
                System.out.println("LastReadAt: " + conversation.getLastReadAt());
                System.out.println("LastDeliveredAt: " + conversation.getLastDeliveredAt());
                conversation.quit(new AVIMConversationCallback() {
                  @Override
                  public void done(AVIMException e) {
                    if (null != e) {
                      System.out.println("failed to quit conversation. cause:" + e.getMessage());
                    } else {
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
    assertTrue(opersationSucceed);
  }

  public void testAttributeGetter() throws Exception {
    String jsonString = "{\"unique\":true,\"updatedAt\":\"2020-01-21T14:22:48.642Z\",\"name\":\"TestConv1579603439430\"," +
            "\"objectId\":\"5dee02017c4cc935c85c93de\",\"m\":[\"T8\",\"William\"],\"tr\":false,\"createdAt\":\"2019-12-09T08:12:49.475Z\"," +
            "\"c\":\"T8\",\"uniqueId\":\"61e52ce97f2b93050655a1c83371b070\",\"mu\":[]," +
            "\"attr\":{\"alias\":\"TestConv1575943063325\",\"type\":4,\"ts\":1579603439430}}";
    JSONObject jsonObj = JSON.parseObject(jsonString);
    AVIMClient client = AVIMClient.getInstance("test");
    AVIMConversation conv = AVIMConversation.parseFromJson(client, jsonObj);
    Date createdAt = StringUtil.dateFromString("2019-12-09T08:12:49.475Z");
    Date updatedAt = StringUtil.dateFromString("2020-01-21T14:22:48.642Z");
    assertTrue(conv.isUnique());
    assertTrue(!conv.isSystem());
    assertTrue(!conv.isTransient());
    assertTrue(!conv.isTemporary());
    assertTrue(conv.getTemporaryExpiredat() == 0);
    assertTrue(conv.getName().equals("TestConv1579603439430"));
    assertTrue(conv.getConversationId().equals("5dee02017c4cc935c85c93de"));
    assertTrue(conv.getCreator().equals("T8"));
    assertTrue(conv.getLastMessage() == null);
    assertTrue(conv.getUniqueId().equals("61e52ce97f2b93050655a1c83371b070"));
    assertTrue(conv.getCreatedAt().getTime() == createdAt.getTime());
    assertTrue(conv.getUpdatedAt().getTime() == updatedAt.getTime());

    assertTrue(conv.getAttribute("name").equals("TestConv1579603439430"));
    assertTrue(conv.getAttribute("alias").equals("TestConv1575943063325"));
    assertTrue((int)conv.getAttribute("type") == 4);
    assertTrue((long)conv.getAttribute("attr.ts") == 1579603439430l);

    assertTrue(conv.get("name").equals("TestConv1579603439430"));
    assertTrue(conv.get("attr.alias").equals("TestConv1575943063325"));
    assertTrue((int)conv.get("attr.type") == 4);
    assertTrue((long)conv.get("attr.ts") == 1579603439430l);

    assertNull(conv.getAttribute("nothing"));
    assertNull(conv.get("nothing"));
    assertNull(conv.get("attr.nothing"));
    assertNull(conv.get("attr.nothing.ts"));
  }

  public void testMuteConversation() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (null != e) {
          e.printStackTrace();
        }
        System.out.println("open client finished.");
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    String conversationId = "5dee02017c4cc935c85c93de";
    System.out.println("begin to join conversation:" + conversationId);
    final AVIMConversation conversation = client.getConversation(conversationId, false, false);
    conversation.join(new AVIMConversationCallback() {
      @Override
      public void done(AVIMException e) {
        if (null != e) {
          System.out.println("failed to join conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to join conversation：" + conversation.toJSONString());
          conversation.mute(new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
              if (null != e) {
                System.out.println("failed to mute conversation. cause:" + e.getMessage());
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to mute conversation.");
                conversation.unmute(new AVIMConversationCallback() {
                  @Override
                  public void done(AVIMException e) {
                    if (null != e) {
                      System.out.println("failed to unmute conversation. cause:" + e.getMessage());
                      countDownLatch.countDown();
                    } else {
                      System.out.println("succeed to unmute conversation.");
                      conversation.quit(new AVIMConversationCallback() {
                        @Override
                        public void done(AVIMException e) {
                          if (null != e) {
                            System.out.println("failed to quit conversation. cause:" + e.getMessage());
                          } else {
                            System.out.println("succeed to quit conversation.");
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
        }
      }
    });
    countDownLatch.await();
    assertTrue(opersationSucceed);
  }

  public void testMuteConversationMembers() throws Exception {
    client = AVIMClient.getInstance("testUser1");
    final CountDownLatch tmpCounter = new CountDownLatch(1);
    client.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        tmpCounter.countDown();
      }
    });
    tmpCounter.await();
    List<String> members = Arrays.asList("testUser2", "testUser3", "testUser4");
    final List<String> blockMembers = Arrays.asList("testUser3", "testUser4");
    client.createConversation(members, "UnitTestConversation", null, false, true, new AVIMConversationCreatedCallback() {
      @Override
      public void done(final AVIMConversation conversation, AVIMException e) {
        if (null != e) {
          System.out.println("failed to create conversation. cause: " + e.getMessage());
          countDownLatch.countDown();
        } else {
          System.out.println("succeed to create conversation.");
          conversation.muteMembers(blockMembers, new AVIMOperationPartiallySucceededCallback() {
            @Override
            public void done(AVIMException e, List<String> successfulClientIds, List<AVIMOperationFailure> failures) {
              if (null != e) {
                System.out.println("failed to mute conversation members. cause: " + e.getMessage());
                countDownLatch.countDown();
              } else {
                System.out.println("succeed to mute conversation members.");
                conversation.unmuteMembers(blockMembers, new AVIMOperationPartiallySucceededCallback() {
                  @Override
                  public void done(AVIMException e, List<String> successfulClientIds, List<AVIMOperationFailure> failures) {
                    if (null != e) {
                      System.out.println("failed to mute conversation members. cause: " + e.getMessage());
                    } else {
                      System.out.println("succeed to unmute conversation members.");
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
    assertTrue(opersationSucceed);
  }

  public void testSendAndReceiveMessage() throws Exception {
    final String senderId = "sender-" + System.currentTimeMillis();
    Thread.sleep(1000);
    final String receiverId = "receiver-" + System.currentTimeMillis();

    final CountDownLatch receierOnlineLatch = new CountDownLatch(1);

    Runnable sendThread = new Runnable() {
      @Override
      public void run() {
        final CountDownLatch tmpCounter = new CountDownLatch(1);
        final AVIMClient client = AVIMClient.getInstance(senderId);
        client.open(new AVIMClientCallback() {
          @Override
          public void done(final AVIMClient client, AVIMException e) {
            if (null != e) {
              System.out.println("failed to open sender client.");
              e.printStackTrace();
              tmpCounter.countDown();
            } else {
              System.out.println(client.getClientId() + " logged in, waiting for receiver online...");
            }
          }
        });
        try {
          receierOnlineLatch.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        client.createConversation(Arrays.asList(receiverId), null, null, false, true,
                new AVIMConversationCreatedCallback() {
                  @Override
                  public void done(final AVIMConversation conversation, AVIMException e) {
                    if (null != e) {
                      System.out.println("failed to create conversation from sender client.");
                      e.printStackTrace();
                      tmpCounter.countDown();
                    } else {
                      conversation.blockMembers(Arrays.asList("blockedUser"), new AVIMOperationPartiallySucceededCallback() {
                        @Override
                        public void done(AVIMException e, List<String> successfulClientIds, List<AVIMOperationFailure> failures) {
                          if (null != e) {
                            System.out.println("failed to block members from sender client.");
                            tmpCounter.countDown();
                          } else {
                            AVIMTextMessage msg = new AVIMTextMessage();
                            msg.setText("try to unblock user. @" + System.currentTimeMillis());
                            conversation.sendMessage(msg, new AVIMConversationCallback() {
                              @Override
                              public void done(AVIMException e) {
                                if (null != e) {
                                  System.out.println("failed to send message from sender client.");
                                  tmpCounter.countDown();
                                } else {
                                  conversation.unblockMembers(Arrays.asList("blockedUser"), new AVIMOperationPartiallySucceededCallback() {
                                    @Override
                                    public void done(AVIMException e, List<String> successfulClientIds, List<AVIMOperationFailure> failures) {
                                      if (null != e) {
                                        System.out.println("failed to unblock members from sender client.");
                                        e.printStackTrace();
                                      }
                                      client.close(new AVIMClientCallback() {
                                        @Override
                                        public void done(AVIMClient client, AVIMException e) {
                                          if (null != e) {
                                            System.out.println("failed to close sender client.");
                                          } else {
                                            System.out.println("succeed to run all flow on sender side.");
                                          }
                                          tmpCounter.countDown();
                                        }
                                      });
                                    }
                                  });
                                }
                              }
                            });

                          }
                        }
                      });
                    }
                  }
                });
        try {
          tmpCounter.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    };
    Runnable receiveThread = new Runnable() {
      @Override
      public void run() {
        final CountDownLatch tmpCounter = new CountDownLatch(1);
        AVIMClient client = AVIMClient.getInstance(receiverId);
        client.open(new AVIMClientCallback() {
          @Override
          public void done(AVIMClient client, AVIMException e) {
            if (null != e) {
              System.out.println("failed to open receiver client");
              e.printStackTrace();
            } else {
              System.out.println(client.getClientId() + " logged in...");
            }
            receierOnlineLatch.countDown();
            tmpCounter.countDown();
          }
        });
        try {
          tmpCounter.await();
        } catch (Exception ex) {
          ex.printStackTrace();
        }

        try {
          Thread.sleep(10000);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        client.close(new AVIMClientCallback() {
          @Override
          public void done(AVIMClient client, AVIMException e) {
            if (null != e) {
              System.out.println("failed to close receiver client");
              e.printStackTrace();
            }
            tmpCounter.countDown();
          }
        });
      }
    };
    Thread t1 = new Thread(sendThread);
    t1.start();
    Thread t2 = new Thread(receiveThread);
    t2.start();

    t1.join();
    t2.join();
    int notifyCount = this.conversationEventHandler.getCount(0x00FFFF);
    System.out.println("notifyCount=" + notifyCount);
    assertTrue(notifyCount > 2);
  }
}
